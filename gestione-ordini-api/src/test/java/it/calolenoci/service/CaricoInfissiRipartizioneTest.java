package it.calolenoci.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CaricoInfissiRipartizioneTest {
    private List<BigDecimal> ripartisci(String totale, String... quantita) {
        return CaricoInfissiService.ripartisci(new BigDecimal(totale),
                java.util.Arrays.stream(quantita).map(BigDecimal::new).toList());
    }

    @Test void pesaLeQuantitaConCostoUnitarioComune() {
        assertEquals(List.of(new BigDecimal("400.00"), new BigDecimal("600.00")), ripartisci("1000.00", "2", "3"));
    }
    @Test void assegnaIlCentesimoResiduoInModoStabile() {
        assertEquals(List.of(new BigDecimal("33.34"), new BigDecimal("33.33"), new BigDecimal("33.33")),
                ripartisci("100.00", "1", "1", "1"));
    }
    @Test void usaIRestiMaggioriInveceDiScaricareLaDifferenzaSullUltimaRiga() {
        assertEquals(List.of(new BigDecimal("0.00"), new BigDecimal("0.01")), ripartisci("0.01", "1", "2"));
        assertEquals(List.of(new BigDecimal("0.01"), new BigDecimal("0.01"), new BigDecimal("0.00")),
                ripartisci("0.02", "1", "1", "1"));
    }
    @Test void sommaEsattaAncheConQuantitaFrazionarieMolteRigheETotaliPiccoli() {
        var quantita = List.of(new BigDecimal("0.125"), new BigDecimal("1.375"), new BigDecimal("7.3"), new BigDecimal("2"));
        for (int centesimi = 1; centesimi < 10000; centesimi += 13) {
            BigDecimal totale = BigDecimal.valueOf(centesimi, 2);
            var quote = CaricoInfissiService.ripartisci(totale, quantita);
            assertEquals(totale, quote.stream().reduce(new BigDecimal("0.00"), BigDecimal::add));
            assertTrue(quote.stream().allMatch(q -> q.signum() >= 0 && q.scale() == 2));
        }
    }
}
