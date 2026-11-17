package it.calolenoci.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RegolePagamentoTest {
    private final LocalDate conferma = LocalDate.of(2026, 11, 17);
    private RegolePagamento.Riga r(int id, String codice, int perc, LocalDate data, String descrizione) {
        return new RegolePagamento.Riga(id, id, codice, descrizione, BigDecimal.valueOf(perc), data);
    }
    @Test void unAccontoImmediatoESaldoAlPronto() {
        var acconto = r(1, "*ACCONTO", 30, conferma, "Acconto");
        var saldo = r(2, "*SALDO", 70, conferma, "Saldo");
        assertTrue(RegolePagamento.anomalie(List.of(acconto, saldo), conferma).values().stream().allMatch(String::isEmpty));
        assertEquals(conferma, RegolePagamento.scadenza(acconto, conferma));
        assertNull(RegolePagamento.scadenza(saldo, conferma));
    }
    @Test void accontoCentoPercentoConsenteSaldoZero() {
        assertTrue(RegolePagamento.anomalie(List.of(r(1, "*ACCONTO", 100, conferma, ""),
                r(2, "*SALDO", 0, conferma, "")), conferma).values().stream().allMatch(String::isEmpty));
    }
    @Test void dataNullaHaSignificatoPredefinito() {
        assertEquals(conferma, RegolePagamento.scadenza(r(1, "*ACCONTO", 30, null, ""), conferma));
        assertNull(RegolePagamento.scadenza(r(2, "*SALDO", 70, null, ""), conferma));
    }
    @Test void scadenzaPrimoDelMeseAnchePerSaldo() {
        assertEquals(LocalDate.of(2027, 1, 1), RegolePagamento.scadenza(r(1, "*ACCONTO", 30, LocalDate.of(2027, 1, 20), ""), conferma));
        assertEquals(LocalDate.of(2026, 12, 1), RegolePagamento.scadenza(r(2, "*SALDO", 70, LocalDate.of(2026, 12, 24), ""), conferma));
    }
    @Test void piuAccontiRichiedonoDateDistinteModificate() {
        var righe = List.of(r(1, "*ACCONTO", 20, conferma, ""), r(2, "*ACCONTO", 20, LocalDate.of(2026, 12, 1), ""),
                r(3, "*ACCONTO", 20, LocalDate.of(2026, 12, 1), ""), r(4, "*SALDO", 40, conferma, ""));
        var anomalie = RegolePagamento.anomalie(righe, conferma);
        assertTrue(anomalie.get(1).contains("diversa"));
        assertTrue(anomalie.get(2).contains("stessa data"));
        assertTrue(anomalie.get(3).contains("stessa data"));
    }
    @Test void controllaPercentualiSaldoEConferma() {
        var anomalie = RegolePagamento.anomalie(List.of(r(1, "*ACCONTO", 0, conferma, "")), null);
        assertTrue(anomalie.get(1).contains("Percentuale"));
        assertTrue(anomalie.get(1).contains("*SALDO"));
        assertTrue(anomalie.get(1).contains("conferma"));
        assertTrue(RegolePagamento.anomalie(List.of(r(1, "*ACCONTO", 20, conferma, ""), r(2, "*SALDO", 70, conferma, "")), conferma).get(1).contains("100"));
    }
    @Test void aggiornaMeseSenzaDuplicazioniEAnnoSoloSeDiversoDalCorrente() {
        var dicembre = r(1, "*ACCONTO", 30, LocalDate.of(2026, 12, 1), "Acconto");
        assertEquals("Acconto DICEMBRE", RegolePagamento.descrizione(dicembre, conferma, 2026));
        assertEquals("Acconto DICEMBRE", RegolePagamento.descrizione(r(1, "*ACCONTO", 30, dicembre.data(), "Acconto DICEMBRE"), conferma, 2026));
        assertEquals("Acconto GENNAIO 2027", RegolePagamento.descrizione(r(1, "*ACCONTO", 30, LocalDate.of(2027, 1, 1), "Acconto DICEMBRE"), conferma, 2026));
        assertEquals("Acconto GENNAIO", RegolePagamento.descrizione(r(1, "*ACCONTO", 30, LocalDate.of(2027, 1, 1), "Acconto GENNAIO 2027"), conferma, 2027));
    }
}
