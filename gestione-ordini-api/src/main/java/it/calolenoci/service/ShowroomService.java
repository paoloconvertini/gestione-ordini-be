package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import it.calolenoci.common.entity.Comune;
import it.calolenoci.common.service.ComuneService;
import it.calolenoci.dto.*;
import it.calolenoci.entity.ShowroomMotivo;
import it.calolenoci.entity.ShowroomVisit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ShowroomService {

    @Inject
    @RestClient
    UserService userService;

    @Inject
    ComuneService comuneService;

    public PageShowroomDto search(FiltroShowroom filtro) {

        PageShowroomDto result = new PageShowroomDto();

        String query = "FROM ShowroomVisit s WHERE s.isDeleted = false ";
        Map<String, Object> params = new HashMap<>();

        if (filtro.getDataDa() != null) {
            query += " AND s.dataVisita >= :dataDa ";
            params.put("dataDa", filtro.getDataDa());
        }

        if (filtro.getDataA() != null) {
            query += " AND s.dataVisita <= :dataA ";
            params.put("dataA", filtro.getDataA());
        }

        if (filtro.getNomeCliente() != null && !filtro.getNomeCliente().isBlank()) {
            query += " AND UPPER(s.nomeCliente) LIKE :cliente ";
            params.put("cliente", "%" + filtro.getNomeCliente().toUpperCase() + "%");
        }

        if (filtro.getProvincia() != null && !filtro.getProvincia().isBlank()) {
            query += " AND s.provincia = :prov ";
            params.put("prov", filtro.getProvincia());
        }

        if (filtro.getComuneIstat() != null && !filtro.getComuneIstat().isBlank()) {
            query += " AND s.comuneIstat = :comune ";
            params.put("comune", filtro.getComuneIstat());
        }

        PanacheQuery<ShowroomVisit> panacheQuery =
                ShowroomVisit.find(query, Sort.descending("dataVisita"), params);

        long count = panacheQuery.count();

        List<ShowroomVisit> entities = panacheQuery
                .page(Page.of(filtro.getPage(), filtro.getSize()))
                .list();

        // 🔹 Lookup comuni
        Set<String> codiciComuni = entities.stream()
                .map(ShowroomVisit::getComuneIstat)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Comune> comuni = comuneService.findByCodici(codiciComuni);

        Map<String, Comune> comuniMap = comuni.stream()
                .collect(Collectors.toMap(
                        Comune::getCodiceIstat,
                        c -> c
                ));

        // 🔹 Lookup venditori
        List<UserResponseDTO> venditoriList = userService.getVenditori();

        Map<String, String> venditoriMap = venditoriList.stream()
                .collect(Collectors.toMap(
                        UserResponseDTO::getCodVenditore,
                        UserResponseDTO::getFullname
                ));

        // 🔹 Mapping DTO
        List<ShowroomVisitDto> dtoList = new ArrayList<>();

        for (ShowroomVisit s : entities) {

            ShowroomVisitDto dto = new ShowroomVisitDto();

            dto.setId(s.getId());
            dto.setNomeCliente(s.getNomeCliente());
            dto.setComuneIstat(s.getComuneIstat());
            dto.setTelefono(s.getTelefono());
            dto.setVenditoreCodice(s.getVenditoreCodice());
            dto.setDataVisita(s.getDataVisita());

            if (s.getMotivo() != null) {
                dto.setMotivoId(s.getMotivo().getId());
                dto.setMotivoDescrizione(s.getMotivo().getDescrizione());
            }

            // Comune
            Comune comune = comuniMap.get(s.getComuneIstat());
            if (comune != null) {
                dto.setComuneNome(comune.getNomeComune());
                dto.setProvinciaSigla(comune.getSiglaProvincia());
            }

            // Venditore
            dto.setVenditoreNome(
                    venditoriMap.get(s.getVenditoreCodice())
            );

            dtoList.add(dto);
        }

        result.setCount(count);
        result.setList(dtoList);

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

        ShowroomMotivo motivo = ShowroomMotivo.findById(dto.getMotivoId());

        if (motivo == null) {
            throw new WebApplicationException("Motivo non valido", 400);
        }

        ShowroomVisit entity = ShowroomVisit.builder()
                .nomeCliente(dto.getNomeCliente())
                .comuneIstat(dto.getComuneIstat())
                .telefono(dto.getTelefono())
                .motivo(motivo)
                .venditoreCodice(dto.getVenditoreCodice())
                .dataVisita(dto.getDataVisita())
                .build();

        entity.persist();

        dto.setId(entity.getId());
        dto.setDataVisita(entity.getDataVisita());

        return dto;
    }

    @Transactional
    public ShowroomVisitDto update(Long id, ShowroomVisitDto dto) {

        ShowroomVisit entity = ShowroomVisit.findById(id);

        if (entity == null) {
            throw new WebApplicationException("Visita non trovata", 404);
        }

        if (entity.getIsDeleted()) {
            throw new WebApplicationException("Visita eliminata", 400);
        }

        if (dto.getNomeCliente() == null || dto.getNomeCliente().isBlank()) {
            throw new WebApplicationException("Nome cliente obbligatorio", 400);
        }

        if (dto.getMotivoId() == null) {
            throw new WebApplicationException("Motivo obbligatorio", 400);
        }

        ShowroomMotivo motivo = ShowroomMotivo.findById(dto.getMotivoId());

        if (motivo == null) {
            throw new WebApplicationException("Motivo non valido", 400);
        }

        entity.setNomeCliente(dto.getNomeCliente());
        entity.setComuneIstat(dto.getComuneIstat());
        entity.setTelefono(dto.getTelefono());
        entity.setMotivo(motivo);
        entity.setVenditoreCodice(dto.getVenditoreCodice());

        if (dto.getDataVisita() != null) {
            entity.setDataVisita(dto.getDataVisita());
        }

        return dto;
    }

    @Transactional
    public void delete(Long id) {

        ShowroomVisit entity = ShowroomVisit.findById(id);

        if (entity == null) {
            throw new WebApplicationException("Visita non trovata", 404);
        }

        entity.setIsDeleted(true);
    }

    // 🔹 Metodo mapper (lo creiamo noi qui)
    private ShowroomVisitDto toDto(ShowroomVisit s,
                                   Map<String, String> venditoriMap) {

        ShowroomVisitDto dto = new ShowroomVisitDto();

        dto.setId(s.getId());
        dto.setNomeCliente(s.getNomeCliente());
        dto.setComuneIstat(s.getComuneIstat());
        dto.setTelefono(s.getTelefono());
        dto.setVenditoreCodice(s.getVenditoreCodice());
        dto.setDataVisita(s.getDataVisita());

        if (s.getMotivo() != null) {
            dto.setMotivoId(s.getMotivo().getId());
            dto.setMotivoDescrizione(s.getMotivo().getDescrizione());
        }

        dto.setVenditoreNome(
                venditoriMap.get(s.getVenditoreCodice())
        );

        return dto;
    }
}