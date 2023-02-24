package it.calolenoci.mapper;

import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.OrdineDettaglio;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class ArticoloMapper {

    public OrdineDettaglioDto fromEntityToDto (OrdineDettaglio o) {
        OrdineDettaglioDto dto = new OrdineDettaglioDto();
        dto.setAnno(o.getAnno());
        dto.setSerie(o.getSerie());
        dto.setProgressivo(o.getProgressivo());
        dto.setRigo(o.getRigo());
        dto.setTipoRigo(o.getTipoRigo());
        dto.setFArticolo(o.getFArticolo());
        dto.setCodArtFornitore(o.getCodArtFornitore());
        dto.setFDescrArticolo(o.getFDescrArticolo());
        dto.setFUnitaMisura(o.getFUnitaMisura());
        dto.setFColli(o.getFColli());
        dto.setScontoArticolo(o.getScontoArticolo());
        dto.setScontoC1(o.getScontoC1());
        dto.setScontoC2(o.getScontoC2());
        dto.setScontoP(o.getScontoP());
        dto.setFCodiceIva(o.getFCodiceIva());
        dto.setGeTono(o.getGeTono());
        dto.setPrezzo(o.getPrezzo());
        dto.setGeFlagNonDisponibile(o.getGeFlagNonDisponibile() == '1');
        dto.setQuantita(o.getQuantita());
        dto.setGeFlagOrdinato(o.getGeFlagOrdinato() == '1');
        dto.setGeFlagRiservato(o.getGeFlagRiservato() == '1');
        dto.setGeFlagConsegnato(o.getGeFlagConsegnato() == '1');
        return dto;
    }

    public void fromDtoToEntity (OrdineDettaglio o, OrdineDettaglioDto dto) {
        o.setGeTono(dto.getGeTono());
        o.setGeFlagNonDisponibile(dto.getGeFlagNonDisponibile()?'1':'0');
        o.setQuantita(dto.getQuantita());
        o.setGeFlagOrdinato(dto.getGeFlagOrdinato()?'1':'0');
        o.setGeFlagRiservato(dto.getGeFlagRiservato()?'1':'0');
        o.setGeFlagConsegnato(dto.getGeFlagConsegnato()?'1':'0');
    }
}
