package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.entity.RegistroAzioni;
import it.calolenoci.enums.AzioneEnum;
import it.calolenoci.enums.StatoOrdineEnum;
import it.calolenoci.mapper.ArticoloMapper;
import it.calolenoci.mapper.RegistroAzioniMapper;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.*;

@ApplicationScoped
public class ArticoloService {


    @Inject
    OrdineService ordineService;

    @Inject
    RegistroAzioniMapper registroAzioniMapper;

    @Inject
    ArticoloMapper mapper;

    public List<OrdineDettaglioDto> findById(Integer anno, String serie, Integer progressivo, Boolean filtro) {
        List<OrdineDettaglioDto> list = new ArrayList<>();
        String query = "anno = :anno AND serie = :serie AND progressivo = :progressivo";
        if(filtro) {
            query += " AND geFlagNonDisponibile = '1'";
        }
        List<OrdineDettaglio> articoli = OrdineDettaglio.find(query, Sort.ascending("rigo"),
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)).list();
        articoli.forEach(a -> list.add(mapper.fromEntityToDto(a)));
        return list;
    }

    public boolean findAnyNoStatus(Integer anno, String serie, Integer progressivo) {
        return OrdineDettaglio.count("anno = :anno AND serie = :serie AND progressivo = :progressivo " +
                        "AND geStatus is null",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)) > 0;
    }

    @Transactional
    public void changeAllStatus(Integer anno, String serie, Integer progressivo, StatoOrdineEnum statoOrdineEnum) {
        OrdineDettaglio.updateStatus(anno, serie, progressivo, statoOrdineEnum.getDesczrizione());
    }

    @Transactional
    public List<OrdineDettaglioDto> findAndChangeStatusById(Integer anno, String serie, Integer progressivo, StatoOrdineEnum statoOrdineEnum) {
        List<OrdineDettaglioDto> list = findById(anno, serie, progressivo, false);
        changeAllStatus(anno, serie, progressivo, statoOrdineEnum);
        return list;
    }

    @Transactional
    public void save(List<OrdineDettaglioDto> list) {
        List<RegistroAzioni> registroAzioniList = new ArrayList<>();
        List<OrdineDettaglio> ordineDettaglioList = new ArrayList<>();
        list.forEach(dto -> {
            OrdineDettaglio ordineDettaglio = OrdineDettaglio.getById(dto.getAnno(), dto.getSerie(), dto.getProgressivo(), dto.getRigo());
            if (!Objects.equals(ordineDettaglio.getQuantita(), dto.getQuantita())) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), dto.getUsername(), AzioneEnum.QUANTITA.getDesczrizione(),
                        dto.getRigo(), null, dto.getQuantita()));
            }
            if (!Objects.equals(ordineDettaglio.getGeTono(), dto.getGeTono())) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), dto.getUsername(), AzioneEnum.TONO.getDesczrizione(),
                        dto.getRigo(), dto.getGeTono(), null));
            }
            if (!Objects.equals(dto.getGeFlagRiservato(), convertiFlag(ordineDettaglio.getGeFlagRiservato()))) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), dto.getUsername(), AzioneEnum.RISERVATO.getDesczrizione()
                        , dto.getRigo(), null, null));
            }
            if (!Objects.equals(dto.getGeFlagOrdinato(), convertiFlag(ordineDettaglio.getGeFlagOrdinato()))) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), dto.getUsername(), AzioneEnum.ORDINATO.getDesczrizione()
                        , dto.getRigo(), null, null));
            }
            if (!Objects.equals(dto.getGeFlagNonDisponibile(), convertiFlag(ordineDettaglio.getGeFlagNonDisponibile()))) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), dto.getUsername(), AzioneEnum.NON_DISPONIBILE.getDesczrizione()
                        , dto.getRigo(), null, null));
            }
            if (!Objects.equals(dto.getGeFlagConsegnato(), convertiFlag(ordineDettaglio.getGeFlagConsegnato()))) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), dto.getUsername(), AzioneEnum.CONSEGNATO.getDesczrizione()
                        , dto.getRigo(), null, null));
            }
            mapper.fromDtoToEntity(ordineDettaglio, dto);
            ordineDettaglioList.add(ordineDettaglio);
        });
        OrdineDettaglio.persist(ordineDettaglioList);
        RegistroAzioni.persist(registroAzioniList);
    }

    @Transactional
    public String chiudi(Integer anno, String serie, Integer progressivo) {
        OrdineDTO ordineDTO = ordineService.findById(anno, serie, progressivo);
        String result = ordineDTO.getStatus();
        List<OrdineDettaglioDto> ordineDettaglioDtoList = findById(anno, serie, progressivo, true);
        if(StatoOrdineEnum.DA_PROCESSARE.getDesczrizione().equals(result)) {
            if(ordineDettaglioDtoList.isEmpty()) {
                ordineService.changeStatus(anno, serie, progressivo, StatoOrdineEnum.INCOMPLETO);
                result = StatoOrdineEnum.INCOMPLETO.getDesczrizione();
            } else {
                ordineService.changeStatus(anno, serie, progressivo, StatoOrdineEnum.DA_ORDINARE);
                result = StatoOrdineEnum.DA_ORDINARE.getDesczrizione();
            }
        }

        if(StatoOrdineEnum.DA_ORDINARE.getDesczrizione().equals(result)){
            ordineService.changeStatus(anno, serie, progressivo, StatoOrdineEnum.INCOMPLETO);
            result = StatoOrdineEnum.INCOMPLETO.getDesczrizione();
        }
        return result;
    }

    private Boolean convertiFlag(Character flag) {
        return flag == '1';
    }
}
