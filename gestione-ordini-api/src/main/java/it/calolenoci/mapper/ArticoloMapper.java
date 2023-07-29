package it.calolenoci.mapper;

import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.GoOrdineDettaglio;
import it.calolenoci.entity.OrdineDettaglio;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class ArticoloMapper {

    public void fromDtoToEntity (GoOrdineDettaglio o, OrdineDettaglioDto dto) {
        if(dto.getFlagNonDisponibile() == null) {
            dto.setFlagNonDisponibile(Boolean.FALSE);
        } else {
            o.setFlagNonDisponibile(dto.getFlagNonDisponibile());
        }
        if(dto.getFlagOrdinato() == null) {
            dto.setFlagOrdinato(Boolean.FALSE);
        } else {
            o.setFlagOrdinato(dto.getFlagOrdinato());
        }
        if(dto.getFlagRiservato() == null) {
            dto.setFlagRiservato(Boolean.FALSE);
        } else {
            o.setFlagRiservato(dto.getFlagRiservato());
        }
        if(dto.getFlagConsegnato() == null) {
            dto.setFlagConsegnato(Boolean.FALSE);
        } else {
            o.setFlagConsegnato(dto.getFlagConsegnato());
        }
        if(dto.getFlProntoConsegna() == null) {
            dto.setFlProntoConsegna(Boolean.FALSE);
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
    }
}
