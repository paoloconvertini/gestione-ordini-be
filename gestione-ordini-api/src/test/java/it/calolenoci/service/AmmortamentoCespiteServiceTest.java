package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.mockito.MockitoConfig;
import it.calolenoci.dto.CespiteRequest;
import it.calolenoci.dto.FiltroCespite;
import it.calolenoci.dto.QuadraturaCespiteRequest;
import it.calolenoci.dto.RegistroCespitiDto;
import it.calolenoci.dto.PrimanotaDto;
import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.CategoriaCespite;
import it.calolenoci.entity.Cespite;
import it.calolenoci.entity.PianoConti;
import it.calolenoci.entity.Primanota;
import it.calolenoci.entity.QuadraturaCespite;
import it.calolenoci.entity.TipoSuperAmm;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@QuarkusTest
class AmmortamentoCespiteServiceTest {

    @Inject
    AmmortamentoCespiteService service;

    @Inject
    EntityManager entityManager;

    @InjectMock
    @MockitoConfig(convertScopes = true)
    JasperService jasperService;

    @Test
    @TestTransaction
    void calcolaLeQuoteFinoAllaDataRichiesta() {
        String tipo = createCategoria(20D).getTipoCespite();
        Cespite cespite = createCespite(tipo, 1_000D, LocalDate.of(2022, 1, 1), false);

        List<AmmortamentoCespite> result = service.calcoloSingoloCespite(
                cespite, LocalDate.of(2024, 12, 31));

        assertEquals(3, result.size());
        assertEquals(200D, result.getFirst().getQuota(), 0.01);
        assertEquals(600D, result.getLast().getFondo(), 0.01);
        assertEquals(400D, result.getLast().getResiduo(), 0.01);
    }

    @Test
    @TestTransaction
    void dimezzaLaPercentualeDelPrimoAnno() {
        String tipo = createCategoria(20D).getTipoCespite();
        Cespite cespite = createCespite(tipo, 1_000D, LocalDate.of(2024, 1, 1), true);

        List<AmmortamentoCespite> result = service.calcoloSingoloCespite(
                cespite, LocalDate.of(2024, 12, 31));

        assertEquals(1, result.size());
        assertEquals(100D, result.getFirst().getQuota(), 0.01);
        assertEquals(10D, result.getFirst().getPercAmm(), 0.01);
    }

    @Test
    @TestTransaction
    void applicaLaQuadraturaAllaQuotaAnnuale() {
        String tipo = createCategoria(20D).getTipoCespite();
        Cespite cespite = createCespite(tipo, 1_000D, LocalDate.of(2024, 1, 1), false);
        service.saveQuadratura(new QuadraturaCespiteRequest(null, cespite.getId(), 2024, 15D));

        List<AmmortamentoCespite> result = service.calcoloSingoloCespite(
                cespite, LocalDate.of(2024, 12, 31));

        assertEquals(150D, result.getFirst().getQuota(), 0.01);
        assertEquals(15D, result.getFirst().getPercAmm(), 0.01);
    }

    @Test
    @TestTransaction
    void salvaEAggiornaLaQuadratura() {
        String idCespite = UUID.randomUUID().toString();
        QuadraturaCespiteRequest request = new QuadraturaCespiteRequest(null, idCespite, 2025, 12.5D);

        service.saveQuadratura(request);
        service.saveQuadratura(new QuadraturaCespiteRequest(null, idCespite, 2025, 17.5D));

        QuadraturaCespite result = QuadraturaCespite.find("idCespite = ?1 and anno = ?2", idCespite, 2025)
                .singleResult();
        assertEquals(17.5D, result.getAmmortamento(), 0.01);
        assertEquals(1, QuadraturaCespite.count("idCespite = ?1 and anno = ?2", idCespite, 2025));
    }

    @Test
    @TestTransaction
    void nonCalcolaCategorieSenzaPercentuale() {
        String tipo = createCategoria(0D).getTipoCespite();
        Cespite cespite = createCespite(tipo, 1_000D, LocalDate.of(2024, 1, 1), false);

        assertTrue(service.calcoloSingoloCespite(cespite, LocalDate.of(2024, 12, 31)).isEmpty());
    }

