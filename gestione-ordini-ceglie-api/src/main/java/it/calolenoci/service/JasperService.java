package it.calolenoci.service;

import io.quarkus.logging.Log;
import it.calolenoci.dto.CespiteView;
import it.calolenoci.dto.FiltroCespite;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRSaver;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.InputStream;
import java.time.Year;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class JasperService {

    @Inject
    AmmortamentoCespiteService ammortamentoCespiteService;

    public File createReport() {
        CespiteView cespiteView = ammortamentoCespiteService.getAll(new FiltroCespite());
        if (cespiteView != null && !cespiteView.getCespiteList().isEmpty()) {
            try {

                // 1. compile template ".jrxml" file
                JasperReport jasperReport = compileReport("RegistroCespiti.jrxml");

                // 2. parameters "empty"
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("inizioEsercizio", cespiteView.getCespiteSommaDto().getInizioEsercizio());
                parameters.put("acquisti", cespiteView.getCespiteSommaDto().getAcquisti());
                parameters.put("vendite", cespiteView.getCespiteSommaDto().getVendite());
                parameters.put("ammortamentoDeducibile", cespiteView.getCespiteSommaDto().getAmmortamentiDeducibili());
                parameters.put("ammortamentoNonDeducibile", cespiteView.getCespiteSommaDto().getAmmortamentiNonDeducibili());
                parameters.put("fineEsercizio", cespiteView.getCespiteSommaDto().getFineEsercizio());

                // 3. datasource "java object"
                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(cespiteView));

                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
                String destFileName = "Registro_cespiti_" + Year.now().getValue() + ".pdf";
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
}
