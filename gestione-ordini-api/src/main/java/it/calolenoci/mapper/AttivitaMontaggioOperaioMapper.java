package it.calolenoci.mapper;

import it.calolenoci.dto.AttivitaMontaggioOperaioDto;
import it.calolenoci.entity.AttivitaMontaggioOperaio;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AttivitaMontaggioOperaioMapper {

    public AttivitaMontaggioOperaioDto toDto(AttivitaMontaggioOperaio entity) {

        if (entity == null) {
            return null;
        }

        AttivitaMontaggioOperaioDto dto =
                new AttivitaMontaggioOperaioDto();

        dto.setId(entity.getId());
        dto.setIdOperaio(entity.getIdOperaio());
        dto.setPrincipale(entity.getPrincipale());

        return dto;
    }

    public AttivitaMontaggioOperaio fromDto(
            AttivitaMontaggioOperaioDto dto) {

        if (dto == null) {
            return null;
        }

        AttivitaMontaggioOperaio entity =
                new AttivitaMontaggioOperaio();

        entity.setIdOperaio(dto.getIdOperaio());
        entity.setPrincipale(Boolean.TRUE.equals(dto.getPrincipale()));

        return entity;
    }

    public void updateEntity(
            AttivitaMontaggioOperaio entity,
            AttivitaMontaggioOperaioDto dto) {

        entity.setIdOperaio(dto.getIdOperaio());
        entity.setPrincipale(Boolean.TRUE.equals(dto.getPrincipale()));
    }
}