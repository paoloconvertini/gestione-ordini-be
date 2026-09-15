package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.ResiduoDto;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ResiduoServiceTest {

    private static final int ANNO_TEST = 1902;

    @Inject
    ResiduoService residuoService;

    @Inject
    EntityManager entityManager;

    @Test
    @TestTransaction
    void restituisceUnaListaVuotaPerInputNulloOVuoto() {
        assertTrue(residuoService.calcolaResidui(null).isEmpty());
        assertTrue(residuoService.calcolaResidui(List.of()).isEmpty());
    }

    @Test
    @TestTransaction
    void mantieneLaQuantitaOrdinataQuandoNonEsistonoBolle() {
        OrdineDettaglioDto riga = newRiga(newId(), 10D);

        ResiduoDto result = residuoService.calcolaResidui(List.of(riga)).getFirst();

        assertEquals(10D, result.getQtaOrdinata());
        assertEquals(0D, result.getQtaBollata());
        assertEquals(10D, result.getResiduo());
        assertEquals(riga.getFArticolo(), result.getFArticolo());
        assertEquals(riga.getFDescrArticolo(), result.getFDescrArticolo());
    }

    @Test
    @TestTransaction
    void sommaLeQuantitaDiPiuBolleSerieB() {
        int progrOrdine = newId();
        insertBolla("B", newId(), progrOrdine, 2.5D);
        insertBolla("B", newId(), progrOrdine, 1.5D);

        ResiduoDto result = residuoService.calcolaResidui(List.of(newRiga(progrOrdine, 10D))).getFirst();

        assertEquals(4D, result.getQtaBollata());
        assertEquals(6D, result.getResiduo());
    }

    @Test
    @TestTransaction
    void ignoraDocumentiDiversiDallaSerieBEImpedisceResiduiNegativi() {
        int progrOrdine = newId();
        insertBolla("A", newId(), progrOrdine, 8D);
        insertBolla("B", newId(), progrOrdine, 12D);

        ResiduoDto result = residuoService.calcolaResidui(List.of(newRiga(progrOrdine, 10D))).getFirst();

        assertEquals(12D, result.getQtaBollata());
        assertEquals(0D, result.getResiduo());
    }

    @Test
    @TestTransaction
    void gestisceQuantitaNullaERigheSenzaIdentificativo() {
        OrdineDettaglioDto senzaQuantita = newRiga(newId(), null);
        OrdineDettaglioDto senzaId = newRiga(null, 5D);

        List<ResiduoDto> result = residuoService.calcolaResidui(List.of(senzaQuantita, senzaId));

        assertEquals(1, result.size());
        assertEquals(0D, result.getFirst().getQtaOrdinata());
        assertEquals(0D, result.getFirst().getResiduo());
    }

    @Test
    @TestTransaction
    void laMappaMantieneLaPrimaRigaPerIdentificativiDuplicati() {
        int progrOrdine = newId();
        OrdineDettaglioDto prima = newRiga(progrOrdine, 10D);
        OrdineDettaglioDto seconda = newRiga(progrOrdine, 20D);

        Map<Integer, ResiduoDto> result = residuoService.calcolaResiduiMap(List.of(prima, seconda));

        assertEquals(1, result.size());
        assertEquals(10D, result.get(progrOrdine).getResiduo());
    }

    private OrdineDettaglioDto newRiga(Integer progrGenerale, Double quantita) {
        OrdineDettaglioDto dto = new OrdineDettaglioDto();
        dto.setAnno(ANNO_TEST);
        dto.setSerie("T");
        dto.setProgressivo(newId());
        dto.setRigo(1);
        dto.setProgrGenerale(progrGenerale);
        dto.setFArticolo("ART-TEST");
        dto.setFDescrArticolo("Articolo di test");
        dto.setQuantita(quantita);
        return dto;
    }

    private void insertBolla(String serie, int progressivo, int progrOrdine, double quantita) {
        entityManager.createNativeQuery("INSERT INTO FATTURE (ANNO, SERIE, PROGRESSIVO) " +
                        "VALUES (:anno, :serie, :progressivo)")
                .setParameter("anno", ANNO_TEST)
                .setParameter("serie", serie)
                .setParameter("progressivo", progressivo)
                .executeUpdate();
        entityManager.createNativeQuery("INSERT INTO FATTURE2 " +
                        "(ANNO, SERIE, PROGRESSIVO, RIGO, PROGRGENERALE, PROGRORDCLI, QUANTITA) " +
                        "VALUES (:anno, :serie, :progressivo, 1, :progrGenerale, :progrOrdine, :quantita)")
                .setParameter("anno", ANNO_TEST)
                .setParameter("serie", serie)
                .setParameter("progressivo", progressivo)
                .setParameter("progrGenerale", newId())
                .setParameter("progrOrdine", progrOrdine)
                .setParameter("quantita", quantita)
                .executeUpdate();
    }

    private int newId() {
        return -Math.abs(UUID.randomUUID().hashCode());
    }
}
