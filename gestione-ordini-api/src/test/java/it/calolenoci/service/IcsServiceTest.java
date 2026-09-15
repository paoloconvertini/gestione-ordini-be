package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.AttivitaMontaggioSearchDto;
import it.calolenoci.dto.PageAttivitaMontaggioDto;
import it.calolenoci.entity.AttivitaMontaggio;
import it.calolenoci.entity.AttivitaMontaggioDett;
import it.calolenoci.entity.TipoAttivitaMontaggio;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class IcsServiceTest {

    @Inject
    IcsService service;

    @Test
    @TestTransaction
    void generaUnCalendarioValidoAncheSenzaEventi() {
        String result = service.buildMontaggiCalendar(page());

        assertEquals("BEGIN:VCALENDAR\r\n" +
                "VERSION:2.0\r\n" +
                "PRODID:-//GESTIONE_ORDINI//Agenda Montaggi//IT\r\n" +
                "CALSCALE:GREGORIAN\r\n" +
                "METHOD:PUBLISH\r\n" +
                "END:VCALENDAR\r\n", result);
    }

    @Test
    @TestTransaction
    void esportaEventoCompletoEscapandoICaratteriIcs() {
        TipoAttivitaMontaggio tipo = persistTipo("Installazione, verifica");
        AttivitaMontaggio attivita = persistAttivita(
                LocalDateTime.of(2026, 9, 8, 9, 5),
                LocalDateTime.of(2026, 9, 8, 11, 30));
        attivita.setNomeCliente("Rossi, Mario; Srl");
        attivita.setVia("Via Roma, 1");
        attivita.setComune("Ostuni");
        attivita.setTelefono("080,123");
        attivita.setNote("Riga 1\nRiga 2; verifica");
        persistDettaglio(attivita.getId(), tipo.getId());

        String result = service.buildMontaggiCalendar(page(attivita.getId()));

        assertTrue(result.contains("BEGIN:VEVENT\r\n"));
        assertTrue(result.contains("UID:" + attivita.getId() + "@GO_\r\n"));
        assertTrue(result.matches("(?s).*DTSTAMP:[0-9]{8}T[0-9]{6}\r\n.*"));
        assertTrue(result.contains("DTSTART:20260908T090500\r\n"));
        assertTrue(result.contains("DTEND:20260908T113000\r\n"));
        assertTrue(result.contains("SUMMARY:Rossi\\, Mario\\; Srl\r\n"));
        assertTrue(result.contains("LOCATION:Via Roma\\, 1\\, Ostuni\r\n"));
        assertTrue(result.contains("DESCRIPTION:Telefono: 080\\,123\\nAttività: Installazione\\, verifica\\n" +
                "Note: Riga 1\\nRiga 2\\; verifica\r\n"));
        assertTrue(result.contains("END:VEVENT\r\nEND:VCALENDAR\r\n"));
    }

    @Test
    @TestTransaction
    void ignoraAttivitaInesistentiOConIntervalloInvertito() {
        AttivitaMontaggio invertita = persistAttivita(
                LocalDateTime.of(2026, 9, 8, 12, 0),
                LocalDateTime.of(2026, 9, 8, 10, 0));

        String result = service.buildMontaggiCalendar(page(Long.MAX_VALUE, invertita.getId()));

        assertFalse(result.contains("BEGIN:VEVENT"));
        assertTrue(result.endsWith("END:VCALENDAR\r\n"));
    }

    private PageAttivitaMontaggioDto page(Long... ids) {
        List<AttivitaMontaggioSearchDto> entries = new ArrayList<>();
        for (Long id : ids) {
            AttivitaMontaggioSearchDto dto = new AttivitaMontaggioSearchDto();
            dto.setId(id);
            entries.add(dto);
        }
        PageAttivitaMontaggioDto page = new PageAttivitaMontaggioDto();
        page.setList(entries);
        page.setCount(entries.size());
        return page;
    }

    private AttivitaMontaggio persistAttivita(LocalDateTime da, LocalDateTime a) {
        AttivitaMontaggio entity = new AttivitaMontaggio();
        entity.setDataOraDa(da);
        entity.setDataOraA(a);
        entity.setNomeCliente("Cliente " + UUID.randomUUID());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setStato("PROGRAMMATO");
        entity.setPromemoriaInviato(false);
        entity.setScalaMobile(false);
        entity.setTipoAppuntamento("MONTAGGIO");
        entity.persist();
        return entity;
    }

    private TipoAttivitaMontaggio persistTipo(String descrizione) {
        TipoAttivitaMontaggio entity = new TipoAttivitaMontaggio();
        entity.setDescrizione(descrizione);
        entity.setOrdineVisualizzazione(999);
        entity.setAttivo(true);
        entity.persist();
        return entity;
    }

    private void persistDettaglio(Long attivitaId, Long tipoId) {
        AttivitaMontaggioDett dettaglio = new AttivitaMontaggioDett();
        dettaglio.setIdAttivitaMontaggio(attivitaId);
        dettaglio.setIdTipoAttivita(tipoId);
        dettaglio.setCompletato(false);
        dettaglio.setQuantita(java.math.BigDecimal.ONE);
        dettaglio.persist();
    }
}
