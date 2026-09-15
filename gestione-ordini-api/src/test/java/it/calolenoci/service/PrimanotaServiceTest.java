package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import it.calolenoci.dto.FiltroPrimanota;
import it.calolenoci.dto.PrimanotaDto;
import it.calolenoci.dto.VenditaCespiteDto;
import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.CategoriaCespite;
import it.calolenoci.entity.Cespite;
import it.calolenoci.entity.Primanota;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@QuarkusTest
class PrimanotaServiceTest {

    @Inject
    PrimanotaService service;

    @Inject
    EntityManager entityManager;

    @InjectMock
    AmmortamentoCespiteService ammortamentoCespiteService;

    @Test
    @TestTransaction
    void recuperaERiproduceIRighiDelProtocollo() {
        Primanota entity = existingPrimanota();
        FiltroPrimanota filtro = new FiltroPrimanota();
        filtro.setAnno(entity.getAnno());
        filtro.setGiornale(entity.getGiornale());
        filtro.setProtocollo(entity.getProtocollo());

        List<PrimanotaDto> result = service.getById(filtro);

        PrimanotaDto dto = result.stream()
                .filter(item -> entity.getProgrprimanota().equals(item.getProgrprimanota()))
                .findFirst()
                .orElseThrow();
        assertEquals(entity.getDatamovimento(), dto.getDatamovimento());
        assertEquals(entity.getNumerodocumento(), dto.getNumerodocumento());
        assertEquals(entity.getCausale(), dto.getCausale());
        assertEquals(entity.getGruppoconto(), dto.getGruppoconto());
        assertEquals(entity.getSottoconto(), dto.getSottoconto());
        assertEquals(entity.getDescrsuppl(), dto.getDescrsuppl());
        assertEquals(entity.getImporto(), dto.getImporto());
        assertEquals(entity.getProgrgenerale(), dto.getProgrgenerale());
        assertEquals(entity.getProtocollo(), dto.getProtocollo());
        assertEquals(entity.getAnno(), dto.getAnno());
        assertEquals(entity.getGiornale(), dto.getGiornale());
    }

    @Test
    @TestTransaction
    void aggiungeUnRigoAlProtocolloEsistente() {
        Primanota template = existingPrimanota();
        Integer nextRigo = Primanota.find(
                "select MAX(progrprimanota) from Primanota where anno = :anno AND giornale = :giornale AND protocollo = :protocollo",
                Parameters.with("anno", template.getAnno()).and("giornale", template.getGiornale())
                        .and("protocollo", template.getProtocollo())).project(Integer.class).firstResult() + 1;
        Integer currentMaxGenerale = Primanota.find("select MAX(progrgenerale) from Primanota")
                .project(Integer.class).firstResult();
        PrimanotaDto dto = newDto(template, Integer.MAX_VALUE, nextRigo, template.getGiornale());

        service.salva(dto);

        Primanota created = Primanota.find(
                "anno = :anno AND giornale = :giornale AND protocollo = :protocollo AND progrprimanota = :rigo",
                Parameters.with("anno", template.getAnno()).and("giornale", template.getGiornale())
                        .and("protocollo", template.getProtocollo()).and("rigo", nextRigo)).firstResult();
        assertNotNull(created);
        assertEquals(currentMaxGenerale + 1, created.getProgrgenerale());
        assertEquals(9999, created.getGruppoconto());
        assertEquals("000001", created.getSottoconto());
        assertEquals("Rigo di test", created.getDescrsuppl());
        assertEquals(123.45, created.getImporto());
        assertEquals(template.getDatamovimento(), created.getDatamovimento());
    }

    @Test
    @TestTransaction
    void aggiornaIlRigoEAttivaLaCreazioneCespitePerIGiornaliPrevisti() {
        Primanota entity = existingPrimanota();
        PrimanotaDto dto = newDto(entity, entity.getProgrgenerale(), entity.getProgrprimanota(), "A");

        service.salva(dto);

        entityManager.clear();
        Primanota updated = Primanota.find("progrgenerale", entity.getProgrgenerale()).firstResult();
        assertEquals(9999, updated.getGruppoconto());
        assertEquals("000001", updated.getSottoconto());
        assertEquals("Rigo di test", updated.getDescrsuppl());
        assertEquals(123.45, updated.getImporto());
        verify(ammortamentoCespiteService).createCespite(dto);
    }

    @Test
    @TestTransaction
    void nonAttivaLaCreazioneCespitePerGliAltriGiornali() {
        Primanota entity = existingPrimanota();
        service.salva(newDto(entity, entity.getProgrgenerale(), entity.getProgrprimanota(), "X"));

        verifyNoInteractions(ammortamentoCespiteService);
    }

