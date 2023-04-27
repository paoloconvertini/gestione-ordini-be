package it.calolenoci.mapper;

import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.OrdineDettaglio;

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
        if(dto.getQtaConsegnatoSenzaBolla() != null) {
            o.setQtaConsegnatoSenzaBolla(dto.getQtaConsegnatoSenzaBolla());
        }
    }
}
