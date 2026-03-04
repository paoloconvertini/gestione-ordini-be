package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.identity.SecurityIdentity;
import it.calolenoci.common.entity.Comune;
import it.calolenoci.common.service.ComuneService;
import it.calolenoci.dto.*;
import it.calolenoci.entity.PianoConti;
import it.calolenoci.entity.Sede;
import it.calolenoci.entity.ShowroomMotivo;
import it.calolenoci.entity.ShowroomVisit;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static it.calolenoci.enums.Ruolo.*;

@ApplicationScoped
public class ShowroomService {

    @Inject
    @RestClient
    UserService userService;

    @Inject
    ComuneService comuneService;

    @Inject
    SecurityIdentity securityIdentity;

    public PageShowroomDto search(FiltroShowroom filtro) {

        boolean admin = isAdmin();
        String receptionSedeCodice = getReceptionSedeCodice();

        PageShowroomDto result = new PageShowroomDto();
        Map<String, Object> params = new HashMap<>();

        StringBuilder query = new StringBuilder(
                "FROM ShowroomVisit s WHERE s.isDeleted = false "
        );

        Sede sedeCorrente = null;

        // =========================================
        // 🔐 GESTIONE SEDE
        // =========================================

        if (!admin) {

            if (receptionSedeCodice == null) {
                throw new WebApplicationException("Sede non autorizzata", 403);
            }

            sedeCorrente = Sede.find("codice", receptionSedeCodice).firstResult();

            query.append(" AND s.sede.id = :sedeId ");
            params.put("sedeId", sedeCorrente.getId());

        } else {

            if (filtro.getSedeId() != null) {
                sedeCorrente = Sede.findById(filtro.getSedeId());
                query.append(" AND s.sede.id = :sedeId ");
                params.put("sedeId", filtro.getSedeId());
            }
        }

        // =========================================
        // 📌 FILTRI
        // =========================================

        if (filtro.getDataDa() != null) {
            query.append(" AND s.dataVisita >= :dataDa ");
            params.put("dataDa", filtro.getDataDa());
        }

        if (filtro.getDataA() != null) {
            query.append(" AND s.dataVisita <= :dataA ");
            params.put("dataA", filtro.getDataA());
        }

        if (filtro.getNomeCliente() != null && !filtro.getNomeCliente().isBlank()) {
            query.append(" AND UPPER(s.nomeCliente) LIKE :cliente ");
            params.put("cliente",
                    "%" + filtro.getNomeCliente().toUpperCase() + "%");
        }

        if (filtro.getComuneIstat() != null && !filtro.getComuneIstat().isBlank()) {

            query.append(" AND s.comuneIstat = :comune ");
            params.put("comune", filtro.getComuneIstat());

        } else if (filtro.getProvincia() != null && !filtro.getProvincia().isBlank()) {

            List<String> comuniProvincia =
                    comuneService.findCodiciByProvincia(filtro.getProvincia());

            if (comuniProvincia.isEmpty()) {
                result.setCount(0);
                result.setList(List.of());
                return result;
            }

            query.append(" AND s.comuneIstat in :comuni ");
            params.put("comuni", comuniProvincia);
        }

        // =========================================
        // 🔎 QUERY
        // =========================================

        PanacheQuery<ShowroomVisit> panacheQuery =
                ShowroomVisit.find(query.toString(),
                        Sort.descending("dataVisita"),
                        params);

        long count = panacheQuery.count();

        List<ShowroomVisit> entities = panacheQuery
                .page(Page.of(filtro.getPage(), filtro.getSize()))
                .list();

        // =========================================
        // 🔹 Lookup comuni
        // =========================================

        Set<String> codiciComuni = entities.stream()
                .map(ShowroomVisit::getComuneIstat)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, Comune> comuniMap =
                comuneService.findByCodici(codiciComuni)
                        .stream()
                        .collect(Collectors.toMap(
                                Comune::getCodiceIstat,
                                c -> c
                        ));

        // =========================================
        // 🔹 Lookup venditori
        // =========================================

        Map<String, String> venditoriMap =
                userService.getVenditori()
                        .stream()
                        .collect(Collectors.toMap(
                                UserResponseDTO::getCodVenditore,
                                UserResponseDTO::getFullname
                        ));

        // =========================================
        // 🔹 Mapping DTO
        // =========================================

        List<ShowroomVisitDto> dtoList = new ArrayList<>();

        for (ShowroomVisit s : entities) {

            ShowroomVisitDto dto = toDto(s, venditoriMap);

            Comune comune = comuniMap.get(s.getComuneIstat());
            if (comune != null) {
                dto.setComuneNome(comune.getNomeComune());
                dto.setProvinciaSigla(comune.getSiglaProvincia());
            }

            dtoList.add(dto);
        }

        result.setCount(count);
        result.setList(dtoList);

        // =========================================
        // 🏷️ Sede corrente per il FE
        // =========================================

        if (admin) {

            if (sedeCorrente != null) {
                result.setSedeCorrenteDescrizione(
                        sedeCorrente.getDescrizione()
                );
            } else {
                result.setSedeCorrenteDescrizione("Tutte le sedi");
            }

        } else if (sedeCorrente != null) {

            result.setSedeCorrenteDescrizione(
                    sedeCorrente.getDescrizione()
            );
        }

        return result;
    }

