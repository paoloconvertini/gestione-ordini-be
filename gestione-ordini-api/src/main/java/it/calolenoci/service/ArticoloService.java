package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import it.calolenoci.enums.AzioneEnum;
import it.calolenoci.enums.StatoOrdineEnum;
import it.calolenoci.mapper.ArticoloMapper;
import it.calolenoci.mapper.RegistroAzioniMapper;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class ArticoloService {


    @Inject
    OrdineService ordineService;

    @Inject
    RegistroAzioniMapper registroAzioniMapper;

    @Inject
    ArticoloMapper mapper;

    @Inject
    MailService mailService;

    @ConfigProperty(name = "ordini.path")
    String pathReport;

    public ResponseOrdineDettaglio findById(FiltroArticoli filtro) {
        long inizio = System.currentTimeMillis();
        ResponseOrdineDettaglio response = new ResponseOrdineDettaglio();
        List<OrdineDettaglioDto> list;
        OrdineDTO ordineDTO = ordineService.findById(filtro.getAnno(), filtro.getSerie(), filtro.getProgressivo());
        list = OrdineDettaglio.findArticoliById(filtro);
        Optional<Double> aDouble = OrdineDettaglio.find("SELECT SUM(((CaSE WHEN prezzo is null then 0 ELSE prezzo end)*(CASE WHEN quantita is null then 0 else quantita end))*(1-scontoArticolo/100)*(1-scontoC1/100)*(1-scontoC2/100)*(1-scontoP/100)) FROM OrdineDettaglio o " +
                                "WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo"
                        , Parameters.with("anno", filtro.getAnno()).and("serie", filtro.getSerie()).and("progressivo", filtro.getProgressivo()))
                .project(Double.class).firstResultOptional();
        aDouble.ifPresent(response::setTotale);
        response.setIntestazione(ordineDTO.getIntestazione());
        response.setRiferimento(ordineDTO.getRiferimento());
        response.setSottoConto(ordineDTO.getSottoConto());
        response.setLocked(ordineDTO.getLocked());
        response.setUserLock(ordineDTO.getUserLock());
        response.setTelefono(ordineDTO.getTelefono());
        response.setCellulare(ordineDTO.getCellulare());
        response.setDataOrdine(ordineDTO.getDataOrdine());
        response.setModalitaPagamento(ordineDTO.getModalitaPagamento());
        response.setArticoli(list);
        long fine = System.currentTimeMillis();
        Log.info("Get articoli ordine cliente: " + (fine - inizio)/1000 + " sec");
        return response;
    }

    public List<OrdineDettaglioDto> findForReport(Integer anno, String serie, Integer progressivo) {
        return OrdineDettaglio.findArticoliForReport(anno, serie, progressivo);
    }

    public boolean findAnyNoStatus(Integer anno, String serie, Integer progressivo) {
        return !OrdineDettaglio.find("SELECT o FROM OrdineDettaglio o" +
                        " WHERE anno = :anno AND serie = :serie AND progressivo = :progressivo and tipoRigo NOT IN ('C', 'AC')" +
                        " AND NOT EXISTS (SELECT 1 FROM GoOrdineDettaglio god WHERE o.progrGenerale = god.progrGenerale)",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)).list().isEmpty();
    }

    @Transactional
    public Integer updateArticoliBolle(List<OrdineDettaglioDto> list) {
        long inizio = System.currentTimeMillis();
        AtomicReference<Integer> aggiornati = new AtomicReference<>(0);
        list.forEach(e -> {
            Optional<GoOrdineDettaglio> optional = GoOrdineDettaglio.getById(e.getProgrGenerale());
            if (optional.isPresent()) {
                GoOrdineDettaglio goOrdineDettaglio = optional.get();
                aggiornati.updateAndGet(v -> v + GoOrdineDettaglio.update(
                          " qtaConsegnatoSenzaBolla = CASE WHEN (:quantita - :qta) = 0 THEN NULL ELSE qtaConsegnatoSenzaBolla END, " +
                                " flagConsegnato = CASE WHEN (:quantita - :qta) = 0 THEN 'T' ELSE 'F' END, " +
                                " qtaProntoConsegna = CASE WHEN (:quantita - qtaDaConsegnare) <> :qta THEN NULL ELSE qtaProntoConsegna END, " +
                                " flProntoConsegna = CASE WHEN (:quantita - qtaDaConsegnare) <> :qta THEN 'F' ELSE 'T' END, " +
                                " qtaRiservata = CASE WHEN (:quantita - qtaDaConsegnare) <> :qta THEN NULL ELSE qtaRiservata END, " +
                                " qtaDaConsegnare = (:quantita - :qta), " +
                                " flBolla = 'T' " +
                                " WHERE progrGenerale = :progrGenerale" +
                                " AND (qtaDaConsegnare IS NULL OR qtaDaConsegnare <> (:quantita - :qta))",
                        Parameters.with("qta", e.getQtaBolla()).and("progrGenerale", e.getProgrGenerale()).and("quantita", e.getQuantita())));

                if (goOrdineDettaglio.getQtaConsegnatoSenzaBolla() == null || goOrdineDettaglio.getQtaConsegnatoSenzaBolla() == 0) {
                    GoOrdine.update("warnNoBolla = 'F' where anno =:anno and serie =:serie and progressivo = :progressivo",
                            Parameters.with("anno", e.getAnno())
                                    .and("serie", e.getSerie())
                                    .and("progressivo", e.getProgressivo()));
                }
            }
        });
        long fine = System.currentTimeMillis();
        Log.info("UpdateArticoliBolle: " + (fine - inizio)/1000 + " sec");
        return aggiornati.get();
    }

    public boolean findNoProntaConsegna(Integer anno, String serie, Integer progressivo) {
        return GoOrdineDettaglio.count("anno = :anno AND serie = :serie AND progressivo = :progressivo " +
                        " and flProntoConsegna = true " +
                        " AND EXISTS (SELECT 1 FROM OrdineDettaglio o WHERE o.progrGenerale = progrGenerale)",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)) == 0;
    }

    public boolean findNoConsegnati(Integer anno, String serie, Integer progressivo) {
        return GoOrdineDettaglio.find("SELECT god FROM GoOrdineDettaglio god " +
                        " WHERE god.anno = :anno AND god.serie = :serie AND god.progressivo = :progressivo " +
                        " and (god.flagConsegnato = false OR god.flagConsegnato IS NULL) " +
                        " AND EXISTS (SELECT 1 FROM OrdineDettaglio o WHERE o.progrGenerale = progrGenerale)",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)).list().isEmpty();
    }

    public void checkNoBolle() {
        long inizio = System.currentTimeMillis();
        GoOrdineDettaglio.update("flagConsegnato = 'F', qtaDaConsegnare = null," +
                            " flBolla = 'F' WHERE progrGenerale IN (" +
                            "select god.progrGenerale " +
                            "FROM GoOrdineDettaglio god " +
                            "WHERE NOT EXISTS (SELECT 1 FROM FattureDettaglio f2 WHERE f2.progrOrdCli = god.progrGenerale) " +
                            "AND god.flBolla = 'T' " +
                            "AND EXISTS (SELECT 1 FROM OrdineDettaglio o WHERE o.progrGenerale = god.progrGenerale))"
            );
        long fine = System.currentTimeMillis();
        Log.info("CheckNoBolle: " + (fine - inizio) + " msec");
    }

    @Transactional
    public void save(List<OrdineDettaglioDto> list, String user, String email) {
        save(list, user, false, email);
    }

    @Transactional
    public String save(List<OrdineDettaglioDto> list, String user, Boolean chiudi, String email) {
        List<RegistroAzioni> registroAzioniList = new ArrayList<>();
        List<OrdineDettaglio> ordineDettaglioList = new ArrayList<>();
        List<GoOrdineDettaglio> goOrdineDettaglioList = new ArrayList<>();
        AtomicBoolean warnNoBolla = new AtomicBoolean(false);
        AtomicBoolean hasProntoConsegna = new AtomicBoolean(false);
        list.forEach(dto -> {
            if (!"C".equals(dto.getTipoRigo()) && !"AC".equals(dto.getTipoRigo())) {
                if (!hasProntoConsegna.get() && dto.getFlProntoConsegna() != null && dto.getFlProntoConsegna()) {
                    hasProntoConsegna.getAndSet(Boolean.TRUE);
                }
                GoOrdineDettaglio goOrdineDettaglio;
                OrdineDettaglio ordineDettaglio = OrdineDettaglio.getById(dto.getAnno(), dto.getSerie(), dto.getProgressivo(), dto.getRigo());
                Optional<GoOrdineDettaglio> goOrdineDettaglioOptional = GoOrdineDettaglio.getById(dto.getProgrGenerale());
                if (goOrdineDettaglioOptional.isPresent()) {
                    goOrdineDettaglio = goOrdineDettaglioOptional.get();
                    Log.info("GO_ORDINE_DETTAGLIO trovato con progrGenerale: " + dto.getProgrGenerale());
                } else {
                    Log.info("GO_ORDINE_DETTAGLIO non trovato con progrGenerale: " + dto.getProgrGenerale());
                    goOrdineDettaglio = new GoOrdineDettaglio();
                    goOrdineDettaglio.setAnno(dto.getAnno());
                    goOrdineDettaglio.setSerie(dto.getSerie());
                    goOrdineDettaglio.setProgressivo(dto.getProgressivo());
                    goOrdineDettaglio.setRigo(dto.getRigo());
                    goOrdineDettaglio.setProgrGenerale(dto.getProgrGenerale());
                    goOrdineDettaglio.setStatus(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
                }
                if (!Objects.equals(ordineDettaglio.getFDescrArticolo(), dto.getFDescrArticolo())) {
                    ordineDettaglio.setFDescrArticolo(dto.getFDescrArticolo());
                }
                if (!Objects.equals(ordineDettaglio.getCodArtFornitore(), dto.getCodArtFornitore())) {
                    ordineDettaglio.setCodArtFornitore(dto.getCodArtFornitore());
                }
                if (!Objects.equals(ordineDettaglio.getQuantita(), dto.getQuantita())) {
                    registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                            dto.getProgressivo(), user, AzioneEnum.QUANTITA.getDesczrizione(),
                            dto.getRigo(), null, dto.getQuantita(), null, null));
                    ordineDettaglio.setQuantita(dto.getQuantita());
                    ordineDettaglio.setQuantitaV(dto.getQuantita());
                }
                if (!Objects.equals(ordineDettaglio.getTono(), dto.getTono())) {
                    registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                            dto.getProgressivo(), user, AzioneEnum.TONO.getDesczrizione(),
                            dto.getRigo(), dto.getTono(), null, null, null));
                    ordineDettaglio.setTono(dto.getTono());
                }
                if (!Objects.equals(dto.getFlagRiservato(), goOrdineDettaglio.getFlagRiservato())) {
                    registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                            dto.getProgressivo(), user, AzioneEnum.RISERVATO.getDesczrizione()
                            , dto.getRigo(), null, null, null, null));
                }
                if (!Objects.equals(dto.getFlagOrdinato(), goOrdineDettaglio.getFlagOrdinato())) {
                    registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                            dto.getProgressivo(), user, AzioneEnum.ORDINATO.getDesczrizione()
                            , dto.getRigo(), null, null, null, null));
                }
                if (!Objects.equals(dto.getFlagNonDisponibile(), goOrdineDettaglio.getFlagNonDisponibile())) {
                    registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                            dto.getProgressivo(), user, AzioneEnum.NON_DISPONIBILE.getDesczrizione()
                            , dto.getRigo(), null, null, null, null));
                }
                if (!Objects.equals(dto.getFlagConsegnato(), goOrdineDettaglio.getFlagConsegnato())) {
                    registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                            dto.getProgressivo(), user, AzioneEnum.CONSEGNATO.getDesczrizione()
                            , dto.getRigo(), null, null, null, null));
                }
                if (!Objects.equals(dto.getFlProntoConsegna(), goOrdineDettaglio.getFlProntoConsegna())) {
                    registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                            dto.getProgressivo(), user, AzioneEnum.PRONTO_CONSEGNA.getDesczrizione()
                            , dto.getRigo(), null, null, null, dto.getQtaProntoConsegna()));
                }
                if (!Objects.equals(goOrdineDettaglio.getQtaRiservata(), dto.getQtaRiservata())) {
                    registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                            dto.getProgressivo(), user, AzioneEnum.QTA_RISERVATA.getDesczrizione(),
                            dto.getRigo(), null, null, dto.getQtaRiservata(), null));
                }
                if (!warnNoBolla.get()) {
                    warnNoBolla.set(dto.getQtaConsegnatoSenzaBolla() != null && dto.getQtaConsegnatoSenzaBolla() != 0);
                }

                mapper.fromDtoToEntity(goOrdineDettaglio, dto);
                goOrdineDettaglioList.add(goOrdineDettaglio);
                ordineDettaglioList.add(ordineDettaglio);

            }
        });
        OrdineDettaglioDto dto = list.get(0);
        GoOrdine.update("warnNoBolla =:warn, hasProntoConsegna =:pc where anno =:anno and serie =:serie and progressivo = :progressivo",
                Parameters.with("anno", dto.getAnno())
                        .and("serie", dto.getSerie())
                        .and("progressivo", dto.getProgressivo())
                        .and("warn", warnNoBolla.get())
                        .and("pc", hasProntoConsegna.get()));
        if (!ordineDettaglioList.isEmpty()) {
            OrdineDettaglio.persist(ordineDettaglioList);
        }
        try {
            GoOrdineDettaglio.persist(goOrdineDettaglioList);
        } catch (Exception e) {
            Log.error(e.getMessage());
            return null;
        }


        RegistroAzioni.persist(registroAzioniList);
        if (chiudi) {
            GoOrdine.update("locked = 'F', userLock = null where anno =:anno and serie =:serie and progressivo = :progressivo",
                    Parameters.with("anno", dto.getAnno())
                            .and("serie", dto.getSerie())
                            .and("progressivo", dto.getProgressivo()));
            String stato = chiudi(dto.getAnno(), dto.getSerie(), dto.getProgressivo(), email);
            GoOrdineDettaglio.updateStatus(dto.getAnno(), dto.getSerie(), dto.getProgressivo(), stato);
            return stato;
        }
        return null;
    }

    private String chiudi(Integer anno, String serie, Integer progressivo, String email) {
        GoOrdine ordine = GoOrdine.findByOrdineId(anno, serie, progressivo);

        final String result = ordine.getStatus();
        if (StatoOrdineEnum.DA_PROCESSARE.getDescrizione().equals(result)) {
            if (GoOrdineDettaglio.count("anno = :anno and serie =:serie" +
                    " and progressivo =:progressivo" +
                    " and flagNonDisponibile = 'T'" +
                    " AND EXISTS (SELECT 1 FROM OrdineDettaglio o WHERE o.progrGenerale = progrGenerale)", Parameters.with("anno", anno)
                    .and("serie", serie)
                    .and("progressivo", progressivo)) > 0) {
                //sendMail(anno, serie, progressivo, email);
                ordine.setStatus(StatoOrdineEnum.DA_ORDINARE.getDescrizione());
            } else if(GoOrdineDettaglio.count("anno = :anno and serie =:serie" +
                    " and progressivo =:progressivo" +
                    " and flagOrdinato = 'T' " +
                    " AND EXISTS (SELECT 1 FROM OrdineDettaglio o WHERE o.progrGenerale = progrGenerale)", Parameters.with("anno", anno)
                    .and("serie", serie)
                    .and("progressivo", progressivo)) > 0) {
                ordine.setStatus(StatoOrdineEnum.INCOMPLETO.getDescrizione());
            } else {
                ordine.setStatus(StatoOrdineEnum.COMPLETO.getDescrizione());
            }
        }

        if (StatoOrdineEnum.DA_ORDINARE.getDescrizione().equals(result)) {
            if (GoOrdineDettaglio.count("anno = :anno and serie =:serie" +
                    " and progressivo =:progressivo" +
                    " and flagNonDisponibile = 'T' " +
                    " AND EXISTS (SELECT 1 FROM OrdineDettaglio o WHERE o.progrGenerale = progrGenerale)", Parameters.with("anno", anno)
                    .and("serie", serie)
                    .and("progressivo", progressivo)) == 0) {
                ordine.setStatus(StatoOrdineEnum.INCOMPLETO.getDescrizione());
            }
        }

        if (StatoOrdineEnum.INCOMPLETO.getDescrizione().equals(result)) {
            if(GoOrdineDettaglio.count("anno = :anno and serie =:serie" +
                    " and progressivo =:progressivo" +
                    " and (flagOrdinato = 'T' AND (flagRiservato = 'F' OR flagRiservato is null) ) " +
                    " AND EXISTS (SELECT 1 FROM OrdineDettaglio o WHERE o.progrGenerale = progrGenerale)", Parameters.with("anno", anno)
                    .and("serie", serie)
                    .and("progressivo", progressivo)) == 0) {
               // sendMail(anno, serie, progressivo, email);
                ordine.setStatus(StatoOrdineEnum.COMPLETO.getDescrizione());
            }

        }
        ordine.persist();
        return ordine.getStatus();
    }

    private void sendMail(Integer anno, String serie, Integer progressivo, String email) {
        OrdineDTO dto = Ordine.find("SELECT p.intestazione, p.localita, p.provincia, p.telefono, p.email, p.sottoConto " +
                                " FROM Ordine o " +
                                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto " +
                                "WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo",
                        Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo))
                .project(OrdineDTO.class).firstResult();
        File f = new File(pathReport + "/" + anno + "/" + serie + "/" + dto.getSottoConto() + "_" + anno + "_" + serie + "_" + progressivo + ".pdf");
        dto.setAnno(anno);
        dto.setSerie(serie);
        dto.setProgressivo(progressivo);
        mailService.sendMailOrdineCompleto(f, dto, email);
    }

    @Transactional
    public boolean addFornitore(PianoContiDto dto, String user) {
        try {
            FornitoreArticolo fornitoreArticolo = new FornitoreArticolo();
            fornitoreArticolo.setTempoConsegna(0);
            fornitoreArticolo.setCoefPrezzo(0f);
            fornitoreArticolo.setPrezzo(0f);
            fornitoreArticolo.setFDefault("S");
            fornitoreArticolo.setCreateUser(user);
            fornitoreArticolo.setUpdateUser(user);
            fornitoreArticolo.setFornitoreArticoloId(new FornitoreArticoloId(dto.getCodiceArticolo(), dto.getGruppoConto(), dto.getSottoConto()));
            fornitoreArticolo.setUpdateDate(new Date());
            fornitoreArticolo.setCreateDate(new Date());
            fornitoreArticolo.persist();
            return true;
        } catch (Exception e) {
            Log.error("Errore nella creazione del FornitoreArticolo ", e);
            return false;
        }
    }

    @Transactional
    public List<String> codificaArticoli(List<OrdineDettaglioDto> list, String user) {
        List<String> errors = new ArrayList<>();
        for (OrdineDettaglioDto dto : list) {
            if(StringUtils.isBlank(dto.getFDescrArticolo()) || !dto.getFDescrArticolo().contains("*")){
                Log.error("Articolo " + dto.getFDescrArticolo() + ": Fornitore senza *");
                errors.add("Articolo " + dto.getFDescrArticolo() + ": Fornitore senza *");
                continue;
            }
            String nomeFornitore = StringUtils.substringBetween(dto.getFDescrArticolo(), "*");
            if(StringUtils.isBlank(nomeFornitore)){
                Log.error("Articolo " + dto.getFDescrArticolo() + ": Fornitore non codificato correttamente");
                errors.add("Articolo " + dto.getFDescrArticolo() + ": Fornitore non codificato correttamente");
                continue;
            }
            Optional<ArticoloClasseFornitore> fornitore = ArticoloClasseFornitore.find("descrizione like '%:nome%' OR descrUser = :nome", Parameters.with("nome", nomeFornitore)).firstResultOptional();
            if(fornitore.isEmpty()){
                Log.error("Articolo " + dto.getFDescrArticolo() + ": Fornitore non trovato in TCA1");
                errors.add("Articolo " + dto.getFDescrArticolo() + ": Fornitore non trovato in TCA1");
                continue;
            }

            if(StringUtils.isBlank(fornitore.get().getDescrUser2())){
                Log.error("Articolo " + dto.getFDescrArticolo() + ": Sottoconto Fornitore non presente in TCA1");
                errors.add("Articolo " + dto.getFDescrArticolo() + ": Sottoconto Fornitore non presente in TCA1");
                continue;
            }

            String classeFornitore = fornitore.get().getCodice();
            String codArtFornitore = StringUtils.deleteWhitespace(dto.getCodArtFornitore());
            String codiceArticolo = this.creaId(codArtFornitore, classeFornitore);
            int length = StringUtils.length(codArtFornitore);
            Articolo articolo = new Articolo();
            if(length >= 13){
                Log.info("Codice articolo forn: " + codArtFornitore);
                Log.info("Codice articolo interno: " + codiceArticolo);
                Optional<Articolo> optArticolo =  Articolo.find("articolo = :codArt",
                        Parameters.with("codArt", codiceArticolo)).firstResultOptional();
                if(optArticolo.isPresent()){
                    Log.info("Trovato un articolo con codice: " + codiceArticolo + ". Codifico eliminando le prime 3 lettere");
                    codiceArticolo = this.creaId(StringUtils.truncate(codArtFornitore, 3,13), classeFornitore);
                }
                //Crea articolo
                createArticolo(articolo, codiceArticolo, user, dto, codArtFornitore, classeFornitore);
                salvaArticolo(user, errors, fornitore, articolo);
            } else {
                Optional<Articolo> optArticolo =  Articolo.find("descrArtSuppl = :codArt OR descrArticolo LIKE '%:codArt%'",
                        Parameters.with("codArt", codArtFornitore)).firstResultOptional();
                if(optArticolo.isPresent()){
                    Log.info("Articolo: " + dto.getFDescrArticolo() + " già presente");
                    articolo = optArticolo.get();
                    if(articolo.getFlTrattato() == null || "N".equals(articolo.getFlTrattato())){
                        articolo.setFlTrattato("S");
                        articolo.persist();
                    }
                } else {
                    //Crea articolo
                    createArticolo(articolo, codiceArticolo, user, dto, codArtFornitore, classeFornitore);
                    salvaArticolo(user, errors, fornitore, articolo);
                }
            }
            //Aggiorno ordine cliente
            OrdineDettaglio.update("fArticolo =:fArticolo, codArtFornitore =:codArtFornitore, fDescrArticolo =:fDescrArticolo " +
                            "WHERE anno =:anno AND serie =:serie AND progressivo =:progressivo AND rigo =:rigo",
                    Parameters.with("fArticolo", articolo.getArticolo()).and("codArtFornitore", articolo.getDescrArtSuppl())
                            .and("fDescrArticolo", articolo.getDescrArticolo()).and("anno", dto.getAnno())
                            .and("serie", dto.getSerie()).and("progressivo", dto.getProgressivo())
                            .and("rigo", dto.getRigo()));
        }
        return errors;

    }

    private void salvaArticolo(String user, List<String> errors, Optional<ArticoloClasseFornitore> fornitore, Articolo articolo) {
        Optional<Articolo> optARticolo = Articolo.findByIdOptional(articolo.getArticolo());
        if(optARticolo.isEmpty()){
            articolo.persist();
            //Crea fornitore alternativo
            FornitoreArticolo fornitoreArticolo = createFornArticolo(user, articolo, fornitore.get());
            fornitoreArticolo.persist();
            Log.info(FornitoreArticolo.findById(fornitoreArticolo.getFornitoreArticoloId()).toString());
        } else {
            Log.info("Impossibile salvare. Articolo già presente" + optARticolo.get().getArticolo());
            errors.add("Impossibile salvare. Articolo già presente " + optARticolo.get().getArticolo());
        }
    }

    private void createArticolo(Articolo articolo, String codiceArticolo, String user, OrdineDettaglioDto dto, String codArtFornitore, String classeFornitore){
        articolo.setArticolo(codiceArticolo);
        articolo.setCreateDate(new Date());
        articolo.setUpdateDate(new Date());
        articolo.setCreateUser(user);
        articolo.setUpdateUser(user);
        articolo.setDescrArticolo(this.pulisciDescrizione(dto.getFDescrArticolo()));
        articolo.setDescrArtSuppl(codArtFornitore);
        articolo.setUnitaMisura(dto.getFUnitaMisura());
        articolo.setClasseA1(classeFornitore);
        articolo.setCodiceIva("22");
        articolo.setFlTrattato("S");
        articolo.setFlagLotto("S");
        articolo.setFlagSconti("S");
    }

    private FornitoreArticolo createFornArticolo(String user, Articolo articolo, ArticoloClasseFornitore fornitore){
        FornitoreArticolo fornitoreArticolo = new FornitoreArticolo();
        fornitoreArticolo.setTempoConsegna(0);
        fornitoreArticolo.setCoefPrezzo(0f);
        fornitoreArticolo.setPrezzo(0f);
        fornitoreArticolo.setFDefault("S");
        fornitoreArticolo.setCreateUser(user);
        fornitoreArticolo.setUpdateUser(user);
        FornitoreArticoloId fornitoreArticoloId = new FornitoreArticoloId(articolo.getArticolo(), 2351, fornitore.getDescrUser2());
        fornitoreArticolo.setFornitoreArticoloId(fornitoreArticoloId);
        fornitoreArticolo.setUpdateDate(new Date());
        fornitoreArticolo.setCreateDate(new Date());
        return fornitoreArticolo;
    }

    private String pulisciDescrizione(String fDescrArticolo) {
        String remove = StringUtils.remove(fDescrArticolo, "*");
        return StringUtils.truncate(remove, 50);
    }

    private String creaId(String codiceArticolo, String classeFornitore) {
        String id = classeFornitore + codiceArticolo;
        int length = StringUtils.length(id);
        if(length == 13){
            return id;
        }
        String result;
        if(length>13){
            result = StringUtils.truncate(id, 13);
        } else {
            result = classeFornitore + StringUtils.leftPad(codiceArticolo, 10, '0');
        }
        return result;
    }

    public List<OrdineDettaglioDto> getArticoli(Integer anno, String serie, Integer progressivo) {
        String query = "SELECT o.anno,  o.progressivo,  o.tipoRigo,  o.rigo,  o.serie,  o.fArticolo,  " +
                "o.codArtFornitore,  o.fDescrArticolo,  o.quantita,  " +
                "  o.fUnitaMisura,  god.flagNonDisponibile, god.flagOrdinato, god.flagRiservato, " +
                "(CASE WHEN god.qtaDaConsegnare IS NULL THEN o.quantita ELSE god.qtaDaConsegnare END) as qtaDaConsegnare, " +
                "god.note " +
                "FROM OrdineDettaglio o " +
                "LEFT JOIN GoOrdineDettaglio god ON o.progrGenerale = god.progrGenerale " +
                "WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo " +
                "AND (god.flagConsegnato = 'F' OR god.flagConsegnato IS NULL OR god.flagConsegnato = '')";
        return OrdineDettaglio.find(query, Sort.ascending("o.rigo"), Parameters.with("anno", anno).and("serie", serie)
                .and("progressivo", progressivo)).project(OrdineDettaglioDto.class).list();
    }
}
