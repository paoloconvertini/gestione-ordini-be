package it.calolenoci.service;

import it.calolenoci.dto.AssenzaCalendarioDto;
import it.calolenoci.dto.AssenzaDto;
import it.calolenoci.dto.AssenzaGiornoDto;
import it.calolenoci.dto.UserResponseDTO;
import it.calolenoci.entity.Assenza;
import it.calolenoci.entity.AssenzaVenditore;
import it.calolenoci.exception.BusinessException;
import it.calolenoci.mapper.AssenzaMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class AssenzaService {

    @Inject
    AssenzaMapper mapper;

    @Inject
    @RestClient
    UserService userService;

    @Transactional
    public AssenzaDto create(AssenzaDto dto) {
        validate(dto);
        Assenza entity = mapper.fromDto(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.persist();
        saveVenditori(entity.getId(), dto.getCodVenditori());
        return getById(entity.getId());
    }

    @Transactional
    public AssenzaDto update(Long id, AssenzaDto dto) {
        validate(dto);
        Assenza entity = getAssenza(id);
        mapper.updateEntity(entity, dto);
        entity.setUpdatedAt(LocalDateTime.now());
        saveVenditori(entity.getId(), dto.getCodVenditori());
        return getById(id);
    }

    @Transactional
    public void delete(Long id) {
        AssenzaVenditore.delete("idAssenza", id);
        getAssenza(id).delete();
    }

    public List<AssenzaCalendarioDto> getCalendario(LocalDate dataDa, LocalDate dataA) {
        List<Assenza> assenze = Assenza.list("dataDa <= ?1 and dataA >= ?2", dataA, dataDa);
        Map<LocalDate, Integer> giorni = new HashMap<>();
        for (Assenza assenza : assenze) {
            LocalDate giorno = assenza.getDataDa();
            while (!giorno.isAfter(assenza.getDataA())) {
                giorni.merge(giorno, 1, Integer::sum);
                giorno = giorno.plusDays(1);
            }
        }
        List<AssenzaCalendarioDto> result = new ArrayList<>();
        for (Map.Entry<LocalDate, Integer> entry : giorni.entrySet()) {
            AssenzaCalendarioDto dto = new AssenzaCalendarioDto();
            dto.setData(entry.getKey().toString());
            dto.setCount(entry.getValue());
            result.add(dto);
        }
        return result;
    }

    public AssenzaDto getById(Long id) {
        Assenza entity = getAssenza(id);
        AssenzaDto dto = mapper.toDto(entity);
        dto.setCodVenditori(getCodVenditori(id));
        dto.setGiornataIntera(entity.getOraDa() == null && entity.getOraA() == null);
        return dto;
    }

    private void validate(AssenzaDto dto) {
        dto.setTipo("ASSENZA");
        if (dto.getCodVenditori() == null || dto.getCodVenditori().isEmpty()) {
            throw new BusinessException("Selezionare almeno un venditore");
        }
        if (dto.getDataDa() == null) {
            throw new BusinessException("Data da obbligatoria");
        }
        if (dto.getDataA() == null) {
            throw new BusinessException("Data a obbligatoria");
        }
    }

    private Assenza getAssenza(Long id) {
        Assenza entity = Assenza.findById(id);
        if (entity == null) {
            throw new BusinessException("Assenza non trovata");
        }
        return entity;
    }

    private List<String> getCodVenditori(Long idAssenza) {
        List<AssenzaVenditore> list = AssenzaVenditore.list("idAssenza", idAssenza);
        return list.stream().map(AssenzaVenditore::getCodVenditore).toList();
    }

    private void saveVenditori(Long idAssenza, List<String> codVenditori) {
        AssenzaVenditore.delete("idAssenza", idAssenza);
        for (String codVenditore : codVenditori) {
            AssenzaVenditore v = new AssenzaVenditore();
            v.setIdAssenza(idAssenza);
            v.setCodVenditore(codVenditore);
            v.persist();
        }
    }

    public List<AssenzaGiornoDto> getByDate(LocalDate data) {
        List<Assenza> assenze = Assenza.list("dataDa <= ?1 and dataA >= ?1", data);
        Map<String, String> venditoriMap = userService.getVenditori()
                        .stream()
                        .collect(Collectors.toMap(
                                UserResponseDTO::getCodVenditore,
                                UserResponseDTO::getFullname));
        List<AssenzaGiornoDto> result = new ArrayList<>();
        for (Assenza assenza : assenze) {
            AssenzaGiornoDto dto = new AssenzaGiornoDto();
            dto.setId(assenza.getId());
            dto.setTipo(assenza.getTipo());
            dto.setVenditoriLabel(getVenditoriLabel(assenza.getId(), venditoriMap));
            if (assenza.getOraDa() != null && assenza.getOraA() != null) {
                dto.setFasciaOraria(assenza.getOraDa() + " - " + assenza.getOraA());
            }
            dto.setNote(assenza.getNote());
            result.add(dto);
        }
        return result;
    }

    private String getVenditoriLabel(Long idAssenza, Map<String, String> venditoriMap) {
        return getCodVenditori(idAssenza)
                .stream()
                .map(venditoriMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }
}
