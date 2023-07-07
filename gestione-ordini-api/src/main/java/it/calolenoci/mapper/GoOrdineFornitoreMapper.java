package it.calolenoci.mapper;

import it.calolenoci.entity.GoOrdine;
import it.calolenoci.entity.GoOrdineFornitore;
import it.calolenoci.entity.Ordine;
import it.calolenoci.enums.StatoOrdineEnum;

import javax.enterprise.context.ApplicationScoped;
import java.util.Date;


@ApplicationScoped
public class GoOrdineFornitoreMapper {

    public GoOrdineFornitore creaEntity(Integer anno, String serie, Integer progressivo) {
        GoOrdineFornitore go = new GoOrdineFornitore();
        go.setAnno(anno);
        go.setSerie(serie);
        go.setProgressivo(progressivo);
        go.setFlInviato(Boolean.FALSE);
        return go;
    }
}