    @Test
    void contabilizzaSenzaMovimentiUnAnnoPrivoDiCespiti() throws Exception {
        LocalDate data = LocalDate.of(1800, 12, 31);
        assertEquals(0L, Primanota.count("datamovimento = ?1 and causale = ?2", data, "GVM"));

        service.contabilizzaAmm(data);

        assertEquals(0L, Primanota.count("datamovimento = ?1 and causale = ?2", data, "GVM"));
    }

    @Test
    void contabilizzaLaQuotaEIlFondoDelCespite() throws Exception {
        LocalDate data = LocalDate.of(1800, 12, 31);
        CategoriaCespite[] categoria = new CategoriaCespite[1];
        Cespite[] cespite = new Cespite[1];
        QuarkusTransaction.requiringNew().run(() -> {
            categoria[0] = createCategoria();
            cespite[0] = new Cespite();
            cespite[0].setTipoCespite(categoria[0].getTipoCespite());
            cespite[0].setProgressivo1(1);
            cespite[0].setProgressivo2(1);
            cespite[0].setCespite("Cespite contabile di test");
            cespite[0].setDataAcq(LocalDate.of(1800, 1, 1));
            cespite[0].setDataInizioCalcoloAmm(LocalDate.of(1800, 1, 1));
            cespite[0].setImporto(1_000D);
            cespite[0].setAttivo(Boolean.TRUE);
            cespite[0].setFlPrimoAnno(Boolean.FALSE);
            cespite[0].persist();
            AmmortamentoCespite quota = new AmmortamentoCespite();
            quota.setIdAmmortamento(cespite[0].getId());
            quota.setAnno(1800);
            quota.setDataAmm(data);
            quota.setQuota(200D);
            quota.setFondo(200D);
            quota.setResiduo(800D);
            quota.persist();
        });

        try {
            service.contabilizzaAmm(data);

            QuarkusTransaction.requiringNew().run(() -> {
                List<Primanota> scritture = Primanota.find(
                        "datamovimento = ?1 and causale = ?2 and numerodocumento = ?3",
                        data, "GVM", categoria[0].getTipoCespite() + " - 1.1").list();
                assertEquals(2, scritture.size());
                assertEquals(200D, scritture.get(0).getImporto(), 0.01);
                assertEquals(-200D, scritture.get(1).getImporto(), 0.01);
                assertEquals(categoria[0].getAmmGruppo(), scritture.get(0).getGruppoconto());
                assertEquals(categoria[0].getFondoGruppo(), scritture.get(1).getGruppoconto());
            });
        } finally {
            QuarkusTransaction.requiringNew().run(() -> {
                Primanota.delete("datamovimento = ?1 and causale = ?2 and numerodocumento = ?3",
                        data, "GVM", categoria[0].getTipoCespite() + " - 1.1");
                AmmortamentoCespite.delete("idAmmortamento", cespite[0].getId());
                Cespite.deleteById(cespite[0].getId());
                CategoriaCespite.delete("tipoCespite", categoria[0].getTipoCespite());
            });
        }
    }

