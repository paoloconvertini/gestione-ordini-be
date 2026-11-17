package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.narayana.jta.QuarkusTransaction;
import it.calolenoci.dto.OrdineFornitoreDto;
import it.calolenoci.entity.*;
import it.calolenoci.exception.BusinessException;
import it.calolenoci.mapper.OrdineFornitoreMapper;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.argThat;

@QuarkusTest
class UnioneOrdiniFornitoriTest {
    @Inject OrdineFornitoreService service;
    @Inject EntityManager em;
    @InjectSpy OrdineFornitoreMapper mapper;

    private OrdineFornitore ordine(String serie, int progressivo, String conto, boolean riga) {
        OrdineFornitore o = new OrdineFornitore(); o.setAnno(2038); o.setSerie(serie); o.setProgressivo(progressivo);
        o.setGruppo(321); o.setConto(conto); o.setProvvisorio("F"); o.persist();
        if (riga) {
            OrdineFornitoreDettaglio r = new OrdineFornitoreDettaglio();
            r.setAnno(2038); r.setSerie(serie); r.setProgressivo(progressivo); r.setRigo(1);
            r.setProgrGenerale(numero()); r.setOArticolo("TEST"); r.setOQuantita(1D); r.setOPrezzo(10D); r.setPid(7654); r.persist();
        }
        em.flush(); return o;
    }
    private int numero() { return -1_000_000_000 - Math.floorMod(UUID.randomUUID().hashCode(), 500_000_000); }
    private OrdineFornitoreDto dto(OrdineFornitore o) {
        OrdineFornitoreDto d = new OrdineFornitoreDto(); d.setAnno(o.getAnno()); d.setSerie(o.getSerie()); d.setProgressivo(o.getProgressivo());
        return d;
    }
    private FornitoreId id(OrdineFornitore o) { return new FornitoreId(o.getAnno(), o.getSerie(), o.getProgressivo()); }
    private long righe(OrdineFornitore o) { return OrdineFornitoreDettaglio.count("anno=?1 and serie=?2 and progressivo=?3", o.getAnno(), o.getSerie(), o.getProgressivo()); }

    @Test @TestTransaction void fornitoriDiversiRichiedonoConfermaVerificataDalDatabase() {
        var a = ordine("UT1", numero(), "000123", true); var b = ordine("UT1", numero(), "000456", true);
        var lista = List.of(dto(a), dto(b)); lista.forEach(d -> { d.setGruppo(1); d.setConto("FALSO"); });
        var p = service.anteprimaUnione(lista); assertTrue(p.isFornitoriDiversi());
        assertEquals("000123", p.getOrdini().get(0).getConto());
        assertThrows(BusinessException.class, () -> service.unisciOrdini(lista, false, p.getVersione()));
        assertThrows(BusinessException.class, () -> service.unisciOrdini(lista, true, null));
        assertEquals(1, righe(a)); assertEquals(1, righe(b)); assertNotNull(OrdineFornitore.findById(id(b)));
    }

    @Test @TestTransaction void confermaEsplicitaSpostaLeRigheEMantieneIBackup() {
        var a = ordine("UT1", numero(), "000123", true); var b = ordine("UT2", numero(), "000456", true);
        var lista = List.of(dto(a), dto(b)); var p = service.anteprimaUnione(lista);
        service.unisciOrdini(lista, true, p.getVersione()); em.flush(); em.clear();
        assertEquals(2, righe(a)); assertEquals(0, righe(b)); assertNull(OrdineFornitore.findById(id(b)));
        OrdineFornitore tenuto = OrdineFornitore.findById(id(a)); assertEquals("000123", tenuto.getConto());
        assertNotNull(GoOrdineFornitoreBK.findById(id(b)));
        assertNotNull(GoOrdineFornitoreDettaglioBK.findById(new FornitoreDettaglioId(2038, b.getSerie(), b.getProgressivo(), 1)));
    }

    @Test @TestTransaction void serieCorrettaNonToccaUnOrdineTerzoConLoStessoNumero() {
        int progressivo = numero();
        var a = ordine("UT1", progressivo, "000123", false);
        var b = ordine("UT2", numero(), "000123", true);
        var estraneo = ordine("UT2", progressivo, "000999", true);
        var lista = List.of(dto(a), dto(b)); var p = service.anteprimaUnione(lista);
        service.unisciOrdini(lista, false, p.getVersione()); em.flush(); em.clear();
        assertEquals(1, righe(a)); assertEquals(1, righe(estraneo)); assertEquals(0, righe(b));
        OrdineFornitore terzo = OrdineFornitore.findById(id(estraneo)); assertEquals("000999", terzo.getConto());
    }

    @Test @TestTransaction void rifiutaAnteprimaObsoletaESelezioniNonValide() {
        var a = ordine("UT1", numero(), "000123", true); var b = ordine("UT1", numero(), "000123", true);
        var lista = List.of(dto(a), dto(b)); var p = service.anteprimaUnione(lista);
        b.setConto("000456"); em.flush();
        assertThrows(BusinessException.class, () -> service.unisciOrdini(lista, true, p.getVersione()));
        assertThrows(BusinessException.class, () -> service.anteprimaUnione(null));
        assertThrows(BusinessException.class, () -> service.anteprimaUnione(List.of(dto(a))));
        assertThrows(BusinessException.class, () -> service.anteprimaUnione(List.of(dto(a), dto(a))));
        b.setProvvisorio("T"); em.flush();
        assertThrows(BusinessException.class, () -> service.anteprimaUnione(lista));
        assertEquals(1, righe(a)); assertEquals(1, righe(b));
    }

    @Test void rollbackRealeAncheDopoUnPrimoOrdineGiaTrasferito() {
        List<OrdineFornitore> fixture = new ArrayList<>();
        AtomicBoolean trasferimentoAvvenuto = new AtomicBoolean();
        try {
            QuarkusTransaction.requiringNew().run(() -> {
                fixture.add(ordine("UT1", numero(), "000123", true));
                fixture.add(ordine("UT2", numero(), "000123", true));
                fixture.add(ordine("UT3", numero(), "000123", true));
            });
            doAnswer(inv -> {
                trasferimentoAvvenuto.set(righe(fixture.get(0)) == 2);
                throw new IllegalStateException("Errore simulato dopo il primo trasferimento");
            }).when(mapper).copyOAF(argThat(o -> o != null && o.getProgressivo().equals(fixture.get(2).getProgressivo())));
            assertThrows(RuntimeException.class, () -> service.unisciOrdini(fixture.stream().map(this::dto).toList(), false, null));
            assertTrue(trasferimentoAvvenuto.get());
            QuarkusTransaction.requiringNew().run(() -> {
                for (var o : fixture) { assertNotNull(OrdineFornitore.findById(id(o))); assertEquals(1, righe(o)); }
                assertNull(GoOrdineFornitoreBK.findById(id(fixture.get(1))));
            });
        } finally {
            reset(mapper);
            QuarkusTransaction.requiringNew().run(() -> {
                for (var o : fixture) {
                    OrdineFornitoreDettaglio.delete("anno=?1 and serie=?2 and progressivo=?3", o.getAnno(), o.getSerie(), o.getProgressivo());
                    GoOrdineFornitoreDettaglioBK.delete("anno=?1 and serie=?2 and progressivo=?3", o.getAnno(), o.getSerie(), o.getProgressivo());
                    GoOrdineFornitoreBK.deleteById(id(o)); OrdineFornitore.deleteById(id(o));
                }
            });
        }
    }
}
