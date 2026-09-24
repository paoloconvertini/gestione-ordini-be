package it.calolenoci.service;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import it.calolenoci.dto.FiltroArticoli;
import it.calolenoci.dto.CodificaArticoliDto;
import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.PianoContiDto;
import it.calolenoci.dto.ResiduoDto;
import it.calolenoci.dto.ResponseOrdineDettaglio;
import it.calolenoci.entity.FornitoreArticolo;
import it.calolenoci.entity.FornitoreArticoloId;
import it.calolenoci.entity.GoOrdine;
import it.calolenoci.entity.GoOrdineDettaglio;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.entity.Articolo;
import it.calolenoci.entity.ArticoloClasseFornitore;
import it.calolenoci.entity.PianoConti;
import it.calolenoci.enums.StatoOrdineEnum;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@QuarkusTest
class ArticoloServiceTest {

    @Inject
    ArticoloService service;

    @InjectMock
    OrdineService ordineService;

    @InjectMock
    ResiduoService residuoService;

    @InjectMock
    FatturaService fatturaService;

    @InjectMock
    MailService mailService;

    @Test
    @TestTransaction
    void recuperaDettaglioETotaleDellOrdine() {
        Ordine ordine = existingOrdineWithDetails();
        OrdineDTO testata = new OrdineDTO();
        testata.setAnno(ordine.getAnno());
        testata.setSerie(ordine.getSerie());
        testata.setProgressivo(ordine.getProgressivo());
        testata.setSottoConto(ordine.getContoCliente());
        when(ordineService.findById(ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo()))
                .thenReturn(testata);
        when(residuoService.calcolaResiduiMap(anyList())).thenAnswer(invocation -> {
            List<OrdineDettaglioDto> righe = invocation.getArgument(0);
            Map<Integer, ResiduoDto> residui = new HashMap<>();
            if (!righe.isEmpty()) {
                ResiduoDto residuo = new ResiduoDto();
                residuo.setResiduo(3D);
                residui.put(righe.getFirst().getProgrGenerale(), residuo);
            }
            return residui;
        });
        FiltroArticoli filtro = new FiltroArticoli();
        filtro.setAnno(ordine.getAnno());
        filtro.setSerie(ordine.getSerie());
        filtro.setProgressivo(ordine.getProgressivo());

        ResponseOrdineDettaglio result = service.findById(filtro);

        assertEquals(ordine.getAnno(), result.getAnno());
        assertEquals(ordine.getProgressivo(), result.getProgressivo());
        assertNotNull(result.getArticoli());
    }

    @Test
    @TestTransaction
    void recuperaArticoliPerReportEBolla() {
        Ordine ordine = existingOrdineWithDetails();
        when(residuoService.calcolaResiduiMap(anyList())).thenAnswer(invocation -> {
            List<OrdineDettaglioDto> righe = invocation.getArgument(0);
            Map<Integer, ResiduoDto> residui = new HashMap<>();
            for (OrdineDettaglioDto riga : righe) {
                ResiduoDto residuo = new ResiduoDto();
                residuo.setResiduo(7D);
                residui.put(riga.getProgrGenerale(), residuo);
            }
            return residui;
        });

        assertNotNull(service.findForReport(ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo()));
        assertNotNull(service.getArticoli("Y", ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo()));
        assertNotNull(service.getArticoli("N", ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo()));
        List<OrdineDettaglioDto> tutti = service.getArticoli(
                null, ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo());
        assertTrue(tutti.stream().allMatch(r -> r.getQtaDaConsegnare() == 7D));
        assertNotNull(service.getArticoliRiservati(ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo()));
    }

