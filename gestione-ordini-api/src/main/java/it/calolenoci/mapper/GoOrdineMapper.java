package it.calolenoci.mapper;

import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.GoOrdine;
import it.calolenoci.entity.GoOrdineDettaglio;
import it.calolenoci.entity.Ordine;
import it.calolenoci.enums.StatoOrdineEnum;
import jdk.jshell.Snippet;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class GoOrdineMapper {

    public GoOrdine fromOrdineToGoOrdine (Ordine o) {
        GoOrdine go = new GoOrdine();
        go.setAnno(o.getAnno());
        go.setSerie(o.getSerie());
        go.setProgressivo(o.getProgressivo());
        go.setStatus(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
        go.setLocked(Boolean.FALSE);
        go.setHasFirma(Boolean.FALSE);
        go.setWarnNoBolla(Boolean.FALSE);
        go.setHasCarico(Boolean.FALSE);
        return go;
    }
}
