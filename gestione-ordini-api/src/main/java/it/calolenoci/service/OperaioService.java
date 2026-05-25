package it.calolenoci.service;

import it.calolenoci.dto.OperaioDto;
import it.calolenoci.entity.Operaio;
import it.calolenoci.mapper.OperaioMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@ApplicationScoped
public class OperaioService {

    @Inject
    OperaioMapper operaioMapper;


    public List<OperaioDto> getOperai() {

        List<Operaio> entities = Operaio.list(
                "attivo = true order by nome"
        );

        return entities.stream()
                .map(operaioMapper::toDto)
                .toList();
    }

    public OperaioDto getOperaioById(Long id) {

        Operaio entity = Operaio.findById(id);

        if (entity == null) {
            throw new WebApplicationException("Operaio non trovato", 404);
        }

        return operaioMapper.toDto(entity);
    }

    @Transactional
    public OperaioDto createOperaio(OperaioDto dto) {

        if (dto == null) {
            throw new WebApplicationException("Dati mancanti", 400);
        }

        if (StringUtils.isBlank(dto.getNome())) {
            throw new WebApplicationException("Nome obbligatorio", 400);
        }

        Operaio entity = operaioMapper.fromDto(dto);

        entity.persist();

        return operaioMapper.toDto(entity);
    }

    @Transactional
    public OperaioDto updateOperaio(Long id, OperaioDto dto) {

        Operaio entity = Operaio.findById(id);

        if (entity == null) {
            throw new WebApplicationException("Operaio non trovato", 404);
        }

        if (StringUtils.isBlank(dto.getNome())) {
            throw new WebApplicationException("Nome obbligatorio", 400);
        }

        operaioMapper.updateEntity(entity, dto);

        return operaioMapper.toDto(entity);
    }

    @Transactional
    public void disattivaOperaio(Long id) {

        Operaio entity = Operaio.findById(id);

        if (entity == null) {
            throw new WebApplicationException("Operaio non trovato", 404);
        }

        entity.setAttivo(false);
    }
}
