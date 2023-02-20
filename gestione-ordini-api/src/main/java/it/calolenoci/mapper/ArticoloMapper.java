package it.calolenoci.mapper;

import it.calolenoci.dto.OrdineClienteDto;
import it.calolenoci.dto.OrdineClienteTestataDto;
import it.calolenoci.entity.OrdineCliente;
import it.calolenoci.entity.OrdineClienteTestata;
import it.calolenoci.entity.PianoConti;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class ArticoloMapper {

    public OrdineClienteDto fromEntityToDto (OrdineCliente o) {
        OrdineClienteDto dto = new OrdineClienteDto();
        dto.setAnno(o.getOrdineDettaglioId().getAnno());
        dto.setSerie(o.getOrdineDettaglioId().getSerie());
        dto.setProgressivo(o.getOrdineDettaglioId().getProgressivo());
        dto.setRigo(o.getOrdineDettaglioId().getRigo());
        dto.setTipoRigo(o.getTipoRigo());
        dto.setFArticolo(o.getFArticolo());
        dto.setCodArtFornitore(o.getCodArtFornitore());
        dto.setFDescrArticolo(o.getFDescrArticolo());
        dto.setFUnitaMisura(o.getFUnitaMisura());
        dto.setGeTono(o.getGeTono());
        dto.setPrezzo(o.getPrezzo());
        dto.setGeFlagNonDisponibile(o.getGeFlagNonDisponibile());
        dto.setQuantita(o.getQuantita());
        dto.setGeFlagOrdinato(o.getGeFlagOrdinato());
        dto.setGeFlagRiservato(o.getGeFlagRiservato());
        return dto;
    }
}
