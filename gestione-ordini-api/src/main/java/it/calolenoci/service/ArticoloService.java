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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class ArticoloService {


    @Inject
    EventoService eventoService;

    @Inject
    RegistroAzioniMapper registroAzioniMapper;

    @Inject
    ArticoloMapper mapper;

    public List<OrdineDettaglioDto> findById(Integer anno, String serie, Integer progressivo) {
        List<OrdineDettaglioDto> list = new ArrayList<>();
        List<OrdineDettaglio> articoli = OrdineDettaglio.find("anno = :anno AND serie = :serie AND progressivo = :progressivo", Sort.ascending("rigo"),
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
    public void changeAllStatus(Integer anno, String serie, Integer progressivo, StatoOrdineEnum statoOrdineEnum){
        OrdineDettaglio.updateStatus(anno, serie, progressivo, statoOrdineEnum.getDesczrizione());
    }

    @Transactional
    public List<OrdineDettaglioDto> findAndChangeStatusById(Integer anno, String serie, Integer progressivo, StatoOrdineEnum statoOrdineEnum) {
        List<OrdineDettaglioDto> list = findById(anno, serie, progressivo);
        changeAllStatus(anno, serie, progressivo, statoOrdineEnum);
        return list;
    }

    @Transactional
    public void save(List<OrdineDettaglioDto> list) {
        List<RegistroAzioni> registroAzioniList = new ArrayList<>();
        List<OrdineDettaglio> ordineDettaglioList = new ArrayList<>();
        list.forEach(dto -> {

            OrdineDettaglio ordineDettaglio = OrdineDettaglio.getById(dto.getAnno(), dto.getSerie(), dto.getProgressivo(), dto.getRigo());
            if(!Objects.equals(ordineDettaglio.getQuantita(), dto.getQuantita())) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), dto.getUsername(), AzioneEnum.QUANTITA.getDesczrizione(), dto.getRigo()));
            }
            if(!Objects.equals(ordineDettaglio.getGeTono(), dto.getGeTono())) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), dto.getUsername(), AzioneEnum.TONO.getDesczrizione(), dto.getRigo()));
            }
            if(dto.getGeFlagRiservato()) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), dto.getUsername(), AzioneEnum.RISERVATO.getDesczrizione(), dto.getRigo()));
            }
            if(dto.getGeFlagOrdinato()){
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), dto.getUsername(), AzioneEnum.ORDINATO.getDesczrizione(), dto.getRigo()));
            }
            if(dto.getGeFlagNonDisponibile()) {
                registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), dto.getUsername(), AzioneEnum.NON_DISPONIBILE.getDesczrizione(), dto.getRigo()));
            }

            mapper.fromDtoToEntity(ordineDettaglio, dto);
            ordineDettaglioList.add(ordineDettaglio);
        });
        OrdineDettaglio.persist(ordineDettaglioList);
        RegistroAzioni.persist(registroAzioniList);
    }
}
