package it.calolenoci.mapper;

import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.PianoConti;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class OrdineTestataMapper {

    public OrdineDTO fromEntityToDto (Ordine o, PianoConti p) {
        OrdineDTO dto = new OrdineDTO();
        dto.setAnno(o.getAnno());
        dto.setSerie(o.getSerie());
        dto.setProgressivo(o.getProgressivo());
        dto.setDataOrdine(o.getDataOrdine());
        dto.setNumeroConferma(o.getNumeroConferma());
        dto.setIntestazione(p.getIntestazione());
        dto.setContinuaIntest(p.getContinuaIntest());
        return dto;
    }
}
