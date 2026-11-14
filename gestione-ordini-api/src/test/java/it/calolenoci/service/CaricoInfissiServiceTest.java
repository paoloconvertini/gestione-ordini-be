package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.CaricoInfissiDto;
import it.calolenoci.entity.*;
import it.calolenoci.exception.BusinessException;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CaricoInfissiServiceTest {
    @Inject CaricoInfissiService service;
    @Inject EntityManager em;

    private OrdineDettaglio riga(int numero, double quantita, String codice) {
        Ordine o = Ordine.findAll().firstResult();
        OrdineDettaglio r = new OrdineDettaglio();
        r.setAnno(o.getAnno()); r.setSerie(o.getSerie()); r.setProgressivo(o.getProgressivo());
        r.setProgrGenerale(-1_000_000_000 - Math.floorMod(UUID.randomUUID().hashCode(), 500_000_000));
        r.setRigo(-10000 - numero); r.setFArticolo(codice); r.setFDescrArticolo("Infisso test");
        r.setTipoRigo(" "); r.setQuantita(quantita); r.setFUnitaMisura("PZ"); r.setMagazz("B");
        r.setFColli(1); r.setScontoArticolo(10D); r.persist(); em.flush(); return r;
    }
    private String codice() { return "I" + UUID.randomUUID().toString().replace("-", "").substring(0, 12); }
    private void fornitore(String codice) {
        em.createNativeQuery("INSERT INTO FORNALTERNATIVI (ARTICOLO,GRUPPOF,CONTOF) VALUES (:art,321,'000123')")
                .setParameter("art", codice).executeUpdate();
    }
    private CaricoInfissiDto richiesta(String totale, OrdineDettaglio... articoli) {
        CaricoInfissiDto dto = new CaricoInfissiDto(); dto.setArticoli(Arrays.asList(articoli));
        dto.setTotaleImponibile(new BigDecimal(totale)); dto.setNumDoc("INF-TEST");
        dto.setDataDocumento(LocalDate.now()); dto.setDataOperazione(LocalDate.now()); dto.setCausale("C01"); dto.setVettore("V01");
        return dto;
    }

    @Test @TestTransaction void salvaQuoteEsatteEGiacenzeSenzaModificareOrdiniClienteOFornitore() {
        String codice = codice(); fornitore(codice);
        var a = riga(1, 1, codice); var b = riga(2, 1, codice); var c = riga(3, 1, codice);
        var dto = richiesta("100.00", a, b, c);
        var anteprima = service.anteprima(dto);
        assertEquals(new BigDecimal("100.00"), anteprima.getRighe().stream().map(r -> r.getImponibile()).reduce(BigDecimal.ZERO, BigDecimal::add));
        assertTrue(service.salva(dto, "TEST").startsWith("Creato carico infissi n.")); em.flush();
        var valori = em.createNativeQuery("SELECT VALORE,VALOREUNITARIO,PREZZO,SCONTOARTICOLO,PID,PROVENIENZA FROM MAGAZZINO WHERE MARTICOLO=:art ORDER BY RIGO")
                .setParameter("art", codice).getResultList();
        assertEquals(3, valori.size());
        BigDecimal totale = BigDecimal.ZERO;
        for (int i = 0; i < valori.size(); i++) {
            Object[] r = (Object[]) valori.get(i);
            BigDecimal valore = BigDecimal.valueOf(((Number) r[0]).doubleValue()).setScale(2);
            assertEquals(anteprima.getRighe().get(i).getImponibile(), valore); totale = totale.add(valore);
            assertEquals(0D, ((Number) r[3]).doubleValue()); assertEquals(0, ((Number) r[4]).intValue());
            assertTrue(r[5] == null || r[5].toString().isBlank());
        }
        assertEquals(new BigDecimal("100.00"), totale);
        SaldiMagazzino saldo = SaldiMagazzino.find("marticolo=?1 and mmagazzino='B'", codice).firstResult();
        assertEquals(3D, saldo.getQcarichi()); assertEquals(3D, saldo.getQgiacenza());
        assertEquals(1D, a.getQuantita()); assertEquals(10D, a.getScontoArticolo());
    }

    @Test @TestTransaction void ripartisceInBaseAlleQuantitaAncheSulMovimento() {
        String codice = codice(); fornitore(codice);
        var dto = richiesta("1000.00", riga(1, 2, codice), riga(2, 3, codice));
        service.salva(dto, "TEST"); em.flush();
        var movimenti = em.createNativeQuery("SELECT MQUANTITA,VALORE,VALOREUNITARIO FROM MAGAZZINO WHERE MARTICOLO=:art")
                .setParameter("art", codice).getResultList();
        for (Object movimento : movimenti) {
            Object[] m = (Object[]) movimento;
            assertEquals(((Number) m[0]).doubleValue() * 200, ((Number) m[1]).doubleValue());
            assertEquals(200D, ((Number) m[2]).doubleValue());
        }
    }

    @Test @TestTransaction void bloccaDatiInvalidiENonSaltaArticoliSenzaFornitore() {
        String codice = codice(); var a = riga(1, 1, codice);
        assertThrows(BusinessException.class, () -> service.salva(richiesta("100", a), "TEST"));
        Number count = (Number) em.createNativeQuery("SELECT COUNT(*) FROM MAGAZZINO WHERE MARTICOLO=:art").setParameter("art", codice).getSingleResult();
        assertEquals(0, count.intValue());
        assertThrows(BusinessException.class, () -> service.anteprima(richiesta("100.001", a)));
        assertThrows(BusinessException.class, () -> service.anteprima(richiesta("0", a)));
        assertThrows(BusinessException.class, () -> service.anteprima(richiesta("10", a, a)));
        OrdineDettaglio vecchia = new OrdineDettaglio(); vecchia.setProgrGenerale(a.getProgrGenerale()); vecchia.setQuantita(2D);
        assertThrows(BusinessException.class, () -> service.anteprima(richiesta("10", vecchia)));
    }
}
