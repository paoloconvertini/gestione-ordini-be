package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.identity.SecurityIdentity;
import it.calolenoci.dto.*;
import it.calolenoci.entity.Appuntamento;
import it.calolenoci.entity.ShowroomMotivo;
import it.calolenoci.mapper.AppuntamentoMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
@Transactional
public class AppuntamentoService {

    @Inject
    @RestClient
    UserService userService;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    AppuntamentoMapper appuntamentoMapper;

    @Inject
    CalendarLabelService calendarLabelService;

    @Inject
    IcsService icsService;

    public PageAppuntamentoDto search(FiltroAppuntamentoDto filtro) {

        PageAppuntamentoDto result = new PageAppuntamentoDto();

        Map<String, Object> params = new HashMap<>();
        StringBuilder query = new StringBuilder("FROM Appuntamento a WHERE 1 = 1 ");

        if (filtro.getSedeId() != null) {
            query.append(" AND a.sede.id = :sedeId ");
            params.put("sedeId", filtro.getSedeId());
        }

        if (filtro.getDataDa() != null) {
            query.append(" AND a.dataAppuntamento >= :dataDa ");
            params.put("dataDa", filtro.getDataDa());
        }

        if (filtro.getDataA() != null) {
            query.append(" AND a.dataAppuntamento <= :dataA ");
            params.put("dataA", filtro.getDataA());
        }

        if (StringUtils.isNotBlank(filtro.getNomeCliente())) {
            query.append(" AND UPPER(a.nomeCliente) LIKE :cliente ");
            params.put("cliente", "%" + filtro.getNomeCliente().toUpperCase() + "%");
        }

        if (StringUtils.isNotBlank(filtro.getCodVenditore())) {
            query.append(" AND a.codVenditore = :venditore ");
            params.put("venditore", filtro.getCodVenditore());
        }

        if (filtro.getMotivoId() != null) {
            query.append(" AND a.idMotivo = :motivoId ");
            params.put("motivoId", filtro.getMotivoId());
        }

        PanacheQuery<Appuntamento> panacheQuery = Appuntamento.find(query.toString(),
                Sort.by("dataAppuntamento").descending().and("oraDa"),
                params);

        long count = panacheQuery.count();

        List<Appuntamento> entities = panacheQuery.list();

        Map<String, String> venditoriMap = userService.getVenditori().stream().collect(Collectors.toMap(
                UserResponseDTO::getCodVenditore,
                UserResponseDTO::getFullname));

        Set<Long> motivoIds = entities.stream()
                .map(Appuntamento::getIdMotivo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, ShowroomMotivo> motiviMap = new HashMap<>();

        List<ShowroomMotivo> motivi = ShowroomMotivo.list("id in ?1", motivoIds);
        if (!motivi.isEmpty()) {
            motiviMap = motivi.stream().collect(Collectors.toMap(ShowroomMotivo::getId, m -> m));
        }

        List<AppuntamentoSearchDto> dtoList = new ArrayList<>();

        for (Appuntamento entity : entities) {

            AppuntamentoSearchDto dto = appuntamentoMapper.toSearchDto(entity);

            dto.setDataOraDa(LocalDateTime.of(entity.getDataAppuntamento(), entity.getOraDa()));
            dto.setDataOraA(LocalDateTime.of(entity.getDataAppuntamento(), entity.getOraA()));
            dto.setNomeVenditore(venditoriMap.get(entity.getCodVenditore()));

            ShowroomMotivo motivo = motiviMap.get(entity.getIdMotivo());

            if (motivo != null) {

                dto.setMotivoId(motivo.getId());
                dto.setMotivoDescrizione(motivo.getDescrizione());

                if (motivo.getParent() != null) {
                    dto.setMotivoParentId(motivo.getParent().getId());
                    dto.setMotivoParentDescrizione(motivo.getParent().getDescrizione());
                }
            }

            dto.setClienteLabel(dto.getNomeCliente());

            dto.setIndirizzoLabel(Stream.of(entity.getVia(), entity.getCivico(), entity.getComune())
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(", ")));

            dto.setDataOraLabel(entity.getOraDa() + " - " + entity.getOraA());

            dto.setVenditoreLabel(dto.getNomeVenditore());

            if (StringUtils.isNotBlank(dto.getMotivoParentDescrizione())) {
                dto.setMotivoLabel(dto.getMotivoParentDescrizione() + " - " + dto.getMotivoDescrizione());
            } else {
                dto.setMotivoLabel(dto.getMotivoDescrizione());
            }
            dto.setColore(getColoreSede(entity.getSede().getId()));
            dtoList.add(dto);
        }

        result.setCount(count);
        result.setList(dtoList);

        return result;
    }

