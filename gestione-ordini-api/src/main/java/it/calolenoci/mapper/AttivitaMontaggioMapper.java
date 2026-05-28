package it.calolenoci.mapper;

import it.calolenoci.dto.AttivitaMontaggioDto;
import it.calolenoci.dto.AttivitaMontaggioSearchDto;
import it.calolenoci.entity.AttivitaMontaggio;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AttivitaMontaggioMapper {

    public AttivitaMontaggioSearchDto toSearchDto(AttivitaMontaggio entity) {

        if (entity == null) {
            return null;
        }

        AttivitaMontaggioSearchDto dto =
                new AttivitaMontaggioSearchDto();

        dto.setId(entity.getId());
        dto.setDataOraDa(entity.getDataOraDa());
        dto.setDataOraA(entity.getDataOraA());
        dto.setNomeCliente(entity.getNomeCliente());
        dto.setComune(entity.getComune());
        dto.setStato(entity.getStato());
        dto.setScalaMobile(entity.getScalaMobile());
        dto.setNumeroOrdine(entity.getNumeroOrdine());
        dto.setTipoAppuntamento(entity.getTipoAppuntamento());
        dto.setTelefono(entity.getTelefono());

        return dto;
    }

    public AttivitaMontaggioDto toDto(AttivitaMontaggio entity) {

        if (entity == null) {
            return null;
        }

        AttivitaMontaggioDto dto =
                new AttivitaMontaggioDto();

        dto.setId(entity.getId());
        dto.setOrdineAnno(entity.getOrdineAnno());
        dto.setOrdineSerie(entity.getOrdineSerie());
        dto.setOrdineProgressivo(entity.getOrdineProgressivo());
        dto.setNumeroOrdine(entity.getNumeroOrdine());
        dto.setDataOraDa(entity.getDataOraDa());
        dto.setDataOraA(entity.getDataOraA());
        dto.setNomeCliente(entity.getNomeCliente());
        dto.setTelefono(entity.getTelefono());
        dto.setEmail(entity.getEmail());
        dto.setVia(entity.getVia());
        dto.setCivico(entity.getCivico());
        dto.setCap(entity.getCap());
        dto.setComune(entity.getComune());
        dto.setProvincia(entity.getProvincia());
        dto.setScalaMobile(entity.getScalaMobile());
        dto.setStato(entity.getStato());
        dto.setPromemoriaInviato(entity.getPromemoriaInviato());
        dto.setDataInvioPromemoria(entity.getDataInvioPromemoria());
        dto.setDataCompletamento(entity.getDataCompletamento());
        dto.setNote(entity.getNote());
        dto.setTipoAppuntamento(entity.getTipoAppuntamento());

        return dto;
    }

    public AttivitaMontaggio fromDto(AttivitaMontaggioDto dto) {

        if (dto == null) {
            return null;
        }

        AttivitaMontaggio entity =
                new AttivitaMontaggio();

        entity.setOrdineAnno(dto.getOrdineAnno());
        entity.setOrdineSerie(dto.getOrdineSerie());
        entity.setOrdineProgressivo(dto.getOrdineProgressivo());
        entity.setNumeroOrdine(dto.getNumeroOrdine());
        entity.setDataOraDa(dto.getDataOraDa());
        entity.setDataOraA(dto.getDataOraA());
        entity.setNomeCliente(dto.getNomeCliente());
        entity.setTelefono(dto.getTelefono());
        entity.setEmail(dto.getEmail());
        entity.setVia(dto.getVia());
        entity.setCivico(dto.getCivico());
        entity.setCap(dto.getCap());
        entity.setComune(dto.getComune());
        entity.setProvincia(dto.getProvincia());
        entity.setScalaMobile(Boolean.TRUE.equals(dto.getScalaMobile()));
        entity.setStato(dto.getStato());
        entity.setPromemoriaInviato(dto.getPromemoriaInviato());
        entity.setDataInvioPromemoria(dto.getDataInvioPromemoria());
        entity.setDataCompletamento(dto.getDataCompletamento());
        entity.setNote(dto.getNote());
        entity.setTipoAppuntamento(dto.getTipoAppuntamento());

        return entity;
    }

    public void updateEntity(AttivitaMontaggio entity, AttivitaMontaggioDto dto) {

        entity.setOrdineAnno(dto.getOrdineAnno());
        entity.setOrdineSerie(dto.getOrdineSerie());
        entity.setOrdineProgressivo(dto.getOrdineProgressivo());
        entity.setNumeroOrdine(dto.getNumeroOrdine());
        entity.setDataOraDa(dto.getDataOraDa());
        entity.setDataOraA(dto.getDataOraA());
        entity.setNomeCliente(dto.getNomeCliente());
        entity.setTelefono(dto.getTelefono());
        entity.setEmail(dto.getEmail());
        entity.setVia(dto.getVia());
        entity.setCivico(dto.getCivico());
        entity.setCap(dto.getCap());
        entity.setComune(dto.getComune());
        entity.setProvincia(dto.getProvincia());
        entity.setScalaMobile(Boolean.TRUE.equals(dto.getScalaMobile()));
        entity.setStato(dto.getStato());
        entity.setPromemoriaInviato(dto.getPromemoriaInviato());
        entity.setDataInvioPromemoria(dto.getDataInvioPromemoria());
        entity.setDataCompletamento(dto.getDataCompletamento());
        entity.setNote(dto.getNote());
        entity.setTipoAppuntamento(dto.getTipoAppuntamento());
    }
}