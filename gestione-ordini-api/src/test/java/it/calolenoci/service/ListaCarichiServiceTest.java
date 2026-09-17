package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.mockito.MockitoConfig;
import it.calolenoci.dto.FiltroCarichi;
import it.calolenoci.dto.ListaCarichiDto;
import it.calolenoci.entity.ListaCarichi;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@QuarkusTest
class ListaCarichiServiceTest {

    @Inject
    ListaCarichiService service;

    @Inject
    EntityManager entityManager;

    @InjectMock
    @MockitoConfig(convertScopes = true)
    JasperService jasperService;

    @Test
    @TestTransaction
    void creaModificaERifiutaNumeriOrdineDuplicati() {
        String primoNumero = uniqueNumeroOrdine();
        ListaCarichiDto nuovo = newDto(primoNumero, LocalDate.of(2026, 9, 10));

        assertTrue(service.salvaCarico(nuovo));
        ListaCarichi created = ListaCarichi.find("numeroOrdine", primoNumero).firstResult();
        assertNotNull(created);
        assertEquals("Azienda test", created.getAzienda());
        assertEquals(125.5, created.getPeso());
        assertEquals(LocalDate.of(2026, 9, 10), created.getDataDisponibile());
        assertFalse(service.salvaCarico(newDto(primoNumero, LocalDate.of(2026, 9, 11))));

        String secondoNumero = uniqueNumeroOrdine();
        ListaCarichi altro = persistCarico(secondoNumero, LocalDate.of(2026, 9, 12), null, null);
        nuovo.setId(created.getId());
        nuovo.setAzienda("Azienda aggiornata");
        nuovo.setPeso(250.0);
        assertTrue(service.salvaCarico(nuovo));
        entityManager.clear();
        ListaCarichi updated = ListaCarichi.findById(created.getId());
        assertEquals("Azienda aggiornata", updated.getAzienda());
        assertEquals(250.0, updated.getPeso());

        nuovo.setNumeroOrdine(altro.getNumeroOrdine());
        assertFalse(service.salvaCarico(nuovo));
    }

    @Test
    @TestTransaction
    void ricercaCarichiPerStatoEDataERecuperaIlDettaglio() {
        LocalDate data = LocalDate.of(1904, 9, 9);
        ListaCarichi nonInviato = persistCarico(uniqueNumeroOrdine(), data, null, null);
        persistCarico(uniqueNumeroOrdine(), data.plusDays(1), null, null);
        ListaCarichi inviato = persistCarico(uniqueNumeroOrdine(), data, data.plusDays(2), 7L);

        FiltroCarichi filtro = new FiltroCarichi();
        filtro.setInviato("0");
        filtro.setDataDisponibile(data);
        List<ListaCarichiDto> nonInviati = service.findCarichi(filtro);
        assertEquals(List.of(nonInviato.getId()), idsCreati(nonInviati, nonInviato.getId(), inviato.getId()));

        filtro.setInviato("1");
        List<ListaCarichiDto> inviati = service.findCarichi(filtro);
        assertEquals(List.of(inviato.getId()), idsCreati(inviati, nonInviato.getId(), inviato.getId()));

        ListaCarichiDto dettaglio = service.getCarico(nonInviato.getId());
        assertEquals(nonInviato.getNumeroOrdine(), dettaglio.getNumeroOrdine());
        assertEquals(data, dettaglio.getDataDisponibile());
        assertThrows(NoSuchElementException.class, () -> service.getCarico(Long.MAX_VALUE));
    }

