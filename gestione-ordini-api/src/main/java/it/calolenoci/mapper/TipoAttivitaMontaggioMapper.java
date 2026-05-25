package it.calolenoci.mapper;

import it.calolenoci.dto.TipoAttivitaMontaggioDto;
import it.calolenoci.entity.TipoAttivitaMontaggio;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TipoAttivitaMontaggioMapper {

    public TipoAttivitaMontaggioDto toDto(TipoAttivitaMontaggio entity) {

        if (entity == null) {
            return null;
        }

        TipoAttivitaMontaggioDto dto = new TipoAttivitaMontaggioDto();

        dto.setId(entity.getId());
        dto.setDescrizione(entity.getDescrizione());
        dto.setOrdineVisualizzazione(entity.getOrdineVisualizzazione());
        dto.setAttivo(entity.getAttivo());

        return dto;
    }

    public TipoAttivitaMontaggio fromDto(TipoAttivitaMontaggioDto dto) {

        if (dto == null) {
            return null;
        }

        TipoAttivitaMontaggio entity = new TipoAttivitaMontaggio();

        entity.setDescrizione(dto.getDescrizione());
        entity.setOrdineVisualizzazione(dto.getOrdineVisualizzazione());
        entity.setAttivo(dto.getAttivo() != null ? dto.getAttivo() : true);

        return entity;
    }

    public void updateEntity(TipoAttivitaMontaggio entity,
                             TipoAttivitaMontaggioDto dto) {

        entity.setDescrizione(dto.getDescrizione());
        entity.setOrdineVisualizzazione(dto.getOrdineVisualizzazione());
        entity.setAttivo(dto.getAttivo());
    }
}
