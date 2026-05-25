package it.calolenoci.mapper;

import it.calolenoci.dto.AttivitaMontaggioDettDto;
import it.calolenoci.entity.AttivitaMontaggioDett;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AttivitaMontaggioDettMapper {

    public AttivitaMontaggioDettDto toDto(AttivitaMontaggioDett entity) {

        if (entity == null) {
            return null;
        }

        AttivitaMontaggioDettDto dto = new AttivitaMontaggioDettDto();

        dto.setId(entity.getId());
        dto.setIdTipoAttivita(entity.getIdTipoAttivita());
        dto.setQuantita(entity.getQuantita());
        dto.setCompletato(entity.getCompletato());
        dto.setNote(entity.getNote());

        return dto;
    }

    public AttivitaMontaggioDett fromDto(AttivitaMontaggioDettDto dto) {

        if (dto == null) {
            return null;
        }

        AttivitaMontaggioDett entity = new AttivitaMontaggioDett();

        entity.setIdTipoAttivita(dto.getIdTipoAttivita());
        entity.setQuantita(dto.getQuantita());
        entity.setCompletato(Boolean.TRUE.equals(dto.getCompletato()));
        entity.setNote(dto.getNote());

        return entity;
    }

    public void updateEntity(AttivitaMontaggioDett entity,
                             AttivitaMontaggioDettDto dto) {

        entity.setIdTipoAttivita(dto.getIdTipoAttivita());
        entity.setQuantita(dto.getQuantita());
        entity.setCompletato(Boolean.TRUE.equals(dto.getCompletato()));
        entity.setNote(dto.getNote());
    }
}
