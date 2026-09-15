package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.RegistroAzioniDto;
import it.calolenoci.entity.RegistroAzioni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class EventoServiceTest {

    @Inject
    EventoService service;

    @Test
    @TestTransaction
    void filtraProiettaEOrdinaLaCronologia() {
        Integer progressivo = uniqueProgressivo();
        RegistroAzioni precedente = newEvento(progressivo, LocalDateTime.of(2026, 9, 8, 10, 0), "QUANTITA");
        precedente.setQuantita(12.5);
        precedente.persist();

        RegistroAzioni recente = newEvento(progressivo, LocalDateTime.of(2026, 9, 8, 11, 0), "QTA_RISERVATA");
        recente.setQuantita(15.0);
        recente.setTono("BIANCO");
        recente.setQtaRiservata(3.5);
        recente.setQtaProntoConsegna(2.0);
        recente.persist();

        RegistroAzioni altroRigo = newEvento(progressivo, LocalDateTime.of(2026, 9, 8, 12, 0), "TONO");
        altroRigo.setRigo(2);
        altroRigo.persist();

        List<RegistroAzioniDto> result = service.getByAnnoSerieProgressivoRigo(2026, "TST", progressivo, 1);

        assertEquals(2, result.size());
        assertEquals(List.of("QTA_RISERVATA", "QUANTITA"),
                result.stream().map(RegistroAzioniDto::getAzione).toList());
        RegistroAzioniDto dto = result.getFirst();
        assertEquals(2026, dto.getAnno());
        assertEquals("TST", dto.getSerie());
        assertEquals(progressivo, dto.getProgressivo());
        assertEquals(1, dto.getRigo());
        assertEquals("utente-test", dto.getUsername());
        assertEquals(LocalDateTime.of(2026, 9, 8, 11, 0), dto.getCreateDate());
        assertEquals(15.0, dto.getQuantita());
        assertEquals("BIANCO", dto.getTono());
        assertEquals(3.5, dto.getQtaRiservata());
        assertEquals(2.0, dto.getQtaProntoConsegna());
    }

    @Test
    @TestTransaction
    void restituisceUnaListaVuotaQuandoNonCiSonoEventi() {
        List<RegistroAzioniDto> result = service.getByAnnoSerieProgressivoRigo(
                2026, "TST", uniqueProgressivo(), 1);

        assertTrue(result.isEmpty());
    }

    private RegistroAzioni newEvento(Integer progressivo, LocalDateTime createDate, String azione) {
        RegistroAzioni entity = new RegistroAzioni();
        entity.setAnno(2026);
        entity.setSerie("TST");
        entity.setProgressivo(progressivo);
        entity.setRigo(1);
        entity.setUsername("utente-test");
        entity.setCreateDate(createDate);
        entity.setAzione(azione);
        return entity;
    }

    private Integer uniqueProgressivo() {
        return UUID.randomUUID().hashCode() & Integer.MAX_VALUE;
    }
}
