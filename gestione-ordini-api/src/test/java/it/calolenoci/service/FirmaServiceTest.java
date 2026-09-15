package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.entity.FirmaOrdineCliente;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class FirmaServiceTest {

    @Inject
    FirmaService service;

    @Test
    @TestTransaction
    void salvaLaPrimaFirmaDellOrdine() {
        int progressivo = uniqueProgressivo();

        service.save(2026, "TST", progressivo, "firma_iniziale.png");

        FirmaOrdineCliente created = FirmaOrdineCliente.findById(2026, "TST", progressivo);
        assertNotNull(created);
        assertEquals(2026, created.getAnno());
        assertEquals("TST", created.getSerie());
        assertEquals(progressivo, created.getProgressivo());
        assertEquals("firma_iniziale.png", created.getFileName());
    }

    @Test
    @TestTransaction
    void aggiornaLaFirmaEsistenteSenzaDuplicarla() {
        int progressivo = uniqueProgressivo();
        service.save(2026, "TST", progressivo, "firma_iniziale.png");

        service.save(2026, "TST", progressivo, "firma_aggiornata.png");

        FirmaOrdineCliente updated = FirmaOrdineCliente.findById(2026, "TST", progressivo);
        assertEquals("firma_aggiornata.png", updated.getFileName());
        assertEquals(1, FirmaOrdineCliente.count(
                "anno = :anno AND serie = :serie AND progressivo = :progressivo",
                Parameters.with("anno", 2026).and("serie", "TST").and("progressivo", progressivo)));
    }

    private int uniqueProgressivo() {
        int progressivo;
        do {
            progressivo = UUID.randomUUID().hashCode() & Integer.MAX_VALUE;
        } while (FirmaOrdineCliente.findById(2026, "TST", progressivo) != null);
        return progressivo;
    }
}
