package it.calolenoci.mapper;

import it.calolenoci.dto.OrdineClienteTestataDto;
import it.calolenoci.entity.OrdineClienteTestata;
import it.calolenoci.entity.PianoConti;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class OrdineTestataMapper {

    public OrdineClienteTestataDto fromEntityToDto (OrdineClienteTestata o, PianoConti p) {
        OrdineClienteTestataDto dto = new OrdineClienteTestataDto();
        dto.setAnno(o.getOrdineId().getAnno());
        dto.setSerie(o.getOrdineId().getSerie());
        dto.setProgressivo(o.getOrdineId().getProgressivo());
        dto.setDataOrdine(o.getDataOrdine());
        dto.setNumeroConferma(o.getNumeroConferma());
        dto.setIntestazione(p.getIntestazione());
        dto.setContinuaIntest(p.getContinuaIntest());
        return dto;
    }
}
