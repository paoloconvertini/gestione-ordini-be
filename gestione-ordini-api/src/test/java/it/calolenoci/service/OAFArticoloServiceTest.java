package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.ArticoloDto;
import it.calolenoci.dto.FiltroArticoli;
import it.calolenoci.dto.OrdineFornitoreDettaglioDto;
import it.calolenoci.dto.ResponseDto;
import it.calolenoci.dto.ResponseOAFDettaglioDTO;
import it.calolenoci.entity.FornitoreDettaglioId;
import it.calolenoci.entity.FornitoreId;
import it.calolenoci.entity.GoOrdineFornitore;
import it.calolenoci.entity.OrdineFornitore;
import it.calolenoci.entity.OrdineFornitoreDettaglio;
import it.calolenoci.entity.OrdineDettaglio;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class OAFArticoloServiceTest {

    @Inject
    OAFArticoloService service;

    @Inject
    EntityManager entityManager;

    @Test
    @TestTransaction
    void recuperaIlDettaglioOafOrdinatoPerRigo() {
        OrdineFornitore ordine = existingOrdine();
        List<OrdineFornitoreDettaglio> dettagli = OrdineFornitoreDettaglio.find(
                        "anno = ?1 AND serie = ?2 AND progressivo = ?3 ORDER BY rigo",
                        ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo())
                .list();
        List<Integer> expectedRighi = dettagli.stream().map(OrdineFornitoreDettaglio::getRigo).toList();

        ResponseOAFDettaglioDTO result = service.findById(
                ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo());

        assertEquals(expectedRighi,
                result.getArticoli().stream().map(OrdineFornitoreDettaglioDto::getRigo).toList());
        assertEquals(ordine.getConto(), result.getSottoConto());
    }

    @Test
    @TestTransaction
    void richiedeECompletaLApprovazioneDellOaf() {
        OrdineFornitore ordine = existingOrdine();
        FornitoreId id = new FornitoreId(ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo());

        service.richiediApprovazione(ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo());
        entityManager.clear();
        assertEquals("T", ((OrdineFornitore) OrdineFornitore.findById(id)).getProvvisorio());

        service.approva(ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo());
        entityManager.flush();
        entityManager.clear();
        assertEquals("", ((OrdineFornitore) OrdineFornitore.findById(id)).getProvvisorio());
        GoOrdineFornitore stato = GoOrdineFornitore.findById(id);
        assertFalse(stato.getFlInviato());
    }

    @Test
    @TestTransaction
    void aggiornaIlDettaglioERicalcolaIlValoreTotale() {
        OrdineFornitoreDettaglio entity = existingDettaglio();
        OrdineFornitoreDettaglioDto dto = new OrdineFornitoreDettaglioDto();
        dto.setAnno(entity.getAnno());
        dto.setSerie(entity.getSerie());
        dto.setProgressivo(entity.getProgressivo());
        dto.setRigo(entity.getRigo());
        dto.setODescrArticolo("Descrizione test");
        dto.setOQuantita(4.0);
        dto.setOPrezzo(12.5);
        dto.setFScontoArticolo(1.0);
        dto.setScontoF1(2.0);
        dto.setScontoF2(3.0);
        dto.setFScontoP(4.0);

        service.save(List.of(dto));

        entityManager.flush();
        entityManager.clear();
        OrdineFornitoreDettaglio updated = OrdineFornitoreDettaglio.findById(
                new FornitoreDettaglioId(entity.getAnno(), entity.getSerie(), entity.getProgressivo(), entity.getRigo()));
        assertEquals("Descrizione test", updated.getODescrArticolo());
        assertEquals(4.0, updated.getOQuantita());
        assertEquals(4.0, updated.getOQuantitaV());
        assertEquals(12.5, updated.getOPrezzo());
        assertEquals(50.0, updated.getValoreTotale());
        assertEquals(1.0, updated.getFScontoArticolo());
        assertEquals(2.0, updated.getScontoF1());
        assertEquals(3.0, updated.getScontoF2());
        assertEquals(4.0, updated.getFScontoP());
    }

    @Test
    @TestTransaction
    void eliminaUnaRigaNonCollegataAUnOrdineCliente() {
        OrdineFornitoreDettaglio entity = OrdineFornitoreDettaglio.find("pid is null OR pid = 0").firstResult();
        if (entity == null) {
            throw new AssertionError("Il database di sviluppo non contiene righe OAF non collegate");
        }
        FornitoreDettaglioId id = new FornitoreDettaglioId(
                entity.getAnno(), entity.getSerie(), entity.getProgressivo(), entity.getRigo());

        ResponseDto result = service.eliminaArticolo(
                entity.getAnno(), entity.getSerie(), entity.getProgressivo(), entity.getRigo());

        assertFalse(result.getError());
        assertEquals("Articolo eliminato", result.getMsg());
        assertNull(OrdineFornitoreDettaglio.findById(id));
    }

    @Test
    @TestTransaction
    void segnalaIlCollegamentoClienteMancanteDuranteLEliminazione() {
        OrdineFornitoreDettaglio entity = new OrdineFornitoreDettaglio();
        entity.setAnno(1907);
        entity.setSerie("T");
        entity.setProgressivo(-Math.abs(UUID.randomUUID().hashCode()));
        entity.setRigo(1);
        entity.setPid(Integer.MAX_VALUE);
        entity.setOArticolo("ART-TEST");
        entity.setOQuantita(1D);
        entity.persist();

        ResponseDto result = service.eliminaArticolo(entity.getAnno(), entity.getSerie(),
                entity.getProgressivo(), entity.getRigo());

        assertTrue(result.getError());
        assertTrue(result.getMsg().startsWith("articolo cliente non trovato"));
    }

    @Test
    @TestTransaction
    void gestisceParametriERiferimentiInesistenti() {
        ResponseDto missingArticolo = service.eliminaArticolo(1800, "TST", Integer.MAX_VALUE, 1);
        assertTrue(missingArticolo.getError());

        ResponseDto missingProgressivo = service.collegaOAF(null, new ArticoloDto(), "utente-test");
        assertTrue(missingProgressivo.getError());
        assertEquals("Collega OAF: progrGenerale dell'ordine cliente mancante", missingProgressivo.getMsg());

        ResponseDto missingDto = service.collegaOAF(Integer.MAX_VALUE, null, "utente-test");
        assertTrue(missingDto.getError());
        assertEquals("Collega OAF: dto nullo", missingDto.getMsg());

        ResponseDto missingOrdine = service.collegaOAF(Integer.MAX_VALUE, new ArticoloDto(), "utente-test");
        assertTrue(missingOrdine.getError());
        assertTrue(missingOrdine.getMsg().startsWith("Ordine cliente non trovato"));
    }

    @Test
    void restituisceUnDettaglioVuotoPerUnOafInesistente() {
        ResponseOAFDettaglioDTO result = service.findById(1800, "TST", Integer.MAX_VALUE);

        assertNull(result.getIntestazione());
        assertNull(result.getSottoConto());
        assertTrue(result.getArticoli().isEmpty());
    }

    @Test
    void applicaTuttiIFiltriAllaRicercaArticoli() {
        FiltroArticoli filtro = new FiltroArticoli();
        filtro.setCodice("CODICE-INESISTENTE-TEST");
        filtro.setDescrizione("DESCRIZIONE-INESISTENTE-TEST");
        filtro.setDescrSuppl("SUPPLEMENTO-INESISTENTE-TEST");

        assertTrue(service.cercaArticoli(filtro).isEmpty());
    }

    @Test
    void segnalaUnInserimentoArticoloNonValido() {
        assertFalse(service.save((ArticoloDto) null, "utente-test"));
    }

    @Test
    @TestTransaction
    void inserisceUnaNuovaRigaOaf() {
        ArticoloDto dto = new ArticoloDto();
        dto.setAnno(1908);
        dto.setSerie("T");
        dto.setProgressivo(-Math.abs(UUID.randomUUID().hashCode()));
        dto.setRigo(1);
        dto.setArticolo("ART-TEST");
        dto.setDescrArticolo("Riga OAF test");
        dto.setQuantita(2D);
        dto.setPrezzoBase(10D);
        dto.setUnitaMisura("PZ");

        assertTrue(service.save(dto, "utente-test"));
        assertNotNull(OrdineFornitoreDettaglio.findById(new FornitoreDettaglioId(
                dto.getAnno(), dto.getSerie(), dto.getProgressivo(), dto.getRigo())));
    }

    @Test
    @TestTransaction
    void segnalaLaRigaOafMancantePerUnOrdineClienteEsistente() {
        OrdineDettaglio cliente = OrdineDettaglio.find("progrGenerale is not null").firstResult();
        if (cliente == null) {
            throw new AssertionError("Il database di sviluppo non contiene dettagli ordine cliente");
        }
        ArticoloDto dto = new ArticoloDto();
        dto.setAnno(1800);
        dto.setSerie("TST");
        dto.setProgressivo(Integer.MAX_VALUE);
        dto.setRigo(1);

        ResponseDto result = service.collegaOAF(cliente.getProgrGenerale(), dto, "utente-test");

        assertTrue(result.getError());
        assertTrue(result.getMsg().startsWith("Riga OAF non trovata"));
    }

    @Test
    @TestTransaction
    void validaLeQuantitaPrimaDiCollegareLOaf() {
        OrdineDettaglio cliente = OrdineDettaglio.find("progrGenerale is not null").firstResult();
        OrdineFornitoreDettaglio oaf = existingDettaglio();
        ArticoloDto dto = articoloDto(oaf);
        cliente.setQuantita(0D);
        oaf.setOQuantita(2D);

        ResponseDto quantitaNulla = service.collegaOAF(cliente.getProgrGenerale(), dto, "utente-test");
        assertTrue(quantitaNulla.getError());
        assertTrue(quantitaNulla.getMsg().startsWith("Quantità ordine cliente nulla o negativa"));

        cliente.setQuantita(3D);
        ResponseDto quantitaEccessiva = service.collegaOAF(cliente.getProgrGenerale(), dto, "utente-test");
        assertTrue(quantitaEccessiva.getError());
        assertTrue(quantitaEccessiva.getMsg().startsWith("Quantità cliente"));
    }

    @Test
    @TestTransaction
    void collegaUnaRigaClienteAllOafEAggiungeIlRiferimento() {
        OrdineDettaglio cliente = OrdineDettaglio.find("progrGenerale is not null").firstResult();
        OrdineFornitoreDettaglio oaf = existingDettaglio();
        cliente.setQuantita(1D);
        oaf.setOQuantita(2D);
        oaf.setOQuantitaV(2D);
        if (oaf.getOPrezzo() == null) {
            oaf.setOPrezzo(10D);
        }
        Integer righePrima = Math.toIntExact(OrdineFornitoreDettaglio.count(
                "serie = ?1 and progressivo = ?2", oaf.getSerie(), oaf.getProgressivo()));

        ResponseDto result = service.collegaOAF(
                cliente.getProgrGenerale(), articoloDto(oaf), "utente-test");

        assertFalse(result.getError());
        assertTrue(result.getMsg().startsWith("Collegato all'OAF"));
        assertEquals(1D, oaf.getOQuantita(), 0.01);
        assertEquals(righePrima + 2, OrdineFornitoreDettaglio.count(
                "serie = ?1 and progressivo = ?2", oaf.getSerie(), oaf.getProgressivo()));
        assertEquals(1L, OrdineFornitoreDettaglio.count(
                "anno = ?1 and serie = ?2 and progressivo = ?3 and pid = ?4",
                oaf.getAnno(), oaf.getSerie(), oaf.getProgressivo(), cliente.getProgrGenerale()));
    }

    @Test
    @TestTransaction
    void approvandoCreaLoStatoApplicativoSeAssente() {
        OrdineFornitore ordine = OrdineFornitore.find(
                "FROM OrdineFornitore f WHERE NOT EXISTS (SELECT 1 FROM GoOrdineFornitore g " +
                        "WHERE g.anno = f.anno AND g.serie = f.serie AND g.progressivo = f.progressivo)").firstResult();
        if (ordine == null) {
            throw new AssertionError("Il database di sviluppo non contiene OAF senza stato applicativo");
        }
        FornitoreId id = new FornitoreId(ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo());

        service.approva(ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo());

        entityManager.flush();
        entityManager.clear();
        GoOrdineFornitore stato = GoOrdineFornitore.findById(id);
        assertFalse(stato.getFlInviato());
    }

    private OrdineFornitore existingOrdine() {
        OrdineFornitore entity = OrdineFornitore.find(
                "FROM OrdineFornitore f WHERE EXISTS (SELECT 1 FROM PianoConti p " +
                        "WHERE p.gruppoConto = f.gruppo AND p.sottoConto = f.conto)").firstResult();
        if (entity == null) {
            throw new AssertionError("Il database di sviluppo non contiene ordini fornitore");
        }
        return entity;
    }

    private OrdineFornitoreDettaglio existingDettaglio() {
        OrdineFornitoreDettaglio entity = OrdineFornitoreDettaglio.findAll().firstResult();
        if (entity == null) {
            throw new AssertionError("Il database di sviluppo non contiene dettagli OAF");
        }
        return entity;
    }

    private ArticoloDto articoloDto(OrdineFornitoreDettaglio entity) {
        ArticoloDto dto = new ArticoloDto();
        dto.setAnno(entity.getAnno());
        dto.setSerie(entity.getSerie());
        dto.setProgressivo(entity.getProgressivo());
        dto.setRigo(entity.getRigo());
        return dto;
    }
}
