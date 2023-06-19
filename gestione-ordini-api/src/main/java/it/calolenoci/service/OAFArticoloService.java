package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.OrdineFornitoreDettaglioDto;
import it.calolenoci.dto.OrdineFornitoreDto;
import it.calolenoci.dto.ResponseOAFDettaglioDTO;
import it.calolenoci.entity.*;
import it.calolenoci.mapper.OafArticoloMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OAFArticoloService {

    @Inject
    OafArticoloMapper mapper;


    public ResponseOAFDettaglioDTO findById(Integer anno, String serie, Integer progressivo) {
        ResponseOAFDettaglioDTO responseOAFDettaglioDTO = new ResponseOAFDettaglioDTO();
        Optional<OrdineFornitoreDto> optional = OrdineFornitore.find("SELECT f.anno, f.serie, f.progressivo, f.gruppo, f.conto, pc.intestazione " +
                "FROM OrdineFornitore f " +
                "JOIN PianoConti pc ON f.gruppo = pc.gruppoConto AND f.conto = pc.sottoConto  " +
                "WHERE f.anno = :anno AND f.serie = :serie AND f.progressivo = :progressivo",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo))
        .project(OrdineFornitoreDto.class).singleResultOptional();
        List<OrdineFornitoreDettaglioDto> list = OrdineFornitoreDettaglio.find("select o.anno, o.serie, o.progressivo, o.rigo, o.nota, o.oArticolo, " +
                " o.oDescrArticolo,  o.oQuantita, o.oPrezzo, o.oUnitaMisura, o.scontoF1, o.scontoF2, o.fScontoP, o.tipoRigo, oc.noteOrdCli" +
                " FROM OrdineFornitoreDettaglio o " +
                " INNER JOIN OrdineDettaglio oc ON o.nota like CONCAT('Riferimento n. ', trim(str(oc.anno)), '/', oc.serie, '/', trim(str(oc.progressivo)), '-', trim(str(oc.rigo)))" +
                " WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo ORDER BY o.rigo",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo))
                .project(OrdineFornitoreDettaglioDto.class).list();
        if(optional.isPresent()){
            responseOAFDettaglioDTO.setIntestazione(optional.get().getIntestazione());
            responseOAFDettaglioDTO.setSottoConto(optional.get().getConto());
        }
        responseOAFDettaglioDTO.setArticoli(list);
        return responseOAFDettaglioDTO;
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
    public void save(List<OrdineFornitoreDettaglioDto> list) {
        list.forEach(o -> {
            OrdineFornitoreDettaglio entity = OrdineFornitoreDettaglio.findById(new FornitoreDettaglioId(o.getAnno(), o.getSerie(), o.getProgressivo(), o.getRigo()));
            mapper.viewToEntity(entity, o);
            entity.persist();
        });
    }

    @Transactional
    public void save(Integer anno, String serie, Integer progressivo, OrdineFornitoreDettaglioDto dto) {
        OrdineFornitoreDettaglio ordineFornitoreDettaglio = mapper.viewToEntity(anno, serie, progressivo, dto);
        ordineFornitoreDettaglio.persist();
    }
}