    @Test
    @TestTransaction
    void calcolaIlResiduoDegliArticoliRiservati() {
        OrdineDettaglio dettaglio = OrdineDettaglio.find("tipoRigo <> 'AC' and progrGenerale is not null").firstResult();
        if (dettaglio == null) {
            throw new AssertionError("Il database di sviluppo non contiene righe ordine");
        }
        GoOrdineDettaglio stato = GoOrdineDettaglio.find("progrGenerale", dettaglio.getProgrGenerale()).firstResult();
        if (stato == null) {
            stato = new GoOrdineDettaglio();
            stato.setProgrGenerale(dettaglio.getProgrGenerale());
            stato.setAnno(dettaglio.getAnno());
            stato.setSerie(dettaglio.getSerie());
            stato.setProgressivo(dettaglio.getProgressivo());
            stato.setRigo(dettaglio.getRigo());
        }
        stato.setFlagRiservato(true);
        stato.setFlagConsegnato(false);
        stato.persist();

        ResiduoDto residuo = new ResiduoDto();
        residuo.setResiduo(2D);
        when(residuoService.calcolaResiduiMap(anyList()))
                .thenReturn(Map.of(dettaglio.getProgrGenerale(), residuo));

        List<OrdineDettaglioDto> result = service.getArticoliRiservati(
                dettaglio.getAnno(), dettaglio.getSerie(), dettaglio.getProgressivo());

        assertTrue(result.stream().anyMatch(r -> r.getProgrGenerale().equals(dettaglio.getProgrGenerale())
                && r.getQtaDaConsegnare() == 2D));
    }

    @Test
    @TestTransaction
    void verificaLaPresenzaDiArticoliPronti() {
        Ordine ordine = existingOrdineWithDetails();
        boolean result = service.findNoProntaConsegna(
                ordine.getAnno(), ordine.getSerie(), ordine.getProgressivo());
        assertNotNull(result);
    }

    @Test
    void ignoraListeVuoteInSalvataggio() {
        assertNull(service.save(null, "utente-test", false));
        assertNull(service.save(List.of(), "utente-test", true));
    }

    @Test
    void delegaLAggiornamentoDegliArticoliBollati() {
        List<OrdineDettaglioDto> righe = List.of(new OrdineDettaglioDto());
        assertTrue(service.updateArticoliBolle(righe));

        doThrow(new RuntimeException("errore test")).when(fatturaService).aggiornaStatoOrdine(righe);
        assertFalse(service.updateArticoliBolle(righe));
    }

    @Test
    void validaGliArticoliPrimaDellaCodifica() {
        OrdineDettaglioDto senzaCodice = new OrdineDettaglioDto();
        senzaCodice.setFDescrArticolo("Articolo senza codice");
        OrdineDettaglioDto senzaFornitore = new OrdineDettaglioDto();
        senzaFornitore.setCodArtFornitore("COD-" + UUID.randomUUID());
        senzaFornitore.setFDescrArticolo("Descrizione senza delimitatore");
        OrdineDettaglioDto fornitoreMancante = new OrdineDettaglioDto();
        fornitoreMancante.setCodArtFornitore("COD-" + UUID.randomUUID());
        fornitoreMancante.setFDescrArticolo("Articolo *FORNITORE-INESISTENTE-TEST*");

        CodificaArticoliDto result = service.codificaArticoli(
                List.of(senzaCodice, senzaFornitore, fornitoreMancante), "utente-test");

        assertEquals(3, result.getErrors().size());
        assertTrue(result.getShowTCA());
    }

