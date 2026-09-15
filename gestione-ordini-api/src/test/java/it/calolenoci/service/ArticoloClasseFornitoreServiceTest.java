package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.ArticoloClasseFornitoreDto;
import it.calolenoci.entity.ArticoloClasseFornitore;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ArticoloClasseFornitoreServiceTest {

    @Inject
    ArticoloClasseFornitoreService service;

    @Test
    @TestTransaction
    void restituisceLeClassiMappate() {
        ArticoloClasseFornitore entity = existingClasse();

        List<ArticoloClasseFornitoreDto> result = service.getClassi();

        assertFalse(result.isEmpty());
        ArticoloClasseFornitoreDto dto = result.stream()
                .filter(item -> entity.getCodice().equals(item.getCodice()))
                .findFirst()
                .orElseThrow();
        assertEquals(entity.getDescrizione(), dto.getDescrizione());
        assertEquals(entity.getDescrUser(), dto.getDescrUser());
        assertEquals(entity.getDescrUser2(), dto.getDescrUser2());
        assertEquals(entity.getDescrUser3(), dto.getDescrUser3());
    }

    @Test
    @TestTransaction
    void restituisceLaClassePerCodice() {
        ArticoloClasseFornitore entity = existingClasse();

        ArticoloClasseFornitoreDto result = service.getClasse(entity.getCodice());

        assertEquals(entity.getCodice(), result.getCodice());
        assertEquals(entity.getDescrizione(), result.getDescrizione());
    }

    @Test
    @TestTransaction
    void restituisceUnDtoVuotoPerUnCodiceInesistente() {
        ArticoloClasseFornitoreDto result = service.getClasse(unknownCodice());

        assertNotNull(result);
        assertNull(result.getCodice());
        assertNull(result.getDescrizione());
        assertNull(result.getDescrUser());
        assertNull(result.getDescrUser2());
        assertNull(result.getDescrUser3());
    }

    @Test
    @TestTransaction
    void aggiornaICampiUtenteSenzaModificareCodiceEDescrizione() {
        ArticoloClasseFornitore entity = existingClasse();
        String codice = entity.getCodice();
        String descrizione = entity.getDescrizione();
        ArticoloClasseFornitoreDto update = new ArticoloClasseFornitoreDto(
                codice, "Descrizione ignorata", "Fornitore test", "Conto test", "Valore test");

        service.save(update);

        ArticoloClasseFornitore updated = ArticoloClasseFornitore.findById(codice);
        assertEquals(descrizione, updated.getDescrizione());
        assertEquals("Fornitore test", updated.getDescrUser());
        assertEquals("Conto test", updated.getDescrUser2());
        assertEquals("Valore test", updated.getDescrUser3());
    }

    @Test
    @TestTransaction
    void nonCreaClassiESalvaguardaIlTerzoCampoSeVuoto() {
        String unknownCodice = unknownCodice();
        service.save(new ArticoloClasseFornitoreDto(
                unknownCodice, "Nuova classe", "Fornitore", "Conto", "Valore"));
        assertNull(ArticoloClasseFornitore.findById(unknownCodice));

        ArticoloClasseFornitore entity = existingClasse();
        entity.setDescrUser3("Valore preesistente");
        service.save(new ArticoloClasseFornitoreDto(
                entity.getCodice(), entity.getDescrizione(), "Fornitore aggiornato", "Conto aggiornato", " "));

        assertEquals("Valore preesistente", entity.getDescrUser3());
        assertTrue(entity.isPersistent());
    }

    private ArticoloClasseFornitore existingClasse() {
        ArticoloClasseFornitore entity = ArticoloClasseFornitore.findAll().firstResult();
        if (entity == null) {
            throw new AssertionError("Il database di sviluppo non contiene classi fornitore in TCA1");
        }
        return entity;
    }

    private String unknownCodice() {
        String codice;
        do {
            codice = UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        } while (ArticoloClasseFornitore.findById(codice) != null);
        return codice;
    }
}
