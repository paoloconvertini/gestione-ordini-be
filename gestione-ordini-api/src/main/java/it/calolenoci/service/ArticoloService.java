package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
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
        boolean[] cambioStato = {false, false, false, false};
        list.forEach(dto -> {
            if(!dto.getGeFlagRiservato()) {
                cambioStato[0] = true;
            }
            if(!dto.getGeFlagRiservato() && !dto.getGeFlagNonDisponibile()) {
                cambioStato[1] = true;
            }
            if(!dto.getGeFlagOrdinato()) {
                cambioStato[2] = true;
            }
            if(!dto.getGeFlagConsegnato()) {
                cambioStato[3] = true;
            }
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
        OrdineDettaglio o = ordineDettaglioList.get(0);
        if(!cambioStato[0] || !cambioStato[2]) {
            //ordineDettaglioList.forEach(ordine -> ordine.setGeStatus(StatoOrdineEnum.INCOMPLETO.getDesczrizione()));
            ordineService.changeStatus(o.getAnno(), o.getSerie(), o.getProgressivo(), StatoOrdineEnum.INCOMPLETO);
        }
        if(!cambioStato[1]) {
            ordineService.changeStatus(o.getAnno(), o.getSerie(), o.getProgressivo(), StatoOrdineEnum.DA_ORDINARE);
            //ordineDettaglioList.forEach(ordine -> ordine.setGeStatus(StatoOrdineEnum.DA_ORDINARE.getDesczrizione()));
        }
        if(!cambioStato[3]) {
            ordineService.changeStatus(o.getAnno(), o.getSerie(), o.getProgressivo(), StatoOrdineEnum.COMPLETO);
            //ordineDettaglioList.forEach(ordine -> ordine.setGeStatus(StatoOrdineEnum.COMPLETO.getDesczrizione()));
        }
        OrdineDettaglio.persist(ordineDettaglioList);
        RegistroAzioni.persist(registroAzioniList);
    }

    private Boolean convertiFlag(Character flag) {
        return flag == '1';
    }
}
