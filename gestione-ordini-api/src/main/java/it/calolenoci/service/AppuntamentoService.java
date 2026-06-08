package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.Appuntamento;
import it.calolenoci.entity.AppuntamentoVenditore;
import it.calolenoci.entity.ShowroomMotivo;
import it.calolenoci.enums.TipoAppuntamentoEnum;
import it.calolenoci.mapper.AppuntamentoMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDateTime;
import java.util.*;
import org.jboss.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
@Transactional
public class AppuntamentoService {

    private static final Logger log = Logger.getLogger(AppuntamentoService.class);

    @Inject
    @RestClient
    UserService userService;

    @Inject
    AppuntamentoMapper appuntamentoMapper;

    @Inject
    CalendarLabelService calendarLabelService;

    @Inject
    IcsService icsService;

    @Inject
    OutlookCalendarService outlookCalendarService;

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
            List<Long> ids = AppuntamentoVenditore.find("codVenditore", filtro.getCodVenditore())
                    .stream()
                    .map(v -> ((AppuntamentoVenditore) v).getIdAppuntamento())
                    .toList();
            if (ids.isEmpty()) {
                result.setCount(0);
                result.setList(List.of());
                return result;
            }

            query.append(" AND a.id IN :ids ");

            params.put("ids", ids);
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
            dto.setTipoEvento(entity.getTipoEvento());
            dto.setDescrizione(entity.getDescrizione());
            dto.setCodVenditori(getCodVenditori(entity.getId()));
            dto.setVenditoriLabel(getVenditoriLabel(entity.getId(), venditoriMap));
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

