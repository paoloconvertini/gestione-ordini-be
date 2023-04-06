package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.OrdineFornitoreDettaglioDto;
import it.calolenoci.entity.*;
import it.calolenoci.enums.AzioneEnum;
import it.calolenoci.enums.StatoOrdineEnum;
import it.calolenoci.mapper.ArticoloMapper;
import it.calolenoci.mapper.OafArticoloMapper;
import it.calolenoci.mapper.RegistroAzioniMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class OAFArticoloService {

    @Inject
    OafArticoloMapper mapper;

    public List<OrdineFornitoreDettaglioDto> findById(Integer anno, String serie, Integer progressivo) {
        return OrdineFornitoreDettaglio.find("select o.anno, o.serie, o.progressivo, o.rigo, o.nota, o.oArticolo, " +
                " o.oDescrArticolo,  o.oQuantita, o.oPrezzo, o.oUnitaMisura" +
                " FROM OrdineFornitoreDettaglio o " +
                " WHERE anno = :anno AND serie = :serie AND progressivo = :progressivo ",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo))
                .project(OrdineFornitoreDettaglioDto.class).list();
    }

    public boolean findAnyNoStatus(Integer anno, String serie, Integer progressivo) {
        return OrdineDettaglio.count("anno = :anno AND serie = :serie AND progressivo = :progressivo " +
                        "AND geStatus is null",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)) > 0;
    }

    @Transactional
    public void approva(Integer anno, String serie, Integer progressivo) {
        OrdineFornitore.update("provvisorio = 'F' where anno = :anno AND serie = :serie AND progressivo = :progressivo",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo));
    }

    @Transactional
    public void richiediApprovazione(Integer anno, String serie, Integer progressivo) {
        OrdineFornitore.update("provvisorio = 'T' where anno = :anno AND serie = :serie AND progressivo = :progressivo",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo));
    }

    @Transactional
    public void save(List<OrdineDettaglioDto> list) {
        save(list, false);
    }

    @Transactional
    public String save(List<OrdineDettaglioDto> list, Boolean chiudi) {
        List<RegistroAzioni> registroAzioniList = new ArrayList<>();
        List<OrdineDettaglio> ordineDettaglioList = new ArrayList<>();
        list.forEach(e -> {
            ordineDettaglioList.add(mapper.viewToEntity(e));
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
        List<OrdineFornitoreDettaglioDto> ordineDettaglioDtoList = findById(anno, serie, progressivo);
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
