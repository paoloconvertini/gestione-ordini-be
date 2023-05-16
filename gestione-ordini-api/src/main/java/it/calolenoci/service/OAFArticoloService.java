package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.OrdineFornitoreDettaglioDto;
import it.calolenoci.dto.OrdineFornitoreDto;
import it.calolenoci.dto.ResponseOAFDettaglioDTO;
import it.calolenoci.entity.*;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OAFArticoloService {


    public ResponseOAFDettaglioDTO findById(Integer anno, String serie, Integer progressivo) {
        ResponseOAFDettaglioDTO responseOAFDettaglioDTO = new ResponseOAFDettaglioDTO();
        Optional<OrdineFornitoreDto> optional = OrdineFornitore.find("SELECT f.anno, f.serie, f.progressivo, f.gruppo, f.conto, pc.intestazione " +
                "FROM OrdineFornitore f " +
                "JOIN PianoConti pc ON f.gruppo = pc.gruppoConto AND f.conto = pc.sottoConto  " +
                "WHERE f.anno = :anno AND f.serie = :serie AND f.progressivo = :progressivo",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo))
        .project(OrdineFornitoreDto.class).singleResultOptional();
        List<OrdineFornitoreDettaglioDto> list = OrdineFornitoreDettaglio.find("select o.anno, o.serie, o.progressivo, o.rigo, o.nota, o.oArticolo, " +
                " o.oDescrArticolo,  o.oQuantita, o.oPrezzo, o.oUnitaMisura" +
                " FROM OrdineFornitoreDettaglio o " +
                " WHERE anno = :anno AND serie = :serie AND progressivo = :progressivo ",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo))
                .project(OrdineFornitoreDettaglioDto.class).list();
        Double aDouble = OrdineFornitoreDettaglio.find("SELECT SUM(o.oPrezzo*o.oQuantita) FROM OrdineFornitoreDettaglio o " +
                                "WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo"
                        , Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo))
                .project(Double.class).singleResult();
        if(optional.isPresent()){
            responseOAFDettaglioDTO.setIntestazione(optional.get().getIntestazione());
            responseOAFDettaglioDTO.setSottoConto(optional.get().getConto());
        }
        responseOAFDettaglioDTO.setTotale(aDouble);
        responseOAFDettaglioDTO.setArticoli(list);
        return responseOAFDettaglioDTO;
    }

    @Transactional
    public void approva(Integer anno, String serie, Integer progressivo) {
        OrdineFornitore.update("provvisorio = 'F' where anno = :anno AND serie = :serie AND progressivo = :progressivo",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo));
    }

}