    public AppuntamentoDto getById(Long id) {

        Appuntamento entity = getAppuntamento(id);

        AppuntamentoDto dto = appuntamentoMapper.toDto(entity);

        if (entity.getIdMotivo() != null) {

            ShowroomMotivo motivo = ShowroomMotivo.findById(entity.getIdMotivo());

            if (motivo != null) {

                dto.setMotivoId(motivo.getId());
                dto.setMotivoDescrizione(motivo.getDescrizione());

                if (motivo.getParent() != null) {
                    dto.setMotivoParentId(motivo.getParent().getId());
                    dto.setMotivoParentDescrizione(motivo.getParent().getDescrizione());
                }
            }
        }

        return dto;
    }

    @Transactional
    public AppuntamentoDto create(AppuntamentoDto dto) {

        if (dto.getSedeId() == null) {
            throw new WebApplicationException("Sede obbligatoria", 400);
        }

        if (dto.getMotivoId() == null) {
            throw new WebApplicationException("Motivo obbligatorio", 400);
        }

        if (StringUtils.isBlank(dto.getCodVenditore())) {
            throw new WebApplicationException("Venditore obbligatorio", 400);
        }

        validateVenditore(dto, null);

        getMotivo(dto.getMotivoId());

        Appuntamento entity = appuntamentoMapper.fromDto(dto);

        entity.setCreatedAt(LocalDateTime.now());
        entity.setPromemoriaInviato(false);

        entity.persist();

        return getById(entity.getId());
    }

    @Transactional
    public AppuntamentoDto update(Long id, AppuntamentoDto dto) {
        Appuntamento entity = getAppuntamento(id);
        if (dto.getSedeId() == null) {
            throw new WebApplicationException("Sede obbligatoria", 400);
        }
        if (dto.getMotivoId() == null) {
            throw new WebApplicationException("Motivo obbligatorio", 400);
        }
        if (StringUtils.isBlank(dto.getCodVenditore())) {
            throw new WebApplicationException("Venditore obbligatorio", 400);
        }

        validateVenditore(dto, id);
        getMotivo(dto.getMotivoId());
        appuntamentoMapper.updateEntity(entity, dto);

        entity.setUpdatedAt(LocalDateTime.now());

        return getById(entity.getId());
    }

    @Transactional
    public void delete(Long id) {
        Appuntamento entity = getAppuntamento(id);
        entity.delete();
    }

    private Appuntamento getAppuntamento(Long id) {

        Appuntamento entity = Appuntamento.findById(id);

        if (entity == null) {
            throw new WebApplicationException("Appuntamento non trovato", 404);
        }

        return entity;
    }

    private ShowroomMotivo getMotivo(Long motivoId) {

        ShowroomMotivo motivo = ShowroomMotivo.findById(motivoId);

        if (motivo == null) {
            throw new WebApplicationException("Motivo non valido", 400);
        }

        if (!Boolean.TRUE.equals(motivo.getAttivo())) {
            throw new WebApplicationException("Motivo non attivo", 400);
        }

        return motivo;
    }

    private void validateVenditore(AppuntamentoDto dto, Long idDaEscludere) {

        if (dto.getDataAppuntamento() == null || dto.getOraDa() == null
                || dto.getOraA() == null || StringUtils.isBlank(dto.getCodVenditore())) {
            return;
        }

        List<Appuntamento> appuntamenti = Appuntamento.list(
                "dataAppuntamento = ?1 and codVenditore = ?2",
                dto.getDataAppuntamento(),
                dto.getCodVenditore());

        for (Appuntamento a : appuntamenti) {
            if (a.getId().equals(idDaEscludere)) {
                continue;
            }

            boolean overlap = dto.getOraDa().isBefore(a.getOraA())
                    && dto.getOraA().isAfter(a.getOraDa());

            if (overlap) {
                throw new WebApplicationException(
                        "Il venditore ha già un appuntamento nella fascia oraria selezionata",
                        400);
            }
        }
    }

    private String getColoreSede(Long sedeId) {

        return switch (sedeId.intValue()) {

            case 1 -> "#1976D2"; // blu
            case 2 -> "#388E3C"; // verde

            default -> "#9E9E9E";
        };
    }

    public String exportIcs(FiltroAppuntamentoDto filtro) {
        return null;
    }
}
