package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
        ResponseOrdineDettaglio response = new ResponseOrdineDettaglio();
        List<OrdineDettaglioDto> list;
        OrdineDTO ordineDTO = ordineService.findById(filtro.getAnno(), filtro.getSerie(), filtro.getProgressivo());
        if (StringUtils.isBlank(ordineDTO.getStatus()) ||
                StatoOrdineEnum.DA_PROCESSARE.getDescrizione().equals(ordineDTO.getStatus()) ||
                StatoOrdineEnum.DA_ORDINARE.getDescrizione().equals(ordineDTO.getStatus())
        ) {
            filtro.setFlDaConsegnare(null);
        }
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
        response.setArticoli(list);
        return response;
    }

    public boolean findAnyNoStatus(Integer anno, String serie, Integer progressivo) {
        return OrdineDettaglio.count("anno = :anno AND serie = :serie AND progressivo = :progressivo " +
                        "AND geStatus is null",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)) > 0;
    }

    @Transactional
    public Integer updateArticoliBolle(List<FatturaDto> list) {
        AtomicReference<Integer> aggiornati = new AtomicReference<>(0);
        list.forEach(e -> {
                    aggiornati.updateAndGet(v -> v + OrdineDettaglio.update("qtaConsegnatoSenzaBolla = CASE WHEN (quantita - :qta) = 0 THEN NULL ELSE qtaConsegnatoSenzaBolla END, geFlagConsegnato = CASE WHEN (quantita - :qta) = 0 THEN 'T' ELSE 'F' END, qtaDaConsegnare = (quantita - :qta), " +
                            "flBolla = 'T' WHERE progrGenerale = :progrGenerale and geFlagConsegnato <> 'T'", Parameters.with("qta", e.getQta())
                            .and("progrGenerale", e.getProgrOrdCli())));

                    OrdineDettaglio ordineDettaglio = OrdineDettaglio.find("progrGenerale = :progrGenerale",
                            Parameters.with("progrGenerale", e.getProgrOrdCli())).singleResult();
                    if (ordineDettaglio.getQtaConsegnatoSenzaBolla() == null || ordineDettaglio.getQtaConsegnatoSenzaBolla() == 0) {
                        Ordine.update("geWarnNoBolla = 'F' where anno =:anno and serie =:serie and progressivo = :progressivo",
                                Parameters.with("anno", ordineDettaglio.getAnno())
                                        .and("serie", ordineDettaglio.getSerie())
                                        .and("progressivo", ordineDettaglio.getProgressivo()));
                    }
                }
        );
        return aggiornati.get();
    }

    public boolean findNoConsegnati(Integer anno, String serie, Integer progressivo) {
        return OrdineDettaglio.count("anno = :anno AND serie = :serie AND progressivo = :progressivo " +
                        "AND geFlagConsegnato = 'F'",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)) == 0;
    }

    @Transactional
    public void changeAllStatus(Integer anno, String serie, Integer progressivo, StatoOrdineEnum statoOrdineEnum) {
        OrdineDettaglio.updateStatus(anno, serie, progressivo, statoOrdineEnum.getDescrizione());
    }

    @Transactional
    public void save(List<OrdineDettaglioDto> list, String user) {
        save(list, user, false);
    }

    @Transactional
    public String save(List<OrdineDettaglioDto> list, String user, Boolean chiudi) {
        List<RegistroAzioni> registroAzioniList = new ArrayList<>();
        List<OrdineDettaglio> ordineDettaglioList = new ArrayList<>();
        AtomicBoolean warnNoBolla = new AtomicBoolean(false);
        list.forEach(dto -> {
            OrdineDettaglio ordineDettaglio = OrdineDettaglio.getById(dto.getAnno(), dto.getSerie(), dto.getProgressivo(), dto.getRigo());
            if (!Objects.equals(ordineDettaglio.getQuantita(), dto.getQuantita())) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), user, AzioneEnum.QUANTITA.getDesczrizione(),
                        dto.getRigo(), null, dto.getQuantita()));
            }
            if (!Objects.equals(ordineDettaglio.getGeTono(), dto.getGeTono())) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), user, AzioneEnum.TONO.getDesczrizione(),
                        dto.getRigo(), dto.getGeTono(), null));
            }
            if (!Objects.equals(dto.getGeFlagRiservato(), ordineDettaglio.getGeFlagRiservato())) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), user, AzioneEnum.RISERVATO.getDesczrizione()
                        , dto.getRigo(), null, null));
            }
            if (!Objects.equals(dto.getGeFlagOrdinato(), ordineDettaglio.getGeFlagOrdinato())) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), user, AzioneEnum.ORDINATO.getDesczrizione()
                        , dto.getRigo(), null, null));
            }
            if (!Objects.equals(dto.getGeFlagNonDisponibile(), ordineDettaglio.getGeFlagNonDisponibile())) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), user, AzioneEnum.NON_DISPONIBILE.getDesczrizione()
                        , dto.getRigo(), null, null));
            }
            if (!Objects.equals(dto.getGeFlagConsegnato(), ordineDettaglio.getGeFlagConsegnato())) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), user, AzioneEnum.CONSEGNATO.getDesczrizione()
                        , dto.getRigo(), null, null));
            }
            warnNoBolla.set(dto.getQtaConsegnatoSenzaBolla() != null && dto.getQtaConsegnatoSenzaBolla() != 0);
            mapper.fromDtoToEntity(ordineDettaglio, dto);
            ordineDettaglioList.add(ordineDettaglio);
        });
        OrdineDettaglio ordineDettaglio = ordineDettaglioList.get(0);
        Ordine.update("geWarnNoBolla =:warn where anno =:anno and serie =:serie and progressivo = :progressivo",
                Parameters.with("anno", ordineDettaglio.getAnno())
                        .and("serie", ordineDettaglio.getSerie())
                        .and("progressivo", ordineDettaglio.getProgressivo())
                        .and("warn", warnNoBolla.get()));
        OrdineDettaglio.persist(ordineDettaglioList);
        RegistroAzioni.persist(registroAzioniList);
        if (chiudi) {
            Ordine.update("geLocked = 'F', geUserLock = null where anno =:anno and serie =:serie and progressivo = :progressivo",
                    Parameters.with("anno", ordineDettaglio.getAnno())
                            .and("serie", ordineDettaglio.getSerie())
                            .and("progressivo", ordineDettaglio.getProgressivo()));
            String stato = chiudi(ordineDettaglio.getAnno(), ordineDettaglio.getSerie(), ordineDettaglio.getProgressivo());
            OrdineDettaglio.updateStatus(ordineDettaglio.getAnno(), ordineDettaglio.getSerie(), ordineDettaglio.getProgressivo(), stato);
            return stato;
        }
        return null;
    }

    private String chiudi(Integer anno, String serie, Integer progressivo) {
        Ordine ordine = Ordine.findByOrdineId(anno, serie, progressivo);
        final String result = ordine.getGeStatus();
        if (StatoOrdineEnum.DA_PROCESSARE.getDescrizione().equals(result)) {
            if (OrdineDettaglio.count("anno = :anno and serie =:serie" +
                    " and progressivo =:progressivo" +
                    " and geFlagNonDisponibile = 'T'", Parameters.with("anno", anno)
                    .and("serie", serie)
                    .and("progressivo", progressivo)) == 0) {
                sendMail(anno, serie, progressivo);
                ordine.setGeStatus(StatoOrdineEnum.COMPLETO.getDescrizione());
            } else {
                ordine.setGeStatus(StatoOrdineEnum.DA_ORDINARE.getDescrizione());
            }
        }

        if (StatoOrdineEnum.DA_ORDINARE.getDescrizione().equals(result)) {
            ordine.setGeStatus(StatoOrdineEnum.INCOMPLETO.getDescrizione());
        }

        if (StatoOrdineEnum.INCOMPLETO.getDescrizione().equals(result)) {
            sendMail(anno, serie, progressivo);
            ordine.setGeStatus(StatoOrdineEnum.COMPLETO.getDescrizione());
        }
        ordine.persist();
        return ordine.getGeStatus();
    }

    private void sendMail(Integer anno, String serie, Integer progressivo) {
        File f = new File(pathReport + "/ordine_" + anno + "_" + serie + "_" + progressivo + ".pdf");
        OrdineDTO dto = Ordine.find("SELECT p.intestazione, p.localita, p.provincia, p.telefono, p.email " +
                                " FROM Ordine o " +
                                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto " +
                                "WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo",
                        Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo))
                .project(OrdineDTO.class).firstResult();
        dto.setAnno(anno);
        dto.setSerie(serie);
        dto.setProgressivo(progressivo);
        mailService.sendMailOrdineCompleto(f, dto);
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
