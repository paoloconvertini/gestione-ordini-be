package it.calolenoci.service;

import it.calolenoci.dto.TipoAttivitaMontaggioDto;
import it.calolenoci.entity.TipoAttivitaMontaggio;
import it.calolenoci.mapper.TipoAttivitaMontaggioMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@ApplicationScoped
public class TipoAttivitaMontaggioService {

    @Inject
    TipoAttivitaMontaggioMapper mapper;

    public List<TipoAttivitaMontaggioDto> getTipiAttivita() {
        List<TipoAttivitaMontaggio> entities = TipoAttivitaMontaggio.list("attivo = true order by ordineVisualizzazione");
        return entities.stream().map(mapper::toDto).toList();
    }

    public TipoAttivitaMontaggioDto getById(Long id) {
        TipoAttivitaMontaggio entity = TipoAttivitaMontaggio.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Tipo attività non trovato", 404);
        }
        return mapper.toDto(entity);
    }

    @Transactional
    public TipoAttivitaMontaggioDto create(TipoAttivitaMontaggioDto dto) {
        if (dto == null) {
            throw new WebApplicationException("Dati mancanti", 400);
        }
        if (StringUtils.isBlank(dto.getDescrizione())) {
            throw new WebApplicationException("Descrizione obbligatoria", 400);
        }
        TipoAttivitaMontaggio entity = mapper.fromDto(dto);
        entity.persist();
        return mapper.toDto(entity);
    }

    @Transactional
    public TipoAttivitaMontaggioDto update(Long id, TipoAttivitaMontaggioDto dto) {
        TipoAttivitaMontaggio entity = TipoAttivitaMontaggio.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Tipo attività non trovato", 404);
        }
        if (StringUtils.isBlank(dto.getDescrizione())) {
            throw new WebApplicationException("Descrizione obbligatoria", 400);
        }
        mapper.updateEntity(entity, dto);
        return mapper.toDto(entity);
    }

    @Transactional
    public void disattiva(Long id) {
        TipoAttivitaMontaggio entity = TipoAttivitaMontaggio.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Tipo attività non trovato", 404);
        }
        entity.setAttivo(false);
    }
}
