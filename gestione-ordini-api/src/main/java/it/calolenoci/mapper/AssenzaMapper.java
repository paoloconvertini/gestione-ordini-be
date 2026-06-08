package it.calolenoci.mapper;

import it.calolenoci.dto.AssenzaDto;
import it.calolenoci.entity.Assenza;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AssenzaMapper {

    public AssenzaDto toDto(Assenza entity) {

        if (entity == null) {
            return null;
        }

        AssenzaDto dto = new AssenzaDto();

        dto.setId(entity.getId());
        dto.setTipo(entity.getTipo());

        dto.setDataDa(entity.getDataDa());
        dto.setDataA(entity.getDataA());

        dto.setOraDa(entity.getOraDa());
        dto.setOraA(entity.getOraA());

        dto.setNote(entity.getNote());

        return dto;
    }

    public Assenza fromDto(AssenzaDto dto) {
        if (dto == null) {
            return null;
        }
        Assenza entity = new Assenza();
        entity.setTipo(dto.getTipo());
        entity.setDataDa(dto.getDataDa());
        entity.setDataA(dto.getDataA());

        entity.setOraDa(dto.getOraDa());
        entity.setOraA(dto.getOraA());

        entity.setNote(dto.getNote());

        return entity;
    }

    public void updateEntity(Assenza entity, AssenzaDto dto) {

        entity.setTipo(dto.getTipo());

        entity.setDataDa(dto.getDataDa());
        entity.setDataA(dto.getDataA());

        entity.setOraDa(dto.getOraDa());
        entity.setOraA(dto.getOraA());

        entity.setNote(dto.getNote());
    }
}