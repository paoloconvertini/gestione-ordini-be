package it.calolenoci.mapper;

import it.calolenoci.dto.AppuntamentoDto;
import it.calolenoci.dto.AppuntamentoSearchDto;
import it.calolenoci.entity.Appuntamento;
import it.calolenoci.entity.Sede;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppuntamentoMapper {

    public AppuntamentoSearchDto toSearchDto(Appuntamento entity) {

        if (entity == null) {
            return null;
        }

        AppuntamentoSearchDto dto =
                new AppuntamentoSearchDto();

        dto.setId(entity.getId());

        if (entity.getSede() != null) {

            dto.setSedeId(entity.getSede().getId());
            dto.setSedeDescrizione(entity.getSede().getDescrizione());
        }

        dto.setNomeCliente(entity.getNomeCliente());
        dto.setTelefono(entity.getTelefono());

        dto.setComune(entity.getComune());

        dto.setDataAppuntamento(entity.getDataAppuntamento());

        dto.setOraDa(entity.getOraDa());
        dto.setOraA(entity.getOraA());
        dto.setTipoEvento(entity.getTipoEvento());
        dto.setDescrizione(entity.getDescrizione());

        dto.setNote(entity.getNote());

        return dto;
    }

    public AppuntamentoDto toDto(Appuntamento entity) {

        if (entity == null) {
            return null;
        }

        AppuntamentoDto dto =
                new AppuntamentoDto();

        dto.setId(entity.getId());

        if (entity.getSede() != null) {

            dto.setSedeId(entity.getSede().getId());
            dto.setSedeDescrizione(entity.getSede().getDescrizione());
        }

        dto.setGruppoConto(entity.getGruppoConto());
        dto.setSottoConto(entity.getSottoConto());

        dto.setNomeCliente(entity.getNomeCliente());
        dto.setTelefono(entity.getTelefono());
        dto.setEmail(entity.getEmail());

        dto.setVia(entity.getVia());
        dto.setCivico(entity.getCivico());
        dto.setCap(entity.getCap());
        dto.setComune(entity.getComune());
        dto.setProvincia(entity.getProvincia());

        dto.setDataAppuntamento(entity.getDataAppuntamento());

        dto.setOraDa(entity.getOraDa());
        dto.setOraA(entity.getOraA());
        dto.setTipoEvento(entity.getTipoEvento());
        dto.setDescrizione(entity.getDescrizione());

        dto.setNote(entity.getNote());

        dto.setPromemoriaInviato(entity.getPromemoriaInviato());

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setMotivoId(entity.getIdMotivo());
        return dto;
    }

    public Appuntamento fromDto(AppuntamentoDto dto) {

        if (dto == null) {
            return null;
        }

        Appuntamento entity =
                new Appuntamento();

        if (dto.getSedeId() != null) {

            Sede sede = new Sede();
            sede.setId(dto.getSedeId());

            entity.setSede(sede);
        }

        entity.setGruppoConto(dto.getGruppoConto());
        entity.setSottoConto(dto.getSottoConto());

        entity.setNomeCliente(dto.getNomeCliente());
        entity.setTelefono(dto.getTelefono());
        entity.setEmail(dto.getEmail());

        entity.setVia(dto.getVia());
        entity.setCivico(dto.getCivico());
        entity.setCap(dto.getCap());
        entity.setComune(dto.getComune());
        entity.setProvincia(dto.getProvincia());

        entity.setDataAppuntamento(dto.getDataAppuntamento());

        entity.setOraDa(dto.getOraDa());
        entity.setOraA(dto.getOraA());

        entity.setTipoEvento(dto.getTipoEvento());
        entity.setDescrizione(dto.getDescrizione());

        entity.setNote(dto.getNote());

        entity.setPromemoriaInviato(dto.getPromemoriaInviato());
        entity.setIdMotivo(dto.getMotivoId());
        return entity;
    }

    public void updateEntity(Appuntamento entity,
                             AppuntamentoDto dto) {

        if (dto.getSedeId() != null) {

            Sede sede = new Sede();
            sede.setId(dto.getSedeId());

            entity.setSede(sede);
        }

        entity.setGruppoConto(dto.getGruppoConto());
        entity.setSottoConto(dto.getSottoConto());

        entity.setNomeCliente(dto.getNomeCliente());
        entity.setTelefono(dto.getTelefono());
        entity.setEmail(dto.getEmail());

        entity.setVia(dto.getVia());
        entity.setCivico(dto.getCivico());
        entity.setCap(dto.getCap());
        entity.setComune(dto.getComune());
        entity.setProvincia(dto.getProvincia());

        entity.setDataAppuntamento(dto.getDataAppuntamento());

        entity.setOraDa(dto.getOraDa());
        entity.setOraA(dto.getOraA());

        entity.setTipoEvento(dto.getTipoEvento());
        entity.setDescrizione(dto.getDescrizione());

        entity.setNote(dto.getNote());
        entity.setIdMotivo(dto.getMotivoId());
        entity.setPromemoriaInviato(dto.getPromemoriaInviato());
    }
}