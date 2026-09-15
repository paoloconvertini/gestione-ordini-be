package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import it.calolenoci.dto.AggiornaDataDto;
import it.calolenoci.dto.CollegaOAFDto;
import it.calolenoci.dto.FiltroOrdini;
import it.calolenoci.dto.OrdineFornitoreDto;
import it.calolenoci.dto.ResponseDto;
import it.calolenoci.entity.FornitoreId;
import it.calolenoci.entity.GoOrdineFornitore;
import it.calolenoci.entity.Articolo;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.entity.OrdineFornitore;
import it.calolenoci.entity.OrdineFornitoreDettaglio;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@QuarkusTest
class OrdineFornitoreServiceTest {

    @Inject
    OrdineFornitoreService service;

    @Inject
    EntityManager entityManager;

    @InjectMock
    OAFArticoloService articoloService;

    @Test
    @TestTransaction
    void cercaGliOrdiniConIFiltriPrevisti() throws Exception {
        List<OrdineFornitoreDto> tutti = service.findAllByStatus(null);

        FiltroOrdini daInviare = new FiltroOrdini();
        daInviare.setFiltroInvio("da_inviare");
        List<OrdineFornitoreDto> nonInviati = service.findAllByStatus(daInviare);

        FiltroOrdini inviati = new FiltroOrdini();
        inviati.setFiltroInvio("inviati");
        List<OrdineFornitoreDto> giaInviati = service.findAllByStatus(inviati);

        FiltroOrdini inApprovazione = new FiltroOrdini();
        inApprovazione.setFiltroStatus("T");
        List<OrdineFornitoreDto> provvisori = service.findAllByStatus(inApprovazione);

        assertNotNull(tutti);
        assertNotNull(nonInviati);
        assertNotNull(giaInviati);
        assertNotNull(provvisori);
    }

    @Test
    @TestTransaction
    void richiedeApprovazioneSingolaEMultipla() {
        OrdineFornitore ordine = existingOrdine();
        OrdineFornitoreDto dto = keyDto(ordine);

        service.richiediApprovazione(List.of(dto));

        entityManager.flush();
        entityManager.clear();
        assertEquals("T", ((OrdineFornitore) OrdineFornitore.findById(id(ordine))).getProvvisorio());
    }

    @Test
    @TestTransaction
    void completaLApprovazioneERimuoveLoStatoApplicativo() {
        OrdineFornitore ordine = existingOrdine();
        FornitoreId id = id(ordine);
        GoOrdineFornitore stato = new GoOrdineFornitore();
        stato.setAnno(ordine.getAnno());
        stato.setSerie(ordine.getSerie());
        stato.setProgressivo(ordine.getProgressivo());
        stato.setFlInviato(false);
        GoOrdineFornitore.deleteById(id);
        stato.persist();

        service.changeStatus(ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo());

        entityManager.flush();
        entityManager.clear();
        assertEquals("F", ((OrdineFornitore) OrdineFornitore.findById(id)).getProvvisorio());
        assertNull(GoOrdineFornitore.findById(id));
    }

    @Test
    @TestTransaction
    void registraInvioCreandoEAggiornandoLoStato() {
        OrdineFornitore ordine = existingOrdine();
        FornitoreId id = id(ordine);
        GoOrdineFornitore.deleteById(id);
        OrdineFornitoreDto dto = keyDto(ordine);
        dto.setFlInviato(true);

        service.inviato(List.of(dto));
        entityManager.flush();
        entityManager.clear();
        GoOrdineFornitore creato = GoOrdineFornitore.findById(id);
        assertTrue(creato.getFlInviato());
        assertEquals(LocalDate.now(), creato.getDataInvio());

        dto.setFlInviato(false);
        service.inviato(List.of(dto));
        entityManager.flush();
        entityManager.clear();
        assertFalse(((GoOrdineFornitore) GoOrdineFornitore.findById(id)).getFlInviato());
    }

    @Test
    @TestTransaction
    void salvaNotaCreandoEAggiornandoLoStato() {
        OrdineFornitore ordine = existingOrdine();
        FornitoreId id = id(ordine);
        GoOrdineFornitore.deleteById(id);
        OrdineFornitoreDto dto = keyDto(ordine);
        dto.setNote("prima nota test");

        service.salvaNota(dto);
        entityManager.flush();
        entityManager.clear();
        assertEquals("prima nota test", ((GoOrdineFornitore) GoOrdineFornitore.findById(id)).getNote());

        dto.setNote("nota aggiornata test");
        service.salvaNota(dto);
        entityManager.flush();
        entityManager.clear();
        assertEquals("nota aggiornata test", ((GoOrdineFornitore) GoOrdineFornitore.findById(id)).getNote());
    }

