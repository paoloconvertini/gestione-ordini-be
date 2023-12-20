package it.calolenoci.service;

import io.quarkus.logging.Log;
import it.calolenoci.dto.RegistroCespitiDto;
import it.calolenoci.dto.FiltroCespite;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRSaver;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class JasperService {

    @Inject
    AmmortamentoCespiteService ammortamentoCespiteService;

    public File createReport(FiltroCespite filtroCespite) {
        RegistroCespitiDto registroCespitiDto = ammortamentoCespiteService.getRegistroCespiti(filtroCespite);
        LocalDate localDate = LocalDate.now();
        if(StringUtils.isNotBlank(filtroCespite.getData())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            localDate = LocalDate.parse(filtroCespite.getData(), formatter);
        }
        int anno = localDate.getYear();
        if (registroCespitiDto != null && !registroCespitiDto.getCespiteList().isEmpty()) {
            try {

                // 1. compile template ".jrxml" file
                JasperReport jasperReport = compileReport("RegistroCespiti.jrxml");

                // 2. parameters "empty"
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("cespiteReport", compileReport("Cespite.jrxml"));
                parameters.put("ammortamentoReport", compileReport("Ammortamento.jrxml"));
                parameters.put("ammortamentoRowReport", compileReport("AmmortamentoRow.jrxml"));
                parameters.put("riepilogoFiscaleReport", compileReport("RiepilogoFiscale.jrxml"));
                parameters.put("riepilogoFiscaleRowReport", compileReport("RiepilogoFiscaleRow.jrxml"));

                parameters.put("cespiteParameter", getCespiteParam(registroCespitiDto));

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

    private static Map getCespiteParam(RegistroCespitiDto registroCespitiDto){
        Map<String, Object> cespiteParam = new HashMap<>();
        cespiteParam.put("cespiteDataset", registroCespitiDto.getCespiteList());
        return cespiteParam;
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
