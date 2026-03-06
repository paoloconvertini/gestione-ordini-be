package it.calolenoci.mapper;

import it.calolenoci.entity.GoOrdineFornitore;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.LocalDateTime;


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
