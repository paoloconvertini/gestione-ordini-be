package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import it.calolenoci.dto.FiltroOrdini;
import it.calolenoci.dto.FatturaAccontoDto;
import it.calolenoci.dto.FatturaAccontoIvaView;
import it.calolenoci.dto.FatturaAccontoView;
import it.calolenoci.dto.ConsegneSettimanaliDto;
import it.calolenoci.dto.PageOrdineDto;
import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.dto.OrdineclienteMonitorDto;
import it.calolenoci.dto.ProgrammaConsegnaDto;
import it.calolenoci.dto.ResponseDto;
import it.calolenoci.entity.GoOrdine;
import it.calolenoci.entity.GoOrdineDettaglio;
import it.calolenoci.entity.GoOrdVeicolo;
import it.calolenoci.entity.GoOrdVeicoloPK;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.entity.Fatture;
import it.calolenoci.entity.FattureDettaglio;
import it.calolenoci.entity.Veicolo;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class OrdineServiceTest {

    @Inject
    OrdineService service;

    @Inject
    EntityManager entityManager;

    @InjectMock
    AuditService auditService;

    @ConfigProperty(name = "data.inizio")
    String dataConfig;

    @Test
    void esponeTuttiGliStatiOrdine() {
        assertFalse(service.getStati().isEmpty());
        assertTrue(service.getStati().stream().allMatch(s -> s.getDescrizione().equals(s.getCodice())));
    }

    @Test
    @TestTransaction
    void recuperaTestataEDatiPerReport() {
        Ordine ordine = existingOrdineWithCliente();

        OrdineDTO result = service.findById(ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo());
        OrdineDTO report = service.findForReport(ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo());

        assertEquals(ordine.getAnno(), result.getAnno());
        assertEquals(ordine.getProgressivo(), report.getProgressivo());
        assertNotNull(result.getIntestazione());
    }

    @Test
    @TestTransaction
    void cambiaLoStatoApplicativoDellOrdine() {
        GoOrdine ordine = GoOrdine.findAll().firstResult();
        if (ordine == null) {
            throw new AssertionError("Il database di sviluppo non contiene ordini applicativi");
        }

        service.changeStatus(ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo(), "TEST_COPERTURA");

        entityManager.flush();
        entityManager.clear();
        GoOrdine updated = GoOrdine.find("anno = ?1 and serie = ?2 and progressivo = ?3",
                ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo()).firstResult();
        assertEquals("TEST_COPERTURA", updated.getStatus());
    }

    @Test
    @TestTransaction
    void archiviaUnOrdineSenzaRigheAperte() {
        int progressivo = uniqueNegativeKey();
        GoOrdine ordine = goOrdine(1904, "T", progressivo, "COMPLETO");
        ordine.setWarnNoBolla(false);
        ordine.setHasProntoConsegna(true);
        ordine.setHasCarico(true);
        ordine.persist();

        service.checkConsegnati(new FiltroOrdini());

        entityManager.flush();
        entityManager.clear();
        GoOrdine updated = GoOrdine.findByOrdineId(1904, "T", progressivo);
        assertEquals("ARCHIVIATO", updated.getStatus());
        assertFalse(updated.getHasProntoConsegna());
        assertFalse(updated.getHasCarico());
    }

    @Test
    @TestTransaction
    void ripristinaDaProcessareSeMancaIlDettaglioApplicativo() {
        it.calolenoci.entity.OrdineDettaglio dettaglio = it.calolenoci.entity.OrdineDettaglio.find(
                "tipoRigo = ' ' and progrGenerale is not null").firstResult();
        if (dettaglio == null) {
            throw new AssertionError("Il database di sviluppo non contiene righe ordine elaborabili");
        }
        GoOrdineDettaglio.delete("progrGenerale", dettaglio.getProgrGenerale());
        GoOrdine ordine = GoOrdine.findByOrdineId(
                dettaglio.getAnno(), dettaglio.getSerie(), dettaglio.getProgressivo());
        if (ordine == null) {
            ordine = goOrdine(dettaglio.getAnno(), dettaglio.getSerie(), dettaglio.getProgressivo(), "COMPLETO");
            ordine.persist();
        } else {
            ordine.setStatus("COMPLETO");
        }
        entityManager.flush();

        service.checkStatusDettaglio(new FiltroOrdini());

        entityManager.flush();
        entityManager.clear();
        assertEquals("DA_PROCESSARE", GoOrdine.findByOrdineId(
                dettaglio.getAnno(), dettaglio.getSerie(), dettaglio.getProgressivo()).getStatus());
    }

    @Test
    @TestTransaction
    void segnalaLaQueryNonValidaNellaSincronizzazioneDettagli() {
        assertThrows(RuntimeException.class, service::syncProntoConsegna);
    }

    @Test
    @TestTransaction
    void sincronizzaLaProntaConsegnaDelleTestate() {
        service.syncProntoTestata();
    }

    @Test
    @TestTransaction
    void cercaAltriOrdiniEPregressi() throws Exception {
        Ordine ordine = existingOrdineWithCliente();

        assertNotNull(service.findAltriOrdiniCliente(
                ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo(), ordine.getContoCliente()));
        FiltroOrdini filtro = new FiltroOrdini();
        filtro.setCodVenditore(ordine.getSerie());
        assertNotNull(service.getAllPregressi(filtro));
    }

    @Test
    void segnalaLaProiezioneClientiNonCompatibile() {
        assertThrows(RuntimeException.class, () -> service.findClienti());
    }

    @Test
    @TestTransaction
    void salvaPregressiSoloQuandoLaListaContieneOrdini() {
        assertFalse(service.salvaPregressi(List.of(), "901"));
        int progressivo = -Math.abs(UUID.randomUUID().hashCode());
        OrdineDTO dto = new OrdineDTO();
        dto.setAnno(1902);
        dto.setSerie("T");
        dto.setProgressivo(progressivo);

        assertTrue(service.salvaPregressi(List.of(dto), "901"));
        GoOrdVeicolo saved = GoOrdVeicolo.findById(new GoOrdVeicoloPK(1902, "T", progressivo));
        assertNotNull(saved);
        assertTrue(saved.getVenditore());
    }

    @Test
    @TestTransaction
    void eliminaUnaProgrammazioneERiordinaLeSuccessive() {
        int base = -Math.abs(UUID.randomUUID().hashCode());
        GoOrdVeicolo primo = vehicle(base, 1L);
        GoOrdVeicolo secondo = vehicle(base - 1, 2L);
        primo.persist();
        secondo.persist();

        service.eliminaProgrammazione(1902, "T", base);

        entityManager.flush();
        entityManager.clear();
        assertNull(GoOrdVeicolo.findById(new GoOrdVeicoloPK(1902, "T", base)));
        assertEquals(1L, ((GoOrdVeicolo) GoOrdVeicolo.findById(
                new GoOrdVeicoloPK(1902, "T", base - 1))).getOrdine());
        service.eliminaProgrammazione(1902, "T", Integer.MAX_VALUE);
    }

    @Test
    @TestTransaction
    void programmaNelPrimoSpazioLiberoDelGiro() {
        int base = uniqueNegativeKey();
        int veicolo = base - 10;
        LocalDate data = LocalDate.of(1902, 2, 1);
        vehicle(base, veicolo, data, 'P', 1L).persist();
        vehicle(base - 1, veicolo, data, 'P', 3L).persist();

        ProgrammaConsegnaDto dto = delivery(base - 2, veicolo, data, 'P', null);

        assertTrue(service.programmaConsegna(dto, ""));

        entityManager.flush();
        entityManager.clear();
        GoOrdVeicolo saved = GoOrdVeicolo.findById(new GoOrdVeicoloPK(1902, "T", base - 2));
        assertEquals(2L, saved.getOrdine());
        assertFalse(saved.getVenditore());
    }

    @Test
    @TestTransaction
    void inserisceNellaPosizioneRichiestaESpostaLeSuccessive() {
        int base = uniqueNegativeKey();
        int veicolo = base - 20;
        LocalDate data = LocalDate.of(1902, 2, 2);
        vehicle(base, veicolo, data, 'M', 1L).persist();
        vehicle(base - 1, veicolo, data, 'M', 2L).persist();

        assertTrue(service.programmaConsegna(delivery(base - 2, veicolo, data, 'M', 1L), "901"));

        entityManager.flush();
        entityManager.clear();
        assertEquals(1L, findVehicle(base - 2).getOrdine());
        assertTrue(findVehicle(base - 2).getVenditore());
        assertEquals(2L, findVehicle(base).getOrdine());
        assertEquals(3L, findVehicle(base - 1).getOrdine());
    }

    @Test
    @TestTransaction
    void spostaUnaConsegnaERiordinaIlGiroPrecedente() {
        int base = uniqueNegativeKey();
        LocalDate vecchiaData = LocalDate.of(1902, 2, 3);
        LocalDate nuovaData = LocalDate.of(1902, 2, 4);
        vehicle(base, base - 30, vecchiaData, 'M', 1L).persist();
        vehicle(base - 1, base - 30, vecchiaData, 'M', 2L).persist();

        assertTrue(service.programmaConsegna(
                delivery(base, base - 31, nuovaData, 'P', null), null));

        entityManager.flush();
        entityManager.clear();
        GoOrdVeicolo moved = findVehicle(base);
        assertEquals(base - 31, moved.getIdVeicolo());
        assertEquals(nuovaData, moved.getDataConsegna());
        assertEquals(1L, moved.getOrdine());
        assertEquals(1L, findVehicle(base - 1).getOrdine());
    }

    @Test
    void rifiutaUnaProgrammazioneSenzaDati() {
        assertFalse(service.programmaConsegna(null, null));
    }

    @Test
    @TestTransaction
    void segnalaLaCopiaDiUnOrdineInesistente() {
        ResponseDto result = service.copiaOrdine(1800, "TST", Integer.MAX_VALUE, "utente-test");
        assertTrue(result.getError());
        assertEquals("Ordine non trovato", result.getMsg());
    }

    @Test
    @TestTransaction
    void copiaUnOrdineConTutteLeRighe() {
        Ordine originale = Ordine.find("FROM Ordine o WHERE EXISTS (SELECT 1 FROM OrdineDettaglio d " +
                "WHERE d.anno = o.anno AND d.serie = o.serie AND d.progressivo = o.progressivo)").firstResult();
        if (originale == null) {
            throw new AssertionError("Il database di sviluppo non contiene ordini copiabili con righe");
        }
        long righeOriginali = OrdineDettaglio.count(
                "anno = ?1 and serie = ?2 and progressivo = ?3",
                originale.getAnno(), originale.getSerie(), originale.getProgressivo());
        Integer maxPrima = Ordine.find(
                "SELECT COALESCE(MAX(o.progressivo),0) FROM Ordine o WHERE o.anno = ?1 AND o.serie = ?2",
                java.time.Year.now().getValue(), originale.getSerie()).project(Integer.class).firstResult();

        ResponseDto result = service.copiaOrdine(originale.getAnno(), originale.getSerie(),
                originale.getProgressivo(), "utente-test");

        assertFalse(result.getError());
        assertTrue(result.getMsg().startsWith("Ordine copiato:"));
        int nuovoProgressivo = maxPrima + 1;
        Ordine copia = Ordine.findByOrdineId(
                java.time.Year.now().getValue(), originale.getSerie(), nuovoProgressivo);
        assertNotNull(copia);
        assertEquals(righeOriginali, OrdineDettaglio.count(
                "anno = ?1 and serie = ?2 and progressivo = ?3",
                copia.getAnno(), copia.getSerie(), copia.getProgressivo()));
        List<OrdineDettaglio> righeCopiate = OrdineDettaglio.find(
                "anno = ?1 and serie = ?2 and progressivo = ?3 order by rigo",
                copia.getAnno(), copia.getSerie(), copia.getProgressivo()).list();
        assertEquals(1, righeCopiate.getFirst().getRigo());
        assertTrue(righeCopiate.stream().allMatch(r -> "utente-test".equals(r.getSysCreateuser())));
    }

    @Test
    @TestTransaction
    void creaUnaFatturaDiAccontoConIRiferimentiAllOrdine() {
        Ordine ordine = Ordine.find("FROM Ordine o WHERE EXISTS (SELECT 1 FROM OrdineDettaglio d " +
                "WHERE d.anno = o.anno AND d.serie = o.serie AND d.progressivo = o.progressivo)").firstResult();
        if (ordine == null) {
            throw new AssertionError("Il database di sviluppo non contiene ordini fatturabili");
        }
        OrdineDettaglio dettaglio = OrdineDettaglio.find(
                "anno = ?1 and serie = ?2 and progressivo = ?3 and fCodiceIva is not null",
                ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo()).firstResult();
        FatturaAccontoDto dto = new FatturaAccontoDto();
        dto.setAnno(ordine.getAnno());
        dto.setSerie(ordine.getSerie());
        dto.setProgressivo(ordine.getProgressivo());
        dto.setContoCliente(ordine.getContoCliente());
        dto.setIva(dettaglio == null ? null : dettaglio.getFCodiceIva());
        dto.setNuovoAccontoIvato(122D);
        dto.setASaldo(false);

        String result = service.creaFatturaAcconto(List.of(dto), "utente-test");

        assertTrue(result.startsWith("Creata fattura acconto n."));
        Fatture fattura = Fatture.find(
                "anno = ?1 and serie = 'A' order by progressivo desc",
                java.time.Year.now().getValue()).firstResult();
        assertNotNull(fattura);
        assertEquals(3L, FattureDettaglio.count(
                "anno = ?1 and serie = ?2 and progressivo = ?3",
                fattura.getAnno(), fattura.getSerie(), fattura.getProgressivo()));
    }

    @Test
    @TestTransaction
    void calcolaResiduiFatturabiliPerOrdineEIva() {
        Ordine ordine = Ordine.find(
                "FROM Ordine o WHERE o.contoCliente is not null " +
                        "AND EXISTS (SELECT 1 FROM GoOrdine go WHERE go.anno = o.anno " +
                        "AND go.serie = o.serie AND go.progressivo = o.progressivo " +
                        "AND go.status <> 'ARCHIVIATO') " +
                        "AND EXISTS (SELECT 1 FROM OrdineDettaglio d WHERE d.anno = o.anno " +
                        "AND d.serie = o.serie AND d.progressivo = o.progressivo " +
                        "AND d.tipoRigo <> 'C' AND d.fCodiceIva is not null)")
                .firstResult();
        if (ordine == null) {
            throw new AssertionError("Il database di sviluppo non contiene ordini attivi fatturabili");
        }

        List<FatturaAccontoView> result = service.findOrdiniPerFatturaAcconto(
                ordine.getContoCliente());

        assertFalse(result.isEmpty());
        FatturaAccontoView view = result.stream()
                .filter(v -> ordine.getAnno().equals(v.getAnno())
                        && ordine.getSerie().equals(v.getSerie())
                        && ordine.getProgressivo().equals(v.getProgressivo()))
                .findFirst()
                .orElseThrow();
        assertFalse(view.getFatturaAccontoIvaViewList().isEmpty());
        for (FatturaAccontoIvaView iva : view.getFatturaAccontoIvaViewList()) {
            assertNotNull(iva.getDdtList());
            assertNotNull(iva.getAcconti());
            assertNotNull(iva.getImportoResiduo());
            assertNotNull(iva.getResiduoFatturabile());
        }
    }

    @Test
    @TestTransaction
    void calcolaIlMonitorDegliOrdiniNonOrdinati() throws Exception {
        OrdineclienteMonitorDto result = service.getOrdiniClienteNonOrdinati();
        assertNotNull(result);
        assertNotNull(result.getTotOrdNonDisp());
        assertNotNull(result.getTotOrdNonProcessati());
    }

    @Test
    void applicaTuttiIFiltriAllaRicercaPerStato() throws Exception {
        FiltroOrdini filtro = impossibleFilter();
        filtro.setProntoConsegna(true);
        filtro.setFiltroStatus("DA_ORDINARE");

        PageOrdineDto result = service.findAllByStatus(filtro);

        assertEquals(0, result.getCount());
        assertTrue(result.getList().isEmpty());

        FiltroOrdini senzaStato = impossibleFilter();
        PageOrdineDto nonArchiviati = service.findAllByStatus(senzaStato);
        assertEquals(0, nonArchiviati.getCount());
    }

    @Test
    void ricercaPerStatiConsegneERiservati() throws Exception {
        FiltroOrdini filtro = impossibleFilter();
        filtro.setStati(new java.util.ArrayList<>(List.of("DA_PROCESSARE")));
        filtro.setVeicolo(Integer.MIN_VALUE);
        filtro.setDataConsegnaStart(LocalDate.of(1800, 1, 1));
        filtro.setDataConsegnaEnd(LocalDate.of(1800, 1, 2));

        PageOrdineDto stati = service.findAllByStati(filtro);
        assertEquals(0, stati.getCount());
        assertTrue(stati.getList().isEmpty());

        ConsegneSettimanaliDto consegne = service.consegneSettimanali(filtro);
        assertEquals(6, consegne.getGiorni().size());
        assertTrue(consegne.getGiorni().stream().allMatch(g -> g.getNumeroConsegne() == 0));

        FiltroOrdini riservati = new FiltroOrdini();
        riservati.setStati(List.of("DA_PROCESSARE"));
        riservati.setCodVenditore("TST");
        riservati.setFiltroStatus("DA_PROCESSARE");
        assertTrue(service.findAllRiservati(riservati).getList().isEmpty());

        FiltroOrdini statoDettaglio = new FiltroOrdini();
        statoDettaglio.setFiltroStatus("COMPLETO");
        service.checkStatusDettaglio(statoDettaglio);

        FiltroOrdini consegneFiltrate = impossibleFilter();
        service.checkConsegnati(consegneFiltrate);
    }

    @Test
    @TestTransaction
    void raggruppaLeConsegneSettimanaliPerFasciaEVeicolo() throws Exception {
        LocalDate dataInizio = LocalDate.parse(dataConfig);
        Ordine ordine = Ordine.find("FROM Ordine o WHERE o.dataConferma >= :data " +
                        "AND o.provvisorio <> 'S' " +
                        "AND EXISTS (SELECT 1 FROM PianoConti p WHERE p.gruppoConto = o.gruppoCliente " +
                        "AND p.sottoConto = o.contoCliente) " +
                        "AND EXISTS (SELECT 1 FROM GoOrdine g WHERE g.anno = o.anno " +
                        "AND g.serie = o.serie AND g.progressivo = o.progressivo)",
                io.quarkus.panache.common.Parameters.with("data", dataInizio)).firstResult();
        Veicolo veicolo = Veicolo.findAll().firstResult();
        if (ordine == null || veicolo == null) {
            throw new AssertionError("Il database di sviluppo non contiene ordine e veicolo programmabili");
        }
        GoOrdVeicoloPK id = new GoOrdVeicoloPK(
                ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo());
        GoOrdVeicolo programmazione = GoOrdVeicolo.findById(id);
        if (programmazione == null) {
            programmazione = new GoOrdVeicolo();
            programmazione.setId(id);
            programmazione.setVenditore(Boolean.FALSE);
            programmazione.persist();
        }
        LocalDate lunedi = LocalDate.now().with(DayOfWeek.MONDAY);
        programmazione.setIdVeicolo(veicolo.getId());
        programmazione.setDataConsegna(lunedi);
        programmazione.setOraConsegna('M');
        programmazione.setOrdine(1L);
        FiltroOrdini filtro = new FiltroOrdini();
        filtro.setDeltaSettimana(0);
        filtro.setVeicolo(veicolo.getId());

        ConsegneSettimanaliDto result = service.consegneSettimanali(filtro);

        var giorno = result.getGiorni().stream()
                .filter(g -> g.getGiorno() == DayOfWeek.MONDAY)
                .findFirst().orElseThrow();
        assertTrue(giorno.getNumeroConsegne() >= 1);
        var fascia = giorno.getFasce().stream()
                .filter(f -> Character.valueOf('M').equals(f.getFascia()))
                .findFirst().orElseThrow();
        var gruppoVeicolo = fascia.getVeicoli().stream()
                .filter(v -> Integer.valueOf(veicolo.getId()).equals(v.getIdVeicolo()))
                .findFirst().orElseThrow();
        assertTrue(gruppoVeicolo.getConsegne().stream().anyMatch(o ->
                ordine.getAnno().equals(o.getAnno()) && ordine.getProgressivo().equals(o.getProgressivo())));
    }

    private FiltroOrdini impossibleFilter() {
        FiltroOrdini filtro = new FiltroOrdini();
        filtro.setAnno(1800);
        filtro.setProgressivo(Integer.MAX_VALUE);
        filtro.setCodVenditore("TST");
        filtro.setCliente("CLIENTE-INESISTENTE-TEST");
        filtro.setLuogo("LUOGO-INESISTENTE-TEST");
        filtro.setDataOrdine(LocalDate.of(1800, 1, 1));
        filtro.setPage(0);
        filtro.setSize(20);
        return filtro;
    }

    private GoOrdVeicolo vehicle(int progressivo, long ordine) {
        return vehicle(progressivo, -991, LocalDate.of(1902, 1, 1), 'M', ordine);
    }

    private GoOrdine goOrdine(int anno, String serie, int progressivo, String status) {
        GoOrdine result = new GoOrdine();
        result.setAnno(anno);
        result.setSerie(serie);
        result.setProgressivo(progressivo);
        result.setStatus(status);
        result.setWarnNoBolla(false);
        result.setLocked(false);
        result.setHasFirma(false);
        result.setHasProntoConsegna(false);
        result.setHasCarico(false);
        return result;
    }

    private GoOrdVeicolo vehicle(int progressivo, int veicolo, LocalDate data, char fascia, long ordine) {
        GoOrdVeicolo result = new GoOrdVeicolo();
        result.setId(new GoOrdVeicoloPK(1902, "T", progressivo));
        result.setIdVeicolo(veicolo);
        result.setDataConsegna(data);
        result.setOraConsegna(fascia);
        result.setOrdine(ordine);
        result.setVenditore(false);
        return result;
    }

    private ProgrammaConsegnaDto delivery(int progressivo, int veicolo, LocalDate data,
                                           char fascia, Long ordine) {
        ProgrammaConsegnaDto dto = new ProgrammaConsegnaDto();
        dto.setAnno(1902);
        dto.setSerie("T");
        dto.setProgressivo(progressivo);
        dto.setVeicolo(veicolo);
        dto.setDataConsegna(data);
        dto.setOraConsegna(fascia);
        dto.setOrdine(ordine);
        return dto;
    }

    private GoOrdVeicolo findVehicle(int progressivo) {
        return GoOrdVeicolo.findById(new GoOrdVeicoloPK(1902, "T", progressivo));
    }

    private int uniqueNegativeKey() {
        return -1_000_000_000 - Math.floorMod(UUID.randomUUID().hashCode(), 500_000_000);
    }

    private Ordine existingOrdineWithCliente() {
        Ordine ordine = Ordine.find("FROM Ordine o WHERE EXISTS (SELECT 1 FROM PianoConti p " +
                "WHERE p.gruppoConto = o.gruppoCliente AND p.sottoConto = o.contoCliente)").firstResult();
        if (ordine == null) {
            throw new AssertionError("Il database di sviluppo non contiene ordini con cliente");
        }
        return ordine;
    }
}
