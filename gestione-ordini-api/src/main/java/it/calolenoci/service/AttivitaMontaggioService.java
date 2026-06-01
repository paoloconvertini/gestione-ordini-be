package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import it.calolenoci.enums.StatoAttivitaMontaggioEnum;
import it.calolenoci.mapper.AttivitaMontaggioDettMapper;
import it.calolenoci.mapper.AttivitaMontaggioMapper;
import it.calolenoci.mapper.AttivitaMontaggioOperaioMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class AttivitaMontaggioService {

    @Inject
    AttivitaMontaggioMapper attivitaMontaggioMapper;

    @Inject
    AttivitaMontaggioDettMapper attivitaMontaggioDettMapper;

    @Inject
    AttivitaMontaggioOperaioMapper attivitaMontaggioOperaioMapper;

    @Inject
    CalendarLabelService calendarLabelService;

    @Inject
    IcsService icsService;

    public void delete(Long id) {
        if (id == null) {
            return;
        }
        AttivitaMontaggioDett.delete("idAttivitaMontaggio = ?1", id);
        AttivitaMontaggioOperaio.delete("idAttivitaMontaggio = ?1", id);
        AttivitaMontaggio.deleteById(id);
    }

    public PageAttivitaMontaggioDto search(FiltroAttivitaMontaggioDto filtro) {
        PageAttivitaMontaggioDto result = new PageAttivitaMontaggioDto();
        Map<String, Object> params = new HashMap<>();
        StringBuilder query = new StringBuilder("FROM AttivitaMontaggio a WHERE 1 = 1 ");
        if (StringUtils.isNotBlank(filtro.getTipoAppuntamento())) {
            query.append(" AND tipoAppuntamento = :tipoAppuntamento ");
            params.put("tipoAppuntamento", filtro.getTipoAppuntamento());
        }
        if (filtro.getDataDa() != null) {
            query.append(" AND a.dataOraDa >= :dataDa ");
            params.put("dataDa", filtro.getDataDa().atStartOfDay());
        }
        if (filtro.getDataA() != null) {
            query.append(" AND a.dataOraA <= :dataA ");
            params.put("dataA", filtro.getDataA().atTime(23, 59, 59));
        }
        if (StringUtils.isNotBlank(filtro.getNomeCliente())) {
            query.append(" AND UPPER(a.nomeCliente) LIKE :nomeCliente ");
            params.put("nomeCliente", "%" + filtro.getNomeCliente().toUpperCase() + "%");
        }
        if (StringUtils.isNotBlank(filtro.getStato())) {
            query.append(" AND a.stato = :stato ");
            params.put("stato", filtro.getStato());
        }
        if (filtro.getScalaMobile() != null) {
            query.append(" AND a.scalaMobile = :scalaMobile ");
            params.put("scalaMobile", filtro.getScalaMobile());
        }
        if (filtro.getOrdineAnno() != null) {
            query.append(" AND a.ordineAnno = :ordineAnno ");
            params.put("ordineAnno", filtro.getOrdineAnno());
        }
        if (StringUtils.isNotBlank(filtro.getOrdineSerie())) {
            query.append(" AND a.ordineSerie = :ordineSerie ");
            params.put("ordineSerie", filtro.getOrdineSerie());
        }
        if (filtro.getOrdineProgressivo() != null) {
            query.append(" AND a.ordineProgressivo = :ordineProgressivo ");
            params.put("ordineProgressivo", filtro.getOrdineProgressivo());
        }
        if (filtro.getIdOperaio() != null) {
            List<AttivitaMontaggioOperaio> operai = AttivitaMontaggioOperaio.list(
                    "idOperaio = ?1",
                    filtro.getIdOperaio()
            );
            List<Long> idsAttivita = operai.stream()
                    .map(AttivitaMontaggioOperaio::getIdAttivitaMontaggio)
                    .distinct()
                    .toList();
            if (idsAttivita.isEmpty()) {
                result.setCount(0);
                result.setList(List.of());
                return result;
            }
            query.append(" AND a.id IN :idsAttivita ");
            params.put("idsAttivita", idsAttivita);
        }
        PanacheQuery<AttivitaMontaggio> panacheQuery = AttivitaMontaggio.find(
                query.toString(),
                Sort.by("dataOraDa").descending(),
                params
        );
        long count = panacheQuery.count();
        List<AttivitaMontaggio> entities = panacheQuery.list();
        List<AttivitaMontaggioSearchDto> dtoList = new ArrayList<>();
        for (AttivitaMontaggio entity : entities) {
            AttivitaMontaggioSearchDto dto = attivitaMontaggioMapper.toSearchDto(entity);
            dto.setColore(getColoreByStato(entity.getTipoAppuntamento(), entity.getStato()));
            dto.setTooltip(buildTooltip(entity));
            dto.setClienteLabel(calendarLabelService.buildClienteLabel(entity));
            dto.setIndirizzoLabel(calendarLabelService.buildIndirizzoLabel(entity));
            dto.setAttivitaLabel(calendarLabelService.buildAttivitaLabel(entity));
            dto.setDataOraLabel(calendarLabelService.buildDataOraLabel(entity));
            dtoList.add(dto);
        }
        result.setCount(count);
        result.setList(dtoList);
        return result;
    }

    public AttivitaMontaggioDto getById(Long id) {
        AttivitaMontaggio entity = AttivitaMontaggio.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Attività non trovata", 404);
        }
        AttivitaMontaggioDto dto = attivitaMontaggioMapper.toDto(entity);
        List<AttivitaMontaggioDett> dettagli = AttivitaMontaggioDett.list(
                "idAttivitaMontaggio = ?1",
                entity.getId()
        );
        List<AttivitaMontaggioDettDto> dettagliDto = dettagli.stream()
                .map(attivitaMontaggioDettMapper::toDto)
                .toList();
        dto.setDettagli(dettagliDto);
        List<AttivitaMontaggioOperaio> operai = AttivitaMontaggioOperaio.list(
                "idAttivitaMontaggio = ?1",
                entity.getId()
        );
        List<AttivitaMontaggioOperaioDto> operaiDto = operai.stream()
                .map(attivitaMontaggioOperaioMapper::toDto)
                .toList();
        dto.setOperai(operaiDto);
        return dto;
    }

    public AttivitaMontaggioDto create(AttivitaMontaggioDto dto) {
        validate(dto);
        AttivitaMontaggio entity = attivitaMontaggioMapper.fromDto(dto);
        entity.setStato(
                StringUtils.isNotBlank(dto.getStato())
                        ? dto.getStato()
                        : StatoAttivitaMontaggioEnum.PROGRAMMATO.name()
        );
        entity.setPromemoriaInviato(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.persist();
        saveDettagli(entity.getId(), dto.getDettagli());
        saveOperai(entity.getId(), dto.getOperai());
        return getById(entity.getId());
    }

    public AttivitaMontaggioDto update(Long id, AttivitaMontaggioDto dto) {
        validate(dto);
        AttivitaMontaggio entity = AttivitaMontaggio.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Attività non trovata", 404);
        }
        attivitaMontaggioMapper.updateEntity(entity, dto);
        entity.setUpdatedAt(LocalDateTime.now());
        AttivitaMontaggioDett.delete("idAttivitaMontaggio = ?1", entity.getId());
        AttivitaMontaggioOperaio.delete("idAttivitaMontaggio = ?1", entity.getId());
        saveDettagli(entity.getId(), dto.getDettagli());
        saveOperai(entity.getId(), dto.getOperai());
        return getById(entity.getId());
    }

    public String exportIcs(FiltroAttivitaMontaggioDto filtro) {
        PageAttivitaMontaggioDto result = search(filtro);
        return icsService.buildMontaggiCalendar(result);
    }

    private void saveDettagli(Long idAttivita, List<AttivitaMontaggioDettDto> dettagli) {
        if (dettagli == null) {
            return;
        }
        for (AttivitaMontaggioDettDto dto : dettagli) {
            AttivitaMontaggioDett entity = attivitaMontaggioDettMapper.fromDto(dto);
            entity.setIdAttivitaMontaggio(idAttivita);
            entity.persist();
        }
    }

    private void saveOperai(Long idAttivita, List<AttivitaMontaggioOperaioDto> operai) {
        if (operai == null) {
            return;
        }
        for (AttivitaMontaggioOperaioDto dto : operai) {
            AttivitaMontaggioOperaio entity = attivitaMontaggioOperaioMapper.fromDto(dto);
            entity.setIdAttivitaMontaggio(idAttivita);
            entity.persist();
        }
    }

    private void validate(AttivitaMontaggioDto dto) {
        if (dto == null) {
            throw new WebApplicationException("Dati mancanti", 400);
        }
        if (dto.getDataOraDa() == null) {
            throw new WebApplicationException("Data ora inizio obbligatoria", 400);
        }
        if (dto.getDataOraA() == null) {
            throw new WebApplicationException("Data ora fine obbligatoria", 400);
        }
        if (StringUtils.isBlank(dto.getNomeCliente())) {
            throw new WebApplicationException("Nome cliente obbligatorio", 400);
        }
    }

    private String buildTooltip(AttivitaMontaggio entity) {
        StringBuilder sb = new StringBuilder();
        sb.append("Cliente: ").append(entity.getNomeCliente());
        if (StringUtils.isNotBlank(entity.getComune())) {
            sb.append("\nComune: ").append(entity.getComune());
        }
        if (StringUtils.isNotBlank(entity.getNumeroOrdine())) {
            sb.append("\nOrdine: ").append(entity.getNumeroOrdine());
        }
        if (StringUtils.isNotBlank(entity.getStato())) {
            sb.append("\nStato: ").append(entity.getStato());
        }
        if (Boolean.TRUE.equals(entity.getScalaMobile())) {
            sb.append("\nScala mobile: SI");
        }
        return sb.toString();
    }

    private String getColoreByStato(String tipoAppuntamento, String stato) {
        if ("RILIEVO".equals(tipoAppuntamento)) {
            return switch (stato) {
                case "COMPLETATO" -> "#2e7d32";
                case "ANNULLATO" -> "#c62828";
                case "IN_CORSO" -> "#66bb6a";
                default -> "#43a047";
            };
        }
        return switch (stato) {
            case "PROGRAMMATO" -> "#1976d2";
            case "IN_CORSO" -> "#f57c00";
            case "COMPLETATO" -> "#388e3c";
            case "ANNULLATO" -> "#d32f2f";
            default -> "#1976d2";
        };
    }

}
