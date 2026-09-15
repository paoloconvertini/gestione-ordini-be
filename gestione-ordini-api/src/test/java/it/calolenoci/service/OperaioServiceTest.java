package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.OperaioDto;
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
class OperaioServiceTest {

    @Inject
    OperaioService service;

    @Test
    @TestTransaction
    void validaIDatiObbligatori() {
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.createOperaio(null)));

        OperaioDto dto = new OperaioDto();
        dto.setNome(" ");
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.createOperaio(dto)));
    }

    @Test
    @TestTransaction
    void creaERecuperaSoloOperaiAttiviInOrdineAlfabetico() {
        String prefix = "TEST-" + UUID.randomUUID().toString().substring(0, 8) + "-";
        OperaioDto secondo = service.createOperaio(newDto(prefix + "Bruno", null));
        OperaioDto primo = service.createOperaio(newDto(prefix + "Alberto", true));
        service.createOperaio(newDto(prefix + "Andrea", false));

        assertNotNull(primo.getId());
        assertTrue(secondo.getAttivo());
        assertEquals(prefix + "Alberto", service.getOperaioById(primo.getId()).getNome());

        List<OperaioDto> created = service.getOperai().stream()
                .filter(dto -> dto.getNome().startsWith(prefix))
                .toList();
        assertEquals(List.of(primo.getId(), secondo.getId()),
                created.stream().map(OperaioDto::getId).toList());
    }

    @Test
    @TestTransaction
    void aggiornaNomeEDisattivaUnOperaio() {
        OperaioDto created = service.createOperaio(newDto("TEST-" + UUID.randomUUID(), true));
        OperaioDto update = newDto("Operaio aggiornato", false);

        OperaioDto updated = service.updateOperaio(created.getId(), update);

        assertEquals("Operaio aggiornato", updated.getNome());
        assertTrue(updated.getAttivo());

        service.disattivaOperaio(created.getId());
        assertFalse(service.getOperaioById(created.getId()).getAttivo());
    }

    @Test
    @TestTransaction
    void segnalaGliIdentificativiInesistenti() {
        assertStatus(404, assertThrows(WebApplicationException.class,
                () -> service.getOperaioById(Long.MAX_VALUE)));
        assertStatus(404, assertThrows(WebApplicationException.class,
                () -> service.updateOperaio(Long.MAX_VALUE, newDto("Test", true))));
        assertStatus(404, assertThrows(WebApplicationException.class,
                () -> service.disattivaOperaio(Long.MAX_VALUE)));
    }

    @Test
    @TestTransaction
    void richiedeNomeAncheInAggiornamento() {
        OperaioDto dto = newDto("Operaio aggiornamento test", true);
        OperaioDto created = service.createOperaio(dto);
        dto.setNome(" ");

        assertStatus(400, assertThrows(WebApplicationException.class,
                () -> service.updateOperaio(created.getId(), dto)));
    }

    private OperaioDto newDto(String nome, Boolean attivo) {
        OperaioDto dto = new OperaioDto();
        dto.setNome(nome);
        dto.setAttivo(attivo);
        return dto;
    }

    private void assertStatus(int expected, WebApplicationException exception) {
        assertEquals(expected, exception.getResponse().getStatus());
    }
}
