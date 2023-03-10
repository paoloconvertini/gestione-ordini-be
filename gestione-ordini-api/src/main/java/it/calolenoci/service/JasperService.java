package it.calolenoci.service;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.OrdineReportDto;
import it.calolenoci.mapper.OrdineClienteReportMapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRSaver;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class JasperService {

    @ConfigProperty(name = "ordini.path")
    String pathReport;

    @Inject
    OrdineClienteReportMapper mapper;

    public List<OrdineReportDto> getOrdiniReport(OrdineDTO dto, List<OrdineDettaglioDto> articoli, String filename) {
        List<OrdineReportDto> dtoList = new ArrayList<>();
        articoli.forEach(a -> dtoList.add(mapper.fromEntityToDto(dto, a, filename)));

        return dtoList;
    }

    public void createReport(List<OrdineReportDto> articoli) throws JRException, IOException {

        OrdineReportDto ordineReportDto = articoli.stream().findAny().get();
        String ordineId = ordineReportDto.getANNO() + "_" + ordineReportDto.getSERIE() + "_" + ordineReportDto.getPROGRESSIVO();

        // 1. compile template ".jrxml" file
        JasperReport jasperReport = compileReport();

        // 2. parameters "empty"
        Map<String, Object> parameters = getParameters(articoli);

        // 3. datasource "java object"
        JRDataSource dataSource = getDataSource(articoli);

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        String destFileName = "ordine_" + ordineId + ".pdf";
        File f =new File(destFileName);
        f.createNewFile();
        JasperExportManager.exportReportToPdfFile(jasperPrint, destFileName);
    }

    private JasperReport compileReport() {
        JasperReport jasperReport = null;
        try {
            InputStream reportStream = getClass().getResourceAsStream("/reports/Invoice.jrxml");
            jasperReport = JasperCompileManager.compileReport(reportStream);
            JRSaver.saveObject(jasperReport, "Invoice.jrxml".replace(".jrxml", ".jasper"));
        } catch (JRException ex) {
           //
        }
        return jasperReport;
    }

    private static Map<String, Object> getParameters(List<OrdineReportDto> articoli){
        Map<String, Object> map = new HashMap<>();
        map.put("totaleImponibile" , articoli.stream().filter(a-> !"C".equals(a.getTIPORIGO())).mapToDouble(OrdineReportDto::getValoreTotale).sum());
        map.put("totaleIVA" , (Double)map.get("totaleImponibile")*22/100);
        map.put("totaleDocumento" , (Double)map.get("totaleImponibile") + (Double)map.get("totaleIVA"));
        map.put("totaleNetto" , map.get("totaleDocumento"));
        return map;
    }

    private static JRDataSource getDataSource(List<OrdineReportDto> articoli){
        return new JRBeanCollectionDataSource(articoli);
    }
}
