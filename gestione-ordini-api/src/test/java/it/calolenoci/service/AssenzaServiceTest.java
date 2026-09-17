package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import it.calolenoci.dto.AssenzaCalendarioDto;
import it.calolenoci.dto.AssenzaDto;
import it.calolenoci.dto.AssenzaGiornoDto;
import it.calolenoci.dto.UserResponseDTO;
import it.calolenoci.entity.Assenza;
import it.calolenoci.entity.AssenzaVenditore;
import it.calolenoci.exception.BusinessException;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@QuarkusTest
class AssenzaServiceTest {

    @Inject
    AssenzaService service;

    @InjectMock
    @RestClient
    UserService userService;

    @Test
    @TestTransaction
    void validaVenditoriEDateObbligatorie() {
        AssenzaDto dto = new AssenzaDto();
        dto.setDataDa(LocalDate.of(2026, 9, 8));
        dto.setDataA(LocalDate.of(2026, 9, 9));
        assertBusinessMessage("Selezionare almeno un venditore",
                assertThrows(BusinessException.class, () -> service.create(dto)));

        dto.setCodVenditori(List.of("001"));
        dto.setDataDa(null);
        assertBusinessMessage("Data da obbligatoria",
                assertThrows(BusinessException.class, () -> service.create(dto)));

        dto.setDataDa(LocalDate.of(2026, 9, 8));
        dto.setDataA(null);
        assertBusinessMessage("Data a obbligatoria",
                assertThrows(BusinessException.class, () -> service.create(dto)));
    }

    @Test
    @TestTransaction
    void creaAssenzaDiGiornataInteraConVenditoriAssociati() {
        AssenzaDto created = service.create(newAssenza(
                LocalDate.of(2026, 9, 8), LocalDate.of(2026, 9, 9), List.of("001", "002")));

        assertNotNull(created.getId());
        assertEquals("ASSENZA", created.getTipo());
        assertTrue(created.getGiornataIntera());
        assertEquals(List.of("001", "002"), created.getCodVenditori());
        assertEquals(2, AssenzaVenditore.count("idAssenza", created.getId()));
    }

    @Test
    @TestTransaction
    void aggiornaFasciaOrariaESostituisceIVenditori() {
        AssenzaDto created = service.create(newAssenza(
                LocalDate.of(2026, 9, 8), LocalDate.of(2026, 9, 8), List.of("001", "002")));
        AssenzaDto update = newAssenza(
                LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 10), List.of("003"));
        update.setOraDa(LocalTime.of(9, 0));
        update.setOraA(LocalTime.of(12, 30));
        update.setNote("Permesso mattutino");

        AssenzaDto updated = service.update(created.getId(), update);

        assertFalse(updated.getGiornataIntera());
        assertEquals(LocalTime.of(9, 0), updated.getOraDa());
        assertEquals(LocalTime.of(12, 30), updated.getOraA());
        assertEquals(List.of("003"), updated.getCodVenditori());
        assertEquals(1, AssenzaVenditore.count("idAssenza", created.getId()));
    }

    @Test
    @TestTransaction
    void aggregaIlNumeroDiAssenzePerOgniGiorno() {
        service.create(newAssenza(
                LocalDate.of(1902, 9, 8), LocalDate.of(1902, 9, 10), List.of("001")));
        service.create(newAssenza(
                LocalDate.of(1902, 9, 9), LocalDate.of(1902, 9, 9), List.of("002")));

        Map<String, Integer> result = service.getCalendario(
                        LocalDate.of(1902, 9, 8), LocalDate.of(1902, 9, 10)).stream()
                .collect(Collectors.toMap(AssenzaCalendarioDto::getData, AssenzaCalendarioDto::getCount));

        assertEquals(Map.of(
                "1902-09-08", 1,
                "1902-09-09", 2,
                "1902-09-10", 1), result);
    }

    @Test
    @TestTransaction
    void cancellaAssenzaEAssociazioniESegnalaGliIdInesistenti() {
        AssenzaDto created = service.create(newAssenza(
                LocalDate.of(2026, 9, 8), LocalDate.of(2026, 9, 8), List.of("001")));

        service.delete(created.getId());

        assertNull(Assenza.findById(created.getId()));
        assertEquals(0, AssenzaVenditore.count("idAssenza", created.getId()));
        assertBusinessMessage("Assenza non trovata",
                assertThrows(BusinessException.class, () -> service.getById(Long.MAX_VALUE)));
        assertBusinessMessage("Assenza non trovata",
                assertThrows(BusinessException.class, () -> service.delete(Long.MAX_VALUE)));
    }

    @Test
    @TestTransaction
    void restituisceLeAssenzeDelGiornoConVenditoriEFasciaOraria() {
        LocalDate data = LocalDate.of(1904, 5, 10);
        AssenzaDto richiesta = newAssenza(data, data, List.of("001", "999"));
        richiesta.setOraDa(LocalTime.of(9, 15));
        richiesta.setOraA(LocalTime.of(11, 45));
        AssenzaDto created = service.create(richiesta);
        UserResponseDTO venditore = new UserResponseDTO();
        venditore.setCodVenditore("001");
        venditore.setFullname("Mario Rossi");
        when(userService.getVenditori()).thenReturn(List.of(venditore));

        List<AssenzaGiornoDto> result = service.getByDate(data);

        AssenzaGiornoDto assenza = result.stream()
                .filter(a -> created.getId().equals(a.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("ASSENZA", assenza.getTipo());
        assertEquals("Mario Rossi", assenza.getVenditoriLabel());
        assertEquals("09:15 - 11:45", assenza.getFasciaOraria());
        assertEquals("Assenza di test", assenza.getNote());
    }

    private AssenzaDto newAssenza(LocalDate dataDa, LocalDate dataA, List<String> venditori) {
        AssenzaDto dto = new AssenzaDto();
        dto.setDataDa(dataDa);
        dto.setDataA(dataA);
        dto.setCodVenditori(venditori);
        dto.setNote("Assenza di test");
        return dto;
    }

    private void assertBusinessMessage(String expected, BusinessException exception) {
        assertEquals(expected, exception.getMessage());
    }
}