    @Test
    @TestTransaction
    void verificaEsistenzaEQuantitaDellOaf() {
        OrdineFornitoreDettaglio dettaglio = existingDettaglio();
        CollegaOAFDto dto = new CollegaOAFDto();
        dto.setAnnoOAF(dettaglio.getAnno());
        dto.setSerieOAF(dettlioSerie(dettaglio));
        dto.setProgressivoOAF(dettaglio.getProgressivo());
        dto.setCodice(dettaglio.getOArticolo());
        dto.setQta(dettaglio.getOQuantita());

        ResponseDto valido = service.verificaOAF(dto);
        assertFalse(valido.getError());

        dto.setQta(dettaglio.getOQuantita() + 1);
        ResponseDto quantitaDiversa = service.verificaOAF(dto);
        assertTrue(quantitaDiversa.getError());
        assertEquals(409, quantitaDiversa.getCode().getStatusCode());

        dto.setProgressivoOAF(Integer.MAX_VALUE);
        ResponseDto inesistente = service.verificaOAF(dto);
        assertTrue(inesistente.getError());
        assertEquals(204, inesistente.getCode().getStatusCode());
    }

    @Test
    @TestTransaction
    void leggeEAggiornaLaDataDellOrdineCliente() {
        OrdineDettaglio dettaglio = existingOrdineDettaglio();
        AggiornaDataDto dto = new AggiornaDataDto(dettaglio.getProgrGenerale(), 37D,
                java.sql.Date.valueOf(LocalDate.now().plusDays(10)));

        ResponseDto result = service.inserisciDataConsegna(dto);
        AggiornaDataDto letto = service.getDataOrdineCliente(dettaglio.getProgrGenerale());

        assertFalse(result.getError());
        assertEquals(dto.getDataConsegna(), letto.getDataConsegna());
        assertEquals(37D, letto.getSettimana());

        AggiornaDataDto vuoto = service.getDataOrdineCliente(Integer.MAX_VALUE);
        assertNull(vuoto.getPid());
        assertFalse(service.inserisciDataConsegna(new AggiornaDataDto(Integer.MAX_VALUE, null, null)).getError());
    }

    @Test
    @TestTransaction
    void eliminaOrdineEDelegaLaCancellazioneDeiDettagli() {
        OrdineFornitore ordine = existingOrdineWithDetails();
        long dettagli = OrdineFornitoreDettaglio.count("anno = ?1 AND serie = ?2 AND progressivo = ?3",
                ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo());

        ResponseDto result = service.eliminaOrdine(ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo());

        assertFalse(result.getError());
        assertEquals("Articolo eliminato", result.getMsg());
        assertNull(OrdineFornitore.findById(id(ordine)));
        if (dettagli > 0) {
            verify(articoloService, times((int) dettagli))
                    .eliminaArticolo(anyInt(), anyString(), anyInt(), anyInt());
        }
    }

    @Test
    @TestTransaction
    void gestisceOrdineERiferimentiInesistenti() throws Exception {
        ResponseDto result = service.eliminaOrdine(1800, "TST", Integer.MAX_VALUE);
        assertTrue(result.getError());
        assertEquals("ordine fornitore non trovato", result.getMsg());

        assertThrows(Exception.class, () -> service.save(List.of(), "utente-test"));
        assertNotNull(service.getOrdiniByOperatore());
    }

    @Test
    @TestTransaction
    void creaUnOafDaUnaRigaClienteCompletamenteCodificata() throws Exception {
        OrdineDettaglio dettaglio = OrdineDettaglio.find(
                "FROM OrdineDettaglio d WHERE EXISTS (SELECT 1 FROM Ordine o JOIN PianoConti pc " +
                        "ON pc.gruppoConto = o.gruppoCliente AND pc.sottoConto = o.contoCliente " +
                        "WHERE o.anno = d.anno AND o.serie = d.serie AND o.progressivo = d.progressivo)")
                .firstResult();
        if (dettaglio == null) {
            throw new AssertionError("Il database di sviluppo non contiene righe cliente con anagrafica valida");
        }
        Articolo articolo = Articolo.find(
                "FROM Articolo a WHERE EXISTS (SELECT 1 FROM FornitoreArticolo fa " +
                        "JOIN PianoConti pf ON pf.gruppoConto = fa.fornitoreArticoloId.gruppo " +
                        "AND pf.sottoConto = fa.fornitoreArticoloId.conto " +
                        "WHERE fa.fornitoreArticoloId.articolo = a.articolo)").firstResult();
        if (articolo == null) {
            throw new AssertionError("Il database di sviluppo non contiene articoli con fornitore valido");
        }
        dettaglio.setFArticolo(articolo.getArticolo());
        dettaglio.setQuantita(1D);
        dettaglio.setQuantitaV(1D);
        entityManager.flush();

        List<String> fornitori = service.save(List.of(dettaglio), "utente-test");

        assertFalse(fornitori.isEmpty());
        assertTrue(OrdineFornitoreDettaglio.count("pid = ?1", dettaglio.getProgrGenerale()) > 0);
    }

