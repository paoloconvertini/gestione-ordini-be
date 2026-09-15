package it.calolenoci.service;

import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import it.calolenoci.common.service.ComuneService;
import it.calolenoci.dto.FiltroShowroom;
import it.calolenoci.dto.PageShowroomDto;
import it.calolenoci.dto.ShowroomMotivoDto;
import it.calolenoci.dto.ShowroomVisitDto;
import it.calolenoci.dto.UserResponseDTO;
import it.calolenoci.entity.PianoConti;
import it.calolenoci.entity.Sede;
import it.calolenoci.entity.ShowroomMotivo;
import it.calolenoci.entity.ShowroomVisit;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static it.calolenoci.enums.Ruolo.ADMIN;
import static it.calolenoci.enums.Ruolo.RECEPTION_CEGLIE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@QuarkusTest
class ShowroomServiceTest {

    @Inject
    ShowroomService service;

    @InjectMock
    SecurityIdentity securityIdentity;

    @InjectMock
    ComuneService comuneService;

    @InjectMock(convertScopes = true)
    @RestClient
    UserService userService;

    @BeforeEach
    void configureCollaborators() {
        when(securityIdentity.hasRole(ADMIN)).thenReturn(true);
        when(comuneService.findByCodici(anySet())).thenReturn(List.of());
        when(userService.getVenditori()).thenReturn(List.of(
                new UserResponseDTO("Venditore Test", "venditore@example.test", "901")));
    }

    @Test
    @TestTransaction
    void creaAggiornaEliminaUnaVisitaComeAdmin() {
        Sede sede = existingSede();
        ShowroomMotivo motivo = createMotivoEntity("Motivo visita");
        ShowroomVisitDto dto = visitDto(sede, motivo);

        ShowroomVisitDto created = service.create(dto);
        assertNotNull(created.getId());
        assertNotNull(created.getDataVisita());

        dto.setNomeCliente("Cliente aggiornato");
        dto.setTelefono("0800000000");
        dto.setNote("nota aggiornata");
        service.update(created.getId(), dto);
        ShowroomVisit updated = ShowroomVisit.findById(created.getId());
        assertEquals("Cliente aggiornato", updated.getNomeCliente());
        assertEquals("nota aggiornata", updated.getNote());

        service.delete(created.getId());
        assertTrue(((ShowroomVisit) ShowroomVisit.findById(created.getId())).getIsDeleted());
    }

