package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.CategoriaCespiteResponse;
import it.calolenoci.entity.CategoriaCespite;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class CategoriaCespiteServiceTest {

    @Inject
    CategoriaCespiteService service;

    @Test
    @TestTransaction
    void creaUnaCategoriaConTuttiIParametriContabili() {
        String tipoCespite = uniqueTipoCespite();

        service.save(newCategoria(tipoCespite, "Categoria test", 10));

        CategoriaCespite created = findByTipoCespite(tipoCespite);
        assertNotNull(created.getId());
        assertCategoria(created, tipoCespite, "Categoria test", 10);
    }

    @Test
    @TestTransaction
    void aggiornaLaCategoriaEsistenteSenzaDuplicarla() {
        String tipoCespite = uniqueTipoCespite();
        service.save(newCategoria(tipoCespite, "Categoria iniziale", 10));
        String id = findByTipoCespite(tipoCespite).getId();

        CategoriaCespiteResponse update = newCategoria(tipoCespite, "Categoria aggiornata", 20);
        update.setId(UUID.randomUUID().toString());
        service.save(update);

        assertEquals(1, CategoriaCespite.count("tipoCespite", tipoCespite));
        CategoriaCespite updated = findByTipoCespite(tipoCespite);
        assertEquals(id, updated.getId());
        assertCategoria(updated, tipoCespite, "Categoria aggiornata", 20);
    }

    private CategoriaCespiteResponse newCategoria(String tipoCespite, String descrizione, int base) {
        CategoriaCespiteResponse dto = new CategoriaCespiteResponse();
        dto.setTipoCespite(tipoCespite);
        dto.setCodice("COD-" + base);
        dto.setDescrizione(descrizione);
        dto.setPercAmmortamento((double) base);
        dto.setCostoGruppo(base + 1);
        dto.setCostoConto(conto(base + 1));
        dto.setAmmGruppo(base + 2);
        dto.setAmmConto(conto(base + 2));
        dto.setFondoGruppo(base + 3);
        dto.setFondoConto(conto(base + 3));
        dto.setPlusGruppo(base + 4);
        dto.setPlusConto(conto(base + 4));
        dto.setMinusGruppo(base + 5);
        dto.setMinusConto(conto(base + 5));
        return dto;
    }

    private void assertCategoria(CategoriaCespite entity, String tipoCespite, String descrizione, int base) {
        assertEquals(tipoCespite, entity.getTipoCespite());
        assertEquals("COD-" + base, entity.getCodice());
        assertEquals(descrizione, entity.getDescrizione());
        assertEquals((double) base, entity.getPercAmmortamento());
        assertEquals(base + 1, entity.getCostoGruppo());
        assertEquals(conto(base + 1), entity.getCostoConto());
        assertEquals(base + 2, entity.getAmmGruppo());
        assertEquals(conto(base + 2), entity.getAmmConto());
        assertEquals(base + 3, entity.getFondoGruppo());
        assertEquals(conto(base + 3), entity.getFondoConto());
        assertEquals(base + 4, entity.getPlusGruppo());
        assertEquals(conto(base + 4), entity.getPlusConto());
        assertEquals(base + 5, entity.getMinusGruppo());
        assertEquals(conto(base + 5), entity.getMinusConto());
    }

    private CategoriaCespite findByTipoCespite(String tipoCespite) {
        return CategoriaCespite.find("tipoCespite = :tipo",
                Parameters.with("tipo", tipoCespite)).firstResult();
    }

    private String uniqueTipoCespite() {
        String tipoCespite;
        do {
            tipoCespite = UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        } while (findByTipoCespite(tipoCespite) != null);
        return tipoCespite;
    }

    private String conto(int value) {
        return String.format("%06d", value);
    }
}