    @Test
    @TestTransaction
    void ricalcolaEPersisteLeQuoteDellAnnoRichiesto() {
        String tipo = createCategoria(20D).getTipoCespite();
        Cespite cespite = createCespite(tipo, 1_000D, LocalDate.of(1800, 1, 1), false);

        service.calcola(LocalDate.of(1800, 12, 31));

        entityManager.flush();
        List<AmmortamentoCespite> result = AmmortamentoCespite.find(
                "idAmmortamento = ?1 and anno = ?2", cespite.getId(), 1800).list();
        assertEquals(1, result.size());
        assertEquals(200D, result.getFirst().getQuota(), 0.01);
    }

    @Test
    @TestTransaction
    void prosegueIlCalcoloDallAmmortamentoDellAnnoPrecedente() {
        String tipo = createCategoria(20D).getTipoCespite();
        Cespite cespite = createCespite(tipo, 1_000D, LocalDate.of(1799, 1, 1), false);
        AmmortamentoCespite precedente = new AmmortamentoCespite();
        precedente.setIdAmmortamento(cespite.getId());
        precedente.setDataAmm(LocalDate.of(1800, 12, 31));
        precedente.setAnno(1800);
        precedente.setPercAmm(20D);
        precedente.setQuota(200D);
        precedente.setFondo(400D);
        precedente.setResiduo(600D);
        precedente.persist();
        service.saveQuadratura(new QuadraturaCespiteRequest(null, cespite.getId(), 1801, 15D));

        service.calcola(LocalDate.of(1801, 12, 31));

        entityManager.flush();
        AmmortamentoCespite result = AmmortamentoCespite.find(
                "idAmmortamento = ?1 and anno = ?2", cespite.getId(), 1801).singleResult();
        assertEquals(150D, result.getQuota(), 0.01);
        assertEquals(550D, result.getFondo(), 0.01);
        assertEquals(450D, result.getResiduo(), 0.01);
    }

    @Test
    @TestTransaction
    void registraLEliminazioneEDisattivaIlCespite() {
        String tipo = createCategoria(20D).getTipoCespite();
        Cespite cespite = createCespite(tipo, 1_000D, LocalDate.of(2022, 1, 1), false);
        cespite.setDataVendita(LocalDate.of(2023, 6, 30));
        cespite.setIntestatarioVendita(null);

        List<AmmortamentoCespite> result = service.calcoloSingoloCespite(
                cespite, LocalDate.of(2025, 12, 31));

        assertFalse(cespite.getAttivo());
        assertTrue(result.stream().anyMatch(a -> a.getDescrizione().startsWith("ELIMINAZIONE")));
    }

    @Test
    @TestTransaction
    void registraLaVenditaDelCespiteECalcolaLaPlusMinusvalenza() {
        String tipo = createCategoria(20D).getTipoCespite();
        Cespite cespite = createCespite(tipo, 1_000D, LocalDate.of(2022, 1, 1), false);
        cespite.setDataVendita(LocalDate.of(2023, 6, 30));
        cespite.setIntestatarioVendita("Cliente test");
        cespite.setNumDocVendita("FT-1");
        cespite.setImportoVendita(750D);

        List<AmmortamentoCespite> result = service.calcoloSingoloCespite(
                cespite, LocalDate.of(2025, 12, 31));

        assertFalse(cespite.getAttivo());
        assertTrue(result.stream().anyMatch(a -> "VENDITA CESPITE".equals(a.getDescrizione())));
        assertTrue(result.stream().anyMatch(a -> a.getDescrizione().startsWith("venduto a Cliente test")));
        assertTrue(result.stream().anyMatch(a -> "Plus/Minus valenza".equals(a.getDescrizione())));
    }