    @Test
    void validaICampiObbligatoriDellaVisita() {
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.create(null)));

        ShowroomVisitDto dto = new ShowroomVisitDto();
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.create(dto)));
        dto.setNomeCliente("Cliente");
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.create(dto)));
        dto.setMotivoId(Long.MAX_VALUE);
        dto.setVenditoreCodice("901");
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.create(dto)));
    }

    @Test
    @TestTransaction
    void impedisceLusoDiUnMotivoDisattivato() {
        Sede sede = existingSede();
        ShowroomMotivo motivo = createMotivoEntity("Motivo disattivato");
        motivo.setAttivo(false);

        assertStatus(400, assertThrows(WebApplicationException.class,
                () -> service.create(visitDto(sede, motivo))));
    }

    @Test
    @TestTransaction
    void ricercaVisiteConFiltriEPaginazione() {
        Sede sede = existingSede();
        ShowroomMotivo motivo = createMotivoEntity("Ricerca showroom");
        String cliente = "Cliente " + UUID.randomUUID();
        ShowroomVisitDto created = visitDto(sede, motivo);
        created.setNomeCliente(cliente);
        created.setDataVisita(LocalDateTime.now().withNano(0));
        service.create(created);

        FiltroShowroom filtro = new FiltroShowroom();
        filtro.setPage(0);
        filtro.setSize(20);
        filtro.setSedeId(sede.getId());
        filtro.setDataDa(LocalDate.now().minusDays(1));
        filtro.setDataA(LocalDate.now().plusDays(1));
        filtro.setNomeCliente(cliente);
        filtro.setCodVenditore("901");
        filtro.setMotivoId(motivo.getId());

        PageShowroomDto result = service.search(filtro);

        assertEquals(1, result.getCount());
        assertEquals(cliente, result.getList().getFirst().getNomeCliente());
        assertEquals("Venditore Test", result.getList().getFirst().getVenditoreNome());
        assertEquals(sede.getDescrizione(), result.getSedeCorrenteDescrizione());
    }

    @Test
    @TestTransaction
    void restituiscePaginaVuotaQuandoLaProvinciaNonHaComuni() {
        when(comuneService.findCodiciByProvincia("XX")).thenReturn(List.of());
        FiltroShowroom filtro = new FiltroShowroom();
        filtro.setPage(0);
        filtro.setSize(20);
        filtro.setProvincia("XX");

        PageShowroomDto result = service.search(filtro);

        assertEquals(0, result.getCount());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    @TestTransaction
    void gestisceMotiviRootFigliELettura() {
        ShowroomMotivo root = createMotivoEntity("Root motivo");
        ShowroomMotivo child = createMotivoEntity("Figlio motivo");
        child.setParent(root);

        assertTrue(service.getMotiviRoot().stream().anyMatch(m -> m.getId().equals(root.getId())));
        assertTrue(service.getFigli(root.getId()).stream().anyMatch(m -> m.getId().equals(child.getId())));
        ShowroomMotivoDto result = service.getMotivoById(child.getId());
        assertEquals(root.getId(), result.getParentId());
        assertStatus(404, assertThrows(WebApplicationException.class,
                () -> service.getMotivoById(Long.MAX_VALUE)));
    }

    @Test
    @TestTransaction
    void creaAggiornaEDisattivaMotiviComeAdmin() {
        ShowroomMotivoDto rootRequest = ShowroomMotivoDto.builder().descrizione("Nuovo root").build();
        ShowroomMotivoDto root = service.createMotivo(rootRequest);
        ShowroomMotivoDto child = service.createMotivo(ShowroomMotivoDto.builder()
                .descrizione("Nuovo figlio").parentId(root.getId()).build());
        assertEquals(root.getId(), child.getParentId());

        ShowroomMotivoDto updated = service.updateMotivo(child.getId(),
                ShowroomMotivoDto.builder().descrizione("Figlio aggiornato").build());
        assertEquals("Figlio aggiornato", updated.getDescrizione());

        service.disattivaMotivo(child.getId());
        assertFalse(((ShowroomMotivo) ShowroomMotivo.findById(child.getId())).getAttivo());
    }

    @Test
    void validaLaGestioneDeiMotivi() {
        assertStatus(400, assertThrows(WebApplicationException.class,
                () -> service.createMotivo(new ShowroomMotivoDto())));
        assertStatus(400, assertThrows(WebApplicationException.class,
                () -> service.createMotivo(ShowroomMotivoDto.builder()
                        .descrizione("Figlio orfano").parentId(Long.MAX_VALUE).build())));
        assertStatus(404, assertThrows(WebApplicationException.class,
                () -> service.updateMotivo(Long.MAX_VALUE,
                        ShowroomMotivoDto.builder().descrizione("x").build())));
        assertStatus(404, assertThrows(WebApplicationException.class,
                () -> service.disattivaMotivo(Long.MAX_VALUE)));
    }

    @Test
    @TestTransaction
    void cercaEAssociaUnCliente() {
        assertTrue(service.searchClienti("ab").isEmpty());
        PianoConti cliente = PianoConti.find("cliFor = 'C' and sottoConto is not null and intestazione is not null")
                .firstResult();
        if (cliente == null) {
            throw new AssertionError("Il database di sviluppo non contiene clienti associabili");
        }
        Sede sede = existingSede();
        ShowroomMotivo motivo = createMotivoEntity("Associazione cliente");
        ShowroomVisitDto visita = service.create(visitDto(sede, motivo));

        service.associaCliente(visita.getId(), cliente.getSottoConto());

        ShowroomVisit entity = ShowroomVisit.findById(visita.getId());
        assertEquals(cliente.getSottoConto(), entity.getCodiceCliente());
        assertEquals(cliente.getIntestazione(), entity.getNomeCliente());
        assertNotNull(service.searchClienti(cliente.getSottoConto().substring(0, 3)));
    }

    @Test
    @TestTransaction
    void applicaIPermessiDellaReception() {
        when(securityIdentity.hasRole(ADMIN)).thenReturn(false);
        when(securityIdentity.hasRole(RECEPTION_CEGLIE)).thenReturn(false);
        FiltroShowroom filtro = new FiltroShowroom();
        filtro.setPage(0);
        filtro.setSize(20);
        assertStatus(403, assertThrows(WebApplicationException.class, () -> service.search(filtro)));

        when(securityIdentity.hasRole(RECEPTION_CEGLIE)).thenReturn(true);
        Sede sedeCeglie = Sede.find("codice", "CEGLIE").firstResult();
        if (sedeCeglie != null) {
            ShowroomMotivo motivo = createMotivoEntity("Visita reception");
            ShowroomVisitDto created = service.create(visitDto(sedeCeglie, motivo));
            assertNotNull(created.getId());
        }
    }

    @Test
    @TestTransaction
    void validaSedeVenditoreEAggiornamentiDellaVisita() {
        Sede sede = existingSede();
        ShowroomMotivo motivo = createMotivoEntity("Validazioni visita");
        ShowroomVisitDto dto = visitDto(sede, motivo);
        dto.setVenditoreCodice(null);
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.create(dto)));

        dto.setVenditoreCodice("901");
        dto.setSedeId(null);
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.create(dto)));
        dto.setSedeId(Long.MAX_VALUE);
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.create(dto)));

        dto.setSedeId(sede.getId());
        ShowroomVisitDto created = service.create(dto);
        dto.setNomeCliente(" ");
        assertStatus(400, assertThrows(WebApplicationException.class,
                () -> service.update(created.getId(), dto)));
        dto.setNomeCliente("Cliente valido");
        dto.setMotivoId(null);
        assertStatus(400, assertThrows(WebApplicationException.class,
                () -> service.update(created.getId(), dto)));
        dto.setMotivoId(motivo.getId());
        dto.setSedeId(Long.MAX_VALUE);
        assertStatus(400, assertThrows(WebApplicationException.class,
                () -> service.update(created.getId(), dto)));

        service.delete(created.getId());
        assertStatus(400, assertThrows(WebApplicationException.class,
                () -> service.update(created.getId(), dto)));
        assertStatus(404, assertThrows(WebApplicationException.class,
                () -> service.update(Long.MAX_VALUE, dto)));
    }

    @Test
    @TestTransaction
    void rifiutaLAssociazioneDiUnClienteNonValido() {
        Sede sede = existingSede();
        ShowroomMotivo motivoEntity = createMotivoEntity("Cliente non valido");
        ShowroomVisitDto visita = service.create(visitDto(sede, motivoEntity));
        assertStatus(400, assertThrows(WebApplicationException.class,
                () -> service.associaCliente(visita.getId(), "ZZZZZZ")));
    }

    @Test
    void impedisceAiNonAdminLaGestioneDeiMotivi() {
        when(securityIdentity.hasRole(ADMIN)).thenReturn(false);
        ShowroomMotivoDto motivo = ShowroomMotivoDto.builder().descrizione("Non autorizzato").build();
        assertStatus(403, assertThrows(WebApplicationException.class,
                () -> service.createMotivo(motivo)));
        assertStatus(403, assertThrows(WebApplicationException.class,
                () -> service.updateMotivo(Long.MAX_VALUE, motivo)));
        assertStatus(403, assertThrows(WebApplicationException.class,
                () -> service.disattivaMotivo(Long.MAX_VALUE)));
    }

    @Test
    void rifiutaLAssociazioneAUnaVisitaInesistente() {
        assertStatus(404, assertThrows(WebApplicationException.class,
                () -> service.associaCliente(Long.MAX_VALUE, "000001")));
    }

    private ShowroomVisitDto visitDto(Sede sede, ShowroomMotivo motivo) {
        return ShowroomVisitDto.builder()
                .nomeCliente("Cliente test")
                .motivoId(motivo.getId())
                .venditoreCodice("901")
                .sedeId(sede.getId())
                .note("nota test")
                .build();
    }

    private ShowroomMotivo createMotivoEntity(String descrizione) {
        ShowroomMotivo motivo = new ShowroomMotivo();
        motivo.setDescrizione(descrizione + " " + UUID.randomUUID());
        motivo.setAttivo(true);
        motivo.persist();
        return motivo;
    }

    private Sede existingSede() {
        Sede sede = Sede.findAll().firstResult();
        if (sede == null) {
            throw new AssertionError("Il database di sviluppo non contiene sedi");
        }
        return sede;
    }

    private void assertStatus(int expected, WebApplicationException exception) {
        assertEquals(expected, exception.getResponse().getStatus());
    }
}
