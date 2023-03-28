package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.entity.OrdineFornitoreDettaglio;
import it.calolenoci.entity.RegistroAzioni;
import it.calolenoci.enums.AzioneEnum;
import it.calolenoci.enums.StatoOrdineEnum;
import it.calolenoci.mapper.ArticoloMapper;
import it.calolenoci.mapper.RegistroAzioniMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class ArticoloService {


    @Inject
    OrdineService ordineService;

    @Inject
    RegistroAzioniMapper registroAzioniMapper;

    @Inject
    ArticoloMapper mapper;

    public List<OrdineDettaglioDto> findById(Integer anno, String serie, Integer progressivo, Boolean filtro) {
        List<OrdineDettaglioDto> list;
        OrdineDTO ordineDTO = ordineService.findById(anno, serie, progressivo);
        if (StatoOrdineEnum.INCOMPLETO.getDescrizione().equals(ordineDTO.getStatus())) {
            list = OrdineDettaglio.findArticoliOrdinatiById(anno, serie, progressivo);
        } else if (StatoOrdineEnum.COMPLETO.getDescrizione().equals(ordineDTO.getStatus())) {
            list = OrdineDettaglio.findArticoliConsegnatiById(anno, serie, progressivo);
        } else {
            list = OrdineDettaglio.findOnlyArticoliById(anno, serie, progressivo, filtro);
        }
        return list;
    }

    public boolean findAnyNoStatus(Integer anno, String serie, Integer progressivo) {
        return OrdineDettaglio.count("anno = :anno AND serie = :serie AND progressivo = :progressivo " +
                        "AND geStatus is null",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)) > 0;
    }

    @Transactional
    public Integer updateFlagConsegnato(List<Integer> param) {
        return OrdineDettaglio.update("geFlagConsegnato = 'T' where geFlagConsegnato = 'F' AND progrGenerale IN (:param)",
                Parameters.with("param", param));
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
    public List<OrdineDettaglioDto> findAndChangeStatusById(Integer anno, String serie, Integer progressivo, StatoOrdineEnum statoOrdineEnum) {
        List<OrdineDettaglioDto> list = findById(anno, serie, progressivo, false);
        changeAllStatus(anno, serie, progressivo, statoOrdineEnum);
        return list;
    }

    @Transactional
    public void save(List<OrdineDettaglioDto> list, String user) {
        save(list, user, false);
    }

    @Transactional
    public String save(List<OrdineDettaglioDto> list, String user, Boolean chiudi) {
        List<RegistroAzioni> registroAzioniList = new ArrayList<>();
        List<OrdineDettaglio> ordineDettaglioList = new ArrayList<>();
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
            mapper.fromDtoToEntity(ordineDettaglio, dto);
            ordineDettaglioList.add(ordineDettaglio);
        });
        OrdineDettaglio.persist(ordineDettaglioList);
        RegistroAzioni.persist(registroAzioniList);
        if (chiudi) {
            OrdineDettaglio ordineDettaglio = ordineDettaglioList.get(0);
            String stato = chiudi(ordineDettaglio.getAnno(), ordineDettaglio.getSerie(), ordineDettaglio.getProgressivo());
            OrdineDettaglio.updateStatus(ordineDettaglio.getAnno(), ordineDettaglio.getSerie(), ordineDettaglio.getProgressivo(), stato);
            return stato;
        }
        return null;
    }

    private String chiudi(Integer anno, String serie, Integer progressivo) {
        Ordine ordine = Ordine.findByOrdineId(anno, serie, progressivo);
        final String result = ordine.getGeStatus();
        List<OrdineDettaglioDto> ordineDettaglioDtoList = findById(anno, serie, progressivo, true);
        if (StatoOrdineEnum.DA_PROCESSARE.getDescrizione().equals(result)) {
            if (ordineDettaglioDtoList.isEmpty()) {
                ordine.setGeStatus(StatoOrdineEnum.COMPLETO.getDescrizione());
            } else {
                ordine.setGeStatus(StatoOrdineEnum.DA_ORDINARE.getDescrizione());
            }
        }

        if (StatoOrdineEnum.DA_ORDINARE.getDescrizione().equals(result)) {
            ordine.setGeStatus(StatoOrdineEnum.INCOMPLETO.getDescrizione());
        }

        if (StatoOrdineEnum.INCOMPLETO.getDescrizione().equals(result)) {
            ordine.setGeStatus(StatoOrdineEnum.COMPLETO.getDescrizione());
        }
        ordine.persist();
        return ordine.getGeStatus();
    }

}
