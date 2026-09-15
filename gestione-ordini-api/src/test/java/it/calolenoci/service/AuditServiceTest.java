package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.entity.Audit;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
class AuditServiceTest {

    @Inject
    AuditService service;

    @Test
    @TestTransaction
    void accumulaLeModificheEPersisteIlBatchUnaSolaVolta() {
        int progressivo = UUID.randomUUID().hashCode() & Integer.MAX_VALUE;
        Date before = new Date();

        service.logChange("GO_ORDINE", 2026, "TST", progressivo, 1, 100,
                "quantita", 10, 12.5, "test-audit", "Aggiornamento test");
        service.logChange("GO_ORDINE", 2026, "TST", progressivo, null, null,
                "hasCarico", null, false, "test-audit", null);

        assertEquals(0, countByProgressivo(progressivo));

        service.flush();

        List<Audit> records = findByProgressivo(progressivo);
        assertEquals(2, records.size());

        Audit quantita = records.get(0);
        assertNotNull(quantita.getId());
        assertEquals("GO_ORDINE", quantita.getEntityName());
        assertEquals(2026, quantita.getAnno());
        assertEquals("TST", quantita.getSerie());
        assertEquals(progressivo, quantita.getProgressivo());
        assertEquals(1, quantita.getRigo());
        assertEquals(100, quantita.getProgrGenerale());
        assertEquals("quantita", quantita.getFieldName());
        assertEquals("10", quantita.getOldValue());
        assertEquals("12.5", quantita.getNewValue());
        assertEquals("UPDATE", quantita.getActionType());
        assertEquals("test-audit", quantita.getOperationSource());
        assertEquals("Aggiornamento test", quantita.getNote());
        assertFalse(quantita.getCreateDate().before(before));

        Audit hasCarico = records.get(1);
        assertEquals("hasCarico", hasCarico.getFieldName());
        assertNull(hasCarico.getOldValue());
        assertEquals("false", hasCarico.getNewValue());
        assertNull(hasCarico.getRigo());
        assertNull(hasCarico.getProgrGenerale());
        assertNull(hasCarico.getNote());

        service.flush();
        assertEquals(2, countByProgressivo(progressivo));
    }

    private long countByProgressivo(int progressivo) {
        return Audit.count("progressivo = :progressivo AND operationSource = :source",
                Parameters.with("progressivo", progressivo).and("source", "test-audit"));
    }

    private List<Audit> findByProgressivo(int progressivo) {
        return Audit.find("progressivo = :progressivo AND operationSource = :source",
                Sort.ascending("id"),
                Parameters.with("progressivo", progressivo).and("source", "test-audit")).list();
    }
}
