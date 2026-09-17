package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;

import it.calolenoci.dto.AccontoDto;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.ResiduoDto;
import it.calolenoci.entity.GoOrdineDettaglio;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.entity.FattureDettaglio;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@QuarkusTest
class FatturaServiceTest {

    private static final int ANNO_TEST = 1901;

    @Inject
    FatturaService fatturaService;

    @Inject
    EntityManager entityManager;

    @InjectMock
    ResiduoService residuoService;

    @Test
    @TestTransaction
    void calcolaOrdineInteramenteApertoConScontiEIva() {
        TestData testData = newTestData();
        insertIva(testData.codiceIva(), 22D);
        insertOrdine(testData, 1, false);
        insertRigaOrdine(testData, 1, 10D, 100D, 10D, 5D, 0D, 2D, false);

        double expected = 100D * 0.90D * 0.95D * 0.98D * 10D * 1.22D;

        assertEquals(expected, fatturaService.getOrdiniAperti(testData.contoCliente()), 0.0001D);
    }

    @Test
    @TestTransaction
    void sottraeLeQuantitaGiaBollate() {
        TestData testData = newTestData();
        insertIva(testData.codiceIva(), 22D);
        insertOrdine(testData, 1, false);
        insertRigaOrdine(testData, 1, 10D, 100D, 0D, 0D, 0D, 0D, false);
        insertBolla(testData, 1, 4D);

        assertEquals(100D * 6D * 1.22D,
                fatturaService.getOrdiniAperti(testData.contoCliente()), 0.0001D);
    }

    @Test
    @TestTransaction
    void nonProduceImportiNegativiQuandoLaQuantitaBollataSuperaQuellaOrdinata() {
        TestData testData = newTestData();
        insertIva(testData.codiceIva(), 22D);
        insertOrdine(testData, 1, false);
        insertRigaOrdine(testData, 1, 10D, 100D, 0D, 0D, 0D, 0D, false);
        insertBolla(testData, 1, 12D);

        assertEquals(0D, fatturaService.getOrdiniAperti(testData.contoCliente()), 0.0001D);
    }

    @Test
    @TestTransaction
    void escludeOrdiniProvvisoriERigheDiSaldoAcconto() {
        TestData testData = newTestData();
        insertIva(testData.codiceIva(), 22D);
        insertOrdine(testData, 1, true);
        insertRigaOrdine(testData, 1, 10D, 100D, 0D, 0D, 0D, 0D, false);
        insertOrdine(testData, 2, false);
        insertRigaOrdine(testData, 2, 10D, 100D, 0D, 0D, 0D, 0D, true);

        assertEquals(0D, fatturaService.getOrdiniAperti(testData.contoCliente()), 0.0001D);
    }

    @Test
    @TestTransaction
    void consideraZeroIValoriEconomiciNulli() {
        TestData testData = newTestData();
        insertOrdine(testData, 1, false);
        insertRigaOrdine(testData, 1, 10D, null, null, null, null, null, false);

        assertEquals(0D, fatturaService.getOrdiniAperti(testData.contoCliente()), 0.0001D);
    }

    @Test
    @TestTransaction
    void restituisceRisultatiVuotiPerRiferimentiInesistenti() {
        String conto = "Z" + UUID.randomUUID().toString().substring(0, 5);

        assertTrue(fatturaService.getBolle(Integer.MIN_VALUE).isEmpty());
        assertTrue(fatturaService.getAcconti(conto).isEmpty());
        assertTrue(fatturaService.getAccontiPerOrdiniClienti(conto, List.of(new OrdineDettaglioDto())).isEmpty());
        assertEquals(0, fatturaService.countAccontiNonValidatiByOrdine(1800, "TST", Integer.MAX_VALUE));
        assertTrue(fatturaService.findAccontiNonValidatiByOrdine(1800, "TST", Integer.MAX_VALUE).isEmpty());
        assertFalse(fatturaService.isValidata(1800, "TST", Integer.MAX_VALUE));
        assertNull(fatturaService.getSaldoContabile(conto));
        assertEquals(0D, fatturaService.getAccontiFatturati(conto), 0.0001D);
        assertEquals(0D, fatturaService.getBolleNonFatturate(conto), 0.0001D);
    }