    @Test
    void segnalaUnFornitoreDelimitatoInModoErrato() {
        OrdineDettaglioDto dto = new OrdineDettaglioDto();
        dto.setCodArtFornitore("COD-" + UUID.randomUUID());
        dto.setFDescrArticolo("Articolo **");

        CodificaArticoliDto result = service.codificaArticoli(List.of(dto), "utente-test");

        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().getFirst().contains("non codificato correttamente"));
    }

    @Test
    void gestisceIlCodiceSpecialeMepo() {
        OrdineDettaglioDto dto = new OrdineDettaglioDto();
        dto.setAnno(1800);
        dto.setSerie("TST");
        dto.setProgressivo(Integer.MAX_VALUE);
        dto.setRigo(1);
        dto.setCodArtFornitore(" MEPO ");
        dto.setFDescrArticolo("Articolo MEPA");

        CodificaArticoliDto result = service.codificaArticoli(List.of(dto), "utente-test");

        assertTrue(result.getErrors().isEmpty());
        assertFalse(result.getShowTCA());
    }

    @Test
    void codificaUnNuovoArticoloECollegaIlFornitore() {
        OrdineDettaglio[] ordineDettaglio = new OrdineDettaglio[1];
        String[] valoriOriginali = new String[3];
        String[] codiceClasse = new String[1];
        String nomeFornitore = "FORNITORE-TEST-" + UUID.randomUUID().toString().substring(0, 8);
        String codiceEsterno = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        QuarkusTransaction.requiringNew().run(() -> {
            ordineDettaglio[0] = OrdineDettaglio.find(
                    "tipoRigo <> 'AC' and progrGenerale is not null").firstResult();
            if (ordineDettaglio[0] == null) {
                throw new AssertionError("Il database di sviluppo non contiene righe ordine codificabili");
            }
            valoriOriginali[0] = ordineDettaglio[0].getFArticolo();
            valoriOriginali[1] = ordineDettaglio[0].getCodArtFornitore();
            valoriOriginali[2] = ordineDettaglio[0].getFDescrArticolo();
            codiceClasse[0] = codiceClasseLibero();
            ArticoloClasseFornitore classe = new ArticoloClasseFornitore();
            classe.setCodice(codiceClasse[0]);
            classe.setDescrizione("Classe test");
            classe.setDescrUser(nomeFornitore);
            classe.setDescrUser2("999999");
            classe.persist();
        });
        String codiceArticolo = codiceClasse[0] + "00" + codiceEsterno;
        OrdineDettaglioDto dto = dettaglioDto(ordineDettaglio[0]);
        dto.setCodArtFornitore(codiceEsterno);
        dto.setFDescrArticolo("Nuovo articolo *" + nomeFornitore + "*");
        dto.setFUnitaMisura("PZ");

        try {
            CodificaArticoliDto result = service.codificaArticoli(List.of(dto), "utente-test");

            assertTrue(result.getErrors().isEmpty());
            assertFalse(result.getShowTCA());
            QuarkusTransaction.requiringNew().run(() -> {
                Articolo articolo = Articolo.findById(codiceArticolo);
                assertNotNull(articolo);
                assertEquals("Nuovo articolo " + nomeFornitore, articolo.getDescrArticolo());
                assertEquals(codiceEsterno, articolo.getDescrArtSuppl());
                assertEquals(codiceClasse[0], articolo.getClasseA1());
                assertNotNull(FornitoreArticolo.findById(
                        new FornitoreArticoloId(codiceArticolo, 2351, "999999")));
                OrdineDettaglio aggiornato = OrdineDettaglio.getById(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), dto.getRigo());
                assertEquals(codiceArticolo, aggiornato.getFArticolo());
                assertEquals(codiceEsterno, aggiornato.getCodArtFornitore());
            });

            QuarkusTransaction.requiringNew().run(() -> {
                Articolo articolo = Articolo.findById(codiceArticolo);
                articolo.setFlTrattato("N");
            });
            CodificaArticoliDto giaCodificato = service.codificaArticoli(List.of(dto), "utente-test");
            assertEquals(1, giaCodificato.getErrors().size());
            assertTrue(giaCodificato.getErrors().getFirst().contains("già codificato come"));
            QuarkusTransaction.requiringNew().run(() -> assertEquals(
                    "S", ((Articolo) Articolo.findById(codiceArticolo)).getFlTrattato()));
        } finally {
            QuarkusTransaction.requiringNew().run(() -> {
                OrdineDettaglio aggiornato = OrdineDettaglio.getById(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), dto.getRigo());
                aggiornato.setFArticolo(valoriOriginali[0]);
                aggiornato.setCodArtFornitore(valoriOriginali[1]);
                aggiornato.setFDescrArticolo(valoriOriginali[2]);
                FornitoreArticolo.deleteById(
                        new FornitoreArticoloId(codiceArticolo, 2351, "999999"));
                Articolo.deleteById(codiceArticolo);
                ArticoloClasseFornitore.deleteById(codiceClasse[0]);
            });
        }
    }

    @Test
    void codificaUnArticoloConCodiceFornitoreLungoGeneraUnProgressivoValido() {
        OrdineDettaglio[] ordineDettaglio = new OrdineDettaglio[1];
        String[] valoriOriginali = new String[3];
        String[] codiceClasse = new String[1];
        String nomeFornitore = "FORNITORE-LUNGO-TEST-" + UUID.randomUUID().toString().substring(0, 8);
        String codiceEsterno = UUID.randomUUID().toString().replace("-", "").substring(0, 13).toUpperCase();
        QuarkusTransaction.requiringNew().run(() -> {
            ordineDettaglio[0] = OrdineDettaglio.find(
                    "tipoRigo <> 'AC' and progrGenerale is not null").firstResult();
            if (ordineDettaglio[0] == null) {
                throw new AssertionError("Il database di sviluppo non contiene righe ordine codificabili");
            }
            valoriOriginali[0] = ordineDettaglio[0].getFArticolo();
            valoriOriginali[1] = ordineDettaglio[0].getCodArtFornitore();
            valoriOriginali[2] = ordineDettaglio[0].getFDescrArticolo();
            codiceClasse[0] = codiceClasseLibero();
            ArticoloClasseFornitore classe = new ArticoloClasseFornitore();
            classe.setCodice(codiceClasse[0]);
            classe.setDescrizione("Classe test codice lungo");
            classe.setDescrUser(nomeFornitore);
            classe.setDescrUser2("999999");
            classe.persist();
        });
        String codiceArticolo = codiceClasse[0] + "0000000001";
        OrdineDettaglioDto dto = dettaglioDto(ordineDettaglio[0]);
        dto.setCodArtFornitore(codiceEsterno);
        dto.setFDescrArticolo("Nuovo articolo codice lungo *" + nomeFornitore + "*");
        dto.setFUnitaMisura("PZ");

        try {
            CodificaArticoliDto result = service.codificaArticoli(List.of(dto), "utente-test");

            assertTrue(result.getErrors().isEmpty());
            QuarkusTransaction.requiringNew().run(() -> {
                Articolo articolo = Articolo.findById(codiceArticolo);
                assertNotNull(articolo);
                assertEquals(codiceEsterno, articolo.getDescrArtSuppl());
                assertEquals(codiceClasse[0], articolo.getClasseA1());
                assertNotNull(FornitoreArticolo.findById(
                        new FornitoreArticoloId(codiceArticolo, 2351, "999999")));
            });
        } finally {
            QuarkusTransaction.requiringNew().run(() -> {
                OrdineDettaglio aggiornato = OrdineDettaglio.getById(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), dto.getRigo());
                aggiornato.setFArticolo(valoriOriginali[0]);
                aggiornato.setCodArtFornitore(valoriOriginali[1]);
                aggiornato.setFDescrArticolo(valoriOriginali[2]);
                FornitoreArticolo.deleteById(
                        new FornitoreArticoloId(codiceArticolo, 2351, "999999"));
                Articolo.deleteById(codiceArticolo);
                ArticoloClasseFornitore.deleteById(codiceClasse[0]);
            });
        }
    }

    @Test
    void codificaUnArticoloConProgressivoEsistenteUsaIlSuccessivo() {
        OrdineDettaglio[] ordineDettaglio = new OrdineDettaglio[1];
        String[] valoriOriginali = new String[3];
        String[] datiClasse = new String[4];
        String[] codiceArticolo = new String[1];
        String codiceEsterno = UUID.randomUUID().toString().replace("-", "").substring(0, 13).toUpperCase();
        QuarkusTransaction.requiringNew().run(() -> {
            ordineDettaglio[0] = OrdineDettaglio.find(
                    "tipoRigo <> 'AC' and progrGenerale is not null").firstResult();
            if (ordineDettaglio[0] == null) {
                throw new AssertionError("Il database di sviluppo non contiene righe ordine codificabili");
            }
            valoriOriginali[0] = ordineDettaglio[0].getFArticolo();
            valoriOriginali[1] = ordineDettaglio[0].getCodArtFornitore();
            valoriOriginali[2] = ordineDettaglio[0].getFDescrArticolo();
            Object[] classe = (Object[]) ArticoloClasseFornitore.getEntityManager().createNativeQuery(
                            "SELECT TOP 1 CODICE, DESCRUSER, DESCRUSER2, DESCRUSER3 FROM TCA1 " +
                            "WHERE NULLIF(LTRIM(RTRIM(DESCRUSER2)), '') IS NOT NULL " +
                            "AND NULLIF(LTRIM(RTRIM(DESCRUSER3)), '') IS NOT NULL " +
                            "AND EXISTS (SELECT 1 FROM ARTICOLI_TAB a " +
                            "WHERE ISNUMERIC(a.ARTICOLO) = 1 " +
                            "AND a.ARTICOLO LIKE LTRIM(RTRIM(TCA1.DESCRUSER3)) + '%') " +
                            "ORDER BY CODICE")
                    .getResultList().stream().findFirst().orElse(null);
            if (classe == null) {
                throw new AssertionError("Il database non contiene classi con progressivi esistenti");
            }
            for (int i = 0; i < classe.length; i++) {
                datiClasse[i] = String.valueOf(classe[i]);
            }
            Object massimo = Articolo.getEntityManager().createNativeQuery(
                            "SELECT ISNULL(MAX(ARTICOLO), '1') FROM ARTICOLI_TAB " +
                            "WHERE ISNUMERIC(ARTICOLO) = 1 AND ARTICOLO LIKE :prefisso")
                    .setParameter("prefisso", datiClasse[3].trim() + "%")
                    .getSingleResult();
            codiceArticolo[0] = String.valueOf(Long.parseLong(String.valueOf(massimo)) + 1);
        });
        OrdineDettaglioDto dto = dettaglioDto(ordineDettaglio[0]);
        dto.setCodArtFornitore(codiceEsterno);
        dto.setFDescrArticolo("Nuovo articolo progressivo *" + datiClasse[1] + "*");
        dto.setFUnitaMisura("PZ");

        try {
            CodificaArticoliDto result = service.codificaArticoli(List.of(dto), "utente-test");

            assertTrue(result.getErrors().isEmpty());
            QuarkusTransaction.requiringNew().run(() -> {
                Articolo articolo = Articolo.findById(codiceArticolo[0]);
                assertNotNull(articolo);
                assertEquals(codiceEsterno, articolo.getDescrArtSuppl());
                assertEquals(datiClasse[3].trim(), articolo.getClasseA1());
            });
        } finally {
            QuarkusTransaction.requiringNew().run(() -> {
                OrdineDettaglio aggiornato = OrdineDettaglio.getById(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), dto.getRigo());
                aggiornato.setFArticolo(valoriOriginali[0]);
                aggiornato.setCodArtFornitore(valoriOriginali[1]);
                aggiornato.setFDescrArticolo(valoriOriginali[2]);
                FornitoreArticolo.deleteById(
                        new FornitoreArticoloId(codiceArticolo[0], 2351, datiClasse[2]));
                Articolo.deleteById(codiceArticolo[0]);
            });
        }
    }

    @Test
    void riconosceUnCodicePresenteNellaDescrizione() {
        OrdineDettaglio[] ordineDettaglio = new OrdineDettaglio[1];
        String[] valoriOriginali = new String[3];
        String[] articoloEsistente = new String[2];
        QuarkusTransaction.requiringNew().run(() -> {
            ordineDettaglio[0] = OrdineDettaglio.find(
                    "tipoRigo <> 'AC' and progrGenerale is not null").firstResult();
            if (ordineDettaglio[0] == null) {
                throw new AssertionError("Il database di sviluppo non contiene righe ordine codificabili");
            }
            valoriOriginali[0] = ordineDettaglio[0].getFArticolo();
            valoriOriginali[1] = ordineDettaglio[0].getCodArtFornitore();
            valoriOriginali[2] = ordineDettaglio[0].getFDescrArticolo();
            Articolo articolo = Articolo.find(
                    "(descrArtSuppl = :codArt OR descrArticolo like :codArtLike) " +
                            "AND articolo NOT IN ('*PZ', '*ML','*KG')",
                    io.quarkus.panache.common.Parameters.with("codArt", "GRONDA")
                            .and("codArtLike", "%GRONDA%"))
                    .firstResult();
            if (articolo == null || "GRONDA".equalsIgnoreCase(articolo.getDescrArtSuppl())) {
                throw new AssertionError("Il database di sviluppo non contiene un caso LIKE reale");
            }
            articoloEsistente[0] = articolo.getArticolo();
            articoloEsistente[1] = articolo.getFlTrattato();
        });
        OrdineDettaglioDto dto = dettaglioDto(ordineDettaglio[0]);
        dto.setCodArtFornitore("GRONDA");
        dto.setFDescrArticolo("Articolo esistente nella descrizione");

        try {
            CodificaArticoliDto result = service.codificaArticoli(List.of(dto), "utente-test");

            assertEquals(1, result.getErrors().size());
            assertTrue(result.getErrors().getFirst().contains("già codificato come"));
            QuarkusTransaction.requiringNew().run(() -> assertEquals(
                    articoloEsistente[0], OrdineDettaglio.getById(dto.getAnno(), dto.getSerie(),
                            dto.getProgressivo(), dto.getRigo()).getFArticolo()));
        } finally {
            QuarkusTransaction.requiringNew().run(() -> {
                OrdineDettaglio aggiornato = OrdineDettaglio.getById(dto.getAnno(), dto.getSerie(),
                        dto.getProgressivo(), dto.getRigo());
                aggiornato.setFArticolo(valoriOriginali[0]);
                aggiornato.setCodArtFornitore(valoriOriginali[1]);
                aggiornato.setFDescrArticolo(valoriOriginali[2]);
                Articolo articolo = Articolo.findById(articoloEsistente[0]);
                articolo.setFlTrattato(articoloEsistente[1]);
            });
        }
    }

    @Test
    @TestTransaction
    void rifiutaUnFornitoreArticoloDuplicato() {
        FornitoreArticolo existing = FornitoreArticolo.findAll().firstResult();
        if (existing == null) {
            throw new AssertionError("Il database di sviluppo non contiene associazioni articolo-fornitore");
        }
        PianoContiDto dto = new PianoContiDto();
        dto.setCodiceArticolo(existing.getFornitoreArticoloId().getArticolo());
        dto.setGruppoConto(existing.getFornitoreArticoloId().getGruppo());
        dto.setSottoConto(existing.getFornitoreArticoloId().getConto());

        assertFalse(service.addFornitore(dto, "utente-test"));
    }

    @Test
    @TestTransaction
    void aggiungeUnFornitoreArticoloNuovo() {
        Articolo articolo = Articolo.findAll().firstResult();
        PianoConti conto = PianoConti.findAll().stream()
                .map(PianoConti.class::cast)
                .filter(p -> FornitoreArticolo.count("fornitoreArticoloId.articolo = ?1 and " +
                                "fornitoreArticoloId.gruppo = ?2 and fornitoreArticoloId.conto = ?3",
                        articolo.getArticolo(), p.getGruppoConto(), p.getSottoConto()) == 0)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Nessun conto disponibile per il test"));
        PianoContiDto dto = new PianoContiDto();
        dto.setCodiceArticolo(articolo.getArticolo());
        dto.setGruppoConto(conto.getGruppoConto());
        dto.setSottoConto(conto.getSottoConto());

        assertTrue(service.addFornitore(dto, "utente-test"));
        assertNotNull(FornitoreArticolo.findById(new FornitoreArticoloId(
                articolo.getArticolo(), conto.getGruppoConto(), conto.getSottoConto())));
    }

    @Test
    @TestTransaction
    void salvaUnaRigaEsistenteSenzaModificheEconomiche() {
        OrdineDettaglio entity = OrdineDettaglio.find("tipoRigo <> 'AC' and progrGenerale is not null").firstResult();
        if (entity == null) {
            throw new AssertionError("Il database di sviluppo non contiene righe ordine aggiornabili");
        }
        GoOrdineDettaglio go = GoOrdineDettaglio.find("progrGenerale", entity.getProgrGenerale()).firstResult();
        OrdineDettaglioDto dto = new OrdineDettaglioDto();
        dto.setAnno(entity.getAnno());
        dto.setSerie(entity.getSerie());
        dto.setProgressivo(entity.getProgressivo());
        dto.setRigo(entity.getRigo());
        dto.setProgrGenerale(entity.getProgrGenerale());
        dto.setTipoRigo(entity.getTipoRigo());
        dto.setFDescrArticolo(entity.getFDescrArticolo());
        dto.setCodArtFornitore(entity.getCodArtFornitore());
        dto.setQuantita(entity.getQuantita());
        dto.setTono(entity.getTono());
        dto.setFlagRiservato(go != null && Boolean.TRUE.equals(go.getFlagRiservato()));
        dto.setFlagNonDisponibile(go != null && Boolean.TRUE.equals(go.getFlagNonDisponibile()));
        dto.setFlagOrdinato(go != null && Boolean.TRUE.equals(go.getFlagOrdinato()));
        dto.setFlagConsegnato(go != null && Boolean.TRUE.equals(go.getFlagConsegnato()));
        dto.setFlProntoConsegna(go != null && Boolean.TRUE.equals(go.getFlProntoConsegna()));
        dto.setQtaRiservata(go == null ? null : go.getQtaRiservata());
        ResiduoDto residuo = new ResiduoDto();
        residuo.setResiduo(entity.getQuantita());
        when(residuoService.calcolaResiduiMap(anyList()))
                .thenReturn(Map.of(entity.getProgrGenerale(), residuo));

        assertNull(service.save(List.of(dto), "utente-test", false));
    }

    @Test
    @TestTransaction
    void ricreaLoStatoApplicativoMancanteDellaRiga() {
        OrdineDettaglio entity = OrdineDettaglio.find("tipoRigo <> 'AC' and progrGenerale is not null " +
                "and exists (select 1 from GoOrdine g where g.anno = anno and g.serie = serie " +
                "and g.progressivo = progressivo)").firstResult();
        if (entity == null) {
            throw new AssertionError("Il database di sviluppo non contiene righe collegate a GO_ORDINE");
        }
        GoOrdineDettaglio.delete("progrGenerale", entity.getProgrGenerale());
        GoOrdineDettaglio.getEntityManager().flush();
        OrdineDettaglioDto dto = dettaglioDto(entity);
        dto.setFlagRiservato(Boolean.TRUE);
        ResiduoDto residuo = new ResiduoDto();
        residuo.setResiduo(entity.getQuantita());
        when(residuoService.calcolaResiduiMap(anyList()))
                .thenReturn(Map.of(entity.getProgrGenerale(), residuo));

        service.save(List.of(dto), "utente-test");

        GoOrdineDettaglio.getEntityManager().flush();
        GoOrdineDettaglio created = GoOrdineDettaglio.find(
                "progrGenerale", entity.getProgrGenerale()).singleResult();
        assertEquals(StatoOrdineEnum.DA_PROCESSARE.getDescrizione(), created.getStatus());
        assertTrue(created.getFlagRiservato());
    }

    @Test
    @TestTransaction
    void aggiornaUnaRigaEChiudeLOrdineDaProcessare() {
        OrdineDettaglio entity = OrdineDettaglio.find("tipoRigo <> 'AC' and progrGenerale is not null " +
                "and exists (select 1 from GoOrdine g where g.anno = anno and g.serie = serie " +
                "and g.progressivo = progressivo)").firstResult();
        if (entity == null) {
            throw new AssertionError("Il database di sviluppo non contiene righe collegate a GO_ORDINE");
        }
        GoOrdine goOrdine = GoOrdine.findByOrdineId(
                entity.getAnno(), entity.getSerie(), entity.getProgressivo());
        goOrdine.setStatus(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
        goOrdine.setLocked(Boolean.TRUE);
        goOrdine.setUserLock("utente-test");
        goOrdine.persist();

        OrdineDettaglioDto dto = dettaglioDto(entity);
        dto.setFDescrArticolo("Descrizione di prova volutamente superiore ai cinquanta caratteri consentiti");
        dto.setCodArtFornitore("COD-TEST-" + entity.getProgrGenerale());
        dto.setQuantita(entity.getQuantita() + 1D);
        dto.setTono("  TONO TEST  ");
        dto.setFlagRiservato(Boolean.FALSE);
        dto.setFlagOrdinato(Boolean.TRUE);
        dto.setFlagNonDisponibile(Boolean.TRUE);
        dto.setFlagConsegnato(Boolean.FALSE);
        dto.setFlProntoConsegna(Boolean.TRUE);
        dto.setQtaRiservata(1D);
        dto.setQtaProntoConsegna(1D);
        dto.setQtaConsegnatoSenzaBolla(1D);
        dto.setNumDoc("DOC-TEST");
        ResiduoDto residuo = new ResiduoDto();
        residuo.setResiduo(entity.getQuantita());
        when(residuoService.calcolaResiduiMap(anyList()))
                .thenReturn(Map.of(entity.getProgrGenerale(), residuo));

        String stato = service.save(List.of(dto), "utente-test", true);
        GoOrdine.getEntityManager().refresh(goOrdine);

        assertEquals(StatoOrdineEnum.DA_ORDINARE.getDescrizione(), stato);
        assertEquals(50, entity.getFDescrArticolo().length());
        assertEquals(dto.getCodArtFornitore(), entity.getCodArtFornitore());
        assertEquals(dto.getQuantita(), entity.getQuantita());
        assertEquals("TONO TEST", entity.getTono());
        assertFalse(goOrdine.getLocked());
        assertNull(goOrdine.getUserLock());
        assertTrue(goOrdine.getWarnNoBolla());
        assertTrue(goOrdine.getHasProntoConsegna());
        assertTrue(goOrdine.getHasCarico());
    }

    @Test
    @TestTransaction
    void esegueLeSincronizzazioniInterneDegliArticoli() {
        assertDoesNotThrow(service::syncHasBolla);
        assertDoesNotThrow(service::findCarichi);
        assertDoesNotThrow(service::sincronizzaCodiceArticolo);
        assertDoesNotThrow(service::resetGoOrdineDettaglio);
    }

    private Ordine existingOrdineWithDetails() {
        Ordine ordine = Ordine.find("FROM Ordine o WHERE EXISTS (SELECT 1 FROM OrdineDettaglio d " +
                "WHERE d.anno = o.anno AND d.serie = o.serie AND d.progressivo = o.progressivo)").firstResult();
        if (ordine == null) {
            throw new AssertionError("Il database di sviluppo non contiene ordini con dettagli");
        }
        return ordine;
    }

    private String codiceClasseLibero() {
        String codice;
        do {
            codice = UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        } while (ArticoloClasseFornitore.findById(codice) != null);
        return codice;
    }

    private OrdineDettaglioDto dettaglioDto(OrdineDettaglio entity) {
        OrdineDettaglioDto dto = new OrdineDettaglioDto();
        dto.setAnno(entity.getAnno());
        dto.setSerie(entity.getSerie());
        dto.setProgressivo(entity.getProgressivo());
        dto.setRigo(entity.getRigo());
        dto.setProgrGenerale(entity.getProgrGenerale());
        dto.setTipoRigo(entity.getTipoRigo());
        dto.setFDescrArticolo(entity.getFDescrArticolo());
        dto.setCodArtFornitore(entity.getCodArtFornitore());
        dto.setQuantita(entity.getQuantita());
        dto.setTono(entity.getTono());
        return dto;
    }
}
