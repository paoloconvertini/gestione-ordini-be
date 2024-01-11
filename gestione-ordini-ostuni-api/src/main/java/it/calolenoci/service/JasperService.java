package it.calolenoci.service;

import io.quarkus.logging.Log;
import it.calolenoci.dto.RegistroCespiteReportDto;
import it.calolenoci.dto.RegistroCespitiDto;
import it.calolenoci.mapper.RegistroCespiteReportMapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRSaver;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class JasperService {

    @Inject
    RegistroCespiteReportMapper mapper;

    public File createReport(RegistroCespitiDto registroCespitiDto) {
        if (registroCespitiDto != null && !registroCespitiDto.getCespiteList().isEmpty()) {
            try {

                RegistroCespiteReportDto dto = mapper.buildRegistroCespiteReport(registroCespitiDto);

                // 1. compile template ".jrxml" file

                // 2. parameters "empty"
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("cespiteReport", compileReport("Cespite.jrxml"));
                parameters.put("ammortamentoReport", compileReport("Ammortamento.jrxml"));
                parameters.put("ammortamentoRowReport", compileReport("AmmortamentoRow.jrxml"));
                parameters.put("anno",registroCespitiDto.getData() == null ? LocalDate.now().getYear() : registroCespitiDto.getData().getYear());

                JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(Collections.singletonList(dto));

                JasperReport jasperReport = compileReport("RegistroCespiti.jrxml");
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, ds);
                String destFileName = "Registro_cespiti.pdf";
                File f = new File(destFileName);
                JasperExportManager.exportReportToPdfFile(jasperPrint, f.getName());
                return f;
            } catch (Exception e) {
                Log.error("Errore nella creazione del report registro cespiti ", e);
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private static Map getCespiteParam(RegistroCespitiDto registroCespitiDto) {
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
