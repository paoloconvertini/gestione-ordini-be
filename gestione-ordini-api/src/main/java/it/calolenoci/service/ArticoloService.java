package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import it.calolenoci.enums.AzioneEnum;
import it.calolenoci.enums.StatoOrdineEnum;
import it.calolenoci.mapper.ArticoloMapper;
import it.calolenoci.mapper.GoOrdineDettaglioMapper;
import it.calolenoci.mapper.RegistroAzioniMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@ApplicationScoped
public class ArticoloService {


    @Inject
    OrdineService ordineService;

    @Inject
    RegistroAzioniMapper registroAzioniMapper;

    @Inject
    ArticoloMapper mapper;

    @ConfigProperty(name = "data.inizio")
    String dataCongig;

    @ConfigProperty(name = "admin.email")
    String adminEmail;
    @Inject
    GoOrdineDettaglioMapper goOrdineDettaglioMapper;

    @Inject
    AuditService auditService;

    @Inject
    MailService mailService;

    @Inject
    CheckNoBolleBatchService batchService;

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
        response.setAnno(ordineDTO.getAnno());
        response.setSerie(ordineDTO.getSerie());
        response.setProgressivo(ordineDTO.getProgressivo());
        response.setIntestazione(ordineDTO.getIntestazione());
        response.setRiferimento(ordineDTO.getRiferimento());
        response.setSottoConto(ordineDTO.getSottoConto());
        response.setLocked(ordineDTO.getLocked());
        response.setUserLock(ordineDTO.getUserLock());
        response.setTelefono(ordineDTO.getTelefono());
        response.setCellulare(ordineDTO.getCellulare());
        response.setDataOrdine(ordineDTO.getDataOrdine());
        response.setModalitaPagamento(ordineDTO.getModalitaPagamento());
        response.setNoteLogistica(ordineDTO.getNoteLogistica());
        response.setUserNoteLogistica(ordineDTO.getUserNoteLogistica());
        response.setDataNoteLogistica(ordineDTO.getDataNoteLogistica());
        response.setStatus(ordineDTO.getStatus());
        response.setArticoli(list);
        long fine = System.currentTimeMillis();
        Log.info("Get articoli ordine cliente: " + (fine - inizio) / 1000 + " sec");
        return response;
    }

    public List<OrdineDettaglioDto> findForReport(Integer anno, String serie, Integer progressivo) {
        return OrdineDettaglio.findArticoliForReport(anno, serie, progressivo);
    }

    @Transactional
    public boolean updateArticoliBolle(List<OrdineDettaglioDto> list) {
        long inizio = System.currentTimeMillis();
        try {

            if (list == null || list.isEmpty()) {
                Log.debug("Nessun articolo da aggiornare.");
                return true;
            }

            List<Integer> progrGenerali = list.stream()
                    .map(OrdineDettaglioDto::getProgrGenerale)
                    .distinct()
                    .toList();

            final int CHUNK_SIZE = 1000;

            Map<Integer, GoOrdineDettaglio> goMap = new HashMap<>();

            for (int i = 0; i < progrGenerali.size(); i += CHUNK_SIZE) {

                List<Integer> subList = progrGenerali.subList(
                        i,
                        Math.min(i + CHUNK_SIZE, progrGenerali.size())
                );

                List<GoOrdineDettaglio> partial = GoOrdineDettaglio.find("progrGenerale in (:pg)",
                        Parameters.with("pg", subList)).list();

                for (GoOrdineDettaglio g : partial) {
                    goMap.put(g.getProgrGenerale(), g);
                }
            }

            List<GoOrdineDettaglio> toUpdate = new ArrayList<>();
            Set<OrdineId> ordiniCoinvolti = new HashSet<>();

            final double TOLLERANZA = 0.1;

            for (OrdineDettaglioDto dto : list) {

                GoOrdineDettaglio go = goMap.get(dto.getProgrGenerale());
                if (go == null) continue;

                Double qta = dto.getQuantita();
                Double qtaBolla = dto.getQtaBolla();
                if (qta == null || qtaBolla == null) continue;

                double qtaDaConsegnare = qta - qtaBolla;
                Double oldQtaDaCons = go.getQtaDaConsegnare() == null ? 0.0 : go.getQtaDaConsegnare();

                if (Math.abs(oldQtaDaCons - qtaDaConsegnare) <= TOLLERANZA) {
                    continue;
                }

                Log.debug("Ho da consegnare per progrOrdCli = " + dto.getProgrGenerale());

                // Salvo copie old per audit
                Double oldQtaConsegnatoSenzaBolla = go.getQtaConsegnatoSenzaBolla();
                Boolean oldFlagConsegnato = go.getFlagConsegnato();
                Boolean oldFlProntoConsegna = go.getFlProntoConsegna();
                Double oldQtaProntoConsegna = go.getQtaProntoConsegna();
                Double oldQtaRiservata = go.getQtaRiservata();
                Boolean oldFlBolla = go.getFlBolla();

                // ======= LOGICA DI AGGIORNAMENTO =======
                if (Math.abs(qtaDaConsegnare) <= TOLLERANZA) {
                    go.setQtaConsegnatoSenzaBolla(null);
                    go.setFlagConsegnato(true);
                    go.setQtaDaConsegnare(0.0);
                } else {
                    go.setFlagConsegnato(false);
                    go.setQtaDaConsegnare(qtaDaConsegnare);
                }

                double diffQtaCons = qta - oldQtaDaCons;
                double diff = Math.abs(diffQtaCons - qtaBolla);

                if (diff > TOLLERANZA) {
                    go.setQtaProntoConsegna(null);
                    go.setQtaRiservata(null);
                    go.setFlProntoConsegna(false);
                } else {
                    go.setFlProntoConsegna(true);
                }

                go.setFlBolla(Boolean.TRUE);
                // =======================================

                // ======= AUDIT =======
                String entity = "GO_ORDINE_DETTAGLIO";
                Integer anno = dto.getAnno();
                String serie = dto.getSerie();
                Integer progressivo = dto.getProgressivo();
                Integer rigo = go.getRigo();
                Integer progr = go.getProgrGenerale();

                if (!Objects.equals(oldQtaDaCons, go.getQtaDaConsegnare())) {
                    auditService.logChange(entity, anno, serie, progressivo, rigo, progr,
                            "qtaDaConsegnare", oldQtaDaCons, go.getQtaDaConsegnare(),
                            "updateArticoliBolle", null);
                }

                if (!Objects.equals(oldQtaConsegnatoSenzaBolla, go.getQtaConsegnatoSenzaBolla())) {
                    auditService.logChange(entity, anno, serie, progressivo, rigo, progr,
                            "qtaConsegnatoSenzaBolla", oldQtaConsegnatoSenzaBolla, go.getQtaConsegnatoSenzaBolla(),
                            "updateArticoliBolle", null);
                }

                if (!Objects.equals(oldFlagConsegnato, go.getFlagConsegnato())) {
                    auditService.logChange(entity, anno, serie, progressivo, rigo, progr,
                            "flagConsegnato", oldFlagConsegnato, go.getFlagConsegnato(),
                            "updateArticoliBolle", null);
                }

                if (!Objects.equals(oldQtaProntoConsegna, go.getQtaProntoConsegna())) {
                    auditService.logChange(entity, anno, serie, progressivo, rigo, progr,
                            "qtaProntoConsegna", oldQtaProntoConsegna, go.getQtaProntoConsegna(),
                            "updateArticoliBolle", null);
                }

                if (!Objects.equals(oldQtaRiservata, go.getQtaRiservata())) {
                    auditService.logChange(entity, anno, serie, progressivo, rigo, progr,
                            "qtaRiservata", oldQtaRiservata, go.getQtaRiservata(),
                            "updateArticoliBolle", null);
                }

                if (!Objects.equals(oldFlProntoConsegna, go.getFlProntoConsegna())) {
                    auditService.logChange(entity, anno, serie, progressivo, rigo, progr,
                            "flProntoConsegna", oldFlProntoConsegna, go.getFlProntoConsegna(),
                            "updateArticoliBolle", null);
                }

                if (!Objects.equals(oldFlBolla, go.getFlBolla())) {
                    auditService.logChange(entity, anno, serie, progressivo, rigo, progr,
                            "flBolla", oldFlBolla, go.getFlBolla(),
                            "updateArticoliBolle", null);
                }
                // ====================== AUDIT END ======================
                toUpdate.add(go);
                ordiniCoinvolti.add(new OrdineId(dto.getAnno(), dto.getSerie(), dto.getProgressivo()));
            }

            if (!toUpdate.isEmpty()) {
                GoOrdineDettaglio.persist(toUpdate);
            }

            for (OrdineId id : ordiniCoinvolti) {

                Long count = GoOrdineDettaglio.find(
                        "anno = :a AND serie = :s AND progressivo = :p " +
                                "AND qtaConsegnatoSenzaBolla IS NOT NULL " +
                                "AND qtaConsegnatoSenzaBolla > 0",
                        Parameters.with("a", id.getAnno())
                                .and("s", id.getSerie())
                                .and("p", id.getProgressivo())
                ).count();

                if (count == 0) {
                    GoOrdine.update(
                            "warnNoBolla = 'F' WHERE anno = :a AND serie = :s AND progressivo = :p",
                            Parameters.with("a", id.getAnno())
                                    .and("s", id.getSerie())
                                    .and("p", id.getProgressivo())
                    );
                }
            }

            long fine = System.currentTimeMillis();
            Log.debug("UpdateArticoliBolle (ottimizzato): " + (fine - inizio) + " ms");
            auditService.flush();
            return true;

        } catch (Exception e) {
            Log.error("Errore UpdateArticoliBolle: " + e.getMessage(), e);
            return false;
        }
    }


    public boolean findNoProntaConsegna(Integer anno, String serie, Integer progressivo) {
        return GoOrdineDettaglio.count("anno = :anno AND serie = :serie AND progressivo = :progressivo " +
                        " and flProntoConsegna = true " +
                        " AND EXISTS (SELECT 1 FROM OrdineDettaglio o WHERE o.progrGenerale = progrGenerale)",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)) == 0;
    }

    public void checkNoBolle() {

        long inizio = System.currentTimeMillis();

        int pageSize = 500;
        int pageIndex = 0;

        while (true) {

            List<GoOrdineDettaglio> daReset = GoOrdineDettaglio.find(
                            "flBolla = TRUE AND NOT EXISTS (" +
                                    "SELECT 1 FROM FattureDettaglio f WHERE f.progrOrdCli = progrGenerale" +
                                    ") AND EXISTS (" +
                                    "SELECT 1 FROM OrdineDettaglio o WHERE o.progrGenerale = progrGenerale" +
                                    ")"
                    )
                    .page(pageIndex, pageSize)
                    .list();

            if (daReset.isEmpty()) {
                break;
            }

            batchService.processBatch(daReset);

            pageIndex++;
        }

        long fine = System.currentTimeMillis();
        Log.info("CheckNoBolle: " + (fine - inizio) + " msec");
    }

    @Transactional
    public void save(List<OrdineDettaglioDto> list, String user) {
        save(list, user, false);
    }

    @Transactional
    public String save(List<OrdineDettaglioDto> list, String user, Boolean chiudi) {
        List<RegistroAzioni> registroAzioniList = new ArrayList<>();
        List<OrdineDettaglio> ordineDettaglioList = new ArrayList<>();
        List<GoOrdineDettaglio> goOrdineDettaglioList = new ArrayList<>();
        List<OrdineDettaglio> listToCheck = new ArrayList<>();
        AtomicBoolean warnNoBolla = new AtomicBoolean(false);
        AtomicBoolean hasProntoConsegna = new AtomicBoolean(false);
        AtomicBoolean hasCarico = new AtomicBoolean(false);
        list.forEach(dto -> {
            if (!"AC".equals(dto.getTipoRigo())) {
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
                    String descrizione = dto.getFDescrArticolo();
                    if(dto.getFDescrArticolo().length() > 50) {
                        descrizione = StringUtils.truncate(dto.getFDescrArticolo(), 50);
                    }
                    ordineDettaglio.setFDescrArticolo(descrizione);
                }
                if (!Objects.equals(ordineDettaglio.getCodArtFornitore(), dto.getCodArtFornitore())) {
                    ordineDettaglio.setCodArtFornitore(dto.getCodArtFornitore());
                    listToCheck.add(ordineDettaglio);
                }
                if (!Objects.equals(ordineDettaglio.getQuantita(), dto.getQuantita())) {
                    registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                            dto.getProgressivo(), user, AzioneEnum.QUANTITA.getDesczrizione(),
                            dto.getRigo(), null, dto.getQuantita(), null, null));
                    mailService.inviaMailQuantita(adminEmail, dto.getAnno(), dto.getSerie(),
                            dto.getProgressivo(), user, ordineDettaglio.getFDescrArticolo(), ordineDettaglio.getQuantita(), dto.getQuantita());
                    ordineDettaglio.setQuantita(dto.getQuantita());
                    ordineDettaglio.setQuantitaV(dto.getQuantita());
                    List<FattureDettaglio> fatture = FattureDettaglio.find("Select f " +
                            "FROM FattureDettaglio f " +
                            "WHERE f.progrOrdCli = :id ",
                            Parameters.with("id", ordineDettaglio.getProgrGenerale())).list();
                    if(!fatture.isEmpty()){
                        double sum = fatture.stream().mapToDouble(FattureDettaglio::getQuantita).sum();
                        dto.setQtaDaConsegnare(dto.getQuantita() - sum);
                    } else {
                        dto.setQtaDaConsegnare(dto.getQuantita());
                    }
                    if(dto.getQtaDaConsegnare() != null && dto.getQtaDaConsegnare() < 0) {
                        dto.setQtaDaConsegnare(0D);
                    }
                }
                if (!Objects.equals(ordineDettaglio.getTono(), dto.getTono())) {
                    registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                            dto.getProgressivo(), user, AzioneEnum.TONO.getDesczrizione(),
                            dto.getRigo(), dto.getTono(), null, null, null));
                    ordineDettaglio.setTono(StringUtils.trim(dto.getTono()));
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

                if (!hasCarico.get()) {
                    hasCarico.set(dto.getFlagOrdinato() != null && !dto.getFlagRiservato() && dto.getNumDoc() != null);
                }

                mapper.fromDtoToEntity(goOrdineDettaglio, dto);
                goOrdineDettaglioList.add(goOrdineDettaglio);
                ordineDettaglioList.add(ordineDettaglio);

            }
        });
        OrdineDettaglioDto dto = list.get(0);
        GoOrdine.update("hasCarico =:hasCarico, warnNoBolla =:warn, hasProntoConsegna =:pc where anno =:anno and serie =:serie and progressivo = :progressivo",
                Parameters.with("anno", dto.getAnno())
                        .and("serie", dto.getSerie())
                        .and("progressivo", dto.getProgressivo())
                        .and("warn", warnNoBolla.get())
                        .and("pc", hasProntoConsegna.get())
                        .and("hasCarico", hasCarico.get()));
        Ordine.update("updateDate = :d " +
                "where anno =:anno and serie =:serie and progressivo = :progressivo",
                Parameters.with("anno", dto.getAnno())
                        .and("serie", dto.getSerie())
                        .and("progressivo", dto.getProgressivo())
                        .and("d", LocalDateTime.now()));
        if (!ordineDettaglioList.isEmpty()) {
            OrdineDettaglio.persist(ordineDettaglioList);
        }
        if (!listToCheck.isEmpty()) {
            checkCodArtFornitore(listToCheck);
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
            String stato = chiudi(dto.getAnno(), dto.getSerie(), dto.getProgressivo());
            GoOrdineDettaglio.updateStatus(dto.getAnno(), dto.getSerie(), dto.getProgressivo(), stato);
            return stato;
        }
        return null;
    }

    private String chiudi(Integer anno, String serie, Integer progressivo) {
        GoOrdine ordine = GoOrdine.findByOrdineId(anno, serie, progressivo);

        final String result = ordine.getStatus();
        List<GoOrdineDettaglio> goOrdineDettaglios = GoOrdineDettaglio.find("SELECT go FROM GoOrdineDettaglio go " +
                " WHERE anno = :anno and serie =:serie" +
                " and progressivo =:progressivo" +
                " AND EXISTS (SELECT 1 FROM OrdineDettaglio o WHERE o.progrGenerale = go.progrGenerale)", Parameters.with("anno", anno)
                .and("serie", serie)
                .and("progressivo", progressivo)).list();

        if (StatoOrdineEnum.DA_PROCESSARE.getDescrizione().equals(result)) {
            if(goOrdineDettaglios.stream().anyMatch(GoOrdineDettaglio::getFlagNonDisponibile)) {
                ordine.setStatus(StatoOrdineEnum.DA_ORDINARE.getDescrizione());
            } else if (goOrdineDettaglios.stream().anyMatch(o -> o.getFlagOrdinato()
                            && (o.getFlagRiservato() == null || !o.getFlagRiservato()))) {
                ordine.setStatus(StatoOrdineEnum.INCOMPLETO.getDescrizione());
            } else if (goOrdineDettaglios.stream().anyMatch(GoOrdineDettaglio::getFlagRiservato)) {
                ordine.setStatus(StatoOrdineEnum.COMPLETO.getDescrizione());
            }
        }

        if (StatoOrdineEnum.DA_ORDINARE.getDescrizione().equals(result)) {
            if (goOrdineDettaglios.stream().noneMatch(GoOrdineDettaglio::getFlagNonDisponibile)) {
                ordine.setStatus(StatoOrdineEnum.INCOMPLETO.getDescrizione());
            }
        }

        if (StatoOrdineEnum.INCOMPLETO.getDescrizione().equals(result)) {
            if (goOrdineDettaglios.stream().noneMatch(o -> o.getFlagOrdinato()
                    && (o.getFlagRiservato() == null || !o.getFlagRiservato()))) {
                ordine.setStatus(StatoOrdineEnum.COMPLETO.getDescrizione());
            }

        }
        ordine.persist();
        return ordine.getStatus();
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
    @TransactionConfiguration(timeout = 5000000)
    public CodificaArticoliDto codificaArticoli(List<OrdineDettaglioDto> list, String user) {
        CodificaArticoliDto codificaArticoliDto = new CodificaArticoliDto();
        codificaArticoliDto.setShowTCA(Boolean.FALSE);
        List<String> errors = new ArrayList<>();
        for (OrdineDettaglioDto dto : list) {
            if(StringUtils.isBlank(dto.getCodArtFornitore())) {
                Log.error("Articolo " + dto.getFDescrArticolo() + "  senza codice fornitore");
                errors.add("Articolo " + dto.getFDescrArticolo() + " senza codice fornitore");
                continue;
            } else {
                if("MEPO".equals(StringUtils.trim(dto.getCodArtFornitore()))){
                    OrdineDettaglio.update("fArticolo =:fArticolo " +
                                    "WHERE anno =:anno AND serie =:serie AND progressivo =:progressivo AND rigo =:rigo",
                            Parameters.with("fArticolo", "1000000000001").and("anno", dto.getAnno())
                                    .and("serie", dto.getSerie()).and("progressivo", dto.getProgressivo())
                                    .and("rigo", dto.getRigo()));
                    continue;
                }
            }

            Optional<Articolo> optArticolo = Articolo.find("(descrArtSuppl = :codArt OR descrArticolo like '%:codArt%') AND articolo NOT IN ('*PZ', '*ML','*KG')",
                    Parameters.with("codArt", dto.getCodArtFornitore())).firstResultOptional();
            if(optArticolo.isPresent()){
                Log.error("Articolo " + dto.getFDescrArticolo() + ":  già codificato come: " + optArticolo.get().getArticolo());
                errors.add("Articolo " + dto.getFDescrArticolo() + ":  già codificato come: " + optArticolo.get().getArticolo());
                Articolo articolo = optArticolo.get();
                if (articolo.getFlTrattato() == null || "N".equals(articolo.getFlTrattato())) {
                    articolo.setFlTrattato("S");
                    articolo.persist();
                }
                aggiornoOrdineCliente(dto, articolo);
                continue;
            }

            if (StringUtils.isBlank(dto.getFDescrArticolo()) || !dto.getFDescrArticolo().contains("*")) {
                Log.error("Articolo " + dto.getFDescrArticolo() + ": Fornitore senza *");
                errors.add("Articolo " + dto.getFDescrArticolo() + ": Fornitore senza *");
                continue;
            }
            String nomeFornitore = StringUtils.substringBetween(dto.getFDescrArticolo(), "*");
            if (StringUtils.isBlank(nomeFornitore)) {
                Log.error("Articolo " + dto.getFDescrArticolo() + ": Fornitore non codificato correttamente");
                errors.add("Articolo " + dto.getFDescrArticolo() + ": Fornitore non codificato correttamente");
                continue;
            }
            Optional<ArticoloClasseFornitore> fornitore = ArticoloClasseFornitore.find("descrUser = :nome", Parameters.with("nome", nomeFornitore)).firstResultOptional();
            if (fornitore.isEmpty()) {
                Log.error("Articolo " + dto.getFDescrArticolo() + ": Fornitore non trovato in TCA1");
                errors.add("Articolo " + dto.getFDescrArticolo() + ": Fornitore non trovato in TCA1");
                codificaArticoliDto.setShowTCA(Boolean.TRUE);
                continue;
            }

            if (StringUtils.isBlank(fornitore.get().getDescrUser2())) {
                Log.error("Articolo " + dto.getFDescrArticolo() + ": Sottoconto Fornitore non presente in TCA1");
                errors.add("Articolo " + dto.getFDescrArticolo() + ": Sottoconto Fornitore non presente in TCA1");
                continue;
            }

            String codiceFornitore;
            if (StringUtils.isNotBlank(fornitore.get().getDescrUser3())) {
                codiceFornitore = fornitore.get().getDescrUser3();
            } else {
                codiceFornitore = fornitore.get().getCodice();
            }
            String classeFornitore = fornitore.get().getCodice();
            String codArtFornitore = StringUtils.deleteWhitespace(dto.getCodArtFornitore());
            String codiceArticolo = this.creaId(codArtFornitore, codiceFornitore);
            int length = StringUtils.length(codArtFornitore);
            Articolo articolo = new Articolo();
            if (length >= 13) {
                Log.info("Codice fornitore maggiore di 13. Cerco max progressivo per la classe fornitore: " + codiceFornitore);
                codiceArticolo = Articolo.find("SELECT ISNULL(MAX(articolo), '1') " +
                                "FROM Articolo " +
                                "WHERE ISNUMERIC(articolo) = 1" +
                                " AND articolo LIKE :codForn",
                        Parameters.with("codForn", codiceFornitore+"%")).project(String.class).firstResult();

                if(StringUtils.equals("1", codiceArticolo)) {
                    this.creaId(codiceArticolo, codiceFornitore);
                } else {
                    try {
                        codiceArticolo = String.valueOf(Long.parseLong(codiceArticolo)+1);
                    } catch (Exception e) {
                        Log.error("Errore nel parse del massimo progressivo articolo: " + codiceArticolo);
                        errors.add("Errore Articolo " + dto.getFDescrArticolo() + ". Modificare codice articolo fornitore: " + codiceArticolo);
                        continue;
                    }

                }

            }
            Log.info("Codice articolo forn: " + codArtFornitore);
            Log.info("Codice articolo interno: " + codiceArticolo);
            //Crea articolo
            createArticolo(articolo, codiceArticolo, user, dto, codArtFornitore, classeFornitore);
            salvaArticolo(user, errors, fornitore, articolo);
            //Aggiorno ordine cliente
            if(!StringUtils.equals(articolo.getArticolo(),"*PZ")){
                aggiornoOrdineCliente(dto, articolo);
            }
        }
        codificaArticoliDto.setErrors(errors);
        return codificaArticoliDto;

    }

    private void aggiornoOrdineCliente(OrdineDettaglioDto dto, Articolo articolo) {
        String descrSuppl = articolo.getDescrArtSuppl();
        if(articolo.getDescrArtSuppl().length() > 25){
            descrSuppl = StringUtils.truncate(articolo.getDescrArtSuppl(), 25);
        }
        OrdineDettaglio.update("fArticolo =:fArticolo, codArtFornitore =:codArtFornitore, fDescrArticolo =:fDescrArticolo " +
                        "WHERE anno =:anno AND serie =:serie AND progressivo =:progressivo AND rigo =:rigo",
                Parameters.with("fArticolo", articolo.getArticolo()).and("codArtFornitore", descrSuppl)
                        .and("fDescrArticolo", articolo.getDescrArticolo()).and("anno", dto.getAnno())
                        .and("serie", dto.getSerie()).and("progressivo", dto.getProgressivo())
                        .and("rigo", dto.getRigo()));
    }

    private void salvaArticolo(String user, List<String> errors, Optional<ArticoloClasseFornitore> fornitore, Articolo articolo) {
        Optional<Articolo> optARticolo = Articolo.findByIdOptional(articolo.getArticolo());
        if (optARticolo.isEmpty()) {
            articolo.persist();
            //Crea fornitore alternativo
            FornitoreArticoloId fornitoreArticoloId = new FornitoreArticoloId(articolo.getArticolo(), 2351, fornitore.get().getDescrUser2());
            FornitoreArticolo fa = FornitoreArticolo.findById(fornitoreArticoloId);
            if(fa == null){
                FornitoreArticolo fornitoreArticolo = createFornArticolo(user, articolo, fornitore.get());
                fornitoreArticolo.persist();
            }
        } else {
            Log.info("Impossibile salvare. Articolo già presente" + optARticolo.get().getArticolo());
            errors.add("Impossibile salvare. Articolo già presente " + optARticolo.get().getArticolo());
        }
    }

    private void createArticolo(Articolo articolo, String codiceArticolo, String user, OrdineDettaglioDto dto, String codArtFornitore, String codiceFornitore) {
        articolo.setArticolo(codiceArticolo);
        articolo.setDescrArticolo(this.pulisciDescrizione(dto.getFDescrArticolo()));
        articolo.setDescrArtSuppl(codArtFornitore);
        articolo.setDescrEstesa("");
        articolo.setOrdinamento("");
        articolo.setUnitaMisura(dto.getFUnitaMisura());
        articolo.setUnitaMisuraSec("");
        articolo.setUnitaMisura2("");
        articolo.setCoefficiente(0D);
        articolo.setCoefficientePro(0D);
        articolo.setCostoBase(0F);
        articolo.setCostoLavoro(0D);
        articolo.setPrezzoBase(0D);
        articolo.setPrezzoUmSec("");
        articolo.setPrezzoExtra(0D);
        articolo.setScontoBase(0D);
        articolo.setCodiceIva("22");
        articolo.setCodDecimaliPrezzo("");
        articolo.setProvvAgente(0D);
        articolo.setCalcolaProvv("");
        articolo.setGruppoVendite(0);
        articolo.setContoVendite("");
        articolo.setClasseA1(codiceFornitore);
        articolo.setClassea2("");
        articolo.setClassea3("");
        articolo.setClassea4("");
        articolo.setClassea5("");
        articolo.setClassea6("");
        articolo.setClassea7("");
        articolo.setClassea8("");
        articolo.setClassea9("");
        articolo.setClassea10("");
        articolo.setFlagListino("");
        articolo.setGruppoAcquisti(0);
        articolo.setContoAquisti("");
        articolo.setQuantitaUser01(0D);
        articolo.setQuantitaUser02(0D);
        articolo.setQuantitaUser03(0D);
        articolo.setQuantitaUser04(0D);
        articolo.setQuantitaUser05(0D);
        articolo.setCampoUser1("");
        articolo.setCampoUser2("");
        articolo.setCampoUser3("");
        articolo.setCampoUser4("");
        articolo.setCampoUser5("");
        articolo.setArticoloRaggr("");
        articolo.setRifOriginale("");
        articolo.setNoteArticolo("");
        articolo.setNomenclatura("");
        articolo.setIvaAgevolata("");
        articolo.setTipoDocumentoFe("");
        articolo.setPeso(0D);
        articolo.setPesoNetto(0D);
        articolo.setQtaPerConf(0D);
        articolo.setDimPerConf("");
        articolo.setPesoPerConf(0D);
        articolo.setQtaBusta(0D);
        articolo.setPesoBusta(0D);
        articolo.setPallet("");
        articolo.setQtaPallet(0D);
        articolo.setGestioneScorta("");
        articolo.setUbicazione("");
        articolo.setDepositoReparto("");
        articolo.setQtaReparto(0D);
        articolo.setLottoMinimo(0D);
        articolo.setQtaLotto(0D);
        articolo.setPuntoRiordino(0D);
        articolo.setScortaMinima(0D);
        articolo.setGgaPProvvig(0);
        articolo.setScortaMinima(0D);
        articolo.setQtaMinimaFatt(0D);
        articolo.setLottoMinimoFatt(0);
        articolo.setQualita("");
        articolo.setModuloetk("");
        articolo.setTempoProd(0D);
        articolo.setArtDistintaBase("");
        articolo.setArticoLoc("");
        articolo.setVariante1("");
        articolo.setVariante2("");
        articolo.setVariante3("");
        articolo.setVariante4("");
        articolo.setVariante5("");
        articolo.setAggrv1("");
        articolo.setAggrv2("");
        articolo.setAggrv3("");
        articolo.setAggrv4("");
        articolo.setAggrv5("");
        articolo.setArticoloVuoto("");
        articolo.setQtyVuoti(0D);
        articolo.setTipoRigoGruppi("");
        articolo.setCespite("");
        articolo.setBloccato("");
        articolo.setProgr1(0);
        articolo.setProgr2(0);
        articolo.setQtaRif(0D);
        articolo.setTipoArticolo("");
        articolo.setCausaleInevaso("");
        articolo.setServizio(0D);
        articolo.setFlGiorniMesiScad("");
        articolo.setPeriodoScadenza(0);
        articolo.setFlNumeroSerie("");
        articolo.setFlAssortimento("");
        articolo.setFlCodiceEan("");
        articolo.setFlagLotto("N");
        articolo.setFlTrattato("S");
        articolo.setFlagSconti("S");
        articolo.setFlPallet("");
        articolo.setFlagTrasferito("");
        articolo.setFlFabbricazione("");
        articolo.setImmagine("");
        articolo.setLink1("");
        articolo.setLink2("");
        articolo.setLink3("");
        articolo.setLinkScheda("");
        articolo.setPubblicazione(0);
        articolo.setFlB2B("S");
        articolo.setFlB2C("S");
        articolo.setNoteCatalogo("");
        articolo.setAppoggio("");
        articolo.setOmaggiabile("");
        articolo.setRendibile("");
        articolo.setGruppoFornitore(0);
        articolo.setContoFornitore("");
        articolo.setDocumento("");
        articolo.setCreateDate(new Date());
        articolo.setUpdateDate(new Date());
        articolo.setCreateUser(user);
        articolo.setUpdateUser(user);
        articolo.setUnitaMisuraPro("");
        articolo.setScortaReparto(0D);
        articolo.setFlCoeffTeorico("N");
        articolo.setFlUm2Produzione("");
        articolo.setFlUm2Vendita("");
        articolo.setFlUmSecAcquisti("");
        articolo.setFlUmSecVendita("");
        articolo.setProvvCapoArea(0F);
        articolo.setDataModifica(LocalDateTime.now());
        articolo.setUsername(user);
        articolo.setFlConf("");
        articolo.setDescrBreve("");
        articolo.setColliStrato(0);
    }

    private FornitoreArticolo createFornArticolo(String user, Articolo articolo, ArticoloClasseFornitore fornitore) {
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
        if (length == 13) {
            return id;
        }
        String result;
        if (length > 13) {
            result = StringUtils.truncate(id, 13);
        } else {
            result = classeFornitore + StringUtils.leftPad(codiceArticolo, 10, '0');
        }
        return result;
    }

    public List<OrdineDettaglioDto> getArticoli(String bolla, Integer anno, String serie, Integer progressivo) {
        String query = "SELECT o.anno,  o.progressivo,  o.tipoRigo,  o.rigo,  o.serie,  o.fArticolo,  " +
                "o.codArtFornitore,  o.fDescrArticolo,  o.quantita,  " +
                "  o.fUnitaMisura,  god.flagNonDisponibile, god.flagOrdinato, god.flagRiservato, " +
                "(CASE WHEN god.qtaDaConsegnare IS NULL THEN o.quantita ELSE god.qtaDaConsegnare END) as qtaDaConsegnare, " +
                "god.note, god.qtaProntoConsegna, o.fCodiceIva, o.prezzo*(1-o.scontoArticolo/100)*(1-o.scontoC1/100)*(1-o.scontoC2/100)*(1-o.scontoP/100), " +
                "f.anno as annoOAF, f.serie as serieOAF, f.progressivo as progressivoOAF, f.dataOrdine as dataOrdineOAF, god.progrGenerale " +
                "FROM OrdineDettaglio o " +
                "LEFT JOIN GoOrdineDettaglio god ON o.progrGenerale = god.progrGenerale " +
                "LEFT JOIN OrdineFornitoreDettaglio f2 ON f2.pid = o.progrGenerale " +
                "LEFT JOIN OrdineFornitore f ON f.anno = f2.anno AND f.serie = f2.serie AND f.progressivo = f2.progressivo " +
                "WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo ";
        if ("Y".equals(bolla)) {
            query += " AND god.flProntoConsegna = 'T'";
        } else if ("N".equals(bolla)) {
            query += " AND (god.flagConsegnato = 'F' OR god.flagConsegnato IS NULL OR god.flagConsegnato = '')";
        } else {
            query += " AND saldoAcconto <> 'S'";
        }
        List<OrdineDettaglioDto> list = OrdineDettaglio.find(query, Sort.ascending("o.rigo"), Parameters.with("anno", anno).and("serie", serie)
                .and("progressivo", progressivo)).project(OrdineDettaglioDto.class).list();
        if(!"Y".equals(bolla) && !"N".equals(bolla)) {
            list.forEach(ordineDettaglio -> {
                List<FattureDettaglio> fatture = FattureDettaglio.find("Select f " +
                                "FROM FattureDettaglio f " +
                                "WHERE f.progrOrdCli = :id ",
                        Parameters.with("id", ordineDettaglio.getProgrGenerale())).list();
                if(!fatture.isEmpty()){
                    double sum = fatture.stream().mapToDouble(FattureDettaglio::getQuantita).sum();
                    ordineDettaglio.setQtaDaConsegnare(ordineDettaglio.getQuantita() - sum);
                } else {
                    ordineDettaglio.setQtaDaConsegnare(ordineDettaglio.getQuantita());
                }
            });

        }
        return list;
    }

    public List<OrdineDettaglioDto> getArticoliRiservati(Integer anno, String serie, Integer progressivo) {
        String query = "SELECT o.anno,  o.progressivo,  o.tipoRigo,  o.rigo,  o.serie,  o.fArticolo,  " +
                "o.codArtFornitore,  o.fDescrArticolo,   " +
                "(CASE WHEN god.qtaDaConsegnare IS NULL THEN o.quantita ELSE god.qtaDaConsegnare END) as qtaDaConsegnare, " +
                "o.fUnitaMisura, god.note, " +
                "f.anno as annoOAF, f.serie as serieOAF, f.progressivo as progressivoOAF, f.dataOrdine as dataOrdineOAF, god.progrGenerale, " +
                "o.prezzo*(1-o.scontoArticolo/100)*(1-o.scontoC1/100)*(1-o.scontoC2/100)*(1-o.scontoP/100) " +
                "FROM OrdineDettaglio o " +
                "LEFT JOIN GoOrdineDettaglio god ON o.progrGenerale = god.progrGenerale " +
                "LEFT JOIN OrdineFornitoreDettaglio f2 ON f2.pid = o.progrGenerale " +
                "LEFT JOIN OrdineFornitore f ON f.anno = f2.anno AND f.serie = f2.serie AND f.progressivo = f2.progressivo " +
                "WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo " +
                "AND god.flagConsegnato <> 'T' AND  god.flagRiservato = 'T' ";
        return OrdineDettaglio.find(query, Sort.ascending("o.rigo"), Parameters.with("anno", anno).and("serie", serie)
                .and("progressivo", progressivo)).project(OrdineDettaglioDto.class).list();
    }

    private void checkCodArtFornitore(List<OrdineDettaglio> ordineDettaglioDtos) {
        ordineDettaglioDtos.forEach(o -> {
            try {
                Articolo.update("descrArtSuppl =:cod WHERE articolo = :desc"
                        , Parameters.with("cod", o.getCodArtFornitore()).and("desc", o.getFArticolo()));
            } catch (Exception e) {
                Log.error("Cod art non aggiornato per articolo: " + o.getFArticolo());
            }
        });

    }

    @Transactional
    public void findCarichi() {
        try {
            long inizio = System.currentTimeMillis();
            LocalDate d = LocalDate.parse(dataCongig);
            List<CaricoDto> data = OrdineDettaglio.find("SELECT og, d, m  FROM OrdineDettaglio o2 " +
                            " JOIN GoOrdineDettaglio d ON d.progrGenerale = o2.progrGenerale " +
                            " JOIN GoOrdine og ON og.anno = d.anno AND og.serie = d.serie AND og.progressivo = d.progressivo " +
                            " JOIN Ordine o ON o.anno = d.anno AND o.serie = d.serie AND o.progressivo = d.progressivo " +
                            " JOIN OrdineFornitoreDettaglio f ON f.pid = o2.progrGenerale " +
                            " JOIN Magazzino m ON m.pid = f.progrGenerale " +
                            " WHERE og.status <> 'ARCHIVIATO' and o.dataConferma >= :data and f.saldo IN ('S', 'A')" +
                            " and d.flagOrdinato = 'T' AND d.flagRiservato <> 'T' and d.flagConsegnato = 'F'",
                    Parameters.with("data", d)).project(CaricoDto.class).list();

            if (!data.isEmpty()) {
                List<GoOrdineDettaglio> goOrdineDettaglioList = new ArrayList<>();
                List<GoOrdine> ordineList = new ArrayList<>();
                for (CaricoDto dto : data) {
                    GoOrdineDettaglio goOrdineDettaglio = dto.getGoOrdineDettaglio();
                    Magazzino magazzino = dto.getMagazzino();
                    goOrdineDettaglio.setNumDoc(magazzino.getNumdocmagazzino());
                    goOrdineDettaglio.setDataCarico(magazzino.getDataMagazzino());
                    goOrdineDettaglio.setDataDoc(magazzino.getDatadocmag());
                    goOrdineDettaglio.setAnnoMag(magazzino.getMagazzinoId().getAnno());
                    goOrdineDettaglio.setSerieMag(magazzino.getMagazzinoId().getSerie());
                    goOrdineDettaglio.setProgressivoMag(magazzino.getMagazzinoId().getProgressivo());
                    goOrdineDettaglioList.add(goOrdineDettaglio);
                    ordineList.add(dto.getGoOrdine());
                }

                if (!goOrdineDettaglioList.isEmpty()) {
                    GoOrdineDettaglio.persist(goOrdineDettaglioList);
                }

                if(!ordineList.isEmpty()){
                    Set<GoOrdine> ordines = new HashSet<>(ordineList);
                    ordines.forEach(o -> {
                        Log.debug("Ordine n." + o.getAnno() + " " + o.getSerie() + " " + o.getProgressivo());
                        o.setHasCarico(Boolean.TRUE);
                    });
                    GoOrdine.persist(ordines);
                }


            }
            long fine = System.currentTimeMillis();
            Log.info("find carichi: " + (fine - inizio) / 1000 + " sec");
        } catch (Exception e) {
            Log.error("Errore find carichi", e);
        }
    }

    @Transactional
    public void sincronizzaCodiceArticolo() {
        try {
            long inizio = System.currentTimeMillis();
            int i = GoOrdineDettaglio.getEntityManager().createNativeQuery(" " +
                    "UPDATE d SET d.fArticolo = o.fArticolo " +
                    "FROM GO_ORDINE_DETTAGLIO d " +
                    "JOIN ORDCLI2 o ON d.PROGRGENERALE = o.PROGRGENERALE " +
                    "WHERE d.FARTICOLO IN ('*PZ', '*MQ', '*ML', '*QL', '*MD') " +
                    "AND o.FARTICOLO NOT IN ('*PZ', '*MQ', '*ML', '*QL', '*MD')").executeUpdate();
            Log.debug("sincronizzaCodiceArticolo: Aggiornati n. " + i + " records");
            long fine = System.currentTimeMillis();
            Log.info("sincronizzaCodiceArticolo: " + (fine - inizio) / 1000 + " sec");
        } catch (Exception e) {
            Log.error("Errore sincronizzaCodiceArticolo");
        }
    }

    @Transactional
    public void resetGoOrdineDettaglio() {
        try {
            long inizio = System.currentTimeMillis();
            List<OrdineClienteDto> list = OrdineDettaglio.find("SELECT o, d " +
                    "FROM OrdineDettaglio o " +
                    "JOIN GoOrdineDettaglio d ON o.progrGenerale = d.progrGenerale AND o.fArticolo <> d.fArticolo " +
                    "WHERE d.FARTICOLO NOT IN ('*PZ', '*MQ', '*ML', '*QL', '*MD') ").project(OrdineClienteDto.class).list();
            if(!list.isEmpty()) {
                List<GoOrdineDettaglio> goOrdineDettaglios = new ArrayList<>();
                list.forEach(e->{
                    Log.debug("*** Ordine n. " + e.getOrdineDettaglio().getAnno() + "/" + e.getOrdineDettaglio().getSerie() + "/" + e.getOrdineDettaglio().getProgressivo() + "*****");
                    Log.debug("GO_Articolo " +  e.getGoOrdineDettaglio().toString());
                    Log.debug("Articolo " + e.getOrdineDettaglio().getFArticolo() + ", " + e.getOrdineDettaglio().getProgrGenerale() + " resettato");
                    goOrdineDettaglios.add(goOrdineDettaglioMapper.fromOrdineDettaglioToGoOrdineDettaglio(e.getOrdineDettaglio()));
                });
                GoOrdineDettaglio.persist(goOrdineDettaglios);
            }
            long fine = System.currentTimeMillis();
            Log.info("resetGoOrdineDettaglio: " + (fine - inizio) / 1000 + " sec");
        } catch (Exception e) {
            Log.error("Errore resetGoOrdineDettaglio");
        }
    }
}