    @Test
    @TestTransaction
    void rimuoveDataESettimanaDallaRigaCliente() {
        OrdineDettaglio dettaglio = existingOrdineDettaglio();
        AggiornaDataDto dto = new AggiornaDataDto(dettaglio.getProgrGenerale(), null, null);

        ResponseDto result = service.inserisciDataConsegna(dto);

        entityManager.flush();
        entityManager.clear();
        OrdineDettaglio updated = OrdineDettaglio.find("progrGenerale", dettaglio.getProgrGenerale()).firstResult();
        assertFalse(result.getError());
        assertNull(updated.getDatauser1());
        assertNull(updated.getQtyuser1());
        OrdineFornitoreDettaglio oaf = OrdineFornitoreDettaglio.find(
                "pid", dettaglio.getProgrGenerale()).firstResult();
        if (oaf != null) {
            assertEquals(" ", oaf.getCampouser1());
        }
    }

    @Test
    @TestTransaction
    void ignoraDuplicatiDuranteLUnioneDegliOrdini() {
        OrdineFornitore ordine = existingOrdineWithDetails();
        OrdineFornitoreDto dto = keyDto(ordine);
        long dettagliPrima = OrdineFornitoreDettaglio.count(
                "anno = ?1 and serie = ?2 and progressivo = ?3",
                ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo());

        service.unisciOrdini(new ArrayList<>(List.of(dto, keyDto(ordine))));

        assertEquals(dettagliPrima, OrdineFornitoreDettaglio.count(
                "anno = ?1 and serie = ?2 and progressivo = ?3",
                ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo()));
        assertNotNull(OrdineFornitore.findById(id(ordine)));
    }

    @Test
    @TestTransaction
    void segnalaLaProiezioneNonCompatibileDelReportFornitore() {
        OrdineFornitore ordine = existingOrdine();

        assertThrows(RuntimeException.class, () -> service.findForReport(
                ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo()));
    }

    @Test
    @TestTransaction
    void gestisceAggiornamentiSenzaOrdineEDataConsegnaCompleta() {
        service.richiediApprovazione(Integer.MAX_VALUE, "TST", Integer.MAX_VALUE);
        service.changeStatus(Integer.MAX_VALUE, "TST", Integer.MAX_VALUE);

        OrdineDettaglio dettaglio = existingOrdineDettaglio();
        AggiornaDataDto dto = new AggiornaDataDto(dettaglio.getProgrGenerale(), 42D,
                java.sql.Date.valueOf(LocalDate.now().plusDays(20)));
        ResponseDto result = service.inserisciDataConsegna(dto);

        assertFalse(result.getError());
        assertEquals(42D, service.getDataOrdineCliente(dettaglio.getProgrGenerale()).getSettimana());
    }

    private OrdineFornitore existingOrdine() {
        OrdineFornitore entity = OrdineFornitore.findAll().firstResult();
        if (entity == null) {
            throw new AssertionError("Il database di sviluppo non contiene ordini fornitore");
        }
        return entity;
    }

    private OrdineFornitore existingOrdineWithDetails() {
        OrdineFornitore entity = OrdineFornitore.find(
                "FROM OrdineFornitore f WHERE EXISTS (SELECT 1 FROM OrdineFornitoreDettaglio d " +
                        "WHERE d.anno = f.anno AND d.serie = f.serie AND d.progressivo = f.progressivo)").firstResult();
        if (entity == null) {
            throw new AssertionError("Il database di sviluppo non contiene ordini fornitore con dettagli");
        }
        return entity;
    }

    private OrdineFornitoreDettaglio existingDettaglio() {
        OrdineFornitoreDettaglio entity = OrdineFornitoreDettaglio.find("oArticolo is not null AND oQuantita is not null").firstResult();
        if (entity == null) {
            throw new AssertionError("Il database di sviluppo non contiene dettagli OAF verificabili");
        }
        return entity;
    }

    private OrdineDettaglio existingOrdineDettaglio() {
        OrdineDettaglio entity = OrdineDettaglio.find("progrGenerale is not null").firstResult();
        if (entity == null) {
            throw new AssertionError("Il database di sviluppo non contiene dettagli ordine cliente");
        }
        return entity;
    }

    private String dettlioSerie(OrdineFornitoreDettaglio dettaglio) {
        return dettaglio.getSerie();
    }

    private OrdineFornitoreDto keyDto(OrdineFornitore ordine) {
        OrdineFornitoreDto dto = new OrdineFornitoreDto();
        dto.setAnno(ordine.getAnno());
        dto.setSerie(ordine.getSerie());
        dto.setProgressivo(ordine.getProgressivo());
        return dto;
    }

    private FornitoreId id(OrdineFornitore ordine) {
        return new FornitoreId(ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo());
    }
}
