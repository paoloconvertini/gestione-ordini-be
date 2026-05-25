package it.calolenoci.mapper;


import it.calolenoci.dto.OperaioDto;
import it.calolenoci.entity.Operaio;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OperaioMapper {
    private OperaioMapper() {
    }

    public OperaioDto toDto(Operaio entity) {

        if (entity == null) {
            return null;
        }

        OperaioDto dto = new OperaioDto();

        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setAttivo(entity.getAttivo());

        return dto;
    }

    public Operaio fromDto(OperaioDto dto) {
        if (dto == null) {
            return null;
        }
        Operaio entity = new Operaio();
        entity.setNome(dto.getNome());
        entity.setAttivo(dto.getAttivo() != null ? dto.getAttivo() : true);
        return entity;
    }

    public void updateEntity(Operaio entity, OperaioDto dto) {
        entity.setNome(dto.getNome());
    }
}
