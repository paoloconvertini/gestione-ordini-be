package it.calolenoci.service;

import io.quarkus.logging.Log;
import it.calolenoci.dto.*;
import it.calolenoci.mapper.OrdineClienteReportMapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRSaver;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Singleton
public class JasperService {

    @ConfigProperty(name = "ordini.path")
    String pathReport;

    @Inject
    OrdineClienteReportMapper mapper;

    @Inject
    OrdineFornitoreService service;

    @ConfigProperty(name = "ordini.tmp")
    String tmpFolder;

    @Inject
    AmmortamentoCespiteService ammortamentoCespiteService;

    public List<OrdineReportDto> getOrdiniReport(OrdineDTO dto, List<OrdineDettaglioDto> articoli, String filename, String firmaVenditore) {
        List<OrdineReportDto> dtoList = new ArrayList<>();
        articoli.forEach(a -> dtoList.add(mapper.fromEntityToDto(dto, a, filename, firmaVenditore)));
        return dtoList;
    }

    public void createReport(List<OrdineReportDto> articoli, String sottoConto, Integer anno, String serie, Integer progressivo) throws JRException, IOException {

        String ordineId = sottoConto + "_" + anno + "_" + serie + "_" + progressivo;

        // 1. compile template ".jrxml" file
        JasperReport jasperReport = compileReport("Invoice.jrxml");

        // 2. parameters "empty"
        Map<String, Object> parameters = getParameters(articoli.stream().filter(a -> !"C".equals(a.getTIPORIGO())).mapToDouble(OrdineReportDto::getValoreTotale).sum());
        parameters.put("totaleNetto", parameters.get("totaleDocumento"));

        // 3. datasource "java object"
        JRDataSource dataSource = getDataSource(articoli);

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        String destFileName = ordineId + ".pdf";
        File folderDest = new File(pathReport + anno + "/" + serie);
        if (!folderDest.exists()) {
            if (!folderDest.mkdirs()) {
                Log.error("Errore creazione sotto cartelle report");
            }
        }
        File f = new File(destFileName);
        if (!f.exists()) {
            try {
                if (!f.createNewFile()) {
                    Log.info("File " + f.getName() + " already exists");
                }
            } catch (IOException ex) {
                Log.error("Errore creazione file e cartelle report", ex);
            }
        }
        JasperExportManager.exportReportToPdfFile(jasperPrint, f.getName());
        Files.move(f.getAbsoluteFile().toPath(), Path.of(folderDest + "/" + destFileName), StandardCopyOption.REPLACE_EXISTING);
    }

    public void createReport(Integer anno, String serie, Integer progressivo) {
        List<OrdineFornitoreDto> dtoList = service.findForReport(anno, serie, progressivo);
        if (dtoList != null && !dtoList.isEmpty()) {
            try {
                String ordineId = anno + "_" + serie + "_" + progressivo;

                // 1. compile template ".jrxml" file
                JasperReport jasperReport = compileReport("OAF.jrxml");

                // 2. parameters "empty"
                Map<String, Object> parameters = getParameters(dtoList.stream().filter(a -> StringUtils.isBlank(a.getTipoRigo())).mapToDouble(OrdineFornitoreDto::getValoreTotale).sum());

                // 3. datasource "java object"
                JRDataSource dataSource = getDataSource(dtoList);

                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
                String destFileName = ordineId + ".pdf";
                File f = new File(destFileName);
                JasperExportManager.exportReportToPdfFile(jasperPrint, f.getName());
                Files.move(f.getAbsoluteFile().toPath(), java.nio.file.Path.of(tmpFolder + f.getName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (JRException e) {
                Log.error("Errore nella creazione del report per l'ordine a fornitore " + anno + "/" + serie + "/" + progressivo, e);
                throw new RuntimeException(e);
            } catch (IOException e) {
                Log.error("Errore nello spostamento del report " + anno + "/" + serie + "/" + progressivo, e);
                throw new RuntimeException(e);
            }
        }
    }

    public File createReport(FiltroCespite filtroCespite) {
        CespiteView cespiteView = ammortamentoCespiteService.getAll(filtroCespite);
        LocalDate localDate = LocalDate.now();
        if(StringUtils.isNotBlank(filtroCespite.getData())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            localDate = LocalDate.parse(filtroCespite.getData(), formatter);
        }
        int anno = localDate.getYear();
        if (cespiteView != null && !cespiteView.getCespiteList().isEmpty()) {
            try {

                // 1. compile template ".jrxml" file
                JasperReport jasperReport = compileReport("RegistroCespiti.jrxml");

                // 2. parameters "empty"
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("sommaDTO", cespiteView.getCespiteSommaDto());
                parameters.put("ds", cespiteView.getCespiteList());

                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
                String destFileName = "Registro_cespiti_" + anno + ".pdf";
                String tempDir = System.getProperty("java.io.tmpdir");
                File f = new File(tempDir + destFileName);
                JasperExportManager.exportReportToPdfFile(jasperPrint, f.getName());
                return f;
            } catch (JRException e) {
                Log.error("Errore nella creazione del report registro cespiti ", e);
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private JasperReport compileReport(String reportName) {
        JasperReport jasperReport = null;
        try {
            InputStream reportStream = getClass().getResourceAsStream("/reports/" + reportName);
            jasperReport = JasperCompileManager.compileReport(reportStream);
            JRSaver.saveObject(jasperReport, reportName.replace(".jrxml", ".jasper"));
        } catch (JRException ex) {
            Log.error("Error compliling report", ex);
        }
        return jasperReport;
    }

    private static Map<String, Object> getParameters(double sum) {
        Map<String, Object> map = new HashMap<>();
        map.put("totaleImponibile", sum);
        map.put("totaleIVA", (Double) map.get("totaleImponibile") * 22 / 100);
        map.put("totaleDocumento", (Double) map.get("totaleImponibile") + (Double) map.get("totaleIVA"));
        return map;
    }

    private <T> JRDataSource getDataSource(List<T> list) {
        return new JRBeanCollectionDataSource(list);
    }
}
