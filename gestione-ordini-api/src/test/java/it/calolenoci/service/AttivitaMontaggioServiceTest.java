package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.AttivitaMontaggioDettDto;
import it.calolenoci.dto.AttivitaMontaggioDto;
import it.calolenoci.dto.AttivitaMontaggioOperaioDto;
import it.calolenoci.dto.FiltroAttivitaMontaggioDto;
import it.calolenoci.dto.OperaioDto;
import it.calolenoci.dto.PageAttivitaMontaggioDto;
import it.calolenoci.dto.TipoAttivitaMontaggioDto;
import it.calolenoci.entity.AttivitaMontaggio;
import it.calolenoci.entity.AttivitaMontaggioDett;
import it.calolenoci.entity.AttivitaMontaggioOperaio;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AttivitaMontaggioServiceTest {

    @Inject
    AttivitaMontaggioService service;

    @Inject
    TipoAttivitaMontaggioService tipoService;

    @Inject
    OperaioService operaioService;

    @Test
    @TestTransaction
    void validaICampiObbligatori() {
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.create(null)));

        AttivitaMontaggioDto dto = new AttivitaMontaggioDto();
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.create(dto)));

        dto.setDataOraDa(LocalDateTime.of(2026, 9, 8, 9, 0));
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.create(dto)));

        dto.setDataOraA(LocalDateTime.of(2026, 9, 8, 11, 0));
        dto.setNomeCliente(" ");
        assertStatus(400, assertThrows(WebApplicationException.class, () -> service.create(dto)));
    }

    @Test
    @TestTransaction
    void creaAttivitaConValoriPredefinitiEDettagliAssociati() {
        Long tipoId = createTipo("Montaggio");
        Long operaioId = createOperaio("Mario");
        AttivitaMontaggioDto request = newAttivita("Cliente " + UUID.randomUUID());
        request.setDettagli(List.of(newDettaglio(tipoId, "2.50", null)));
        request.setOperai(List.of(newOperaio(operaioId, true)));

        AttivitaMontaggioDto created = service.create(request);

        assertNotNull(created.getId());
        assertEquals("PROGRAMMATO", created.getStato());
        assertFalse(created.getPromemoriaInviato());
        assertEquals(1, created.getDettagli().size());
        assertEquals(new BigDecimal("2.50"), created.getDettagli().getFirst().getQuantita());
        assertFalse(created.getDettagli().getFirst().getCompletato());
        assertEquals(operaioId, created.getOperai().getFirst().getIdOperaio());
        assertTrue(created.getOperai().getFirst().getPrincipale());
    }

    @Test
    @TestTransaction
    void aggiornaAttivitaSostituendoDettagliEOperai() {
        Long tipoVecchio = createTipo("Vecchio");
        Long tipoNuovo = createTipo("Nuovo");
        Long operaioVecchio = createOperaio("Vecchio");
        Long operaioNuovo = createOperaio("Nuovo");
        AttivitaMontaggioDto request = newAttivita("Cliente iniziale");
        request.setDettagli(List.of(newDettaglio(tipoVecchio, "1", false)));
        request.setOperai(List.of(newOperaio(operaioVecchio, true)));
        AttivitaMontaggioDto created = service.create(request);

        AttivitaMontaggioDto update = newAttivita("Cliente aggiornato");
        update.setStato("IN_CORSO");
        update.setDettagli(List.of(newDettaglio(tipoNuovo, "3", true)));
        update.setOperai(List.of(newOperaio(operaioNuovo, false)));

        AttivitaMontaggioDto updated = service.update(created.getId(), update);

        assertEquals("Cliente aggiornato", updated.getNomeCliente());
        assertEquals("IN_CORSO", updated.getStato());
        assertEquals(List.of(tipoNuovo),
                updated.getDettagli().stream().map(AttivitaMontaggioDettDto::getIdTipoAttivita).toList());
        assertEquals(List.of(operaioNuovo),
                updated.getOperai().stream().map(AttivitaMontaggioOperaioDto::getIdOperaio).toList());
        assertTrue(updated.getDettagli().getFirst().getCompletato());
    }

    @Test
    @TestTransaction
    void ricercaPerFiltriEComponeLeEtichette() {
        Long tipoId = createTipo("Installazione");
        Long operaioId = createOperaio("Luigi");
        String cliente = "Cliente ricerca " + UUID.randomUUID();
        AttivitaMontaggioDto matching = newAttivita(cliente);
        matching.setTipoAppuntamento("MANUTENZIONE");
        matching.setStato("IN_CORSO");
        matching.setScalaMobile(true);
        matching.setVia("Via Roma");
        matching.setCivico("10");
        matching.setComune("Ostuni");
        matching.setNumeroOrdine("2026/T/1");
        matching.setDettagli(List.of(newDettaglio(tipoId, "1", false)));
        matching.setOperai(List.of(newOperaio(operaioId, true)));
        AttivitaMontaggioDto created = service.create(matching);
        service.create(newAttivita("Altro cliente " + UUID.randomUUID()));

        FiltroAttivitaMontaggioDto filtro = new FiltroAttivitaMontaggioDto();
        filtro.setNomeCliente(cliente.substring(0, cliente.length() - 5));
        filtro.setStato("IN_CORSO");
        filtro.setTipoAppuntamento("MANUTENZIONE");
        filtro.setScalaMobile(true);
        filtro.setIdOperaio(operaioId);
        filtro.setDataDa(LocalDate.of(2026, 9, 8));
        filtro.setDataA(LocalDate.of(2026, 9, 8));

        PageAttivitaMontaggioDto result = service.search(filtro);

        assertEquals(1, result.getCount());
        assertEquals(created.getId(), result.getList().getFirst().getId());
        assertEquals("#7b1fa2", result.getList().getFirst().getColore());
        assertEquals("Via Roma 10, Ostuni", result.getList().getFirst().getIndirizzoLabel());
        assertEquals(tipoService.getById(tipoId).getDescrizione(),
                result.getList().getFirst().getAttivitaLabel());
        assertTrue(result.getList().getFirst().getTooltip().contains("Scala mobile: SI"));
    }

    @Test
    @TestTransaction
    void coloraLeAttivitaAffidateAllaDittaEsterna() {
        String cliente = "Cliente ditta esterna " + UUID.randomUUID();
        AttivitaMontaggioDto request = newAttivita(cliente);
        AttivitaMontaggioDto created = service.create(request);
        AttivitaMontaggioOperaio assegnazione = new AttivitaMontaggioOperaio();
        assegnazione.setIdAttivitaMontaggio(created.getId());
        assegnazione.setIdOperaio(5L);
        assegnazione.setPrincipale(true);
        assegnazione.persist();

        FiltroAttivitaMontaggioDto filtro = new FiltroAttivitaMontaggioDto();
        filtro.setNomeCliente(cliente);

        PageAttivitaMontaggioDto result = service.search(filtro);

        assertEquals("rgb(25, 210, 148)", result.getList().getFirst().getColore());
        assertTrue(result.getList().getFirst().getDittaEsterna());
    }

    @Test
    @TestTransaction
    void cancellaAttivitaEAssociazioni() {
        Long tipoId = createTipo("Da cancellare");
        Long operaioId = createOperaio("Da cancellare");
        AttivitaMontaggioDto request = newAttivita("Cliente da cancellare");
        request.setDettagli(List.of(newDettaglio(tipoId, "1", false)));
        request.setOperai(List.of(newOperaio(operaioId, true)));
        Long id = service.create(request).getId();

        service.delete(id);

        assertNull(AttivitaMontaggio.findById(id));
        assertEquals(0, AttivitaMontaggioDett.count("idAttivitaMontaggio", id));
        assertEquals(0, AttivitaMontaggioOperaio.count("idAttivitaMontaggio", id));
        service.delete(null);
    }

    @Test
    @TestTransaction
    void segnalaLeAttivitaInesistenti() {
        assertStatus(404, assertThrows(WebApplicationException.class, () -> service.getById(Long.MAX_VALUE)));
        assertStatus(404, assertThrows(WebApplicationException.class,
                () -> service.update(Long.MAX_VALUE, newAttivita("Cliente"))));
    }

    private AttivitaMontaggioDto newAttivita(String cliente) {
        AttivitaMontaggioDto dto = new AttivitaMontaggioDto();
        dto.setNomeCliente(cliente);
        dto.setDataOraDa(LocalDateTime.of(2026, 9, 8, 9, 0));
        dto.setDataOraA(LocalDateTime.of(2026, 9, 8, 11, 0));
        dto.setTipoAppuntamento("MONTAGGIO");
        return dto;
    }

    private AttivitaMontaggioDettDto newDettaglio(Long tipoId, String quantita, Boolean completato) {
        AttivitaMontaggioDettDto dto = new AttivitaMontaggioDettDto();
        dto.setIdTipoAttivita(tipoId);
        dto.setQuantita(new BigDecimal(quantita));
        dto.setCompletato(completato);
        return dto;
    }

    private AttivitaMontaggioOperaioDto newOperaio(Long operaioId, Boolean principale) {
        AttivitaMontaggioOperaioDto dto = new AttivitaMontaggioOperaioDto();
        dto.setIdOperaio(operaioId);
        dto.setPrincipale(principale);
        return dto;
    }

    private Long createTipo(String descrizione) {
        TipoAttivitaMontaggioDto dto = new TipoAttivitaMontaggioDto();
        dto.setDescrizione(limit50(descrizione + " " + UUID.randomUUID()));
        dto.setOrdineVisualizzazione(999);
        return tipoService.create(dto).getId();
    }

    private Long createOperaio(String nome) {
        OperaioDto dto = new OperaioDto();
        dto.setNome(limit50(nome + " " + UUID.randomUUID()));
        return operaioService.createOperaio(dto).getId();
    }

    private String limit50(String value) {
        return value.substring(0, Math.min(50, value.length()));
    }

    private void assertStatus(int expected, WebApplicationException exception) {
        assertEquals(expected, exception.getResponse().getStatus());
    }
}