    @Test
    void recuperaLaVistaAggregataDelleBolle() {
        assertNotNull(fatturaService.getBolle());
    }

    @Test
    void gestisceUnaConfigurazioneDataNonValida() {
        FatturaService servizio = new FatturaService();
        servizio.dataCongig = "data-non-valida";

        assertTrue(servizio.getBolle().isEmpty());
    }

    @Test
    void restituisceUnaListaBolleVuotaPerUnaDataFutura() {
        FatturaService servizio = new FatturaService();
        servizio.dataCongig = "2999-01-01";

        assertTrue(servizio.getBolle().isEmpty());
    }

    @Test
    void associaIlRiferimentoOrdineAlleRigheDiAcconto() {
        Date data = new Date();
        AccontoDto acconto1 = acconto(2025, "A", 10, 1, "*ACC", "Acconto", data);
        AccontoDto acconto2 = acconto(2025, "A", 10, 2, "*ACC", "Secondo acconto", data);
        AccontoDto separatore = acconto(2025, "A", 10, 3, "*PZ", "testo", data);
        AccontoDto riferimento = acconto(2025, "A", 10, 4, "*PZ", "Ns. ord. 2024/B/123", data);

        List<AccontoDto> result = fatturaService.settaRifOrdCliente(
                List.of(acconto1, acconto2, separatore, riferimento));

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(a -> "2024/B/123".equals(a.getRifOrdCliente())));
    }

    @Test
    void ignoraAccontiIncompletiERiferimentiNonRiconoscibili() {
        assertTrue(fatturaService.settaRifOrdCliente(null).isEmpty());
        assertTrue(fatturaService.settaRifOrdCliente(List.of()).isEmpty());
        AccontoDto incompleto = new AccontoDto();
        AccontoDto acconto = acconto(2025, "A", 11, 1, "*ACC", "Acconto", new Date());
        AccontoDto separatore = acconto(2025, "A", 11, 2, "*PZ", "testo", new Date());
        AccontoDto riferimento = acconto(2025, "A", 11, 3, "*PZ", "nessun riferimento", new Date());

        assertTrue(fatturaService.settaRifOrdCliente(
                List.of(incompleto, acconto, separatore, riferimento)).isEmpty());
    }

    @Test
    void riconosceFormatiAlternativiESegnaSoloIlDocumentoCorretto() {
        Date data = new Date();
        AccontoDto parlato = acconto(2025, "A", 20, 1, "*ACC", "Acconto", data);
        AccontoDto separatoreParlato = acconto(2025, "A", 20, 2, "*PZ", "testo", data);
        AccontoDto riferimentoParlato = acconto(
                2025, "A", 20, 3, "*PZ", "Ns. conf. ord. n. 2024-ab-321", data);
        AccontoDto nudo = acconto(2025, "A", 21, 1, "*ACC", "Acconto", data);
        AccontoDto separatoreNudo = acconto(2025, "A", 21, 2, "*PZ", "testo", data);
        AccontoDto riferimentoNudo = acconto(
                2025, "A", 21, 3, "*PZ", "riferimento 2023-C-99", data);
        AccontoDto senzaRiferimento = acconto(2025, "A", 22, 1, "*ACC", "Acconto", data);

        List<AccontoDto> result = fatturaService.settaRifOrdCliente(List.of(
                senzaRiferimento,
                nudo, separatoreNudo, riferimentoNudo,
                parlato, separatoreParlato, riferimentoParlato));

        assertEquals(2, result.size());
        assertEquals("2024/ab/321", parlato.getRifOrdCliente());
        assertEquals("2023/C/99", nudo.getRifOrdCliente());
        assertNull(senzaRiferimento.getRifOrdCliente());
        assertEquals(20, result.get(0).getProgressivo());
        assertEquals(21, result.get(1).getProgressivo());
    }

    @Test
    void calcolaResiduoAccontoEScorporaGliStorni() {
        FatturaService servizio = spy(new FatturaService());
        EntityManager em = mock(EntityManager.class);
        Query accontoQuery = mock(Query.class);
        Query stornoQuery = mock(Query.class);
        servizio.em = em;

        AccontoDto acconto = acconto(2025, "A", 30, 1, "*ACC", "Acconto", new Date());
        acconto.setRifOrdCliente("2024/A/1");
        acconto.setPrezzo(100D);
        AccontoDto storno = new AccontoDto();
        storno.setOrdineCliente("2024/A/1");
        storno.setPrezzo(-20D);

        when(em.createNamedQuery("AccontoDto")).thenReturn(accontoQuery);
        when(accontoQuery.setParameter("sottoConto", "C100")).thenReturn(accontoQuery);
        when(accontoQuery.getResultList()).thenReturn(List.of(acconto));
        when(em.createNamedQuery("StornoDto")).thenReturn(stornoQuery);
        when(stornoQuery.setParameter(anyString(), any())).thenReturn(stornoQuery);
        when(stornoQuery.getResultList()).thenReturn(List.of(storno));
        doReturn(List.of(acconto)).when(servizio).settaRifOrdCliente(anyList());

        List<AccontoDto> result = servizio.getAcconti("C100");

        assertEquals(1, result.size());
        assertEquals(80D, result.get(0).getImportoResiduo(), 0.0001D);
        assertEquals(1, result.get(0).getStorni().size());
    }

    @Test
    void filtraAccontiPerOrdineEResiduoPositivo() {
        FatturaService servizio = spy(new FatturaService());
        EntityManager em = mock(EntityManager.class);
        Query accontoQuery = mock(Query.class);
        Query stornoQuery = mock(Query.class);
        servizio.em = em;

        AccontoDto acconto = acconto(2025, "A", 31, 1, "*ACC", "Acconto", new Date());
        acconto.setRifOrdCliente("2024/A/2");
        acconto.setPrezzo(50D);
        when(em.createNamedQuery("AccontoDto")).thenReturn(accontoQuery);
        when(accontoQuery.setParameter("sottoConto", "C200")).thenReturn(accontoQuery);
        when(accontoQuery.getResultList()).thenReturn(List.of(acconto));
        when(em.createNamedQuery("StornoDto")).thenReturn(stornoQuery);
        when(stornoQuery.setParameter(anyString(), any())).thenReturn(stornoQuery);
        when(stornoQuery.getResultList()).thenReturn(List.of());
        doReturn(List.of(acconto)).when(servizio).settaRifOrdCliente(anyList());

        OrdineDettaglioDto ordine = new OrdineDettaglioDto();
        ordine.setAnno(2024);
        ordine.setSerie("A");
        ordine.setProgressivo(2);
        ordine.setFCodiceIva("22");
        acconto.setIva("22");

        List<AccontoDto> result = servizio.getAccontiPerOrdiniClienti("C200", List.of(ordine));

        assertEquals(1, result.size());
        assertEquals(50D, result.get(0).getImportoResiduo(), 0.0001D);
    }

    @Test
    @TestTransaction
    void aggiornaStatoBollaPerResiduoZeroEPositivo() {
        GoOrdineDettaglio go = GoOrdineDettaglio.find(
                "FROM GoOrdineDettaglio g WHERE EXISTS (SELECT 1 FROM OrdineDettaglio d " +
                        "WHERE d.progrGenerale = g.progrGenerale)").firstResult();
        if (go == null) {
            throw new AssertionError("Il database di sviluppo non contiene righe ordine con stato applicativo");
        }
        OrdineDettaglio ordine = OrdineDettaglio.find("progrGenerale", go.getProgrGenerale()).firstResult();
        OrdineDettaglioDto dto = new OrdineDettaglioDto();
        dto.setAnno(ordine.getAnno());
        dto.setSerie(ordine.getSerie());
        dto.setProgressivo(ordine.getProgressivo());
        dto.setRigo(ordine.getRigo());
        dto.setProgrGenerale(ordine.getProgrGenerale());
        ResiduoDto residuoZero = new ResiduoDto();
        residuoZero.setResiduo(0D);
        when(residuoService.calcolaResiduiMap(anyList()))
                .thenReturn(java.util.Map.of(ordine.getProgrGenerale(), residuoZero));

        fatturaService.aggiornaStatoOrdine(List.of(dto));

        entityManager.flush();
        entityManager.clear();
        GoOrdineDettaglio consegnato = GoOrdineDettaglio.find(
                "progrGenerale", ordine.getProgrGenerale()).firstResult();
        OrdineDettaglio saldato = OrdineDettaglio.find(
                "progrGenerale", ordine.getProgrGenerale()).firstResult();
        assertEquals(0D, consegnato.getQtaDaConsegnare());
        assertTrue(consegnato.getFlagConsegnato());
        assertTrue(consegnato.getFlBolla());
        assertEquals("S", saldato.getSaldoAcconto());

        ResiduoDto residuoPositivo = new ResiduoDto();
        residuoPositivo.setResiduo(2D);
        when(residuoService.calcolaResiduiMap(anyList()))
                .thenReturn(java.util.Map.of(ordine.getProgrGenerale(), residuoPositivo));
        fatturaService.aggiornaStatoOrdine(List.of(dto));

        entityManager.flush();
        entityManager.clear();
        assertFalse(((GoOrdineDettaglio) GoOrdineDettaglio.find(
                "progrGenerale", ordine.getProgrGenerale()).firstResult()).getFlagConsegnato());
        assertEquals("A", ((OrdineDettaglio) OrdineDettaglio.find(
                "progrGenerale", ordine.getProgrGenerale()).firstResult()).getSaldoAcconto());
    }

    @Test
    @TestTransaction
    void creaUnaBollaCompletaPerUnaRigaOrdineReale() {
        OrdineDettaglio ordine = existingFatturabile();
        OrdineDettaglioDto dto = dettaglioDto(ordine);
        dto.setQtaProntoConsegna(Math.min(1D, ordine.getQuantita()));
        ResiduoDto residuo = new ResiduoDto();
        residuo.setResiduo(ordine.getQuantita());
        when(residuoService.calcolaResiduiMap(anyList()))
                .thenReturn(java.util.Map.of(ordine.getProgrGenerale(), residuo));

        it.calolenoci.dto.ResponseDto result = fatturaService.creaBollaCompleta(
                List.of(dto), List.of(), "utente-test");

        entityManager.flush();
        assertFalse(result.getError());
        assertEquals(201, result.getCode().getStatusCode());
        assertTrue(result.getMsg().startsWith("Creata bolla n. "));
        assertTrue(FattureDettaglio.count("progrOrdCli", ordine.getProgrGenerale()) > 0);
    }

    @Test
    @TestTransaction
    void applicaLoStornoDellAccontoAllaBolla() {
        OrdineDettaglio ordine = existingFatturabile();
        OrdineDettaglioDto dto = dettaglioDto(ordine);
        dto.setQtaProntoConsegna(Math.min(1D, ordine.getQuantita()));
        double imponibile = dto.getPrezzoScontato() * dto.getQtaProntoConsegna();
        AccontoDto acconto = new AccontoDto();
        acconto.setRifOrdCliente(ordine.getAnno() + "/" + ordine.getSerie() + "/" + ordine.getProgressivo());
        acconto.setIva(ordine.getFCodiceIva());
        acconto.setDataFattura(new Date());
        acconto.setNumeroFattura("ACC-TEST");
        acconto.setImportoResiduo(imponibile / 2D);
        ResiduoDto residuo = new ResiduoDto();
        residuo.setResiduo(ordine.getQuantita());
        when(residuoService.calcolaResiduiMap(anyList()))
                .thenReturn(java.util.Map.of(ordine.getProgrGenerale(), residuo));

        String result = fatturaService.creaBolla(
                List.of(dto), List.of(acconto), "utente-test");

        entityManager.flush();
        assertTrue(result.startsWith("Creata bolla n. "));
        assertEquals(0D, acconto.getImportoResiduo(), 0.01);
        assertTrue(FattureDettaglio.count(
                "fArticolo = ?1 and fDescrArticolo like ?2", "*ACC", "Storno fattura acconto%") > 0);
    }

    @Test
    @TestTransaction
    void rifiutaUnaBollaQuandoIlResiduoNonEsiste() {
        OrdineDettaglio ordine = existingFatturabile();
        OrdineDettaglioDto dto = dettaglioDto(ordine);
        dto.setQtaProntoConsegna(1D);
        when(residuoService.calcolaResiduiMap(anyList())).thenReturn(java.util.Map.of());

        it.calolenoci.dto.ResponseDto result = fatturaService.creaBollaCompleta(
                List.of(dto), List.of(), "utente-test");

        assertTrue(result.getError());
        assertEquals(400, result.getCode().getStatusCode());
        assertTrue(result.getMsg().startsWith("Residuo non trovato"));
    }

    @Test
    @TestTransaction
    void rifiutaQuantitaSuperioreAlResiduo() {
        OrdineDettaglio ordine = existingFatturabile();
        OrdineDettaglioDto dto = dettaglioDto(ordine);
        dto.setQtaProntoConsegna(2D);
        ResiduoDto residuo = new ResiduoDto();
        residuo.setResiduo(1D);
        when(residuoService.calcolaResiduiMap(anyList()))
                .thenReturn(java.util.Map.of(ordine.getProgrGenerale(), residuo));

        it.calolenoci.dto.ResponseDto result = fatturaService.creaBollaCompleta(
                List.of(dto), List.of(), "utente-test");

        assertTrue(result.getError());
        assertEquals(400, result.getCode().getStatusCode());
        assertTrue(result.getMsg().startsWith("Quantità superiore al residuo"));
    }

    @Test
    @TestTransaction
    void rifiutaUnArticoloGiaInteramenteConsegnato() {
        OrdineDettaglio ordine = existingFatturabile();
        OrdineDettaglioDto dto = dettaglioDto(ordine);
        dto.setQtaProntoConsegna(1D);
        ResiduoDto residuo = new ResiduoDto();
        residuo.setResiduo(0D);
        when(residuoService.calcolaResiduiMap(anyList()))
                .thenReturn(java.util.Map.of(ordine.getProgrGenerale(), residuo));

        it.calolenoci.dto.ResponseDto result = fatturaService.creaBollaCompleta(
                List.of(dto), List.of(), "utente-test");

        assertTrue(result.getError());
        assertEquals(400, result.getCode().getStatusCode());
        assertTrue(result.getMsg().startsWith("Articolo già completamente consegnato"));
    }

    @Test
    void nonAggiornaLoStatoSenzaRigheOrdine() {
        fatturaService.aggiornaStatoOrdine(null);
        fatturaService.aggiornaStatoOrdine(List.of());
    }

    private OrdineDettaglioDto dettaglioDto(OrdineDettaglio ordine) {
        OrdineDettaglioDto dto = new OrdineDettaglioDto();
        dto.setAnno(ordine.getAnno());
        dto.setSerie(ordine.getSerie());
        dto.setProgressivo(ordine.getProgressivo());
        dto.setRigo(ordine.getRigo());
        dto.setProgrGenerale(ordine.getProgrGenerale());
        dto.setTipoRigo(ordine.getTipoRigo());
        dto.setFArticolo(ordine.getFArticolo());
        dto.setFDescrArticolo(ordine.getFDescrArticolo());
        dto.setFCodiceIva(ordine.getFCodiceIva());
        dto.setQuantita(ordine.getQuantita());
        dto.setPrezzo(ordine.getPrezzo());
        dto.setPrezzoScontato(ordine.getPrezzo());
        return dto;
    }

    private OrdineDettaglio existingFatturabile() {
        OrdineDettaglio ordine = OrdineDettaglio.find(
                "FROM OrdineDettaglio d WHERE d.progrGenerale is not null " +
                        "AND d.quantita is not null AND d.quantita > 0 AND d.prezzo is not null " +
                        "AND (d.tipoRigo is null OR d.tipoRigo = '' OR d.tipoRigo = ' ') " +
                        "AND EXISTS (SELECT 1 FROM Ordine o WHERE o.anno = d.anno " +
                        "AND o.serie = d.serie AND o.progressivo = d.progressivo)").firstResult();
        if (ordine == null) {
            throw new AssertionError("Il database di sviluppo non contiene righe fatturabili");
        }
        return ordine;
    }

    private AccontoDto acconto(int anno, String serie, int progressivo, int rigo,
                               String articolo, String operazione, Date data) {
        AccontoDto dto = new AccontoDto();
        dto.setAnno(anno);
        dto.setSerie(serie);
        dto.setProgressivo(progressivo);
        dto.setFArticolo(articolo);
        dto.setOperazione(operazione);
        dto.setDataFattura(data);
        dto.setNumeroFattura(String.valueOf(rigo));
        dto.setPrezzo(100D);
        dto.setIva("22");
        return dto;
    }

    private TestData newTestData() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 5).toUpperCase();
        String contoCliente = "T" + suffix;
        String codiceIva = suffix.substring(0, 3);
        int baseProgressivo = -Math.abs(UUID.randomUUID().hashCode());
        return new TestData(contoCliente, codiceIva, baseProgressivo);
    }

    private void insertIva(String codiceIva, Double aliquota) {
        entityManager.createNativeQuery("INSERT INTO TGCI (CODICEIVA, ALIQUOTA) VALUES (:codiceIva, :aliquota)")
                .setParameter("codiceIva", codiceIva)
                .setParameter("aliquota", aliquota)
                .executeUpdate();
    }

    private void insertOrdine(TestData testData, int offset, boolean provvisorio) {
        entityManager.createNativeQuery("INSERT INTO ORDCLI " +
                        "(ANNO, SERIE, PROGRESSIVO, GRUPPOCLIENTE, CONTOCLIENTE, PROVVISORIO) " +
                        "VALUES (:anno, 'T', :progressivo, 1231, :contoCliente, :provvisorio)")
                .setParameter("anno", ANNO_TEST)
                .setParameter("progressivo", testData.baseProgressivo() + offset)
                .setParameter("contoCliente", testData.contoCliente())
                .setParameter("provvisorio", provvisorio ? "S" : "N")
                .executeUpdate();
    }

    private void insertRigaOrdine(TestData testData, int offset, Double quantita, Double prezzo,
                                  Double scontoArticolo, Double scontoCliente1, Double scontoCliente2,
                                  Double scontoPagamento, boolean saldoAcconto) {
        entityManager.createNativeQuery("INSERT INTO ORDCLI2 " +
                        "(ANNO, SERIE, PROGRESSIVO, RIGO, PROGRGENERALE, QUANTITA, PREZZO, " +
                        "SCONTOARTICOLO, SCONTOC1, SCONTOC2, SCONTOP, FCODICEIVA, SALDOACCONTO) " +
                        "VALUES (:anno, 'T', :progressivo, 1, :progrGenerale, :quantita, :prezzo, " +
                        ":scontoArticolo, :scontoCliente1, :scontoCliente2, :scontoPagamento, " +
                        ":codiceIva, :saldoAcconto)")
                .setParameter("anno", ANNO_TEST)
                .setParameter("progressivo", testData.baseProgressivo() + offset)
                .setParameter("progrGenerale", testData.baseProgressivo() + offset)
                .setParameter("quantita", quantita)
                .setParameter("prezzo", prezzo)
                .setParameter("scontoArticolo", scontoArticolo)
                .setParameter("scontoCliente1", scontoCliente1)
                .setParameter("scontoCliente2", scontoCliente2)
                .setParameter("scontoPagamento", scontoPagamento)
                .setParameter("codiceIva", testData.codiceIva())
                .setParameter("saldoAcconto", saldoAcconto ? "S" : "N")
                .executeUpdate();
    }

    private void insertBolla(TestData testData, int offset, Double quantita) {
        int progressivo = testData.baseProgressivo() + offset;
        entityManager.createNativeQuery("INSERT INTO FATTURE (ANNO, SERIE, PROGRESSIVO) " +
                        "VALUES (:anno, 'B', :progressivo)")
                .setParameter("anno", ANNO_TEST)
                .setParameter("progressivo", progressivo)
                .executeUpdate();
        entityManager.createNativeQuery("INSERT INTO FATTURE2 " +
                        "(ANNO, SERIE, PROGRESSIVO, RIGO, PROGRORDCLI, QUANTITA) " +
                        "VALUES (:anno, 'B', :progressivo, 1, :progrOrdine, :quantita)")
                .setParameter("anno", ANNO_TEST)
                .setParameter("progressivo", progressivo)
                .setParameter("progrOrdine", progressivo)
                .setParameter("quantita", quantita)
                .executeUpdate();
    }

    private record TestData(String contoCliente, String codiceIva, int baseProgressivo) {
    }
}
