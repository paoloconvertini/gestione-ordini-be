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
        dto.setGeFlagNonDisponibile(o.getGeFlagNonDisponibile());
        dto.setQuantita(o.getQuantita());
        dto.setGeFlagOrdinato(o.getGeFlagOrdinato());
        dto.setGeFlagRiservato(o.getGeFlagRiservato());
        return dto;
    }
}
