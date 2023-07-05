package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import it.calolenoci.mapper.OafArticoloMapper;
import org.apache.commons.lang3.StringUtils;
import org.jfree.util.Log;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;

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
                " o.oDescrArticolo,  o.oQuantita, o.oPrezzo, o.oUnitaMisura, o.fScontoArticolo, o.scontoF1, o.scontoF2, o.fScontoP, o.tipoRigo" +
                " FROM OrdineFornitoreDettaglio o " +
                //" LEFT JOIN OrdineDettaglio oc ON o.nota like CONCAT('Riferimento n. ', trim(str(oc.anno)), '/', oc.serie, '/', trim(str(oc.progressivo)), '-', trim(str(oc.rigo)))" +
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
    public boolean save(Integer anno, String serie, Integer progressivo, OrdineFornitoreDettaglioDto dto) {
        try {
            List<OrdineFornitoreDettaglio> list = OrdineFornitoreDettaglio.find("anno = :anno AND serie = :serie" +
                            " AND progressivo = :progressivo and rigo >=:rigo",
                    Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo).and("rigo", dto.getRigo())).list();

            OrdineFornitoreDettaglio rigoDaAggiornare = OrdineFornitoreDettaglio.find("anno = :anno AND serie = :serie" +
                            " AND progressivo = :progressivo and rigo =:rigo",
                    Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo).and("rigo", dto.getRigo())).singleResult();

            mapper.aggiornaRigo(rigoDaAggiornare, dto);

            list.forEach(e-> e.setRigo(e.getRigo()+1));
            list.add(rigoDaAggiornare);
            OrdineFornitoreDettaglio.persist(list);
            return true;
        } catch (Exception e) {
            Log.error("Errore nella creazione del rigo, " + e.getMessage());
            return false;
        }
    }

    public List<ArticoloDto> cercaArticoli(FiltroArticoli filtro){
        String query = "SELECT a.articolo, a.descrArticolo, a.unitaMisura FROM Articolo a " +
                " WHERE 1=1 ";
        Map<String, String> parameters = new HashMap<>();
        if(StringUtils.isNotBlank(filtro.getCodice())) {
            query += " AND a.articolo LIKE '%codice%' ";
            parameters.put("codice", filtro.getCodice());
        }
        if(StringUtils.isNotBlank(filtro.getDescrizione())) {
            query += " AND a.descrArticolo LIKE '%descrizione%' ";
            parameters.put("descrizione", filtro.getDescrizione());
        }
        List<ArticoloDto> list = Articolo.find(query, parameters).project(ArticoloDto.class).list();
        list.forEach(e -> Magazzino.find("Select valoreUnitario FROM Magazzino " +
                                "WHERE mArticolo = :codArticolo ORDER BY dataMagazzino desc",
                        Parameters.with("codArticolo", e.getArticolo()))
                .project(Double.class)
                .firstResultOptional()
                .ifPresent(e::setPrezzoBase));
        return list;
    }
}
