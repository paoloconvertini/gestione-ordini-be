package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import it.calolenoci.enums.AzioneEnum;
import it.calolenoci.enums.StatoOrdineEnum;
import it.calolenoci.mapper.ArticoloMapper;
import it.calolenoci.mapper.RegistroAzioniMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        ResponseOrdineDettaglio response = new ResponseOrdineDettaglio();
        List<OrdineDettaglioDto> list;
        OrdineDTO ordineDTO = ordineService.findById(filtro.getAnno(), filtro.getSerie(), filtro.getProgressivo());
        list = OrdineDettaglio.findArticoliById(filtro);
        Double aDouble = OrdineDettaglio.find("SELECT SUM(o.prezzo*o.quantita) FROM OrdineDettaglio o " +
                                "WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo"
                        , Parameters.with("anno", filtro.getAnno()).and("serie", filtro.getSerie()).and("progressivo", filtro.getProgressivo()))
                .project(Double.class).singleResult();
        response.setTotale(aDouble);
        response.setIntestazione(ordineDTO.getIntestazione());
        response.setSottoConto(ordineDTO.getSottoConto());
        response.setLocked(ordineDTO.getLocked());
        response.setUserLock(ordineDTO.getUserLock());
        response.setTelefono(ordineDTO.getTelefono());
        response.setArticoli(list);
        return response;
    }

    public boolean findAnyNoStatus(Integer anno, String serie, Integer progressivo) {
        return !OrdineDettaglio.find("SELECT o FROM OrdineDettaglio o" +
                        " WHERE anno = :anno AND serie = :serie AND progressivo = :progressivo " +
                        " AND NOT EXISTS (SELECT 1 FROM GoOrdineDettaglio god WHERE god.anno = o.anno" +
                        " AND god.serie = o.serie AND god.progressivo = o.progressivo and god.rigo = o.rigo)",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)).list().isEmpty();
    }

    @Transactional
    public Integer updateArticoliBolle(List<OrdineDettaglioDto> list) {
        AtomicReference<Integer> aggiornati = new AtomicReference<>(0);
        list.forEach(e -> {
            Optional<GoOrdineDettaglio> optional = GoOrdineDettaglio.getById(e.getAnno(), e.getSerie(), e.getProgressivo(), e.getRigo());
            if (optional.isPresent()) {
                GoOrdineDettaglio goOrdineDettaglio = optional.get();
                aggiornati.updateAndGet(v -> v + GoOrdineDettaglio.update(
                        " qtaConsegnatoSenzaBolla = CASE WHEN (:quantita - :qta) = 0 THEN NULL ELSE qtaConsegnatoSenzaBolla END, " +
                                " flagConsegnato = CASE WHEN (:quantita - :qta) = 0 THEN 'T' ELSE 'F' END, " +
                                " qtaProntoConsegna = CASE WHEN (:quantita - qtaDaConsegnare) <> :qta THEN NULL ELSE qtaProntoConsegna END, " +
                                " flProntoConsegna = CASE WHEN (:quantita - qtaDaConsegnare) <> :qta THEN 'F' ELSE 'T' END, " +
                                " qtaRiservata = CASE WHEN (:quantita - qtaDaConsegnare) <> :qta THEN NULL ELSE qtaRiservata END, " +
                                " qtaDaConsegnare = (:quantita - :qta), " +
                                " flBolla = 'T' WHERE anno=:anno AND serie=:serie AND progressivo =:progressivo and rigo =:rigo" +
                                " AND qtaDaConsegnare <> (:quantita - :qta)",
                        Parameters.with("qta", e.getQtaBolla()).and("anno", e.getAnno()).and("rigo", e.getRigo())
                                .and("serie", e.getSerie()).and("progressivo", e.getProgressivo()).and("quantita", e.getQuantita())));

                if (goOrdineDettaglio.getQtaConsegnatoSenzaBolla() == null || goOrdineDettaglio.getQtaConsegnatoSenzaBolla() == 0) {
                    GoOrdine.update("warnNoBolla = 'F' where anno =:anno and serie =:serie and progressivo = :progressivo",
                            Parameters.with("anno", e.getAnno())
                                    .and("serie", e.getSerie())
                                    .and("progressivo", e.getProgressivo()));
                }
            }
        });
        return aggiornati.get();
    }

    public boolean findNoConsegnati(Integer anno, String serie, Integer progressivo) {
        return OrdineDettaglio.find("SELECT o FROM OrdineDettaglio o" +
                        " LEFT JOIN GoOrdineDettaglio god ON god.anno = o.anno " +
                        " AND god.serie = o.serie AND god.progressivo = o.progressivo " +
                        " WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo " +
                        " and (god.flagConsegnato = false OR god.flagConsegnato IS NULL)",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)).list().isEmpty();
    }

    public void checkNoBolle() {

        List<OrdineDettaglioDto> list = GoOrdineDettaglio.find("select god.anno, god.serie, god.progressivo, god.rigo  FROM GoOrdineDettaglio god " +
                        "INNER JOIN OrdineDettaglio o ON o.anno = god.anno AND o.serie = god.serie AND " +
                        "o.progressivo = god.progressivo AND o.rigo = god.rigo " +
                        "WHERE NOT EXISTS (SELECT 1 FROM FattureDettaglio f2 WHERE f2.progrOrdCli = o.progrGenerale) " +
                        "AND god.flBolla = 'T' ")
                .project(OrdineDettaglioDto.class).list();
        if (!list.isEmpty()) {
            list.forEach(a -> GoOrdineDettaglio.update("flagConsegnato = 'F', qtaDaConsegnare = null," +
                            " flBolla = 'F' WHERE anno = :anno AND serie = :serie AND progressivo = :progressivo " +
                            " and rigo =:rigo"
                    , Parameters.with("anno", a.getAnno())
                            .and("serie", a.getSerie())
                            .and("progressivo", a.getProgressivo())
                            .and("rigo", a.getRigo())));
        }


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
        list.forEach(dto -> {
            GoOrdineDettaglio goOrdineDettaglio = new GoOrdineDettaglio();
            OrdineDettaglio ordineDettaglio = OrdineDettaglio.getById(dto.getAnno(), dto.getSerie(), dto.getProgressivo(), dto.getRigo());
            Optional<GoOrdineDettaglio> goOrdineDettaglioOptional = GoOrdineDettaglio.getById(dto.getAnno(), dto.getSerie(), dto.getProgressivo(), dto.getRigo());
            if (goOrdineDettaglioOptional.isPresent()) {
                goOrdineDettaglio = goOrdineDettaglioOptional.get();
            } else {
                goOrdineDettaglio.setAnno(dto.getAnno());
                goOrdineDettaglio.setSerie(dto.getSerie());
                goOrdineDettaglio.setProgressivo(dto.getProgressivo());
                goOrdineDettaglio.setRigo(dto.getRigo());
            }
            if (!Objects.equals(ordineDettaglio.getQuantita(), dto.getQuantita())) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), user, AzioneEnum.QUANTITA.getDesczrizione(),
                        dto.getRigo(), null, dto.getQuantita(), null, null));
                ordineDettaglio.setQuantita(dto.getQuantita());
                ordineDettaglio.setQuantitaV(dto.getQuantita());
                ordineDettaglioList.add(ordineDettaglio);
            }
            if (!Objects.equals(goOrdineDettaglio.getTono(), dto.getTono())) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), user, AzioneEnum.TONO.getDesczrizione(),
                        dto.getRigo(), dto.getTono(), null, null, null));
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
        });
        OrdineDettaglioDto dto = list.get(0);
        GoOrdine.update("warnNoBolla =:warn where anno =:anno and serie =:serie and progressivo = :progressivo",
                Parameters.with("anno", dto.getAnno())
                                .and("serie", dto.getSerie())
                                .and("progressivo", dto.getProgressivo())
                                .and("warn", warnNoBolla.get()));
        if (!ordineDettaglioList.isEmpty()) {
            OrdineDettaglio.persist(ordineDettaglioList);
        }
        GoOrdineDettaglio.persist(goOrdineDettaglioList);
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
                    " and flagNonDisponibile = 'T'", Parameters.with("anno", anno)
                    .and("serie", serie)
                    .and("progressivo", progressivo)) == 0) {
                sendMail(anno, serie, progressivo, email);
                ordine.setStatus(StatoOrdineEnum.COMPLETO.getDescrizione());
            } else {
                ordine.setStatus(StatoOrdineEnum.DA_ORDINARE.getDescrizione());
            }
        }

        if (StatoOrdineEnum.DA_ORDINARE.getDescrizione().equals(result)) {
            ordine.setStatus(StatoOrdineEnum.INCOMPLETO.getDescrizione());
        }

        if (StatoOrdineEnum.INCOMPLETO.getDescrizione().equals(result)) {
            sendMail(anno, serie, progressivo, email);
            ordine.setStatus(StatoOrdineEnum.COMPLETO.getDescrizione());
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
}
