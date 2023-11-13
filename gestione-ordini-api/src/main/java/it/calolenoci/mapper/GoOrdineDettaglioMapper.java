package it.calolenoci.mapper;

import it.calolenoci.entity.GoOrdineDettaglio;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.enums.StatoOrdineEnum;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class GoOrdineDettaglioMapper {

    public GoOrdineDettaglio fromOrdineDettaglioToGoOrdineDettaglio (OrdineDettaglio o) {
        GoOrdineDettaglio go = new GoOrdineDettaglio();
        go.setAnno(o.getAnno());
        go.setSerie(o.getSerie());
        go.setProgressivo(o.getProgressivo());
        go.setRigo(o.getRigo());
        go.setStatus(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
        go.setFlagConsegnato(Boolean.FALSE);
        go.setFlagNonDisponibile(Boolean.FALSE);
        go.setFlagOrdinato(Boolean.FALSE);
        go.setFlagRiservato(Boolean.FALSE);
        go.setFlProntoConsegna(Boolean.FALSE);
        go.setFlBolla(Boolean.FALSE);
        go.setQtaDaConsegnare(o.getQuantita());
        go.setProgrGenerale(o.getProgrGenerale());
       // go.setFArticolo(o.getFArticolo());
        return go;
    }
}
