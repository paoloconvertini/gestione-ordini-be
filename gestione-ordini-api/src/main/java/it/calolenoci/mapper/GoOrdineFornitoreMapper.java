package it.calolenoci.mapper;

import it.calolenoci.entity.GoOrdineFornitore;

import javax.enterprise.context.ApplicationScoped;


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