    @Test
    @TestTransaction
    void calcolaIlSuperAmmortamento() {
        String tipo = createCategoria(20D).getTipoCespite();
        TipoSuperAmm superAmm = TipoSuperAmm.find("perc is not null and perc > 0").firstResult();
        if (superAmm == null) {
            throw new AssertionError("Il database di sviluppo non contiene tipi super ammortamento");
        }
        Cespite cespite = createCespite(tipo, 1_000D, LocalDate.of(2024, 1, 1), false);
        cespite.setSuperAmm(superAmm.id);

        AmmortamentoCespite result = service.calcoloSingoloCespite(
                cespite, LocalDate.of(2024, 12, 31)).getFirst();

        assertEquals(20D, result.getSuperPercentuale(), 0.01);
        assertEquals(20D * (superAmm.getPerc() * 10D) / 100D, result.getSuperQuota(), 0.01);
    }

    @Test
    @TestTransaction
    void nonCreaCespitiPerUnContoNonConfigurato() {
        long before = Cespite.count();
        PrimanotaDto dto = new PrimanotaDto();
        dto.setGruppoconto(Integer.MIN_VALUE);
        dto.setSottoconto("CONTO-INESISTENTE");

        service.createCespite(dto);

        assertEquals(before, Cespite.count());
    }

    @Test
    @TestTransaction
    void cambiaCategoriaEProgressivoDelCespite() {
        CategoriaCespite iniziale = createCategoria(10D);
        CategoriaCespite nuova = createCategoria(25D);
        Cespite cespite = createCespite(iniziale.getTipoCespite(), 500D, LocalDate.of(2024, 1, 1), false);
        CespiteRequest request = new CespiteRequest();
        request.setId(cespite.getId());
        request.setTipoCespite(nuova.getTipoCespite());

        service.updateCespiti(request);

        entityManager.flush();
        entityManager.clear();
        Cespite updated = Cespite.findById(cespite.getId());
        assertEquals(nuova.getTipoCespite(), updated.getTipoCespite());
        assertEquals(1, updated.getProgressivo2());
    }

    @Test
    @TestTransaction
    void aggiornaLaCategoriaAncheSullaPrimaNota() {
        CategoriaCespite iniziale = createCategoria(10D);
        CategoriaCespite nuova = createCategoria(25D);
        nuova.setCostoGruppo(9876);
        nuova.setCostoConto("CESP25");
        Cespite cespite = createCespite(iniziale.getTipoCespite(), 500D,
                LocalDate.of(2024, 1, 1), false);
        cespite.setAnno(1798);
        cespite.setGiornale("T");
        cespite.setProtocollo(Integer.MAX_VALUE - 2);
        Primanota primanota = createPrimanota(cespite.getAnno(), cespite.getGiornale(),
                cespite.getProtocollo(), cespite.getImporto(), 1, "TEST", null);
        CespiteRequest request = new CespiteRequest();
        request.setId(cespite.getId());
        request.setTipoCespite(nuova.getTipoCespite());

        service.updateCespiti(request);

        entityManager.flush();
        assertEquals(nuova.getCostoGruppo(), primanota.getGruppoconto());
        assertEquals(nuova.getCostoConto(), primanota.getSottoconto());
    }


    @Test
    @TestTransaction
    void creaIlCespiteDallaPrimaNotaConfigurata() {
        PianoConti conto = PianoConti.<PianoConti>listAll().stream()
                .filter(p -> CategoriaCespite.count("costoGruppo = ?1 and costoConto = ?2",
                        p.getGruppoConto(), p.getSottoConto()) == 0)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Nessun conto disponibile per il test cespiti"));
        CategoriaCespite categoria = createCategoria(20D);
        categoria.setCostoGruppo(conto.getGruppoConto());
        categoria.setCostoConto(conto.getSottoConto());
        int anno = 1797;
        String giornale = "T";
        int protocollo = Integer.MAX_VALUE - 3;
        createPrimanota(anno, giornale, protocollo, 1_250D,
                conto.getGruppoConto(), conto.getSottoConto(), Integer.MIN_VALUE);
        PrimanotaDto dto = new PrimanotaDto(LocalDate.of(1797, 3, 15), "FT-CESP",
                null, conto.getGruppoConto(), conto.getSottoConto(), "Macchinario test",
                1_250D, null, protocollo, anno, giornale, 1);

        service.createCespite(dto);

        entityManager.flush();
        Cespite result = Cespite.find("anno = ?1 and giornale = ?2 and protocollo = ?3",
                anno, giornale, protocollo).singleResult();
        assertEquals(categoria.getTipoCespite(), result.getTipoCespite());
        assertEquals("Macchinario test", result.getCespite());
        assertEquals(1, result.getProgressivo1());
        assertEquals(LocalDate.of(1797, 3, 15), result.getDataInizioCalcoloAmm());
        assertTrue(result.getFornitore().startsWith(conto.getIntestazione()));
    }