    @Transactional
    public ShowroomVisitDto create(ShowroomVisitDto dto) {

        if (dto == null) {
            throw new WebApplicationException("Dati mancanti", 400);
        }

        if (dto.getNomeCliente() == null || dto.getNomeCliente().isBlank()) {
            throw new WebApplicationException("Nome cliente obbligatorio", 400);
        }

        if (dto.getMotivoId() == null) {
            throw new WebApplicationException("Motivo obbligatorio", 400);
        }

        if (dto.getVenditoreCodice() == null || dto.getVenditoreCodice().isBlank()) {
            throw new WebApplicationException("Venditore obbligatorio", 400);
        }

        ShowroomMotivo motivo = getShowroomMotivo(dto);

        boolean admin = isAdmin();
        String receptionSedeCodice = getReceptionSedeCodice();
        Sede sede;
        if (admin) {
            if (dto.getSedeId() == null) {
                throw new WebApplicationException("Sede obbligatoria per admin", 400);
            }
            sede = Sede.findById(dto.getSedeId());
            if (sede == null) {
                throw new WebApplicationException("Sede non valida", 400);
            }
        } else {
            if (receptionSedeCodice == null) {
                throw new WebApplicationException("Sede non autorizzata", 403);
            }
            sede = Sede.find("codice", receptionSedeCodice).firstResult();
        }
        ShowroomVisit entity = ShowroomVisit.builder()
                .nomeCliente(dto.getNomeCliente())
                .comuneIstat(dto.getComuneIstat())
                .telefono(dto.getTelefono())
                .motivo(motivo)
                .venditoreCodice(dto.getVenditoreCodice())
                .dataVisita(LocalDateTime.now())
                .sede(sede)
                .build();

        entity.persist();

        dto.setId(entity.getId());
        dto.setDataVisita(entity.getDataVisita());

        return dto;
    }

    @Nonnull
    private ShowroomMotivo getShowroomMotivo(ShowroomVisitDto dto) {
        ShowroomMotivo motivo = ShowroomMotivo.findById(dto.getMotivoId());

        if (motivo == null) {
            throw new WebApplicationException("Motivo non valido", 400);
        }

        if (!Boolean.TRUE.equals(motivo.getAttivo())) {
            throw new WebApplicationException("Motivo non attivo", 400);
        }
        return motivo;
    }

