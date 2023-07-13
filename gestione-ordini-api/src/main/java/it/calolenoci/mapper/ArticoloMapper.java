package it.calolenoci.mapper;

import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.GoOrdineDettaglio;
import it.calolenoci.entity.OrdineDettaglio;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class ArticoloMapper {

    public void fromDtoToEntity (GoOrdineDettaglio o, OrdineDettaglioDto dto) {
        o.setFlagNonDisponibile(dto.getFlagNonDisponibile());
        o.setFlagOrdinato(dto.getFlagOrdinato());
        o.setFlagRiservato(dto.getFlagRiservato());
        o.setFlagConsegnato(dto.getFlagConsegnato());
        o.setQtaDaConsegnare(dto.getQtaDaConsegnare());
        o.setFlProntoConsegna(dto.getFlProntoConsegna());
        if(dto.getQtaConsegnatoSenzaBolla() != null) {
            o.setQtaConsegnatoSenzaBolla(dto.getQtaConsegnatoSenzaBolla());
        }
        if(dto.getQtaProntoConsegna() != null) {
            o.setQtaProntoConsegna(dto.getQtaProntoConsegna());
        }
        if(dto.getQtaRiservata() != null){
            o.setQtaRiservata(dto.getQtaRiservata());
        }
        if(StringUtils.isNotBlank(dto.getNote())){
            o.setNote(dto.getNote());
        }
    }
}
