package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.entity.*;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class OrdiniConNoteServiceTest {
    @Inject OrdineService service;
    @Inject EntityManager em;
    @ConfigProperty(name = "data.inizio") String dataConfig;

    @Test @TestTransaction
    void contaOrdiniUnaVoltaConNotaEDaOrdinareSullaStessaRiga() throws Exception {
        Ordine o = ordine();
        long prima = service.countOrdiniDaOrdinareConNote();
        GoOrdineDettaglio senzaNota = riga(o, true, null, LocalDateTime.now(), " ");
        GoOrdineDettaglio conNota = riga(o, false, "Verificare misura", LocalDateTime.now(), " ");
        assertEquals(prima, service.countOrdiniDaOrdinareConNote());
        senzaNota.setNote("Manca conferma cliente");
        assertEquals(prima + 1, service.countOrdiniDaOrdinareConNote());
        conNota.setFlagNonDisponibile(true);
        assertEquals(prima + 1, service.countOrdiniDaOrdinareConNote());
        assertEquals(2, righeOrdine(o).size());
    }

    @Test @TestTransaction
    void escludeNoteVuoteLogisticaCommentiEOrdinaLeDateConAssentiInFondo() throws Exception {
        Ordine o = ordine();
        long prima = service.countOrdiniDaOrdinareConNote();
        riga(o, true, "", LocalDateTime.now(), " ");
        riga(o, true, "  ", LocalDateTime.now(), " ");
        riga(o, true, " NULL ", LocalDateTime.now(), " ");
        riga(o, true, "Nota commento", LocalDateTime.now(), "C");
        riga(o, true, "Nota acconto", LocalDateTime.now(), "AC");
        assertEquals(prima, service.countOrdiniDaOrdinareConNote());
        var recente = riga(o, true, "Recente", LocalDateTime.of(2025, 3, 1, 10, 0), " ");
        var senzaData = riga(o, true, "Senza data", null, " ");
        var vecchia = riga(o, true, "Vecchia", LocalDateTime.of(2025, 1, 1, 10, 0), " ");
        var lista = righeOrdine(o);
        assertEquals(java.util.List.of(vecchia.getProgrGenerale(), recente.getProgrGenerale(), senzaData.getProgrGenerale()),
                lista.stream().map(it.calolenoci.dto.OrdineNotaDaOrdinareDto::getProgrGenerale).toList());
        assertEquals(prima + 1, service.countOrdiniDaOrdinareConNote());
        assertTrue(service.noteArticoliDaOrdinare("_VENDITORE_INESISTENTE_").isEmpty());
    }

    @Test @TestTransaction
    void contatoreEListaEscludonoArchiviatiProvvisoriEOrdiniPrimaDellaDataConfigurata() throws Exception {
        Ordine o = ordine();
        long prima = service.countOrdiniDaOrdinareConNote();
        riga(o, true, "Nota da verificare", LocalDateTime.now(), " ");
        assertEquals(prima + 1, service.countOrdiniDaOrdinareConNote());
        GoOrdine go = GoOrdine.findByOrdineId(o.getAnno(), o.getSerie(), o.getProgressivo());
        go.setStatus("ARCHIVIATO");
        assertEquals(prima, service.countOrdiniDaOrdinareConNote()); assertTrue(righeOrdine(o).isEmpty());
        go.setStatus("INCOMPLETO"); o.setProvvisorio("S");
        assertEquals(prima, service.countOrdiniDaOrdinareConNote()); assertTrue(righeOrdine(o).isEmpty());
        o.setProvvisorio("N"); o.setDataConferma(LocalDate.parse(dataConfig).minusDays(1));
        assertEquals(prima, service.countOrdiniDaOrdinareConNote()); assertTrue(righeOrdine(o).isEmpty());
    }

    @Test @TestTransaction
    void includeSoloSaldoAccontoVuotoONulloEdEscludeAccontoESaldo() throws Exception {
        Ordine o = ordine();
        long prima = service.countOrdiniDaOrdinareConNote();
        var g = riga(o, true, "Da verificare", LocalDateTime.now(), " ");
        g.setFlagConsegnato(false); // Lo stato ERP prevale sul flag applicativo non aggiornato.
        OrdineDettaglio r = OrdineDettaglio.find("progrGenerale", g.getProgrGenerale()).firstResult();
        for (String stato : new String[]{null, "", " ", "A", "S"}) {
            r.setSaldoAcconto(stato);
            boolean inclusa = stato == null || stato.isBlank();
            assertEquals(inclusa ? 1 : 0, righeOrdine(o).size(), "SALDOACCONTO=" + stato);
            assertEquals(prima + (inclusa ? 1 : 0), service.countOrdiniDaOrdinareConNote(), "SALDOACCONTO=" + stato);
        }
    }

    @Test @TestTransaction
    void contaOrdineMistoSoloSeRimaneUnaRigaNonConsegnataConNota() throws Exception {
        Ordine o = ordine();
        long prima = service.countOrdiniDaOrdinareConNote();
        var aperta = riga(o, true, "Nota riga aperta", LocalDateTime.now(), " ");
        var parziale = riga(o, true, "Nota riga parziale", LocalDateTime.now(), " ");
        var completa = riga(o, true, "Nota riga a saldo", LocalDateTime.now(), " ");
        OrdineDettaglio rAperta = OrdineDettaglio.find("progrGenerale", aperta.getProgrGenerale()).firstResult();
        OrdineDettaglio rParziale = OrdineDettaglio.find("progrGenerale", parziale.getProgrGenerale()).firstResult();
        OrdineDettaglio rCompleta = OrdineDettaglio.find("progrGenerale", completa.getProgrGenerale()).firstResult();
        rParziale.setSaldoAcconto("A"); rCompleta.setSaldoAcconto("S");
        assertEquals(java.util.List.of(aperta.getProgrGenerale()), righeOrdine(o).stream()
                .map(it.calolenoci.dto.OrdineNotaDaOrdinareDto::getProgrGenerale).toList());
        assertEquals(prima + 1, service.countOrdiniDaOrdinareConNote());
        rAperta.setSaldoAcconto("A");
        assertTrue(righeOrdine(o).isEmpty()); assertEquals(prima, service.countOrdiniDaOrdinareConNote());
    }

    private java.util.List<it.calolenoci.dto.OrdineNotaDaOrdinareDto> righeOrdine(Ordine o) throws Exception {
        return service.noteArticoliDaOrdinare(o.getSerie()).stream().filter(r -> r.getAnno().equals(o.getAnno())
                && r.getProgressivo().equals(o.getProgressivo())).toList();
    }

    private Ordine ordine() {
        Ordine o = Ordine.find("FROM Ordine o WHERE EXISTS (SELECT 1 FROM PianoConti p "
                + "WHERE p.gruppoConto=o.gruppoCliente AND p.sottoConto=o.contoCliente)").firstResult();
        assertNotNull(o);
        o.setDataConferma(LocalDate.parse(dataConfig).plusDays(1)); o.setProvvisorio("N");
        GoOrdine go = GoOrdine.findByOrdineId(o.getAnno(), o.getSerie(), o.getProgressivo());
        if (go == null) { go = new GoOrdine(); go.setAnno(o.getAnno()); go.setSerie(o.getSerie()); go.setProgressivo(o.getProgressivo()); go.persist(); }
        go.setStatus("INCOMPLETO"); go.setNoteLogistica("Nota logistica da ignorare");
        GoOrdineDettaglio.update("flagNonDisponibile=false where anno=?1 and serie=?2 and progressivo=?3", o.getAnno(), o.getSerie(), o.getProgressivo());
        return o;
    }

    @Test @TestTransaction
    void vistaArticoliFiltraRighePronteESenzaBollaEConservaLeNote() throws Exception {
        Ordine o = ordine();
        GoOrdine go = GoOrdine.findByOrdineId(o.getAnno(), o.getSerie(), o.getProgressivo());
        go.setHasProntoConsegna(true); go.setWarnNoBolla(true);
        var pronta = riga(o, false, "Nota pronta", LocalDateTime.now(), " ");
        pronta.setFlProntoConsegna(true);
        var senzaBolla = riga(o, false, "Nota consegna", null, " ");
        senzaBolla.setQtaConsegnatoSenzaBolla(2.0);
        var altra = riga(o, false, null, null, " ");
        em.flush();
        var pronte = tutteRigheDashboard("pronto", o.getSerie());
        assertTrue(pronte.contains(pronta.getProgrGenerale()));
        assertFalse(pronte.contains(senzaBolla.getProgrGenerale()));
        assertFalse(pronte.contains(altra.getProgrGenerale()));
        var bolle = tutteRigheDashboard("bolle", o.getSerie());
        assertTrue(bolle.contains(senzaBolla.getProgrGenerale()));
        assertFalse(bolle.contains(pronta.getProgrGenerale()));
        assertTrue(service.articoliDashboard("all", "_INESISTENTE_", 0).getRighe().isEmpty());
        assertThrows(jakarta.ws.rs.BadRequestException.class, () -> service.articoliDashboard("carichi", null, 0));
    }

    @Test @TestTransaction
    void vistaOrdiniConNoteUsaLoStessoCriterioDelContatore() throws Exception {
        Ordine o = ordine();
        riga(o, true, "Nota aperta", LocalDateTime.now(), " ");
        var filtro = new it.calolenoci.dto.FiltroOrdini(); filtro.setDaOrdinareConNote(true);
        assertEquals(service.countOrdiniDaOrdinareConNote(), service.countDashboard(filtro));
        var pagina = service.articoliDashboard("all", o.getSerie(), 10000);
        assertTrue(pagina.getRighe().isEmpty());
    }

    private java.util.Set<Integer> tutteRigheDashboard(String tipo, String venditore) throws Exception {
        java.util.Set<Integer> ids = new java.util.HashSet<>();
        var prima = service.articoliDashboard(tipo, venditore, 0);
        prima.getRighe().forEach(r -> ids.add(r.getProgrGenerale()));
        for (int pagina = 1; pagina * 20 < prima.getTotaleOrdini(); pagina++)
            service.articoliDashboard(tipo, venditore, pagina).getRighe().forEach(r -> ids.add(r.getProgrGenerale()));
        return ids;
    }

    private GoOrdineDettaglio riga(Ordine o, boolean daOrdinare, String nota, LocalDateTime data, String tipo) {
        int id = -1_000_000_000 - Math.floorMod(UUID.randomUUID().hashCode(), 500_000_000);
        OrdineDettaglio r = new OrdineDettaglio(); r.setAnno(o.getAnno()); r.setSerie(o.getSerie()); r.setProgressivo(o.getProgressivo());
        r.setProgrGenerale(id); r.setRigo(id); r.setTipoRigo(tipo); r.setFArticolo("TEST"); r.setFDescrArticolo("Articolo test"); r.persist();
        GoOrdineDettaglio g = new GoOrdineDettaglio(); g.setProgrGenerale(id); g.setAnno(o.getAnno()); g.setSerie(o.getSerie()); g.setProgressivo(o.getProgressivo());
        g.setRigo(id); g.setFlagNonDisponibile(daOrdinare); g.setNote(nota); g.setDataNote(data); g.persist();
        em.flush(); return g;
    }
}
