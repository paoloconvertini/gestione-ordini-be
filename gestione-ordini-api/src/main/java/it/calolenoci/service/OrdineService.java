package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.GoOrdine;
import it.calolenoci.entity.GoOrdineDettaglio;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.enums.StatoOrdineEnum;
import it.calolenoci.mapper.GoOrdineDettaglioMapper;
import it.calolenoci.mapper.GoOrdineMapper;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrdineService {

    @Inject
    GoOrdineMapper goOrdineMapper;

    @Inject
    GoOrdineDettaglioMapper goOrdineDettaglioMapper;

    @ConfigProperty(name = "data.inizio")
    String dataCongig;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @ConfigProperty(name = "ordini.path")
    String path;

    @Inject
    ArticoloService articoloService;

    @Inject
    JasperService service;

    @ConfigProperty(name = "firma.venditore.path")
    String pathFirmaVendtore;


    public List<OrdineDTO> findAllByStatus(FiltroOrdini filtro) throws ParseException {
        long inizio = System.currentTimeMillis();
        if (!StatoOrdineEnum.DA_PROCESSARE.getDescrizione().equals(filtro.getStatus()) &&
                !StatoOrdineEnum.ARCHIVIATO.getDescrizione().equals(filtro.getStatus()) &&
        !StatoOrdineEnum.DA_ORDINARE.getDescrizione().equals(filtro.getStatus())) {
            checkStatusDettaglio(filtro.getStatus());
        }
        if (!StatoOrdineEnum.ARCHIVIATO.getDescrizione().equals(filtro.getStatus())) {
            checkConsegnati(filtro.getStatus());
            checkNoProntaConegna(filtro.getStatus());
        }
        String query = " SELECT o.anno,  o.serie,  o.progressivo, o.dataConferma,  o.numeroConferma,  " +
                "p.intestazione, p.sottoConto,  o.riferimento,  p.indirizzo,  p.localita, p.cap,  p.provincia,  " +
                "p.statoResidenza,  p.statoEstero,  p.telefono,  p.cellulare,  p.email,  p.pec,  go.status, " +
                "go.locked, go.userLock, go.warnNoBolla, go.hasFirma, go.hasProntoConsegna, go.note, go.noteLogistica " +
                "FROM Ordine o " +
                "LEFT JOIN GoOrdine go ON o.anno = go.anno AND o.serie = go.serie AND o.progressivo = go.progressivo " +
                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto WHERE o.dataConferma >= :dataConfig and o.provvisorio <> 'S' ";

        Map<String, Object> map = new HashMap<>();
        map.put("dataConfig", sdf.parse(dataCongig));
        if (StringUtils.isNotBlank(filtro.getStatus())) {
            query += " AND go.status = :status ";
            map.put("status", filtro.getStatus());
        } else {
            query += " AND go.status <> 'ARCHIVIATO' AND go.status IS NOT NULL AND go.status <> '' ";
        }
        if (filtro.getProntoConsegna()) {
            query += " AND go.hasProntoConsegna = true ";
        }
        if (StringUtils.isNotBlank(filtro.getCodVenditore())) {
            query += " and o.serie = :venditore";
            map.put("venditore", filtro.getCodVenditore());
        }
        long inizioQuery = System.currentTimeMillis();
        List<OrdineDTO> list = Ordine.find(query, Sort.descending("dataConferma"), map).project(OrdineDTO.class).list();
        long fineQuery = System.currentTimeMillis();
        Log.info("Query all ordini: " + (fineQuery - inizioQuery)/1000 + " sec");
        long fine = System.currentTimeMillis();
        Log.info("Metodo all ordini: " + (fine - inizio)/1000 + " sec");
        return list;
    }

    public List<OrdineDTO> findAltriOrdiniCliente(Integer anno, String serie, Integer progressivo, String sottoConto) throws ParseException {
        String query = " SELECT o.anno,  o.serie,  o.progressivo, o.dataConferma,  o.numeroConferma,  " +
                " go.status, go.hasProntoConsegna, go.note, go.noteLogistica, p.intestazione, o.riferimento, p.localita, p.provincia " +
                "FROM Ordine o " +
                "LEFT JOIN GoOrdine go ON o.anno = go.anno AND o.serie = go.serie AND o.progressivo = go.progressivo " +
                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto " +
                "WHERE o.dataConferma >= :dataConfig and o.provvisorio <> 'S' " +
                "AND go.status <> 'ARCHIVIATO' AND go.status IS NOT NULL AND go.status <> '' AND go.hasProntoConsegna = true " +
                "AND o.contoCliente = :sottoConto";

        Map<String, Object> map = new HashMap<>();
        map.put("dataConfig", sdf.parse(dataCongig));
        map.put("sottoConto", sottoConto);

        List<OrdineDTO> list = Ordine.find(query, Sort.descending("dataConferma"), map).project(OrdineDTO.class).list();
        list.removeIf(o -> Objects.equals(o.getAnno(), anno) && StringUtils.equals(o.getSerie(), serie) && Objects.equals(o.getProgressivo(), progressivo));
        return list;
    }

    public List<PianoContiDto> findClienti() throws ParseException {
        String query = " SELECT p.gruppoConto, p.sottoConto, p.intestazione, p.indirizzo, p.localita, p.cap, " +
                "p.provincia, p.latitudine, p.longitudine " +
                "FROM Ordine o " +
                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto " +
                "WHERE o.dataConferma >= :dataConfig and o.provvisorio <> 'S' " +
                "AND p.latitudine = 0 AND p.provincia <> 'EE'";

        Map<String, Object> map = new HashMap<>();
        map.put("dataConfig", sdf.parse(dataCongig));

        return Ordine.find(query, map).project(PianoContiDto.class).list();
    }


    @Transactional
    public void checkStatusDettaglio(String status) {
        List<String> list = new ArrayList<>();
        if(StringUtils.isBlank(status)) {
            list.add(StatoOrdineEnum.COMPLETO.getDescrizione());
            list.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
        } else {
            list.add(status);
        }
        long inizio = System.currentTimeMillis();
        List<GoOrdine> ordineList = GoOrdine.findOrdiniWithNewItems(list);
        ordineList.forEach(o -> {
            o.setStatus(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
            o.persist();
        });
        long fine = System.currentTimeMillis();
        Log.info("check nuovi articoli: " + (fine - inizio) + " msec");
    }

    @Transactional
    public void checkConsegnati(String status) {
        List<String> list = new ArrayList<>();
        if(StringUtils.isBlank(status)) {
            list.add(StatoOrdineEnum.COMPLETO.getDescrizione());
            list.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
            list.add(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
        } else {
            list.add(status);
        }

        long i = System.currentTimeMillis();
        List<GoOrdineDto> ordineList = GoOrdine.findOrdiniConsegnatiByStatus(list);
        long f = System.currentTimeMillis();
        Log.info("GoOrdine.findOrdiniByStatus: " + (f - i) + " msec");
        long inizio = System.currentTimeMillis();
        Map<String, List<GoOrdineDto>> map = ordineList.stream().collect(Collectors.groupingBy(GoOrdineDto::creaId));
        for (String s : map.keySet()) {
            List<GoOrdineDto> ordines = map.get(s);
            GoOrdineDto o = ordines.get(0);
            GoOrdine ordine = o.getGo();
            boolean anyMatch = ordines.stream().allMatch(or -> StringUtils.equals("S", or.getSaldoAcconto()));
                if (anyMatch && !ordine.getWarnNoBolla()) {
                    ordine.setStatus(StatoOrdineEnum.ARCHIVIATO.getDescrizione());
                    if (ordine.getHasProntoConsegna() != null && ordine.getHasProntoConsegna()) {
                        ordine.setHasProntoConsegna(Boolean.FALSE);
                    }
                    ordine.persist();
                }
        }
        long fine = System.currentTimeMillis();
        Log.info("FindNoConsegnati: " + (fine - inizio) + " msec");
    }

    @Transactional
    public void checkNoProntaConegna(String status) {
        List<String> list = new ArrayList<>();
        if(StringUtils.isBlank(status)){
            list.add(StatoOrdineEnum.COMPLETO.getDescrizione());
            list.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
            list.add(StatoOrdineEnum.DA_ORDINARE.getDescrizione());
            list.add(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
        } else {
            list.add(status);
        }

        long i = System.currentTimeMillis();
        List<GoOrdineDto> ordineList = GoOrdine.findOrdiniNoProntaConsegnaByStatus(list);
        long f = System.currentTimeMillis();
        Log.info("GoOrdine.findOrdiniByStatus: " + (f - i) + " msec");
        long inizio = System.currentTimeMillis();
        Map<String, List<GoOrdineDto>> map = ordineList.stream().collect(Collectors.groupingBy(GoOrdineDto::creaId));
        for (String s : map.keySet()) {
            List<GoOrdineDto> ordines = map.get(s);
            GoOrdineDto o = ordines.get(0);
            GoOrdine ordine = o.getGo();
            boolean noneMatch = ordines.stream().noneMatch(GoOrdineDto::getFlProntoConsegna);
            if (noneMatch) {
                ordine.setHasProntoConsegna(Boolean.FALSE);
                GoOrdine.persist(ordine);
            }
        }

        long fine = System.currentTimeMillis();
        Log.info("Fine checkNoProntaConegna: " + (fine - inizio) + " msec");
    }

    public OrdineDTO findById(Integer anno, String serie, Integer progressivo) {
        return Ordine.find(" SELECT o.anno,  o.serie,  o.progressivo, o.dataConferma,  o.numeroConferma, pa.descrizione, " +
                                "p.intestazione, p.sottoConto,  o.riferimento,  p.indirizzo,  p.localita, p.cap,  p.provincia,  " +
                                "p.statoResidenza,  p.statoEstero,  p.telefono,  p.cellulare,  p.email,  p.pec,  go.status, go.locked, go.userLock, go.warnNoBolla " +
                                "FROM Ordine o " +
                                "LEFT JOIN GoOrdine go ON o.anno = go.anno AND o.serie = go.serie AND o.progressivo = go.progressivo " +
                                "LEFT JOIN ModalitaPagamento pa ON o.codicePagamento = pa.codice " +
                                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto " +
                                "WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo",
                        Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo))
                .project(OrdineDTO.class).firstResult();
    }

    public OrdineDTO findForReport(Integer anno, String serie, Integer progressivo) {
        return Ordine.find(" SELECT o.anno,  o.serie,  o.progressivo, o.dataOrdine, o.dataConferma,  o.numeroConferma,  " +
                                "p.intestazione, p.sottoConto,  p.continuaIntest,  p.indirizzo,  p.localita, p.cap,  p.provincia,  " +
                                "p.statoResidenza,  p.statoEstero,  p.telefono,  p.cellulare,  p.email,  p.pec " +
                                "FROM Ordine o " +
                                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto " +
                                "WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo",
                        Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo))
                .project(OrdineDTO.class).firstResult();
    }

    @Transactional
    public void changeStatus(Integer anno, String serie, Integer progressivo, String status) {
        GoOrdine.update("status =:stato where anno = :anno and progressivo = :progressivo and serie = :serie",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)
                        .and("stato", status));
    }


    public List<FiltroStati> getStati() {
        return Arrays.stream(StatoOrdineEnum.values())
                .map(s -> new FiltroStati(s.getDescrizione(), s.getDescrizione()))
                .collect(Collectors.toList());
    }

    public void addNuoviOrdini() throws ParseException {
        long inizio = System.currentTimeMillis();
        List<Ordine> list = Ordine.find("SELECT o FROM Ordine o " +
                "WHERE o.dataConferma >= :dataConfig and o.provvisorio <> 'S' AND NOT EXISTS (SELECT 1 FROM GoOrdine god WHERE god.anno =  o.anno" +
                " AND god.serie = o.serie AND god.progressivo = o.progressivo)", Parameters.with("dataConfig", sdf.parse(dataCongig))).list();
        if (!list.isEmpty()) {
            List<GoOrdine> listToSave = new ArrayList<>();
            List<GoOrdineDettaglio> listDettaglioToSave = new ArrayList<>();
            list.forEach(o -> {
                listToSave.add(goOrdineMapper.fromOrdineToGoOrdine(o));
                List<OrdineDettaglio> dettaglioList = OrdineDettaglio
                        .find("anno = :anno AND serie = :serie AND progressivo = :progressivo and (tipoRigo <> 'C' AND tipoRigo <> 'AC')",
                                Parameters.with("anno", o.getAnno()).and("serie", o.getSerie())
                                        .and("progressivo", o.getProgressivo())).list();
                dettaglioList.forEach(d -> listDettaglioToSave.add(goOrdineDettaglioMapper.fromOrdineDettaglioToGoOrdineDettaglio(d)));

            });
            GoOrdine.persist(listToSave);
            GoOrdineDettaglio.persist(listDettaglioToSave);
            long fine = System.currentTimeMillis();
            Log.info("addNuoviOrdini: " + (fine - inizio) / 1000 + " sec");
            creaReport(list);
        }
    }

    private void creaReport(List<Ordine> list) {
        long inizio = System.currentTimeMillis();
        list.forEach(o -> {
            OrdineDTO ordineDTO = findForReport(o.getAnno(), o.getSerie(), o.getProgressivo());
            if (ordineDTO != null) {
                String firmaVenditore = pathFirmaVendtore + o.getSerie() + ".png";
                List<OrdineDettaglioDto> articoli = articoloService.findForReport(o.getAnno(), o.getSerie(), o.getProgressivo());
                List<OrdineReportDto> dtoList = service.getOrdiniReport(ordineDTO, articoli, null, firmaVenditore);
                if (!dtoList.isEmpty()) {
                    try {
                        service.createReport(dtoList, ordineDTO.getSottoConto(), o.getAnno(), o.getSerie(), o.getProgressivo());
                    } catch (JRException | IOException e) {
                        Log.error("Errore nella creazione del report per l'ordine " + o.getAnno() + '/' + o.getSerie() + '/' + o.getProgressivo(), e);
                    }
                }
            }

        });
        long fine = System.currentTimeMillis();
        Log.info("Fine creaReport: " + (fine - inizio) / 1000 + " sec");
    }

    public List<OrdineDTO> findAllByStati(FiltroOrdini filtro) throws ParseException {
        checkStatusDettaglio(filtro.getStatus());
        checkConsegnati(filtro.getStatus());
        checkNoProntaConegna(filtro.getStatus());

        String query = " SELECT o.anno,  o.serie,  o.progressivo, o.dataConferma,  o.numeroConferma,  " +
                "p.intestazione, p.sottoConto,  o.riferimento,  p.indirizzo,  p.localita, p.cap,  p.provincia, p.latitudine, p.longitudine, " +
                "p.statoResidenza,  p.statoEstero,  p.telefono,  p.cellulare,  p.email,  p.pec,  go.status, " +
                "go.locked, go.userLock, go.warnNoBolla, go.hasFirma, go.hasProntoConsegna, go.note, go.noteLogistica " +
                "FROM Ordine o " +
                "LEFT JOIN GoOrdine go ON o.anno = go.anno AND o.serie = go.serie AND o.progressivo = go.progressivo " +
                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto WHERE o.dataConferma >= :dataConfig and o.provvisorio <> 'S'" ;

        Map<String, Object> map = new HashMap<>();

        if(filtro.getStati() != null && !filtro.getStati().isEmpty()){
            query += "AND go.status IN (:list)";
            map.put("list", filtro.getStati());
        }
        if(StringUtils.isNotBlank(filtro.getStatus())) {
            query += " AND go.status = :status";
            map.put("status", filtro.getStatus());
        }




        map.put("dataConfig", sdf.parse(dataCongig));
        if (StringUtils.isNotBlank(filtro.getCodVenditore())) {
            query += " and o.serie = :venditore";
            map.put("venditore", filtro.getCodVenditore());
        }
        return Ordine.find(query, Sort.descending("dataConferma"), map).project(OrdineDTO.class).list();

    }
}