    @Test
    @TestTransaction
    void ricercaConvalideECarichiInviatiConFiltri() {
        LocalDate dataConvalida = LocalDate.of(1904, 9, 10);
        String marker = "AZ-" + UUID.randomUUID().toString().substring(0, 8);
        ListaCarichi primo = persistCarico(marker + "-ORD-1", dataConvalida, dataConvalida, 3L);
        primo.setAzienda(marker);
        ListaCarichi secondo = persistCarico(marker + "-ORD-2", dataConvalida, dataConvalida, 3L);
        secondo.setAzienda(marker);
        persistCarico(uniqueNumeroOrdine(), dataConvalida, dataConvalida, 4L);

        FiltroCarichi filtro = new FiltroCarichi();
        filtro.setDataConvalida(dataConvalida);
        filtro.setNumeroConvalida(3L);
        filtro.setFornitore(marker);
        filtro.setNumeroOrdine("ORD");

        assertEquals(2, service.findCarichiInviati(filtro).stream()
                .filter(dto -> dto.getId().equals(primo.getId()) || dto.getId().equals(secondo.getId())).count());
        List<ListaCarichiDto> convalide = service.findConvalide(filtro);
        assertEquals(1, convalide.size());
        assertEquals(dataConvalida, convalide.getFirst().getDataConvalida());
        assertEquals(3L, convalide.getFirst().getNumeroConvalida());
    }

    @Test
    @TestTransaction
    void convalidaICarichiConProgressivoGiornalieroECreaIlReport() throws Exception {
        ListaCarichi primo = persistCarico(uniqueNumeroOrdine(), LocalDate.now(), null, null);
        ListaCarichi secondo = persistCarico(uniqueNumeroOrdine(), LocalDate.now(), null, null);
        Number currentMax = ListaCarichi.find(
                "select COALESCE(MAX(numeroConvalida), 0) from ListaCarichi where dataConvalida = :data",
                Parameters.with("data", LocalDate.now())).project(Number.class).firstResult();
        long expectedProgressivo = currentMax.longValue() + 1;
        List<ListaCarichiDto> input = List.of(dtoWithId(primo), dtoWithId(secondo));

        String filename = service.creaReport(input);

        assertEquals(LocalDate.now() + "_" + expectedProgressivo + ".pdf", filename);
        entityManager.clear();
        assertConvalida(primo.getId(), expectedProgressivo);
        assertConvalida(secondo.getId(), expectedProgressivo);
        verify(jasperService).createReport(eq(input), eq(filename));
    }

    private ListaCarichiDto newDto(String numeroOrdine, LocalDate dataDisponibile) {
        ListaCarichiDto dto = new ListaCarichiDto();
        dto.setAzienda("Azienda test");
        dto.setNumeroOrdine(numeroOrdine);
        dto.setDataDisponibile(dataDisponibile);
        dto.setPeso(125.5);
        return dto;
    }

    private ListaCarichi persistCarico(String numeroOrdine, LocalDate dataDisponibile,
                                       LocalDate dataConvalida, Long numeroConvalida) {
        ListaCarichi entity = new ListaCarichi();
        entity.setId(uniqueId());
        entity.setAzienda("Azienda test");
        entity.setNumeroOrdine(numeroOrdine);
        entity.setDataDisponibile(dataDisponibile);
        entity.setPeso(10.0);
        entity.setDataConvalida(dataConvalida);
        entity.setNumeroConvalida(numeroConvalida);
        entity.persist();
        return entity;
    }

    private ListaCarichiDto dtoWithId(ListaCarichi entity) {
        ListaCarichiDto dto = new ListaCarichiDto();
        dto.setId(entity.getId());
        dto.setAzienda(entity.getAzienda());
        dto.setNumeroOrdine(entity.getNumeroOrdine());
        return dto;
    }

    private void assertConvalida(Long id, long progressivo) {
        ListaCarichi entity = ListaCarichi.findById(id);
        assertEquals(LocalDate.now(), entity.getDataConvalida());
        assertEquals(progressivo, entity.getNumeroConvalida());
    }

    private List<Long> idsCreati(List<ListaCarichiDto> result, Long... ids) {
        List<Long> expectedIds = List.of(ids);
        return result.stream().map(ListaCarichiDto::getId).filter(expectedIds::contains).toList();
    }

    private long uniqueId() {
        Number max = ListaCarichi.find("select COALESCE(MAX(id), 0) from ListaCarichi")
                .project(Number.class).firstResult();
        return max.longValue() + 1;
    }

    private String uniqueNumeroOrdine() {
        return "TEST-" + UUID.randomUUID();
    }
}
