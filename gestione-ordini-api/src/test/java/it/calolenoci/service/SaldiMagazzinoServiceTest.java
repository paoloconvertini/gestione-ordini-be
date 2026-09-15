package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.CaricoMagazzinoDto;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.entity.FattureDettaglio;
import it.calolenoci.entity.GoTmpScarico;
import it.calolenoci.entity.GoTmpScaricoPK;
import it.calolenoci.entity.SaldiMagazzino;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class SaldiMagazzinoServiceTest {

    @Inject
    SaldiMagazzinoService saldiMagazzinoService;

    @Inject
    EntityManager entityManager;

    @Test
    @TestTransaction
    void creaMovimentoConFornitoreNormalizzatoENuovoSaldo() throws Exception {
        String articolo = newArticolo();
        insertFornitore(articolo, 321, "123");

        String result = saldiMagazzinoService.save(newCarico(articolo, 4.567D), "TEST");
        entityManager.flush();

        Object[] movimento = (Object[]) entityManager.createNativeQuery(
                        "SELECT GRUPPOMAG, CONTOMAG, MQUANTITA, MVETTORE " +
                                "FROM MAGAZZINO WHERE SERIEMAGAZZINO = 'CF' AND MARTICOLO = :articolo")
                .setParameter("articolo", articolo)
                .getSingleResult();
        Object[] saldo = (Object[]) entityManager.createNativeQuery(
                        "SELECT QCARICHI, QSCARICHI, QGIACENZA FROM SALDIMAGAZZINO " +
                                "WHERE MARTICOLO = :articolo AND MMAGAZZINO = 'B'")
                .setParameter("articolo", articolo)
                .getSingleResult();

        assertTrue(result.startsWith("Creato carico di magazzino n. "));
        assertEquals(321, ((Number) movimento[0]).intValue());
        assertEquals("000123", movimento[1]);
        assertEquals(4.567D, ((Number) movimento[2]).doubleValue(), 0.0001D);
        assertEquals("V01", movimento[3]);
        assertEquals(4.57D, ((Number) saldo[0]).doubleValue(), 0.0001D);
        assertEquals(0D, ((Number) saldo[1]).doubleValue(), 0.0001D);
        assertEquals(4.57D, ((Number) saldo[2]).doubleValue(), 0.0001D);
    }

    @Test
    @TestTransaction
    void aggiornaCarichiEGiacenzaDelSaldoEsistente() throws Exception {
        String articolo = newArticolo();
        insertFornitore(articolo, 654, "45");
        insertSaldo(articolo, 10D, 3D, 7D);

        saldiMagazzinoService.save(newCarico(articolo, 2.345D), "TEST");
        entityManager.flush();
        entityManager.clear();

        Object[] saldo = (Object[]) entityManager.createNativeQuery(
                        "SELECT QCARICHI, QSCARICHI, QGIACENZA FROM SALDIMAGAZZINO " +
                                "WHERE MARTICOLO = :articolo AND MMAGAZZINO = 'B'")
                .setParameter("articolo", articolo)
                .getSingleResult();

        assertEquals(12.35D, ((Number) saldo[0]).doubleValue(), 0.0001D);
        assertEquals(3D, ((Number) saldo[1]).doubleValue(), 0.0001D);
        assertEquals(9.35D, ((Number) saldo[2]).doubleValue(), 0.0001D);
    }

    @Test
    @TestTransaction
    void ignoraArticoloSenzaFornitore() throws Exception {
        String articolo = newArticolo();

        String result = saldiMagazzinoService.save(newCarico(articolo, 2D), "TEST");
        entityManager.flush();

        Number movimenti = (Number) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM MAGAZZINO WHERE SERIEMAGAZZINO = 'CF' AND MARTICOLO = :articolo")
                .setParameter("articolo", articolo)
                .getSingleResult();
        Number saldi = (Number) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM SALDIMAGAZZINO WHERE MARTICOLO = :articolo")
                .setParameter("articolo", articolo)
                .getSingleResult();

        assertNull(result);
        assertEquals(0, movimenti.intValue());
        assertEquals(0, saldi.intValue());
    }

    @Test
    @TestTransaction
    void gestisceCaricoVuotoEScarichiDaProcessareAssenti() throws Exception {
        CaricoMagazzinoDto vuoto = new CaricoMagazzinoDto();
        vuoto.setArticoli(List.of());

        assertNull(saldiMagazzinoService.save(vuoto, "TEST"));
        saldiMagazzinoService.findCaricoMagazzino();
    }

    @Test
    @TestTransaction
    void aggiornaIlSaldoDaUnoScaricoTemporaneo() {
        String articolo = newArticolo();
        insertSaldo(articolo, 10D, 2D, 8D);
        int progressivo = -Math.abs(UUID.randomUUID().hashCode());
        FattureDettaglio bolla = new FattureDettaglio();
        bolla.setAnno(1909);
        bolla.setSerie("T");
        bolla.setProgressivo(progressivo);
        bolla.setRigo(1);
        bolla.setProgrGenerale(progressivo);
        bolla.setQuantita(3D);
        bolla.setFArticolo(articolo);
        bolla.persist();
        GoTmpScarico tmp = new GoTmpScarico();
        tmp.setId(new GoTmpScaricoPK(articolo, "B", progressivo));
        tmp.setAttivo(true);
        tmp.persist();

        saldiMagazzinoService.findCaricoMagazzino();

        SaldiMagazzino saldo = SaldiMagazzino.find("marticolo = ?1 and mmagazzino = 'B'", articolo)
                .firstResult();
        assertEquals(5D, saldo.getQscarichi(), 0.0001D);
        assertEquals(5D, saldo.getQgiacenza(), 0.0001D);
        assertFalse(((GoTmpScarico) GoTmpScarico.findById(tmp.getId())).getAttivo());
    }

    private CaricoMagazzinoDto newCarico(String codiceArticolo, Double quantita) {
        OrdineDettaglio articolo = new OrdineDettaglio();
        articolo.setFArticolo(codiceArticolo);
        articolo.setFDescrArticolo("Articolo test");
        articolo.setFUnitaMisura("PZ");
        articolo.setQuantita(quantita);
        articolo.setMagazz("B");
        articolo.setFColli(1);

        CaricoMagazzinoDto carico = new CaricoMagazzinoDto();
        carico.setNumDoc("TEST");
        carico.setDataOperazione(LocalDate.of(2026, 9, 8));
        carico.setDataDocumento(LocalDate.of(2026, 9, 8));
        carico.setCausale("C01");
        carico.setVettore("V01");
        carico.setArticoli(List.of(articolo));
        return carico;
    }

    private void insertFornitore(String articolo, int gruppo, String conto) {
        entityManager.createNativeQuery(
                        "INSERT INTO FORNALTERNATIVI (articolo, GRUPPOF, CONTOF) " +
                                "VALUES (:articolo, :gruppo, :conto)")
                .setParameter("articolo", articolo)
                .setParameter("gruppo", gruppo)
                .setParameter("conto", conto)
                .executeUpdate();
    }

    private void insertSaldo(String articolo, Double carichi, Double scarichi, Double giacenza) {
        entityManager.createNativeQuery("INSERT INTO SALDIMAGAZZINO " +
                        "(MARTICOLO, VAR1, VAR2, VAR3, VAR4, VAR5, MMAGAZZINO, QTY, " +
                        "QCARICHI, QSCARICHI, QGIACENZA) " +
                        "VALUES (:articolo, '', '', '', '', '', 'B', '1', :carichi, :scarichi, :giacenza)")
                .setParameter("articolo", articolo)
                .setParameter("carichi", carichi)
                .setParameter("scarichi", scarichi)
                .setParameter("giacenza", giacenza)
                .executeUpdate();
    }

    private String newArticolo() {
        return "T" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
