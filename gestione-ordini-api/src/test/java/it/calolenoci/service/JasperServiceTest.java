package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.OrdineReportDto;
import it.calolenoci.dto.OrdineFornitoreDto;
import it.calolenoci.dto.CategoriaCespitiDto;
import it.calolenoci.dto.ListaCarichiDto;
import it.calolenoci.dto.RegistroCespiteReportDto;
import it.calolenoci.dto.RegistroCespitiDto;
import it.calolenoci.dto.RiordinoDto;
import it.calolenoci.entity.GoOrdVeicolo;
import it.calolenoci.entity.GoOrdVeicoloPK;
import it.calolenoci.mapper.OrdineClienteReportMapper;
import it.calolenoci.mapper.RegistroCespiteReportMapper;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class JasperServiceTest {

    @Inject
    EntityManager entityManager;

    @Test
    void converteOgniRigaDellOrdinePerIlReport() {
        JasperService service = new JasperService();
        service.mapper = mock(OrdineClienteReportMapper.class);
        OrdineDTO ordine = new OrdineDTO();
        OrdineDettaglioDto prima = new OrdineDettaglioDto();
        OrdineDettaglioDto seconda = new OrdineDettaglioDto();
        OrdineReportDto reportPrima = new OrdineReportDto();
        OrdineReportDto reportSeconda = new OrdineReportDto();
        when(service.mapper.fromEntityToDto(ordine, prima, "ordine.pdf", "firma"))
                .thenReturn(reportPrima);
        when(service.mapper.fromEntityToDto(ordine, seconda, "ordine.pdf", "firma"))
                .thenReturn(reportSeconda);

        List<OrdineReportDto> result = service.getOrdiniReport(
                ordine, List.of(prima, seconda), "ordine.pdf", "firma");

        assertEquals(List.of(reportPrima, reportSeconda), result);
    }

    @Test
    void nonGeneraReportSenzaDati() {
        JasperService service = new JasperService();
        service.service = mock(OrdineFornitoreService.class);
        when(service.service.findForReport(1900, "T", 1)).thenReturn(List.of());

        service.createReport(1900, "T", 1);

        verify(service.service).findForReport(1900, "T", 1);
        assertNull(service.createReport((RegistroCespitiDto) null));
        assertNull(service.createReport(new RegistroCespitiDto()));
    }

    @Test
    @TestTransaction
    void riordinaSoloLeConsegneEsistenti() {
        int progressivo = -1_000_000_000 - Math.floorMod(UUID.randomUUID().hashCode(), 500_000_000);
        GoOrdVeicolo consegna = new GoOrdVeicolo();
        consegna.setId(new GoOrdVeicoloPK(1903, "T", progressivo));
        consegna.setIdVeicolo(progressivo);
        consegna.setDataConsegna(LocalDate.of(1903, 1, 1));
        consegna.setOraConsegna('M');
        consegna.setOrdine(1L);
        consegna.setVenditore(false);
        consegna.persist();
        RiordinoDto presente = riordino(progressivo, 7L);
        RiordinoDto assente = riordino(progressivo - 1, 9L);

        new JasperService().riordinaConsegne(List.of(presente, assente));

        entityManager.flush();
        entityManager.clear();
        GoOrdVeicolo updated = GoOrdVeicolo.findById(new GoOrdVeicoloPK(1903, "T", progressivo));
        assertEquals(7L, updated.getOrdine());
    }

    @Test
    void generaIlReportOrdineClienteInUnaDirectoryTemporanea(@TempDir Path tempDir) throws Exception {
        JasperService service = new JasperService();
        service.pathReport = tempDir.toString() + "/";
        OrdineReportDto articolo = new OrdineReportDto();
        articolo.setValoreTotale(100D);
        articolo.setTIPORIGO(" ");
        Path compiledReport = Path.of("Invoice.jasper");
        byte[] originalReport = Files.readAllBytes(compiledReport);

        try {
            service.createReport(List.of(articolo), "CLI001", 1905, "T", 7);

            assertTrue(Files.exists(tempDir.resolve("1905/T/CLI001_1905_T_7.pdf")));
        } finally {
            Files.write(compiledReport, originalReport);
        }
    }

    @Test
    void generaLaListaCarichiInUnaDirectoryTemporanea(@TempDir Path tempDir) throws Exception {
        JasperService service = new JasperService();
        service.pathListaCarico = tempDir.toString() + "/";
        ListaCarichiDto carico = new ListaCarichiDto();
        carico.setPeso(125.5D);
        carico.setNumeroOrdine("ORD-TEST");

        service.createReport(List.of(carico), "lista-test.pdf");

        assertTrue(Files.exists(tempDir.resolve("lista-test.pdf")));
    }

    @Test
    void generaIlReportOrdineFornitore(@TempDir Path tempDir) throws Exception {
        JasperService service = new JasperService();
        service.service = mock(OrdineFornitoreService.class);
        service.tmpFolder = tempDir.toString() + "/";
        OrdineFornitoreDto riga = new OrdineFornitoreDto();
        riga.setTipoRigo(" ");
        riga.setValoreTotale(100D);
        when(service.service.findForReport(1906, "T", 8)).thenReturn(List.of(riga));

        Path compiledReport = Path.of("OAF.jasper");
        byte[] originalReport = Files.exists(compiledReport) ? Files.readAllBytes(compiledReport) : null;
        try {
            service.createReport(1906, "T", 8);
            assertTrue(Files.exists(tempDir.resolve("1906_T_8.pdf")));
        } finally {
            Files.deleteIfExists(tempDir.resolve("1906_T_8.pdf"));
            if (originalReport != null) {
                Files.write(compiledReport, originalReport);
            } else {
                Files.deleteIfExists(compiledReport);
            }
        }
    }

    @Test
    void segnalaErroreNelPercorsoTemporaneoDelReportOaf(@TempDir Path tempDir) throws Exception {
        JasperService service = new JasperService();
        service.service = mock(OrdineFornitoreService.class);
        service.tmpFolder = tempDir.resolve("cartella-inesistente").toString() + "/";
        OrdineFornitoreDto riga = new OrdineFornitoreDto();
        riga.setTipoRigo(" ");
        riga.setValoreTotale(10D);
        when(service.service.findForReport(1910, "T", 9)).thenReturn(List.of(riga));

        Path compiledReport = Path.of("OAF.jasper");
        byte[] originalReport = Files.exists(compiledReport) ? Files.readAllBytes(compiledReport) : null;
        try {
            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                    () -> service.createReport(1910, "T", 9));
        } finally {
            Files.deleteIfExists(Path.of("1910_T_9.pdf"));
            if (originalReport != null) {
                Files.write(compiledReport, originalReport);
            } else {
                Files.deleteIfExists(compiledReport);
            }
        }
    }

    @Test
    void generaIlRegistroCespiti(@TempDir Path tempDir) throws Exception {
        JasperService service = new JasperService();
        service.registroCespiteReportMapper = mock(RegistroCespiteReportMapper.class);
        RegistroCespitiDto registro = new RegistroCespitiDto();
        registro.setCespiteList(List.of(new CategoriaCespitiDto()));
        registro.setData(LocalDate.of(1906, 12, 31));
        when(service.registroCespiteReportMapper.buildRegistroCespiteReport(registro))
                .thenReturn(new RegistroCespiteReportDto());

        java.io.File result = service.createReport(registro);

        assertEquals("Registro_cespiti.pdf", result.getName());
        assertTrue(result.exists());
        Files.deleteIfExists(result.toPath());
    }

    private RiordinoDto riordino(int progressivo, long ordine) {
        RiordinoDto dto = new RiordinoDto();
        dto.setAnno(1903);
        dto.setSerie("T");
        dto.setProgressivo(progressivo);
        dto.setOrdine(ordine);
        return dto;
    }
}
