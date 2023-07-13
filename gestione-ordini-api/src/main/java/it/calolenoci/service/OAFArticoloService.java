package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import it.calolenoci.mapper.GoOrdineFornitoreMapper;
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

    @Inject
    GoOrdineFornitoreMapper goOrdineFornitoreMapper;


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
        OrdineFornitore.update("provvisorio = '' where anno = :anno AND serie = :serie AND progressivo = :progressivo",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo));
        GoOrdineFornitore goOrdineFornitore = goOrdineFornitoreMapper.creaEntity(anno, serie, progressivo);
        goOrdineFornitore.persist();
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
    public boolean save(ArticoloDto dto) {
        try {
            OrdineFornitoreDettaglio.update("rigo = (rigo+1) WHERE anno = :anno AND serie = :serie" +
                            " AND progressivo = :progressivo and rigo >=:rigo",
                    Parameters.with("anno", dto.getAnno()).and("serie", dto.getSerie())
                            .and("progressivo", dto.getProgressivo()).and("rigo", dto.getRigo()));
            mapper.fromDtoToEntity(dto).persist();
            return true;
        } catch (Exception e) {
            Log.error("Errore nella creazione del rigo, " + e.getMessage());
            return false;
        }
    }

    public List<ArticoloDto> cercaArticoli(FiltroArticoli filtro){
        String query = "SELECT a.articolo, a.descrArticolo, a.descrArtSuppl, a.unitaMisura FROM Articolo a " +
                " WHERE 1=1 ";
        Map<String, Object> parameters = new HashMap<>();
        if(StringUtils.isNotBlank(filtro.getCodice())) {
            query += " AND a.articolo LIKE :codice ";
            parameters.put("codice", "%"+filtro.getCodice()+"%");
        }
        if(StringUtils.isNotBlank(filtro.getDescrizione())) {
            query += " AND a.descrArticolo LIKE :descrizione ";
            parameters.put("descrizione", "%"+filtro.getDescrizione()+"%");
        }
        if(StringUtils.isNotBlank(filtro.getDescrSuppl())) {
            query += " AND a.descrArtSuppl LIKE :descrSuppl ";
            parameters.put("descrSuppl", "%"+filtro.getDescrSuppl()+"%");
        }

        List<ArticoloDto> list = Articolo.find(query, parameters).project(ArticoloDto.class).list();
        list.forEach(e -> Magazzino.find("Select valoreUnitario FROM Magazzino " +
                                " WHERE mArticolo = :codArticolo AND valoreUnitario <> null " +
                                " and valoreUnitario <> '' " +
                                " and valoreUnitario <> ' ' " +
                                " ORDER BY dataMagazzino desc ",
                        Parameters.with("codArticolo", e.getArticolo()))
                .project(Double.class)
                .firstResultOptional()
                .ifPresent(e::setPrezzoBase));
        return list;
    }

    @Transactional
    public ResponseDto eliminaArticolo(Integer anno, String serie, Integer progressivo, Integer rigo) {
        ResponseDto dto = new ResponseDto();
        OrdineFornitoreDettaglio articolo = OrdineFornitoreDettaglio.findById(new FornitoreDettaglioId(anno, serie, progressivo, rigo));
        if(articolo == null){
            Log.error("articolo fornitore non trovato al rigo = " + rigo);
            dto.setError(Boolean.TRUE);
            dto.setMsg("articolo fornitore non trovato al rigo = " + rigo);
            return dto;
        }
        Integer progrGeneraleCliente = articolo.getPid();
        if(progrGeneraleCliente != null && progrGeneraleCliente != 0) {
            Optional<OrdineDettaglio> optArticoloCliente = OrdineDettaglio.find("progrGenerale =:pid",
                    Parameters.with("pid", progrGeneraleCliente)).singleResultOptional();
            if(optArticoloCliente.isEmpty()) {
                Log.error("articolo cliente non trovato con progrGenerale = " + progrGeneraleCliente);
                dto.setError(Boolean.TRUE);
                dto.setMsg("articolo cliente non trovato con progrGenerale = " + progrGeneraleCliente);
                return dto;
            }
            OrdineDettaglio o = optArticoloCliente.get();
            GoOrdineDettaglio.update("flagOrdinato = 'F', flagNonDisponibile = 'T' " +
                            "WHERE anno =:anno AND serie =:serie AND progressivo =:progressivo and rigo =:rigo",
                    Parameters.with("anno", o.getAnno()).and("serie", o.getSerie())
                            .and("progressivo", o.getProgressivo()).and("rigo", o.getRigo()));
            GoOrdine.update("status='DA_ORDINARE' WHERE anno =:anno AND serie =:serie AND progressivo =:progressivo and status not in ('DA_ORDINARE','DA_PROCESSARE', 'ARCHIVIATO')",
                    Parameters.with("anno", o.getAnno()).and("serie", o.getSerie()).and("progressivo", o.getProgressivo()));

        }
        OrdineFornitoreDettaglio.deleteById(new FornitoreDettaglioId(anno, serie, progressivo, rigo));
        dto.setError(Boolean.FALSE);
        dto.setMsg("Articolo eliminato");
        return dto;
    }
}