    @Transactional
    public ShowroomVisitDto update(Long id, ShowroomVisitDto dto) {

        ShowroomVisit entity = getShowroomVisit(id);

        checkSedeAccess(entity);

        if (dto.getNomeCliente() == null || dto.getNomeCliente().isBlank()) {
            throw new WebApplicationException("Nome cliente obbligatorio", 400);
        }

        if (dto.getMotivoId() == null) {
            throw new WebApplicationException("Motivo obbligatorio", 400);
        }

        ShowroomMotivo motivo = getShowroomMotivo(dto);

        entity.setNomeCliente(dto.getNomeCliente());
        entity.setComuneIstat(dto.getComuneIstat());
        entity.setTelefono(dto.getTelefono());
        entity.setMotivo(motivo);
        entity.setVenditoreCodice(dto.getVenditoreCodice());

        if (dto.getDataVisita() != null) {
            entity.setDataVisita(dto.getDataVisita());
        }

        // 👑 Solo admin può cambiare sede
        if (isAdmin() && dto.getSedeId() != null) {

            Sede nuovaSede = Sede.findById(dto.getSedeId());

            if (nuovaSede == null) {
                throw new WebApplicationException("Sede non valida", 400);
            }

            entity.setSede(nuovaSede);
        }

        return dto;
    }

