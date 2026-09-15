package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import it.calolenoci.dto.AppuntamentoDto;
import it.calolenoci.dto.FiltroAppuntamentoDto;
import it.calolenoci.dto.OutlookEventDto;
import it.calolenoci.dto.PageAppuntamentoDto;
import it.calolenoci.dto.UserResponseDTO;
import it.calolenoci.entity.Appuntamento;
import it.calolenoci.entity.AppuntamentoVenditore;
import it.calolenoci.entity.Sede;
import it.calolenoci.entity.ShowroomMotivo;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class AppuntamentoServiceTest {

    @Inject
    AppuntamentoService service;

    @InjectMock(convertScopes = true)
    @RestClient
    UserService userService;

    @InjectMock
    OutlookCalendarService outlookCalendarService;

    @BeforeEach
    void configureExternalServices() throws Exception {
        when(userService.getVenditori()).thenReturn(List.of(
                new UserResponseDTO("Venditore Uno", "uno@example.test", "901"),
                new UserResponseDTO("Venditore Due", "due@example.test", "902")));
        when(outlookCalendarService.createEvent(any(OutlookEventDto.class))).thenReturn("outlook-test");
        when(outlookCalendarService.eventExists("outlook-test")).thenReturn(true);
    }

    @Test
    @TestTransaction
    void richiedeLaSede() {
        AppuntamentoDto dto = new AppuntamentoDto();
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.create(dto)));
    }

    @Test
    @TestTransaction
    void richiedeAlmenoUnVenditore() {
        AppuntamentoDto dto = new AppuntamentoDto();
        dto.setSedeId(existingSede().getId());
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.create(dto)));
    }

    @Test
    @TestTransaction
    void richiedeIlMotivoPerGliAppuntamenti() {
        AppuntamentoDto dto = newAppuntamento("Cliente", "901", 9, 11);
        dto.setCodVenditori(List.of("901"));
        dto.setTipoEvento("APPUNTAMENTO");
        dto.setMotivoId(null);
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.create(dto)));
    }

    @Test
    @TestTransaction
    void creaAppuntamentoConVenditoriESincronizzazioneOutlook() throws Exception {
        AppuntamentoDto request = newAppuntamento("Cliente " + UUID.randomUUID(), "901", 9, 11);

        AppuntamentoDto created = service.create(request);

        assertNotNull(created.getId());
        assertFalse(created.getPromemoriaInviato());
        assertEquals(List.of("901"), created.getCodVenditori());
        assertEquals(1, AppuntamentoVenditore.count("idAppuntamento", created.getId()));
        assertEquals("outlook-test", ((Appuntamento) Appuntamento.findById(created.getId())).getOutlookEventId());
        verify(outlookCalendarService).createEvent(any(OutlookEventDto.class));
    }

    @Test
    @TestTransaction
    void impedisceSovrapposizioniPerLoStessoVenditore() {
        service.create(newAppuntamento("Primo cliente", "901", 9, 11));

        AppuntamentoDto overlapping = newAppuntamento("Secondo cliente", "901", 10, 12);

        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> service.create(overlapping));
        assertStatus(400, exception);
        assertEquals("Uno dei venditori selezionati ha già un appuntamento nella fascia oraria indicata",
                exception.getMessage());
    }

    @Test
    @TestTransaction
    void ricercaPerSedeDataClienteEVenditore() {
        String cliente = "Cliente ricerca " + UUID.randomUUID();
        AppuntamentoDto matching = service.create(newAppuntamento(cliente, "901", 9, 11));
        service.create(newAppuntamento("Altro cliente " + UUID.randomUUID(), "902", 12, 13));

        FiltroAppuntamentoDto filtro = new FiltroAppuntamentoDto();
        filtro.setSedeId(existingSede().getId());
        filtro.setDataDa(LocalDate.of(1903, 9, 8));
        filtro.setDataA(LocalDate.of(1903, 9, 8));
        filtro.setNomeCliente(cliente.substring(0, cliente.length() - 5));
        filtro.setCodVenditore("901");

        PageAppuntamentoDto result = service.search(filtro);

        assertEquals(1, result.getCount());
        assertEquals(matching.getId(), result.getList().getFirst().getId());
        assertEquals("Venditore Uno", result.getList().getFirst().getVenditoriLabel());
        assertEquals("09:00 - 11:00", result.getList().getFirst().getDataOraLabel());
        assertEquals("#9C27B0", result.getList().getFirst().getColore());
    }

    @Test
    @TestTransaction
    void aggiornaVenditoriECancellaAppuntamento() throws Exception {
        AppuntamentoDto created = service.create(newAppuntamento("Cliente iniziale", "901", 9, 11));
        AppuntamentoDto update = newAppuntamento("Cliente aggiornato", "902", 14, 16);

        AppuntamentoDto updated = service.update(created.getId(), update);

        assertEquals("Cliente aggiornato", updated.getNomeCliente());
        assertEquals(List.of("902"), updated.getCodVenditori());
        verify(outlookCalendarService).updateEvent(eq("outlook-test"), any(OutlookEventDto.class));

        service.delete(created.getId());

        assertNull(Appuntamento.findById(created.getId()));
        assertEquals(0, AppuntamentoVenditore.count("idAppuntamento", created.getId()));
        verify(outlookCalendarService).deleteEvent("outlook-test");
    }

    @Test
    @TestTransaction
    void segnalaGliIdentificativiInesistenti() {
        assertStatus(404, assertThrows(WebApplicationException.class, () -> service.getById(Long.MAX_VALUE)));
        assertStatus(404, assertThrows(WebApplicationException.class, () -> service.delete(Long.MAX_VALUE)));
    }

    @Test
    @TestTransaction
    void gestisceFiltriSenzaVenditoriEValidazioniAggiornamento() {
        FiltroAppuntamentoDto filtro = new FiltroAppuntamentoDto();
        filtro.setCodVenditore("venditore-inesistente");
        PageAppuntamentoDto vuoto = service.search(filtro);
        assertEquals(0, vuoto.getCount());
        assertTrue(vuoto.getList().isEmpty());

        AppuntamentoDto created = service.create(newAppuntamento("Cliente validazione", "901", 9, 11));
        AppuntamentoDto update = newAppuntamento("Cliente validazione", "901", 9, 11);
        update.setSedeId(null);
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.update(created.getId(), update)));

        update.setSedeId(existingSede().getId());
        update.setCodVenditori(List.of());
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.update(created.getId(), update)));
    }

    @Test
    @TestTransaction
    void validaMotivoEConflittoVenditoreInAggiornamento() {
        AppuntamentoDto created = service.create(newAppuntamento("Cliente motivo", "901", 9, 11));
        AppuntamentoDto update = newAppuntamento("Cliente motivo", "901", 9, 11);
        update.setTipoEvento("APPUNTAMENTO");
        update.setMotivoId(null);
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.update(created.getId(), update)));

        update.setMotivoId(Long.MAX_VALUE);
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.update(created.getId(), update)));
    }

    @Test
    @TestTransaction
    void ricercaERecuperaUnAppuntamentoConMotivo() {
        ShowroomMotivo motivo = ShowroomMotivo.find("attivo", true).firstResult();
        if (motivo == null) {
            throw new AssertionError("Il database di sviluppo non contiene motivi attivi");
        }
        AppuntamentoDto request = newAppuntamento("Cliente con motivo", "902", 17, 18);
        request.setTipoEvento("APPUNTAMENTO");
        request.setMotivoId(motivo.getId());
        request.setTelefono("0801234567");
        request.setNote("Nota test");

        AppuntamentoDto created = service.create(request);

        FiltroAppuntamentoDto filtro = new FiltroAppuntamentoDto();
        filtro.setMotivoId(motivo.getId());
        PageAppuntamentoDto result = service.search(filtro);
        assertTrue(result.getList().stream().anyMatch(a -> created.getId().equals(a.getId())));
        assertEquals(motivo.getId(), service.getById(created.getId()).getMotivoId());
    }

    @Test
    @TestTransaction
    void rifiutaUnMotivoNonAttivo() {
        ShowroomMotivo motivo = ShowroomMotivo.find("attivo", false).firstResult();
        if (motivo == null) {
            throw new AssertionError("Il database di sviluppo non contiene motivi disattivi");
        }
        AppuntamentoDto request = newAppuntamento("Cliente motivo disattivo", "902", 19, 20);
        request.setTipoEvento("APPUNTAMENTO");
        request.setMotivoId(motivo.getId());

        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.create(request)));
    }

    @Test
    @TestTransaction
    void gestisceMotivoConPadre() {
        ShowroomMotivo padre = new ShowroomMotivo();
        padre.setDescrizione("Motivo padre test");
        padre.setAttivo(true);
        padre.persist();
        ShowroomMotivo figlio = new ShowroomMotivo();
        figlio.setDescrizione("Motivo figlio test");
        figlio.setAttivo(true);
        figlio.setParent(padre);
        figlio.persist();

        AppuntamentoDto request = newAppuntamento("Cliente gerarchia", "902", 21, 22);
        request.setTipoEvento("APPUNTAMENTO");
        request.setMotivoId(figlio.getId());
        AppuntamentoDto created = service.create(request);

        AppuntamentoDto loaded = service.getById(created.getId());
        assertEquals(padre.getId(), loaded.getMotivoParentId());
        assertEquals("Motivo padre test", loaded.getMotivoParentDescrizione());
    }

    private AppuntamentoDto newAppuntamento(String cliente, String venditore, int oraDa, int oraA) {
        AppuntamentoDto dto = new AppuntamentoDto();
        dto.setSedeId(existingSede().getId());
        dto.setNomeCliente(cliente);
        dto.setDataAppuntamento(LocalDate.of(1903, 9, 8));
        dto.setOraDa(LocalTime.of(oraDa, 0));
        dto.setOraA(LocalTime.of(oraA, 0));
        dto.setTipoEvento("FORMAZIONE");
        dto.setDescrizione("Formazione di test");
        dto.setPromemoriaInviato(false);
        dto.setCodVenditori(List.of(venditore));
        return dto;
    }

    private Sede existingSede() {
        List<Sede> sedi = Sede.listAll();
        if (sedi.isEmpty()) {
            throw new AssertionError("Il database di sviluppo non contiene sedi");
        }
        return sedi.getFirst();
    }

    private void assertStatus(int expected, WebApplicationException exception) {
        assertEquals(expected, exception.getResponse().getStatus());
    }
}
