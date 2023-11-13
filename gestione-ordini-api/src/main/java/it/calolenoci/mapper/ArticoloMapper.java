package it.calolenoci.mapper;

import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.GoOrdineDettaglio;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class ArticoloMapper {

    public void fromDtoToEntity (GoOrdineDettaglio o, OrdineDettaglioDto dto) {
        if(dto.getFlagNonDisponibile() == null) {
            o.setFlagNonDisponibile(Boolean.FALSE);
        } else {
            o.setFlagNonDisponibile(dto.getFlagNonDisponibile());
        }
        if(dto.getFlagOrdinato() == null) {
            o.setFlagOrdinato(Boolean.FALSE);
        } else {
            o.setFlagOrdinato(dto.getFlagOrdinato());
        }
        if(dto.getFlagRiservato() == null) {
            o.setFlagRiservato(Boolean.FALSE);
        } else {
            o.setFlagRiservato(dto.getFlagRiservato());
        }
        if(dto.getFlagConsegnato() == null) {
            o.setFlagConsegnato(Boolean.FALSE);
        } else {
            o.setFlagConsegnato(dto.getFlagConsegnato());
        }
        if(dto.getFlProntoConsegna() == null) {
            o.setFlProntoConsegna(Boolean.FALSE);
        } else {
            o.setFlProntoConsegna(dto.getFlProntoConsegna());
        }
        o.setQtaDaConsegnare(dto.getQtaDaConsegnare());
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
       // o.setFArticolo(dto.getFArticolo());
    }
}
