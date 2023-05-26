package it.calolenoci.mapper;

import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.OrdineDettaglio;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class ArticoloMapper {

    public void fromDtoToEntity (OrdineDettaglio o, OrdineDettaglioDto dto) {
        o.setGeTono(dto.getGeTono());
        o.setGeFlagNonDisponibile(dto.getGeFlagNonDisponibile());
        o.setQuantita(dto.getQuantita());
        o.setGeFlagOrdinato(dto.getGeFlagOrdinato());
        o.setGeFlagRiservato(dto.getGeFlagRiservato());
        o.setGeFlagConsegnato(dto.getGeFlagConsegnato());
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
