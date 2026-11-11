package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.RiordinoDto;
import it.calolenoci.entity.GoOrdVeicolo;
import it.calolenoci.entity.GoOrdVeicoloPK;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class GiroConsegneServiceTest {
    @Inject
    EntityManager entityManager;

    @Test
    @TestTransaction
    void riordinaSoloLeConsegneEsistenti() {
        int progressivo = -1_000_000_000 - Math.floorMod(UUID.randomUUID().hashCode(), 500_000_000);
        GoOrdVeicolo consegna = new GoOrdVeicolo();
        consegna.setId(new GoOrdVeicoloPK(1903, "T", progressivo));
        consegna.setIdVeicolo(progressivo);
        consegna.setDataConsegna(LocalDate.of(1903, 1, 1));
        consegna.setOraConsegna('M');
        consegna.setOrdine(1L);
        consegna.setVenditore(false);
        consegna.persist();
        RiordinoDto presente = riordino(progressivo, 7L);
        RiordinoDto assente = riordino(progressivo - 1, 9L);

        new GiroConsegneService().riordinaConsegne(List.of(presente, assente));

        entityManager.flush();
        entityManager.clear();
        GoOrdVeicolo updated = GoOrdVeicolo.findById(new GoOrdVeicoloPK(1903, "T", progressivo));
        assertEquals(7L, updated.getOrdine());
    }

    private RiordinoDto riordino(int progressivo, long ordine) {
        RiordinoDto dto = new RiordinoDto();
        dto.setAnno(1903);
        dto.setSerie("T");
        dto.setProgressivo(progressivo);
        dto.setOrdine(ordine);
        return dto;
    }
}