            if (StringUtils.isNotBlank(dto.getMotivoParentDescrizione())) {
                dto.setMotivoLabel(dto.getMotivoParentDescrizione() + " - " + dto.getMotivoDescrizione());
            } else {
                dto.setMotivoLabel(dto.getMotivoDescrizione());
            }
            dto.setColore(getColore(entity));
            dtoList.add(dto);
        }

        result.setCount(count);
        result.setList(dtoList);

        return result;
    }

    public AppuntamentoDto getById(Long id) {

        Appuntamento entity = getAppuntamento(id);

        AppuntamentoDto dto = appuntamentoMapper.toDto(entity);
        dto.setCodVenditori(getCodVenditori(entity.getId()));
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

        if (dto.getCodVenditori() == null || dto.getCodVenditori().isEmpty()) {
            throw new WebApplicationException("Almeno un venditore è obbligatorio", 400);
        }

        if (TipoAppuntamentoEnum.APPUNTAMENTO.getDescrizione().equals(dto.getTipoEvento()) && dto.getMotivoId() == null) {
            throw new WebApplicationException("Motivo obbligatorio", 400);
        }

        validateVenditori(dto, null);

        if (TipoAppuntamentoEnum.APPUNTAMENTO.getDescrizione().equals(dto.getTipoEvento())) {
            getMotivo(dto.getMotivoId());
        }

        Appuntamento entity = appuntamentoMapper.fromDto(dto);

        entity.setCreatedAt(LocalDateTime.now());
        entity.setPromemoriaInviato(false);

        entity.persist();
        saveVenditori(entity.getId(), dto.getCodVenditori());
        syncCreateOutlook(entity);
        return getById(entity.getId());
    }

    @Transactional
    public AppuntamentoDto update(Long id, AppuntamentoDto dto) {
        Appuntamento entity = getAppuntamento(id);
        if (dto.getSedeId() == null) {
            throw new WebApplicationException("Sede obbligatoria", 400);
        }
        if (TipoAppuntamentoEnum.APPUNTAMENTO.getDescrizione().equals(dto.getTipoEvento()) && dto.getMotivoId() == null) {
            throw new WebApplicationException("Motivo obbligatorio", 400);
        }
        if (dto.getCodVenditori() == null || dto.getCodVenditori().isEmpty()) {
            throw new WebApplicationException("Almeno un venditore è obbligatorio", 400);
        }

        validateVenditori(dto, id);
        if (TipoAppuntamentoEnum.APPUNTAMENTO.getDescrizione().equals(dto.getTipoEvento())) {
            getMotivo(dto.getMotivoId());
        }
        appuntamentoMapper.updateEntity(entity, dto);
        saveVenditori(entity.getId(), dto.getCodVenditori());
        entity.setUpdatedAt(LocalDateTime.now());
        syncUpdateOutlook(entity);
        return getById(entity.getId());
    }

    @Transactional
    public void delete(Long id) {
        Appuntamento entity = getAppuntamento(id);
        syncDeleteOutlook(entity);
        AppuntamentoVenditore.delete("idAppuntamento", entity.getId());
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

    private void validateVenditori(AppuntamentoDto dto, Long idDaEscludere) {
        if (dto.getDataAppuntamento() == null || dto.getOraDa() == null
                || dto.getOraA() == null || dto.getCodVenditori() == null
                || dto.getCodVenditori().isEmpty()) {
            return;
        }

        List<Appuntamento> appuntamenti = Appuntamento.list("dataAppuntamento", dto.getDataAppuntamento());

        for (Appuntamento appuntamento : appuntamenti) {
            if (appuntamento.getId().equals(idDaEscludere)) {
                continue;
            }
            boolean overlap = dto.getOraDa().isBefore(appuntamento.getOraA()) && dto.getOraA().isAfter(appuntamento.getOraDa());
            if (!overlap) {
                continue;
            }
            List<String> venditoriEsistenti = getCodVenditori(appuntamento.getId());
            boolean conflitto =
                    dto.getCodVenditori()
                            .stream()
                            .anyMatch(venditoriEsistenti::contains);
            if (conflitto) {
                throw new WebApplicationException("Uno dei venditori selezionati ha già un appuntamento nella fascia oraria indicata", 400);
            }
        }
    }

    private String getColore(Appuntamento entity) {
        if (TipoAppuntamentoEnum.FORMAZIONE.getDescrizione().equals(entity.getTipoEvento())) {
            return "#9C27B0"; // viola
        }
        return getColoreSede(entity.getSede().getId());
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

    private OutlookEventDto buildOutlookEvent(Appuntamento entity, String motivo, List<String> venditori) {

        OutlookEventDto dto = new OutlookEventDto();

        String subject = entity.getNomeCliente();

        if (StringUtils.isNotBlank(motivo)) {
            subject += " - " + motivo;
        }

        dto.setSubject(subject);

        String location = Stream.of(
                        entity.getVia(),
                        entity.getCivico(),
                        entity.getComune())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(", "));

        dto.setLocation(location);

        StringBuilder description = new StringBuilder();

        description.append("Cliente: ")
                .append(entity.getNomeCliente());

        if (StringUtils.isNotBlank(entity.getTelefono())) {
            description.append("\n\nTelefono: ")
                    .append(entity.getTelefono());
        }

        if (venditori != null && !venditori.isEmpty()) {
            description.append("\n\nVenditori:");
            for (String venditore : venditori) {
                description.append("\n- ").append(venditore);
            }
        }

        if (StringUtils.isNotBlank(motivo)) {
            description.append("\n\nMotivo: ")
                    .append(motivo);
        }

        if (StringUtils.isNotBlank(entity.getNote())) {
            description.append("\n\nNote:\n")
                    .append(entity.getNote());
        }

        dto.setDescription(description.toString());

        dto.setStart(LocalDateTime.of(
                entity.getDataAppuntamento(),
                entity.getOraDa()));

        dto.setEnd(LocalDateTime.of(
                entity.getDataAppuntamento(),
                entity.getOraA()));

        return dto;
    }

    private void syncCreateOutlook(Appuntamento entity) {
        try {
            OutlookEventDto dto = buildOutlookDto(entity);
            String eventId = outlookCalendarService.createEvent(dto);
            entity.setOutlookEventId(eventId);
        } catch (Exception e) {
            log.error("Errore sincronizzazione Outlook CREATE appuntamento " + entity.getId(), e);
        }
    }

    private void syncUpdateOutlook(Appuntamento entity) {
        try {
            if (StringUtils.isBlank(entity.getOutlookEventId())) {
                syncCreateOutlook(entity);
                return;
            }
            OutlookEventDto dto = buildOutlookDto(entity);
            if (!outlookCalendarService.eventExists(entity.getOutlookEventId())) {
                log.info("Evento Outlook " + entity.getOutlookEventId() + " non trovato. Ricreazione automatica.");
                String newEventId = outlookCalendarService.createEvent(dto);
                entity.setOutlookEventId(newEventId);
                return;
            }
            outlookCalendarService.updateEvent(entity.getOutlookEventId(), dto);
        } catch (Exception e) {
            log.error("Errore sincronizzazione Outlook UPDATE appuntamento " + entity.getId(), e);
        }
    }

    private void syncDeleteOutlook(Appuntamento entity) {
        try {
            if (StringUtils.isBlank(entity.getOutlookEventId())) {
                return;
            }
            outlookCalendarService.deleteEvent(entity.getOutlookEventId());
        } catch (Exception e) {
            log.error("Errore sincronizzazione Outlook DELETE appuntamento " + entity.getId(), e);
        }
    }

    private OutlookEventDto buildOutlookDto(Appuntamento entity) {
        Map<String, String> venditoriMap = userService.getVenditori()
                        .stream()
                        .collect(Collectors.toMap(UserResponseDTO::getCodVenditore, UserResponseDTO::getFullname));
        List<String> nomiVenditori = getNomiVenditori(entity.getId(), venditoriMap);
        String motivo = null;
        if (entity.getIdMotivo() != null) {
            ShowroomMotivo m = ShowroomMotivo.findById(entity.getIdMotivo());
            if (m != null) {
                if (m.getParent() != null) {
                    motivo = m.getParent().getDescrizione() + " - " + m.getDescrizione();
                } else {
                    motivo = m.getDescrizione();
                }
            }
        }
        return buildOutlookEvent(entity, motivo, nomiVenditori);
    }

    private List<String> getCodVenditori(Long idAppuntamento) {
        List<AppuntamentoVenditore> list = AppuntamentoVenditore.list("idAppuntamento", idAppuntamento);
        return list.stream()
                .map(AppuntamentoVenditore::getCodVenditore)
                .toList();
    }

    private void saveVenditori(Long idAppuntamento, List<String> codVenditori) {
        AppuntamentoVenditore.delete("idAppuntamento", idAppuntamento);
        if (codVenditori == null) {
            return;
        }
        for (String codVenditore : codVenditori) {
            AppuntamentoVenditore v = new AppuntamentoVenditore();
            v.setIdAppuntamento(idAppuntamento);
            v.setCodVenditore(codVenditore);
            v.persist();
        }
    }

    private String getVenditoriLabel(Long idAppuntamento, Map<String, String> venditoriMap) {
        List<String> codVenditori = getCodVenditori(idAppuntamento);
        return codVenditori.stream()
                .map(venditoriMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    private List<String> getNomiVenditori(Long idAppuntamento, Map<String, String> venditoriMap) {

        return getCodVenditori(idAppuntamento)
                .stream()
                .map(venditoriMap::get)
                .filter(Objects::nonNull)
                .toList();
    }
}