    @Test
    @TestTransaction
    void registraLaVenditaELeRelativeScrittureContabili() {
        LocalDate dataVendita = LocalDate.of(1801, 6, 30);
        CategoriaCespite categoria = createCategoria();
        Cespite cespite = new Cespite();
        cespite.setTipoCespite(categoria.getTipoCespite());
        cespite.setProgressivo1(2);
        cespite.setProgressivo2(3);
        cespite.setCespite("Cespite venduto di test");
        cespite.setDataAcq(LocalDate.of(1800, 1, 1));
        cespite.setDataInizioCalcoloAmm(LocalDate.of(1800, 1, 1));
        cespite.setImporto(1_000D);
        cespite.setAttivo(Boolean.TRUE);
        cespite.setFlPrimoAnno(Boolean.FALSE);
        cespite.persist();
        Integer progrGenerale = Primanota.find("select MAX(progrgenerale) from Primanota")
                .project(Integer.class).firstResult();
        Primanota vendita = new Primanota();
        vendita.setAnno(1801);
        vendita.setGiornale("V");
        vendita.setProtocollo(Integer.MAX_VALUE);
        vendita.setProgrprimanota(1);
        vendita.setProgrgenerale(progrGenerale + 1);
        vendita.setDatamovimento(dataVendita);
        vendita.setNumerodocumento("FT-TEST");
        vendita.setCausale("VEN");
        vendita.setGruppoconto(categoria.getCostoGruppo());
        vendita.setSottoconto(categoria.getCostoConto());
        vendita.setDescrsuppl("Cliente test");
        vendita.setImporto(900D);
        vendita.persist();
        AmmortamentoCespite quota = new AmmortamentoCespite();
        quota.setAnno(1801);
        quota.setQuota(200D);
        quota.setFondo(300D);
        quota.setResiduo(700D);
        when(ammortamentoCespiteService.calcoloSingoloCespite(any(Cespite.class), eq(dataVendita)))
                .thenReturn(List.of(quota));

        service.registraVendita(new VenditaCespiteDto(
                cespite.getId(), vendita.getProtocollo(), vendita.getGiornale(), vendita.getAnno()));

        assertEquals(dataVendita, cespite.getDataVendita());
        assertEquals("FT-TEST", cespite.getNumDocVendita());
        assertEquals("Cliente test", cespite.getIntestatarioVendita());
        assertEquals(900D, cespite.getImportoVendita(), 0.01);
        List<Primanota> scritture = Primanota.find(
                "anno = ?1 and giornale = '' and causale = ?2 and numerodocumento = ?3",
                1801, "GVV", categoria.getTipoCespite() + " - 2.3").list();
        assertEquals(6, scritture.size());
        assertEquals(200D, scritture.get(0).getImporto(), 0.01);
        assertEquals(-300D, scritture.get(1).getImporto(), 0.01);

        vendita.setImporto(100D);
        entityManager.flush();
        Primanota.delete("anno = ?1 and giornale = '' and causale = ?2 and numerodocumento = ?3",
                1801, "GVV", categoria.getTipoCespite() + " - 2.3");
        entityManager.clear();
        AmmortamentoCespite quotaPrecedente = new AmmortamentoCespite();
        quotaPrecedente.setAnno(1800);
        quotaPrecedente.setDescrizione("Ammortamento ordinario");
        quotaPrecedente.setQuota(200D);
        quotaPrecedente.setFondo(300D);
        quotaPrecedente.setResiduo(700D);
        when(ammortamentoCespiteService.calcoloSingoloCespite(any(Cespite.class), eq(dataVendita)))
                .thenReturn(List.of(quotaPrecedente));

        service.registraVendita(new VenditaCespiteDto(
                cespite.getId(), vendita.getProtocollo(), vendita.getGiornale(), vendita.getAnno()));

        List<Primanota> scrittureMinus = Primanota.find(
                "anno = ?1 and giornale = '' and causale = ?2 and numerodocumento = ?3",
                1801, "GVV", categoria.getTipoCespite() + " - 2.3").list();
        assertEquals(6, scrittureMinus.size());
        assertTrue(scrittureMinus.stream().anyMatch(p ->
                categoria.getMinusGruppo().equals(p.getGruppoconto()) && p.getImporto() == -600D));
    }

    @Test
    @TestTransaction
    void rifiutaLaVenditaSenzaUnContoCespiteConfigurato() {
        VenditaCespiteDto vendita = new VenditaCespiteDto(
                "cespite-inesistente", Integer.MAX_VALUE, "Z", 1800);

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> service.registraVendita(vendita));

        assertEquals("Nessuna riga di prima nota trovata con conto cespite", error.getMessage());
    }

    private PrimanotaDto newDto(Primanota entity, Integer progrGenerale, Integer rigo, String giornale) {
        return new PrimanotaDto(entity.getDatamovimento(), entity.getNumerodocumento(), entity.getCausale(),
                9999, "000001", "Rigo di test", 123.45, progrGenerale, entity.getProtocollo(),
                entity.getAnno(), giornale, rigo);
    }

    private Primanota existingPrimanota() {
        Primanota entity = Primanota.find("progrgenerale is not null", Sort.descending("progrgenerale")).firstResult();
        if (entity == null) {
            throw new AssertionError("Il database di sviluppo non contiene righe di prima nota");
        }
        return entity;
    }

    private CategoriaCespite createCategoria() {
        String tipo;
        do {
            tipo = UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        } while (CategoriaCespite.count("tipoCespite", tipo) > 0);
        CategoriaCespite categoria = new CategoriaCespite();
        categoria.setTipoCespite(tipo);
        categoria.setCodice("TEST");
        categoria.setDescrizione("Categoria contabile di test");
        categoria.setPercAmmortamento(20D);
        categoria.setCostoGruppo(9001);
        categoria.setCostoConto("000001");
        categoria.setAmmGruppo(9002);
        categoria.setAmmConto("000002");
        categoria.setFondoGruppo(9003);
        categoria.setFondoConto("000003");
        categoria.setPlusGruppo(9004);
        categoria.setPlusConto("000004");
        categoria.setMinusGruppo(9005);
        categoria.setMinusConto("000005");
        categoria.persist();
        return categoria;
    }
}
