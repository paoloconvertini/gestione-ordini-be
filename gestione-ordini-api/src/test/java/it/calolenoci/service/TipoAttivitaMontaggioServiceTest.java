package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.TipoAttivitaMontaggioDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class TipoAttivitaMontaggioServiceTest {

    @Inject
    TipoAttivitaMontaggioService service;

    @Test
    @TestTransaction
    void validaIDatiObbligatori() {
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.create(null)));

        TipoAttivitaMontaggioDto dto = new TipoAttivitaMontaggioDto();
        dto.setDescrizione(" ");
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.create(dto)));
    }

    @Test
    @TestTransaction
    void creaERecuperaSoloTipiAttiviInOrdine() {
        String prefix = "TEST-" + UUID.randomUUID() + "-";
        TipoAttivitaMontaggioDto secondo = service.create(newDto(prefix + "secondo", 20, true));
        TipoAttivitaMontaggioDto primo = service.create(newDto(prefix + "primo", 10, null));
        service.create(newDto(prefix + "inattivo", 5, false));

        assertNotNull(primo.getId());
        assertTrue(primo.getAttivo());
        assertEquals(prefix + "primo", service.getById(primo.getId()).getDescrizione());

        List<TipoAttivitaMontaggioDto> created = service.getTipiAttivita().stream()
                .filter(dto -> dto.getDescrizione().startsWith(prefix))
                .toList();
        assertEquals(List.of(primo.getId(), secondo.getId()),
                created.stream().map(TipoAttivitaMontaggioDto::getId).toList());
    }

    @Test
    @TestTransaction
    void aggiornaEDisattivaUnTipo() {
        TipoAttivitaMontaggioDto created = service.create(newDto("TEST-" + UUID.randomUUID(), 10, true));
        TipoAttivitaMontaggioDto update = newDto("Descrizione aggiornata", 30, true);

        TipoAttivitaMontaggioDto updated = service.update(created.getId(), update);

        assertEquals("Descrizione aggiornata", updated.getDescrizione());
        assertEquals(30, updated.getOrdineVisualizzazione());

        service.disattiva(created.getId());
        assertFalse(service.getById(created.getId()).getAttivo());
    }

    @Test
    @TestTransaction
    void segnalaGliIdentificativiInesistenti() {
        assertStatus(404, assertThrows(WebApplicationException.class, () -> service.getById(Long.MAX_VALUE)));
        assertStatus(404, assertThrows(WebApplicationException.class,
                () -> service.update(Long.MAX_VALUE, newDto("Test", 1, true))));
        assertStatus(404, assertThrows(WebApplicationException.class, () -> service.disattiva(Long.MAX_VALUE)));
    }

    @Test
    @TestTransaction
    void richiedeDescrizioneAncheInAggiornamento() {
        TipoAttivitaMontaggioDto dto = newDto("Tipo aggiornamento test", 1, true);
        TipoAttivitaMontaggioDto created = service.create(dto);
        dto.setDescrizione(" ");

        assertStatus(400, assertThrows(WebApplicationException.class,
                () -> service.update(created.getId(), dto)));
    }

    private TipoAttivitaMontaggioDto newDto(String descrizione, int ordine, Boolean attivo) {
        TipoAttivitaMontaggioDto dto = new TipoAttivitaMontaggioDto();
        dto.setDescrizione(descrizione);
        dto.setOrdineVisualizzazione(ordine);
        dto.setAttivo(attivo);
        return dto;
    }

    private void assertStatus(int expected, WebApplicationException exception) {
        assertEquals(expected, exception.getResponse().getStatus());
    }
}