    @Test
    void gestisceEsitoEDerroreDellaGenerazioneReport() throws Exception {
        RegistroCespitiDto view = new RegistroCespitiDto();
        File report = new File("registro-test.pdf");
        when(jasperService.createReport(view)).thenReturn(report).thenThrow(new RuntimeException("errore test"));

        assertSame(report, service.scaricaRegistroCespiti(view));
        assertNull(service.scaricaRegistroCespiti(view));
    }

    @Test
    @TestTransaction
    void costruisceIlRegistroCespitiConRiepiloghi() {
        CategoriaCespite categoria = createCategoria(20D);
        Cespite cespite = createCespite(categoria.getTipoCespite(), 1_000D,
                LocalDate.of(2022, 1, 1), false);
        cespite.setFornitore("Fornitore test");
        List<AmmortamentoCespite> quote = service.calcoloSingoloCespite(
                cespite, LocalDate.of(2024, 12, 31));
        AmmortamentoCespite.persist(quote);
        FiltroCespite filtro = new FiltroCespite();
        filtro.setTipoCespite(categoria.getTipoCespite());
        filtro.setData("31122024");

        RegistroCespitiDto result = service.getRegistroCespiti(filtro);

        assertEquals(1, result.getCespiteList().size());
        assertEquals(categoria.getTipoCespite(), result.getCespiteList().getFirst().getTipoCespite());
        assertEquals(1_000D,
                result.getCespiteSommaDto().getInizioEsercizio().getValoreAggiornato(), 0.01);
        assertNotNull(result.getCespiteSommaDto().getFineEsercizio());
    }


    private CategoriaCespite createCategoria(double percentuale) {
        CategoriaCespite categoria = new CategoriaCespite();
        categoria.setTipoCespite(uniqueTipo());
        categoria.setCodice("TEST");
        categoria.setDescrizione("Categoria test");
        categoria.setPercAmmortamento(percentuale);
        categoria.persist();
        return categoria;
    }

    private Cespite createCespite(String tipo, double importo, LocalDate dataInizio, boolean primoAnno) {
        Cespite cespite = new Cespite();
        cespite.setTipoCespite(tipo);
        cespite.setProgressivo1(1);
        cespite.setProgressivo2(1);
        cespite.setCespite("Cespite test");
        cespite.setDataAcq(dataInizio);
        cespite.setDataInizioCalcoloAmm(dataInizio);
        cespite.setImporto(importo);
        cespite.setAttivo(true);
        cespite.setFlPrimoAnno(primoAnno);
        cespite.persist();
        return cespite;
    }

    private Primanota createPrimanota(int anno, String giornale, int protocollo, double importo,
                                      int gruppoConto, String sottoConto, Integer pid) {
        Integer maxProgressivo = Primanota.find("select MAX(progrgenerale) from Primanota")
                .project(Integer.class).firstResult();
        Primanota primanota = new Primanota();
        primanota.setAnno(anno);
        primanota.setGiornale(giornale);
        primanota.setProtocollo(protocollo);
        primanota.setProgrprimanota(1);
        primanota.setProgrgenerale(maxProgressivo == null ? 1 : maxProgressivo + 1);
        primanota.setImporto(importo);
        primanota.setGruppoconto(gruppoConto);
        primanota.setSottoconto(sottoConto);
        primanota.setPid(pid);
        primanota.persist();
        return primanota;
    }

    private String uniqueTipo() {
        String tipo;
        do {
            tipo = UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        } while (CategoriaCespite.count("tipoCespite", tipo) > 0);
        return tipo;
    }
}
