package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.AggiornamentoPrezziDto;
import it.calolenoci.dto.PrezzoArticoloDto;
import it.calolenoci.dto.SogliaRicaricoDto;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class PrezziArticoliServiceTest {
    private static final AtomicInteger MOVIMENTO = new AtomicInteger(-900000000 + new Random().nextInt(100000000));
    @Inject
    PrezziArticoliService service;
    @Inject
    EntityManager em;

    @Test
    @TestTransaction
    void storicoRegistraEntrambiIGruppiEIgnoraPrezziInvariati() {
        String primo = articolo(100, "PZ", " ");
        String secondo = articolo(110, "PZ", " ");
        for (String codice : List.of(primo, secondo)) {
            movimento(codice, 1, 100, "PZ", "2024-01-01", 1);
            assertTrue(service.storico(codice).isEmpty());
        }
        AggiornamentoPrezziDto richiesta = richiesta(List.of(cerca(primo, "60", false)), "60");
        richiesta.setArticoliDeselezionati(List.of(cerca(secondo, "30", false)));
        richiesta.setPercentualeDeselezionati(new BigDecimal("30"));
        service.aggiorna(richiesta, "TEST");
        var storico = service.storico(primo);
        assertEquals(1, storico.size());
        assertEquals(0, new BigDecimal("100").compareTo(storico.get(0).getPrezzoPrecedente()));
        assertEquals(0, new BigDecimal("160").compareTo(storico.get(0).getPrezzoBase()));
        assertNotNull(storico.get(0).getDataModifica());
        assertEquals("TEST", storico.get(0).getUtente());
        assertEquals(0, new BigDecimal("130").compareTo(service.storico(secondo).get(0).getPrezzoBase()));
        service.aggiorna(richiesta(List.of(cerca(primo, "60", false)), "60"), "TEST");
        assertEquals(1, service.storico(primo).size());
        service.aggiorna(richiesta(List.of(cerca(primo, "70", false)), "70"), "TEST2");
        storico = service.storico(primo);
        assertEquals(2, storico.size());
        assertEquals(0, new BigDecimal("170").compareTo(storico.get(0).getPrezzoBase()));
        assertEquals(0, new BigDecimal("160").compareTo(storico.get(0).getPrezzoPrecedente()));
        assertEquals("TEST2", storico.get(0).getUtente());
    }

    @Test
    void storicoRifiutaCodiciNonValidi() {
        assertThrows(BadRequestException.class, () -> service.storico(null));
        assertThrows(BadRequestException.class, () -> service.storico(" "));
        assertThrows(BadRequestException.class, () -> service.storico("12345678901234"));
    }

    @Test
    @TestTransaction
    void usaCostoUltimoValidoERicaricoSulCostoSenzaIva() {
        String codice = articolo(150, "PZ", " ");
        movimento(codice, 10, 100, "PZ", "2024-01-01", 1);
        movimento(codice, 2, 200, "PZ", "2024-02-01", 2);
        movimento(codice, 2, 240, "PZ", "2024-02-01", 3);
        movimento(codice, 0, 240, "PZ", "2024-03-01", 4);
        movimento(codice, -2, 240, "PZ", "2024-03-01", 5);
        PrezzoArticoloDto riga = cerca(codice, "45", false);
        assertEquals(0, new BigDecimal("120").compareTo(riga.getCostoUltimo()));
        assertEquals(0, new BigDecimal("25").compareTo(riga.getRicaricoAttuale()));
        assertEquals(new BigDecimal("174.000"), riga.getNuovoPrezzo());
        assertTrue(riga.isAggiornabile());
    }

    @Test
    @TestTransaction
    void filtraPerClasseAncheConCodiceDiversoERifiutaCambioClasse() {
        String originale = articolo(100, "PZ", " ");
        String codice = "LUX" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        em.createNativeQuery("UPDATE ARTICOLI_TAB SET ARTICOLO=:nuovo WHERE ARTICOLO=:vecchio")
                .setParameter("nuovo", codice).setParameter("vecchio", originale).executeUpdate();
        em.createNativeQuery("UPDATE SALDIMAGAZZINO SET MARTICOLO=:nuovo WHERE MARTICOLO=:vecchio")
                .setParameter("nuovo", codice).setParameter("vecchio", originale).executeUpdate();
        movimento(codice, 1, 50, "PZ", "2024-01-01", 1);
        PrezzoArticoloDto visto = cerca(codice, "60", false);
        assertEquals(1, service.aggiorna(richiesta(List.of(visto), "60"), "TEST"));
        assertEquals(80, prezzo(codice), 0.000001);
        visto = cerca(codice, "60", false);
        em.createNativeQuery("UPDATE ARTICOLI_TAB SET CLASSEA1='997' WHERE ARTICOLO=:a")
                .setParameter("a", codice).executeUpdate();
        assertFalse(service.cerca("998", null, false).stream().anyMatch(a -> a.getArticolo().equals(codice)));
        AggiornamentoPrezziDto richiesta = richiesta(List.of(visto), "60");
        assertEquals(409, assertThrows(WebApplicationException.class,
                () -> service.aggiorna(richiesta, "TEST")).getResponse().getStatus());
        assertEquals(80, prezzo(codice), 0.000001);
    }

    @Test
    @TestTransaction
    void segnalaCostiMancantiUnitaDiverseEBloccati() {
        String senzaCosto = articolo(100, "PZ", " ");
        assertFalse(cerca(senzaCosto, "0", false).isAggiornabile());
        String unitaDiversa = articolo(100, "PZ", " ");
        movimento(unitaDiversa, 2, 100, "MQ", "2024-01-01", 1);
        PrezzoArticoloDto diversa = cerca(unitaDiversa, "45", false);
        assertFalse(diversa.isAggiornabile());
        assertNull(diversa.getRicaricoAttuale());
        assertNull(diversa.getNuovoPrezzo());
        String bloccato = articolo(100, "PZ", "S");
        movimento(bloccato, 2, 100, "PZ", "2024-01-01", 1);
        assertFalse(cerca(bloccato, "45", false).isAggiornabile());
        assertTrue(cerca(bloccato, "45", true).isAggiornabile());
    }

    @Test
    @TestTransaction
    void aggiornaSoloArticoliVisualizzatiConPrezzoRicalcolatoSulServer() {
        String codice = articolo(100, "PZ", " ");
        movimento(codice, 2, 100, "PZ", "2024-01-01", 1);
        PrezzoArticoloDto visto = cerca(codice, "45", false);
        visto.setNuovoPrezzo(new BigDecimal("999999"));
        String nuovo = articolo(200, "PZ", " ");
        movimento(nuovo, 2, 200, "PZ", "2024-01-01", 1);
        assertEquals(1, service.aggiorna(richiesta(List.of(visto), "45"), "TEST"));
        assertEquals(72.5, prezzo(codice), 0.000001);
        assertNull(service.ricaricoImpostato("998"));
        assertEquals(200, prezzo(nuovo), 0.000001);
        Object[] metadata = (Object[]) em.createNativeQuery(
                "SELECT SYS_UPDATEDATE,SYS_UPDATEUSER FROM ARTICOLI_TAB WHERE ARTICOLO=:a")
                .setParameter("a", codice).getSingleResult();
        assertNotNull(metadata[0]);
        assertEquals("TEST", metadata[1]);
    }

    @Test
    @TestTransaction
    void rifiutaPrezziCambiatiSenzaAggiornamentiParziali() {
        String primo = articolo(100, "PZ", " ");
        String secondo = articolo(100, "PZ", " ");
        movimento(primo, 2, 100, "PZ", "2024-01-01", 1);
        movimento(secondo, 2, 100, "PZ", "2024-01-01", 1);
        List<PrezzoArticoloDto> visti = List.of(cerca(primo, "45", false), cerca(secondo, "45", false));
        em.createNativeQuery("UPDATE ARTICOLI_TAB SET PREZZOBASE=101 WHERE ARTICOLO=:a")
                .setParameter("a", secondo).executeUpdate();
        WebApplicationException errore = assertThrows(WebApplicationException.class,
                () -> service.aggiorna(richiesta(visti, "45"), "TEST"));
        assertEquals(409, errore.getResponse().getStatus());
        assertEquals(100, prezzo(primo), 0.000001);
        assertEquals(101, prezzo(secondo), 0.000001);
    }

    @Test
    @TestTransaction
    void rifiutaNuovoAcquistoSuccessivoAllaRicerca() {
        String codice = articolo(100, "PZ", " ");
        movimento(codice, 2, 100, "PZ", "2024-01-01", 1);
        PrezzoArticoloDto visto = cerca(codice, "48", false);
        movimento(codice, 2, 120, "PZ", "2024-02-01", 2);
        WebApplicationException errore = assertThrows(WebApplicationException.class,
                () -> service.aggiorna(richiesta(List.of(visto), "48"), "TEST"));
        assertEquals(409, errore.getResponse().getStatus());
        assertEquals(100, prezzo(codice), 0.000001);
    }

    @Test
    @TestTransaction
    void rifiutaDuplicatiECodiciFuoriClasseA1() {
        String codice = articolo(100, "PZ", " ");
        movimento(codice, 2, 100, "PZ", "2024-01-01", 1);
        PrezzoArticoloDto visto = cerca(codice, "45", false);
        assertThrows(BadRequestException.class,
                () -> service.aggiorna(richiesta(List.of(visto, visto), "45"), "TEST"));
        visto.setArticolo("4141234567890");
        assertThrows(WebApplicationException.class,
                () -> service.aggiorna(richiesta(List.of(visto), "45"), "TEST"));
    }

    @Test
    void validaFiltriEPercentualeEArrotondamento() {
        assertThrows(BadRequestException.class, () -> service.cerca("", null, false));
        assertThrows(BadRequestException.class, () -> service.cerca("4%_", null, false));
        assertThrows(BadRequestException.class, () -> service.cerca("414", new BigDecimal("-1"), false));
        assertThrows(BadRequestException.class, () -> service.cerca("414", new BigDecimal("0.12345"), false));
        assertThrows(BadRequestException.class, () -> service.aggiorna(null, "TEST"));
        assertEquals(new BigDecimal("148.000"), PrezziArticoliService.calcolaPrezzo(new BigDecimal("100"), new BigDecimal("48")));
        assertEquals(new BigDecimal("14.507"), PrezziArticoliService.calcolaPrezzo(new BigDecimal("10.005"), new BigDecimal("45")));
    }

    @Test
    @TestTransaction
    void salvaTreDecimaliComeInAnteprima() {
        String codice = articolo(2, "PZ", " ");
        movimento(codice, 1, 1.23, "PZ", "2024-01-01", 1);
        PrezzoArticoloDto visto = cerca(codice, "60", false);
        assertEquals(new BigDecimal("1.968"), visto.getNuovoPrezzo());
        assertEquals(1, service.aggiorna(richiesta(List.of(visto), "60"), "TEST"));
        assertEquals(1.968, prezzo(codice), 0.000000001);
        assertEquals(new BigDecimal("60.00"), cerca(codice, "60", false).getRicaricoAttuale());
        assertEquals(new BigDecimal("1.969"), PrezziArticoliService.calcolaPrezzo(
                new BigDecimal("1.2303125"), new BigDecimal("60")));
    }

    @Test
    @TestTransaction
    void applicaPercentualiDistinteAiDueGruppi() {
        String selezionato = articolo(100, "PZ", " ");
        String deselezionato = articolo(100, "PZ", " ");
        movimento(selezionato, 2, 100, "PZ", "2024-01-01", 1);
        movimento(deselezionato, 2, 100, "PZ", "2024-01-01", 1);
        AggiornamentoPrezziDto richiesta = richiesta(List.of(cerca(selezionato, "45", false)), "45");
        richiesta.setArticoliDeselezionati(List.of(cerca(deselezionato, "45", false)));
        richiesta.setPercentualeDeselezionati(new BigDecimal("30"));
        assertEquals(2, service.aggiorna(richiesta, "TEST"));
        assertEquals(72.5, prezzo(selezionato), 0.000001);
        assertEquals(65, prezzo(deselezionato), 0.000001);
    }

    @Test
    @TestTransaction
    void datiCambiatiNeiDeselezionatiBloccanoEntrambiIGruppi() {
        String selezionato = articolo(100, "PZ", " ");
        String deselezionato = articolo(100, "PZ", " ");
        movimento(selezionato, 2, 100, "PZ", "2024-01-01", 1);
        movimento(deselezionato, 2, 100, "PZ", "2024-01-01", 1);
        AggiornamentoPrezziDto richiesta = richiesta(List.of(cerca(selezionato, "45", false)), "45");
        richiesta.setArticoliDeselezionati(List.of(cerca(deselezionato, "45", false)));
        richiesta.setPercentualeDeselezionati(new BigDecimal("30"));
        movimento(deselezionato, 2, 200, "PZ", "2024-02-01", 2);
        WebApplicationException errore = assertThrows(WebApplicationException.class,
                () -> service.aggiorna(richiesta, "TEST"));
        assertEquals(409, errore.getResponse().getStatus());
        assertEquals(100, prezzo(selezionato), 0.000001);
        assertEquals(100, prezzo(deselezionato), 0.000001);
    }

    @Test
    @TestTransaction
    void rifiutaArticoliPresentiInEntrambiIGruppi() {
        String codice = articolo(100, "PZ", " ");
        movimento(codice, 2, 100, "PZ", "2024-01-01", 1);
        PrezzoArticoloDto visto = cerca(codice, "45", false);
        AggiornamentoPrezziDto richiesta = richiesta(List.of(visto), "45");
        richiesta.setArticoliDeselezionati(List.of(visto));
        richiesta.setPercentualeDeselezionati(new BigDecimal("30"));
        assertThrows(BadRequestException.class, () -> service.aggiorna(richiesta, "TEST"));
        assertEquals(100, prezzo(codice), 0.000001);
    }

    @Test
    void rifiutaSecondaPercentualeMancanteOSenzaArticoli() {
        AggiornamentoPrezziDto richiesta = richiesta(List.of(new PrezzoArticoloDto()), "45");
        richiesta.setArticoliDeselezionati(List.of(new PrezzoArticoloDto()));
        assertThrows(BadRequestException.class, () -> service.aggiorna(richiesta, "TEST"));
        richiesta.setArticoliDeselezionati(List.of());
        richiesta.setPercentualeDeselezionati(new BigDecimal("30"));
        assertThrows(BadRequestException.class, () -> service.aggiorna(richiesta, "TEST"));
    }

    @Test
    @TestTransaction
    void rettificaCostiImportatiNeiDueGruppiSenzaModificareMovimenti() {
        String primo = articolo(210, "PZ", " ");
        String secondo = articolo(210, "PZ", " ");
        movimento(primo, 2, 200, "PZ", "2021-12-31 18:30:00", 1);
        movimento(secondo, 2, 200, "PZ", "2021-12-31", 1);
        PrezzoArticoloDto visto = cerca(primo, "60", false);
        assertTrue(visto.isCostoRettificato());
        assertEquals(0, new BigDecimal("100").compareTo(visto.getCostoUltimo()));
        assertEquals(0, new BigDecimal("140").compareTo(visto.getCostoRiferimento()));
        assertEquals(new BigDecimal("50.00"), visto.getRicaricoAttuale());
        assertEquals(new BigDecimal("224.000"), visto.getNuovoPrezzo());
        // Il server ricalcola il costo rettificato senza fidarsi del valore del client.
        visto.setCostoRiferimento(new BigDecimal("9999"));
        visto.setCostoRettificato(false);
        AggiornamentoPrezziDto richiesta = richiesta(List.of(visto), "60");
        richiesta.setArticoliDeselezionati(List.of(cerca(secondo, "60", false)));
        richiesta.setPercentualeDeselezionati(new BigDecimal("70"));
        assertEquals(2, service.aggiorna(richiesta, "TEST"));
        assertEquals(224, prezzo(primo), 0.000001);
        assertEquals(238, prezzo(secondo), 0.000001);
        for (String codice : List.of(primo, secondo)) {
            Object[] movimento = (Object[]) em.createNativeQuery(
                    "SELECT VALORE,MQUANTITA,VALOREUNITARIO FROM MAGAZZINO WHERE MARTICOLO=:a")
                    .setParameter("a", codice).getSingleResult();
            assertEquals(200, ((Number) movimento[0]).doubleValue(), 0.000001);
            assertEquals(2, ((Number) movimento[1]).doubleValue(), 0.000001);
            assertEquals(100, ((Number) movimento[2]).doubleValue(), 0.000001);
            assertEquals(0, new BigDecimal("100").compareTo(cerca(codice, "60", false).getCostoUltimo()));
            assertEquals(0, new BigDecimal("140").compareTo(cerca(codice, "60", false).getCostoRiferimento()));
        }
    }

    @Test
    @TestTransaction
    void rettificaSoloLaDataEsattaDelMovimentoUltimo() {
        for (String data : List.of("2021-12-30", "2022-01-01")) {
            String codice = articolo(100, "PZ", " ");
            movimento(codice, 1, 100, "PZ", data, 1);
            PrezzoArticoloDto visto = cerca(codice, "60", false);
            assertFalse(visto.isCostoRettificato());
            assertEquals(visto.getCostoUltimo(), visto.getCostoRiferimento());
            assertEquals(new BigDecimal("160.000"), visto.getNuovoPrezzo());
        }
        String codice = articolo(100, "PZ", " ");
        movimento(codice, 1, 100, "PZ", "2021-12-31", 1);
        movimento(codice, 1, 120, "PZ", "2022-01-01", 2);
        PrezzoArticoloDto visto = cerca(codice, "60", false);
        assertFalse(visto.isCostoRettificato());
        assertEquals(new BigDecimal("192.000"), visto.getNuovoPrezzo());
        assertNull(cerca(articolo(100, "PZ", " "), "60", false).getCostoRiferimento());
    }

    @Test
    @TestTransaction
    void cambioDataDelMovimentoInvalidaAnteprimaAncheAParitaDiCosto() {
        String codice = articolo(100, "PZ", " ");
        movimento(codice, 1, 100, "PZ", "2021-12-31", 1);
        PrezzoArticoloDto visto = cerca(codice, "60", false);
        em.createNativeQuery("UPDATE MAGAZZINO SET DATAOPMAGAZZINO='2022-01-01' WHERE MARTICOLO=:a")
                .setParameter("a", codice).executeUpdate();
        assertEquals(409, assertThrows(WebApplicationException.class,
                () -> service.aggiorna(richiesta(List.of(visto), "60"), "TEST")).getResponse().getStatus());
        assertEquals(100, prezzo(codice), 0.000001);
    }

    @Test
    @TestTransaction
    void salvaSogliaSeparatamenteSenzaCambiarePrezziEApplicareRicarichiNonLaModifica() {
        assertNull(service.ricaricoImpostato("998"));
        String primo = articolo(100, "PZ", " ");
        String secondo = articolo(100, "PZ", " ");
        movimento(primo, 1, 100, "PZ", "2024-01-01", 1);
        movimento(secondo, 1, 100, "PZ", "2024-01-01", 1);
        service.salvaSoglia(soglia("60", null), "TEST");
        assertEquals(100, prezzo(primo), 0.000001);
        assertEquals(100, prezzo(secondo), 0.000001);
        assertEquals(0, new BigDecimal("60").compareTo(service.ricaricoImpostato("998")));
        AggiornamentoPrezziDto richiesta = richiesta(List.of(cerca(primo, "45", false)), "45");
        richiesta.setArticoliDeselezionati(List.of(cerca(secondo, "45", false)));
        richiesta.setPercentualeDeselezionati(new BigDecimal("70"));
        service.aggiorna(richiesta, "TEST");
        assertEquals(0, new BigDecimal("60").compareTo(service.ricaricoImpostato("998")));
        assertEquals(145, prezzo(primo), 0.000001);
        assertEquals(170, prezzo(secondo), 0.000001);
        service.salvaSoglia(soglia("0", "60"), "TEST2");
        assertEquals(0, BigDecimal.ZERO.compareTo(service.ricaricoImpostato("998")));
        assertEquals(145, prezzo(primo), 0.000001);
        assertEquals(170, prezzo(secondo), 0.000001);
        Object[] dati = (Object[]) em.createNativeQuery(
                "SELECT CREATED_BY,UPDATED_BY,CREATED_AT,UPDATED_AT FROM GO_RICARICO_FORNITORE WHERE CLASSEA1='998'")
                .getSingleResult();
        assertEquals("TEST", dati[0]);
        assertEquals("TEST2", dati[1]);
        assertNotNull(dati[2]);
        assertNotNull(dati[3]);
        assertEquals(1, ((Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM GO_RICARICO_FORNITORE WHERE CLASSEA1='998'").getSingleResult()).intValue());
    }

    @Test
    void validaSoglia() {
        assertThrows(BadRequestException.class, () -> service.salvaSoglia(null, "TEST"));
        assertThrows(BadRequestException.class, () -> service.salvaSoglia(soglia("-1", null), "TEST"));
        assertThrows(BadRequestException.class, () -> service.salvaSoglia(soglia("0.12345", null), "TEST"));
    }

    @Test
    @TestTransaction
    void rifiutaAggiornamentiSogliaConcorrenti() {
        service.salvaSoglia(soglia("60", null), "TEST");
        assertEquals(409, assertThrows(WebApplicationException.class,
                () -> service.salvaSoglia(soglia("70", null), "TEST2")).getResponse().getStatus());
        assertEquals(0, new BigDecimal("60").compareTo(service.ricaricoImpostato("998")));
        assertNull(service.ricaricoImpostato("997"));
    }

    @Test
    @TestTransaction
    void portaASogliaAggiornaSoloISelezionatiEMantieneSogliaEAltriPrezzi() {
        String sotto = articolo(100, "PZ", " ");
        String escluso = articolo(110, "PZ", " ");
        String sopra = articolo(170, "PZ", " ");
        for (String codice : List.of(sotto, escluso, sopra)) {
            movimento(codice, 1, 100, "PZ", "2024-01-01", 1);
        }
        service.salvaSoglia(soglia("60", null), "TEST");
        AggiornamentoPrezziDto richiesta = richiesta(List.of(cerca(sotto, "60", false)), "60");
        richiesta.setPortaASoglia(true);
        assertEquals(1, service.aggiorna(richiesta, "TEST"));
        assertEquals(160, prezzo(sotto), 0.000001);
        assertEquals(110, prezzo(escluso), 0.000001);
        assertEquals(170, prezzo(sopra), 0.000001);
        assertEquals(0, new BigDecimal("60").compareTo(service.ricaricoImpostato("998")));
    }

    @Test
    @TestTransaction
    void portaASogliaAccettaSecondoRicaricoSoloPerDeselezionatiSottoSoglia() {
        String sotto = articolo(100, "PZ", " ");
        String escluso = articolo(110, "PZ", " ");
        String sopra = articolo(170, "PZ", " ");
        for (String codice : List.of(sotto, escluso, sopra)) {
            movimento(codice, 1, 100, "PZ", "2024-01-01", 1);
        }
        service.salvaSoglia(soglia("60", null), "TEST");
        AggiornamentoPrezziDto richiesta = richiesta(List.of(cerca(sotto, "60", false)), "60");
        richiesta.setPortaASoglia(true);
        richiesta.setPercentualeDeselezionati(new BigDecimal("30"));
        richiesta.setArticoliDeselezionati(List.of(cerca(sopra, "30", false)));
        assertThrows(BadRequestException.class, () -> service.aggiorna(richiesta, "TEST"));
        assertEquals(100, prezzo(sotto), 0.000001);
        assertEquals(170, prezzo(sopra), 0.000001);
        richiesta.setArticoliDeselezionati(List.of(cerca(escluso, "30", false)));
        assertEquals(2, service.aggiorna(richiesta, "TEST"));
        assertEquals(160, prezzo(sotto), 0.000001);
        assertEquals(130, prezzo(escluso), 0.000001);
        assertEquals(170, prezzo(sopra), 0.000001);
        assertEquals(0, new BigDecimal("60").compareTo(service.ricaricoImpostato("998")));
    }

    @Test
    @TestTransaction
    void portaASogliaRifiutaArticoliInSogliaPrimaDiScrivere() {
        String sotto = articolo(100, "PZ", " ");
        String pari = articolo(159.98, "PZ", " ");
        movimento(sotto, 1, 100, "PZ", "2024-01-01", 1);
        movimento(pari, 1, 100, "PZ", "2024-01-01", 1);
        service.salvaSoglia(soglia("60", null), "TEST");
        AggiornamentoPrezziDto richiesta = richiesta(List.of(cerca(sotto, "60", false), cerca(pari, "60", false)), "60");
        richiesta.setPortaASoglia(true);
        assertThrows(BadRequestException.class, () -> service.aggiorna(richiesta, "TEST"));
        assertEquals(100, prezzo(sotto), 0.000001);
        assertEquals(159.98, prezzo(pari), 0.000001);
    }

    @Test
    @TestTransaction
    void portaASogliaRichiedeSogliaSalvataEPercentualeUguale() {
        String codice = articolo(100, "PZ", " ");
        movimento(codice, 1, 100, "PZ", "2024-01-01", 1);
        AggiornamentoPrezziDto richiesta = richiesta(List.of(cerca(codice, "60", false)), "60");
        richiesta.setPortaASoglia(true);
        assertThrows(BadRequestException.class, () -> service.aggiorna(richiesta, "TEST"));
    }

    private SogliaRicaricoDto soglia(String percentuale, String precedente) {
        SogliaRicaricoDto dto = new SogliaRicaricoDto();
        dto.setClasseA1("998");
        dto.setPercentuale(new BigDecimal(percentuale));
        dto.setRicaricoImpostato(precedente == null ? null : new BigDecimal(precedente));
        return dto;
    }

    @Test
    @TestTransaction
    void conflittoNonSalvaLaSogliaERifiutaUnoSnapshotDiSogliaSuperato() {
        String codice = articolo(100, "PZ", " ");
        movimento(codice, 1, 100, "PZ", "2024-01-01", 1);
        AggiornamentoPrezziDto richiesta = richiesta(List.of(cerca(codice, "60", false)), "60");
        richiesta.getArticoli().get(0).setPrezzoBase(new BigDecimal("999"));
        assertThrows(WebApplicationException.class, () -> service.aggiorna(richiesta, "TEST"));
        assertNull(service.ricaricoImpostato("998"));
        richiesta.setArticoli(List.of(cerca(codice, "60", false)));
        em.createNativeQuery("INSERT INTO GO_RICARICO_FORNITORE(CLASSEA1,PERCENTUALE) VALUES('998',50)").executeUpdate();
        assertEquals(409, assertThrows(WebApplicationException.class,
                () -> service.aggiorna(richiesta, "TEST")).getResponse().getStatus());
        assertEquals(100, prezzo(codice), 0.000001);
        assertEquals(0, new BigDecimal("50").compareTo(service.ricaricoImpostato("998")));
        assertNull(service.ricaricoImpostato("997"));
    }

    @Test
    @TestTransaction
    void aggregaGiacenzaPrincipaleDiTuttiIMagazziniSenzaDuplicareIlPrezzo() {
        String codice = articolo(100, "PZ", " ");
        eliminaSaldi(codice);
        saldo(codice, "B", "", "1", 3D);
        saldo(codice, "B", "A", "1", 2D);
        saldo(codice, "C", "", "1", -1D);
        saldo(codice, "Z", "", "1", 0D);
        saldo(codice, "OST", "", "1", 4D);
        saldo(codice, "B", "", "2", 100D);
        movimento(codice, 1, 50, "PZ", "2024-01-01", 1);
        var trovati = service.cerca("998", new BigDecimal("60"), false).stream()
                .filter(a -> codice.equals(a.getArticolo())).toList();
        assertEquals(1, trovati.size());
        PrezzoArticoloDto visto = trovati.getFirst();
        assertEquals(0, new BigDecimal("8").compareTo(visto.getGiacenzaTotale()));
        assertEquals(List.of("B", "OST"), visto.getGiacenze().stream().map(g -> g.getMagazzino()).toList());
        assertEquals(0, new BigDecimal("5").compareTo(visto.getGiacenze().getFirst().getGiacenza()));
        assertEquals(1, service.aggiorna(richiesta(List.of(visto), "60"), "TEST"));
        assertEquals(80, prezzo(codice), 0.000001);
    }

    @Test
    @TestTransaction
    void escludeGiacenzeNulleZeroNegativeEQuantitaSecondarie() {
        String zero = articolo(100, "PZ", " ");
        eliminaSaldi(zero);
        saldo(zero, "B", "", "1", 3D);
        saldo(zero, "C", "", "1", -3D);
        String negativo = articolo(100, "PZ", " ");
        eliminaSaldi(negativo);
        saldo(negativo, "B", "", "1", -1D);
        String nullo = articolo(100, "PZ", " ");
        eliminaSaldi(nullo);
        saldo(nullo, "B", "", "1", null);
        String assente = articolo(100, "PZ", " ");
        eliminaSaldi(assente);
        String secondaria = articolo(100, "PZ", " ");
        eliminaSaldi(secondaria);
        saldo(secondaria, "B", "", "2", 10D);
        var esclusi = List.of(zero, negativo, nullo, assente, secondaria);
        assertFalse(service.cerca("998", null, true).stream().anyMatch(a -> esclusi.contains(a.getArticolo())));
        String soloOst = articolo(100, "PZ", " ");
        eliminaSaldi(soloOst);
        saldo(soloOst, "OST", "", "1", 0.5D);
        assertEquals(0, new BigDecimal("0.5").compareTo(cerca(soloOst, "0", true).getGiacenzaTotale()));
    }

    @Test
    @TestTransaction
    void rifiutaInteroAggiornamentoSeLaGiacenzaSiEsaurisceDopoAnteprima() {
        String primo = articolo(100, "PZ", " ");
        String secondo = articolo(100, "PZ", " ");
        movimento(primo, 1, 50, "PZ", "2024-01-01", 1);
        movimento(secondo, 1, 50, "PZ", "2024-01-01", 1);
        PrezzoArticoloDto vistoPrimo = cerca(primo, "60", false);
        PrezzoArticoloDto vistoSecondo = cerca(secondo, "60", false);
        em.createNativeQuery("UPDATE SALDIMAGAZZINO SET QGIACENZA=0 WHERE MARTICOLO=:a")
                .setParameter("a", secondo).executeUpdate();
        var richiesta = richiesta(List.of(vistoPrimo, vistoSecondo), "60");
        assertEquals(409, assertThrows(WebApplicationException.class,
                () -> service.aggiorna(richiesta, "TEST")).getResponse().getStatus());
        assertEquals(100, prezzo(primo), 0.000001);
        assertEquals(100, prezzo(secondo), 0.000001);
    }

    private String articolo(double prezzo, String unita, String bloccato) {
        String codice = "998" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        em.createNativeQuery("""
                INSERT INTO ARTICOLI_TAB(ARTICOLO,DESCRARTICOLO,UNITAMISURA,PREZZOBASE,BLOCCATO,CODICEIVA,CLASSEA1)
                VALUES (:a,'TEST PREZZI',:u,:p,:b,'22','998')
                """).setParameter("a", codice).setParameter("u", unita)
                .setParameter("p", prezzo).setParameter("b", bloccato).executeUpdate();
        saldo(codice, "B", "", "1", 10D);
        return codice;
    }

    private void saldo(String articolo, String magazzino, String variante, String qty, Double giacenza) {
        em.createNativeQuery("""
                INSERT INTO SALDIMAGAZZINO
                    (MARTICOLO,VAR1,VAR2,VAR3,VAR4,VAR5,MMAGAZZINO,QTY,QGIACENZA)
                VALUES (:a,:v,'','','','',:m,:q,:g)
                """).setParameter("a", articolo).setParameter("v", variante)
                .setParameter("m", magazzino).setParameter("q", qty).setParameter("g", giacenza).executeUpdate();
    }

    private void eliminaSaldi(String articolo) {
        em.createNativeQuery("DELETE FROM SALDIMAGAZZINO WHERE MARTICOLO=:a")
                .setParameter("a", articolo).executeUpdate();
    }

    private void movimento(String articolo, double quantita, double valore, String unita, String data, int rigo) {
        int id = MOVIMENTO.incrementAndGet();
        em.createNativeQuery("""
                INSERT INTO MAGAZZINO(ANNO,SERIEMAGAZZINO,PROGRESSIVOMAG,FLAGCONTROMAG,RIGO,
                    PROGRGENERALE,MARTICOLO,MQUANTITA,VALORE,VALOREUNITARIO,MUNITA,MCAUSALE,DATAOPMAGAZZINO,IVAMAG)
                VALUES(2024,'TST',:id,' ',:r,:id,:a,:q,:v,100,:u,
                    (SELECT TOP 1 MCAUSALE FROM TMMC WHERE AGGIORNAULTIMO='S' ORDER BY MCAUSALE),:d,'22')
                """).setParameter("id", id).setParameter("r", rigo).setParameter("a", articolo)
                .setParameter("q", quantita).setParameter("v", valore).setParameter("u", unita)
                .setParameter("d", data).executeUpdate();
    }

    private PrezzoArticoloDto cerca(String codice, String pct, boolean bloccati) {
        return service.cerca("998", new BigDecimal(pct), bloccati).stream()
                .filter(r -> r.getArticolo().equals(codice)).findFirst().orElseThrow();
    }

    private AggiornamentoPrezziDto richiesta(List<PrezzoArticoloDto> righe, String pct) {
        AggiornamentoPrezziDto richiesta = new AggiornamentoPrezziDto();
        richiesta.setClasseA1("998");
        richiesta.setRicaricoImpostato(service.ricaricoImpostato("998"));
        richiesta.setPercentuale(new BigDecimal(pct));
        richiesta.setArticoli(righe);
        return richiesta;
    }

    private double prezzo(String articolo) {
        return ((Number) em.createNativeQuery("SELECT PREZZOBASE FROM ARTICOLI_TAB WHERE ARTICOLO=:a")
                .setParameter("a", articolo).getSingleResult()).doubleValue();
    }
}