    @Nonnull
    private ShowroomVisit getShowroomVisit(Long id) {
        ShowroomVisit entity = ShowroomVisit.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Visita non trovata", 404);
        }
        if (entity.getIsDeleted()) {
            throw new WebApplicationException("Visita eliminata", 400);
        }
        return entity;
    }

    @Transactional
    public void delete(Long id) {
        ShowroomVisit entity = getShowroomVisit(id);
        checkSedeAccess(entity);
        entity.setIsDeleted(true);
    }

    // 🔹 Metodo mapper (lo creiamo noi qui)
    private ShowroomVisitDto toDto(ShowroomVisit s, Map<String, String> venditoriMap) {

        ShowroomVisitDto dto = new ShowroomVisitDto();

        dto.setId(s.getId());
        dto.setNomeCliente(s.getNomeCliente());
        dto.setCodiceCliente(s.getCodiceCliente());
        dto.setComuneIstat(s.getComuneIstat());
        dto.setTelefono(s.getTelefono());
        dto.setVenditoreCodice(s.getVenditoreCodice());
        dto.setDataVisita(s.getDataVisita());

        if (s.getMotivo() != null) {

            ShowroomMotivo motivo = s.getMotivo();

            dto.setMotivoId(motivo.getId());
            dto.setMotivoDescrizione(motivo.getDescrizione());

            if (motivo.getParent() != null) {
                dto.setMotivoParentId(motivo.getParent().getId());
                dto.setMotivoParentDescrizione(
                        motivo.getParent().getDescrizione()
                );
            }
        }

        dto.setVenditoreNome(
                venditoriMap.get(s.getVenditoreCodice())
        );
        dto.setSedeId(s.getSede().getId());
        dto.setSedeDescrizione(s.getSede().getDescrizione());

        return dto;
    }

    public List<ShowroomMotivoDto> getMotiviRoot() {

        List<ShowroomMotivo> motivi =
                ShowroomMotivo.find("parent is null and attivo = true ORDER BY descrizione")
                        .list();

        return motivi.stream()
                .map(m -> ShowroomMotivoDto.builder()
                        .id(m.getId())  // oppure m.getId() se hai getter
                        .descrizione(m.getDescrizione())
                        .build())
                .toList();
    }

    public List<ShowroomMotivoDto> getFigli(Long parentId) {

        List<ShowroomMotivo> figli =
                ShowroomMotivo.find(
                        "parent.id = ?1 and attivo = true ORDER BY descrizione",
                        parentId
                ).list();
        return figli.stream()
                .map(m -> ShowroomMotivoDto.builder()
                        .id(m.getId())  // oppure m.getId() se hai getter
                        .descrizione(m.getDescrizione())
                        .build())
                .toList();
    }

    public ShowroomMotivoDto getMotivoById(Long id) {

        ShowroomMotivo m = ShowroomMotivo.findById(id);

        if (m == null) {
            throw new WebApplicationException("Motivo non trovato", 404);
        }

        return ShowroomMotivoDto.builder()
                .id(m.getId())
                .descrizione(m.getDescrizione())
                .parentId(m.getParent() != null ? m.getParent().getId() : null)
                .build();
    }

    public List<ClienteLightDto> searchClienti(String q) {

        if (q == null || q.trim().length() < 3) {
            return List.of();
        }

        String search = "%" + q.toUpperCase() + "%";

        List<PianoConti> clienti = PianoConti.find(
                        " cliFor = ?1 and (upper(sottoConto) like ?2 or upper(intestazione) like ?2)",
                        "C",
                        search
                ).page(Page.ofSize(20))
                .list();

        return clienti.stream()
                .map(c -> ClienteLightDto.builder()
                        .codiceCliente(c.getSottoConto())
                        .nome(c.getIntestazione())
                        .build())
                .toList();
    }

    @Transactional
    public ShowroomMotivoDto createMotivo(ShowroomMotivoDto dto) {

        if (!isAdmin()) {
            throw new WebApplicationException("Operazione non autorizzata", 403);
        }

        if (dto.getDescrizione() == null || dto.getDescrizione().isBlank()) {
            throw new WebApplicationException("Descrizione obbligatoria", 400);
        }

        ShowroomMotivo parent = null;

        if (dto.getParentId() != null) {
            parent = ShowroomMotivo.findById(dto.getParentId());

            if (parent == null) {
                throw new WebApplicationException("Parent non valido", 400);
            }
        }

        ShowroomMotivo motivo = new ShowroomMotivo();
        motivo.setDescrizione(dto.getDescrizione());
        motivo.setParent(parent);
        motivo.setAttivo(true);

        motivo.persist();

        return ShowroomMotivoDto.builder()
                .id(motivo.getId())
                .descrizione(motivo.getDescrizione())
                .parentId(parent != null ? parent.getId() : null)
                .attivo(true)
                .build();
    }

    @Transactional
    public ShowroomMotivoDto updateMotivo(Long id, ShowroomMotivoDto dto) {

        if (!isAdmin()) {
            throw new WebApplicationException("Operazione non autorizzata", 403);
        }

        ShowroomMotivo motivo = ShowroomMotivo.findById(id);

        if (motivo == null) {
            throw new WebApplicationException("Motivo non trovato", 404);
        }

        if (dto.getDescrizione() == null || dto.getDescrizione().isBlank()) {
            throw new WebApplicationException("Descrizione obbligatoria", 400);
        }

        motivo.setDescrizione(dto.getDescrizione());

        return ShowroomMotivoDto.builder()
                .id(motivo.getId())
                .descrizione(motivo.getDescrizione())
                .parentId(
                        motivo.getParent() != null ? motivo.getParent().getId() : null
                )
                .attivo(motivo.getAttivo())
                .build();
    }

    @Transactional
    public void disattivaMotivo(Long id) {

        if (!isAdmin()) {
            throw new WebApplicationException("Operazione non autorizzata", 403);
        }

        ShowroomMotivo motivo = ShowroomMotivo.findById(id);

        if (motivo == null) {
            throw new WebApplicationException("Motivo non trovato", 404);
        }

        motivo.setAttivo(false);
    }

    @Transactional
    public void associaCliente(Long visitaId, String codiceCliente) {

        ShowroomVisit visita = ShowroomVisit.findById(visitaId);

        if (visita == null) {
            throw new WebApplicationException("Visita non trovata", 404);
        }

        PianoConti cliente = PianoConti.find(
                "cliFor = ?1 and sottoConto = ?2",
                "C",
                codiceCliente
        ).firstResult();

        if (cliente == null) {
            throw new WebApplicationException("Cliente non valido", 400);
        }

        visita.setCodiceCliente(cliente.getSottoConto());
        visita.setNomeCliente(cliente.getIntestazione());
    }

    private boolean isAdmin() {
        return securityIdentity.hasRole(ADMIN );
    }

    private String getReceptionSedeCodice() {
        if (securityIdentity.hasRole(RECEPTION_CEGLIE)) {
            return "CEGLIE";
        }
        if (securityIdentity.hasRole(RECEPTION_OSTUNI)) {
            return "OSTUNI";
        }
        return null;
    }

    private void checkSedeAccess(ShowroomVisit entity) {
        if (isAdmin()) {
            return;
        }
        String receptionSedeCodice = getReceptionSedeCodice();
        if (receptionSedeCodice == null) {
            throw new WebApplicationException("Sede non autorizzata", 403);
        }
        if (!entity.getSede().getCodice().equals(receptionSedeCodice)) {
            throw new WebApplicationException("Operazione non consentita su questa sede", 403);
        }
    }
}