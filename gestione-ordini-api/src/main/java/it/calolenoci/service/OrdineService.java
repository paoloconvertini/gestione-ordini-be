package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.calolenoci.enums.StatoOrdineEnum.ARCHIVIATO;
import static it.calolenoci.enums.StatoOrdineEnum.COMPLETO;

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


    @Transactional
    @TransactionConfiguration(timeout = 15)
    public PageOrdineDto findAllByStatus(FiltroOrdini filtro) throws ParseException {
        PageOrdineDto pageOrdineDto = new PageOrdineDto();
       /* if (!StatoOrdineEnum.DA_PROCESSARE.getDescrizione().equals(filtro.getStatus()) &&
                !ARCHIVIATO.getDescrizione().equals(filtro.getStatus()) &&
                !StatoOrdineEnum.DA_ORDINARE.getDescrizione().equals(filtro.getStatus())) {
            checkStatusDettaglio(filtro);
        }
        if (!ARCHIVIATO.getDescrizione().equals(filtro.getStatus())) {
            checkConsegnati(filtro);
            checkNoProntaConegna(filtro);
        }*/
        long inizio = System.currentTimeMillis();
        String query = " SELECT o.anno,  o.serie,  o.progressivo, o.dataConferma,  o.numeroConferma,  " +
                "p.intestazione, p.sottoConto,  o.riferimento,  p.indirizzo,  p.localita, p.cap,  p.provincia,  " +
                "p.statoResidenza,  p.statoEstero,  p.telefono,  p.cellulare,  p.email,  p.pec,  go.status, " +
                "go.locked, go.userLock, go.warnNoBolla, go.hasFirma, go.hasProntoConsegna, go.note, go.noteLogistica, go.hasCarico " +
                ", go.dataNote, go.userNote, go.dataNoteLogistica, go.userNoteLogistica, o.createDate, o.updateDate " +
                "FROM Ordine o " +
                "LEFT JOIN GoOrdine go ON o.anno = go.anno AND o.serie = go.serie AND o.progressivo = go.progressivo " +
                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto WHERE o.dataConferma >= :dataConfig and o.provvisorio <> 'S' ";

        Map<String, Object> map = new HashMap<>();
        map.put("dataConfig", sdf.parse(dataCongig));
        if (filtro.getProntoConsegna()) {
            query += " AND go.hasProntoConsegna = true ";
        }
        query = applyFilters(filtro, query, map);
        long inizioQuery = System.currentTimeMillis();

        Sort sorting = Sort.descending("go.hasCarico", "dataConferma");
        if (StatoOrdineEnum.DA_ORDINARE.getDescrizione().equals(filtro.getStatus())) {
            sorting = Sort.descending("o.updateDate", "go.hasCarico");
        }
        PanacheQuery<Ordine> panacheQuery = Ordine.find(query, sorting, map);
        long count = panacheQuery.count();
        List<OrdineDTO> list = panacheQuery
                .page(Page.of(filtro.getPage(), filtro.getSize()))
                .project(OrdineDTO.class)
                .list();
        pageOrdineDto.setCount(count);
        pageOrdineDto.setList(list);
        long fineQuery = System.currentTimeMillis();
        Log.info("Query all ordini: " + (fineQuery - inizioQuery) / 1000 + " sec");
        long fine = System.currentTimeMillis();
        Log.info("Metodo all ordini: " + (fine - inizio) / 1000 + " sec");
        return pageOrdineDto;
    }

    private String applyFilters(FiltroOrdini filtro, String query, Map<String, Object> map) throws ParseException {
        if (filtro.getAnno() != null) {
            query += " and o.anno = :a";
            map.put("a", filtro.getAnno());
        }
        if (StringUtils.isNotBlank(filtro.getCodVenditore())) {
            query += " and o.serie = :venditore";
            map.put("venditore", filtro.getCodVenditore());
        }
        if (filtro.getProgressivo() != null) {
            query += " and o.progressivo = :p";
            map.put("p", filtro.getProgressivo());
        }
        if (StringUtils.isNotBlank(filtro.getStatus())) {
            query += " AND go.status = :status ";
            map.put("status", filtro.getStatus());
        } else {
            query += " AND (go.status <> 'ARCHIVIATO' AND go.status IS NOT NULL AND go.status <> '') ";
        }
        if (StringUtils.isNotBlank(filtro.getCliente())) {
            query += " and UPPER(p.intestazione) LIKE :c";
            map.put("c", "%" + StringUtils.upperCase(filtro.getCliente()) + "%");
        }
        if (StringUtils.isNotBlank(filtro.getLuogo())) {
            query += " and UPPER(p.localita) LIKE :l";
            map.put("l", "%" + StringUtils.upperCase(filtro.getLuogo()) + "%");
        }
        if (filtro.getDataOrdine() != null) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String format = filtro.getDataOrdine().format(dateTimeFormatter);
            query += " and o.dataConferma = :d";
            map.put("d", sdf.parse(format));
        }
        return query;
    }

    private String applyFiltersConsegna(FiltroOrdini filtro, String query, Map<String, Object> map) {
        if (filtro.getAnno() != null) {
            query += " and go.anno = :a";
            map.put("a", filtro.getAnno());
        }
        if (StringUtils.isNotBlank(filtro.getCodVenditore())) {
            query += " and go.serie = :venditore";
            map.put("venditore", filtro.getCodVenditore());
        }
        if (filtro.getProgressivo() != null) {
            query += " and go.progressivo = :p";
            map.put("p", filtro.getProgressivo());
        }
        return query;
    }

    public List<OrdineDTO> findAltriOrdiniCliente(Integer anno, String serie, Integer progressivo, String sottoConto) throws ParseException {
        String query = " SELECT o.anno,  o.serie,  o.progressivo, o.dataConferma,  o.numeroConferma,  " +
                " go.status, go.hasProntoConsegna, go.note, go.noteLogistica, p.intestazione, o.riferimento, p.localita, p.provincia " +
                ", go.dataNote, go.userNote, go.dataNoteLogistica, go.userNoteLogistica " +
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
    public void checkStatusDettaglio(FiltroOrdini filtroOrdini) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isBlank(filtroOrdini.getStatus())) {
            list.add(StatoOrdineEnum.COMPLETO.getDescrizione());
            list.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
        } else {
            list.add(filtroOrdini.getStatus());
        }
        long inizio = System.currentTimeMillis();
        List<GoOrdineDto> ordineList = GoOrdine.find("SELECT distinct o.anno, o.serie, o.progressivo, o.status " +
                " FROM GoOrdine o " +
                "JOIN OrdineDettaglio o2 ON o2.anno = o.anno AND o2.serie = o.serie AND o2.progressivo = o.progressivo " +
                "WHERE NOT EXISTS (SELECT 1 FROM GoOrdineDettaglio god WHERE o2.progrGenerale = god.progrGenerale) " +
                "and o.status in (:param) and o2.tipoRigo = ' '", Parameters.with("param", list)).project(GoOrdineDto.class).list();
        if (!ordineList.isEmpty()) {
            for (GoOrdineDto o : ordineList) {
                Log.error("Ordineid checkStatusDettaglio= " + o.getAnno() + "/" + o.getSerie() + "/" + o.getProgressivo() +
                        ", old status " + o.getStatus());
                GoOrdine.update("status =:status " +
                                "WHERE anno =:a AND serie=:s AND progressivo=:p",
                        Parameters.with("status", StatoOrdineEnum.DA_PROCESSARE.getDescrizione())
                                .and("a", o.getAnno())
                                .and("s", o.getSerie()).and("p", o.getProgressivo()));
            }
        }
        long fine = System.currentTimeMillis();
        Log.error("check nuovi articoli: " + (fine - inizio) + " msec");
    }

    @Transactional
    public void checkConsegnati(FiltroOrdini filtro) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isBlank(filtro.getStatus())) {
            list.add(StatoOrdineEnum.COMPLETO.getDescrizione());
            list.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
            list.add(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
        } else {
            list.add(filtro.getStatus());
        }
        long i = System.currentTimeMillis();
        String query = "select go.anno, go.serie, go.progressivo, go.status, go.hasProntoConsegna " +
                "from GoOrdine go " +
                "WHERE NOT EXISTS (SELECT 1 FROM OrdineDettaglio o WHERE go.anno = o.anno AND  go.progressivo = o.progressivo AND go.serie = o.serie " +
                "and o.tipoRigo = ' ' and o.saldoAcconto <> 'S') " +
                "AND go.status IN (:param)" +
                "and go.warnNoBolla = :w";
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("param", list);
        filterMap.put("w", Boolean.FALSE);
        query = applyFiltersConsegna(filtro, query, filterMap);
        PanacheQuery<GoOrdine> panacheQuery = GoOrdine.find(query, filterMap);
        List<GoOrdineDto> ordineList = panacheQuery
                .project(GoOrdineDto.class)
                .list();
        long f = System.currentTimeMillis();
        Log.error("GoOrdine.findOrdiniConsegnatiByStatus: " + (f - i) + " msec");
        long inizio = System.currentTimeMillis();
        if (!ordineList.isEmpty()) {
            for (GoOrdineDto o : ordineList) {
                Log.error("Ordineid checkconsegnati= " + o.getAnno() + "/" + o.getSerie() + "/" + o.getProgressivo() +
                        ", old status " + o.getStatus() + "  old hasProntoConsegna=" + o.getHasProntoConsegna());
                GoOrdine.update("status =:status, hasProntoConsegna =:f " +
                                "WHERE anno =:a AND serie=:s AND progressivo=:p",
                        Parameters.with("status", ARCHIVIATO.getDescrizione())
                                .and("f", Boolean.FALSE)
                                .and("a", o.getAnno())
                                .and("s", o.getSerie()).and("p", o.getProgressivo()));
            }
        }
        long fine = System.currentTimeMillis();
        Log.error("FindNoConsegnati: " + (fine - inizio) + " msec");
    }

    @Transactional
    public void checkNoProntaConegna(FiltroOrdini filtro) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isBlank(filtro.getStatus())) {
            list.add(StatoOrdineEnum.COMPLETO.getDescrizione());
            list.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
            list.add(StatoOrdineEnum.DA_ORDINARE.getDescrizione());
            list.add(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
        } else {
            list.add(filtro.getStatus());
        }
        long i = System.currentTimeMillis();
        String query = "select go.anno, go.serie, go.progressivo, go.status, go.hasProntoConsegna " +
                "from GoOrdine go " +
                "WHERE exists (SELECT 1 FROM GoOrdineDettaglio god WHERE go.anno = god.anno AND  go.progressivo = god.progressivo AND go.serie = god.serie AND god.flProntoConsegna =:f) " +
                "AND exists (SELECT 1 FROM OrdineDettaglio o " +
                "WHERE go.anno = o.anno AND  go.progressivo = o.progressivo AND go.serie = o.serie and o.tipoRigo = ' ') AND go.status IN (:param) and go.hasProntoConsegna = true";
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("param", list);
        filterMap.put("f", Boolean.FALSE);
        query = applyFiltersConsegna(filtro, query, filterMap);
        PanacheQuery<GoOrdine> panacheQuery = GoOrdine.find(query, filterMap);
        List<GoOrdineDto> ordineList = panacheQuery.project(GoOrdineDto.class).list();
        long f = System.currentTimeMillis();
        Log.error("GoOrdine.findOrdiniNoProntaConsegnaByStatus: " + (f - i) + " msec");
        long inizio = System.currentTimeMillis();
        if (!ordineList.isEmpty()) {
            for (GoOrdineDto o : ordineList) {
                Log.error("Ordineid checkNoProntaConegna= " + o.getAnno() + "/" + o.getSerie() + "/" + o.getProgressivo()
                        + ", old hasProntoConsegna=" + o.getHasProntoConsegna());
                GoOrdine.update("hasProntoConsegna =:f WHERE anno =:a AND serie=:s AND progressivo=:p",
                        Parameters.with("f", Boolean.FALSE).and("a", o.getAnno())
                                .and("s", o.getSerie()).and("p", o.getProgressivo()));
            }
        }
        long fine = System.currentTimeMillis();
        Log.error("Fine checkNoProntaConegna: " + (fine - inizio) + " msec");
    }

    public OrdineDTO findById(Integer anno, String serie, Integer progressivo) {
        return Ordine.find(" SELECT o.anno,  o.serie,  o.progressivo, o.dataConferma,  o.numeroConferma, pa.descrizione, " +
                                "p.intestazione, p.sottoConto,  o.riferimento,  p.indirizzo,  p.localita, p.cap,  p.provincia,  " +
                                "p.statoResidenza,  p.statoEstero,  p.telefono,  p.cellulare,  p.email,  " +
                                "p.pec,  go.status, go.locked, go.userLock, go.warnNoBolla, go.noteLogistica, go.userNoteLogistica, go.dataNoteLogistica " +
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
            Log.error("addNuoviOrdini: " + (fine - inizio) / 1000 + " sec");
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

    public PageOrdineDto findAllByStati(FiltroOrdini filtro) throws ParseException {
        PageOrdineDto result = new PageOrdineDto();
        // checkStatusDettaglio(filtro);
        //checkConsegnati(filtro);
        //checkNoProntaConegna(filtro);

        String query = " SELECT o.anno,  o.serie,  o.progressivo, o.dataConferma,  o.numeroConferma, o.indirdiverse, o.locdiverse, o.provdiverse, " +
                "p.intestazione, p.sottoConto,  o.riferimento,  p.indirizzo,  p.localita, p.cap,  p.provincia, p.latitudine, p.longitudine,  " +
                "p.statoResidenza,  p.statoEstero,  p.telefono,  p.cellulare,  p.email,  p.pec,  go.status, " +
                "go.locked, go.userLock, go.warnNoBolla, go.hasFirma, go.hasProntoConsegna, go.note, go.noteLogistica, v.idVeicolo, v.dataConsegna, v.venditore, v.oraConsegna, v.ordine " +
                ", go.dataNote, go.userNote, go.dataNoteLogistica, go.userNoteLogistica " +
                "FROM Ordine o " +
                "LEFT JOIN GoOrdine go ON o.anno = go.anno AND o.serie = go.serie AND o.progressivo = go.progressivo " +
                "LEFT JOIN GoOrdVeicolo v ON v.id.anno = go.anno AND v.id.serie = go.serie AND v.id.progressivo = go.progressivo " +
                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto WHERE o.dataConferma >= :dataConfig and o.provvisorio <> 'S'";

        String queryPregressi = " SELECT o.anno,  o.serie,  o.progressivo, o.dataConferma,  o.numeroConferma, o.indirdiverse, o.locdiverse, o.provdiverse,  " +
                "p.intestazione, p.sottoConto,  o.riferimento, p.localita, p.provincia, p.latitudine, p.longitudine, " +
                "p.telefono,  p.cellulare, v.idVeicolo, v.dataConsegna, v.venditore, v.oraConsegna, v.ordine  " +
                "FROM Ordine o " +
                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto " +
                "JOIN GoOrdVeicolo v ON v.id.anno = o.anno AND v.id.serie = o.serie AND v.id.progressivo = o.progressivo " +
                "WHERE o.dataConferma <:dataConfig AND o.provvisorio <> 'S' AND " +
                "EXISTS (SELECT 1 FROM OrdineDettaglio od WHERE od.anno = o.anno AND od.serie = o.serie AND od.progressivo = o.progressivo and " +
                "od.saldoAcconto IN ('A', '', ' ') AND od.tipoRigo <> 'C')";

        Map<String, Object> map = new HashMap<>();
        Map<String, Object> mapPregressi = new HashMap<>();

        if (filtro.getStati() != null && !filtro.getStati().isEmpty()) {
            query += "AND go.status IN (:list)";
            if (filtro.getDataConsegnaEnd() != null) {
                filtro.getStati().add(ARCHIVIATO.getDescrizione());
            }
            map.put("list", filtro.getStati());
        }
        if (StringUtils.isNotBlank(filtro.getStatus())) {
            query += " AND go.status = :status";
            map.put("status", filtro.getStatus());
        }
        map.put("dataConfig", sdf.parse(dataCongig));
        mapPregressi.put("dataConfig", sdf.parse(dataCongig));

        if (StringUtils.isNotBlank(filtro.getCodVenditore())) {
            query += " and o.serie = :venditore";
            queryPregressi += " and o.serie = :venditore";
            map.put("venditore", filtro.getCodVenditore());
            mapPregressi.put("venditore", filtro.getCodVenditore());
        }
        if (filtro.getVeicolo() != null) {
            query += " and v.idVeicolo = :v";
            queryPregressi += " and v.idVeicolo = :v";
            map.put("v", filtro.getVeicolo());
            mapPregressi.put("v", filtro.getVeicolo());
        }
        if (filtro.getDataConsegnaStart() != null) {
            query += " and v.dataConsegna >= :d";
            queryPregressi += " and v.dataConsegna >= :d";
            map.put("d", filtro.getDataConsegnaStart());
            mapPregressi.put("d", filtro.getDataConsegnaStart());
        }
        if (filtro.getDataConsegnaEnd() != null) {
            query += " and v.dataConsegna <= :de";
            queryPregressi += " and v.dataConsegna <= :de";
            map.put("de", filtro.getDataConsegnaEnd());
            mapPregressi.put("de", filtro.getDataConsegnaEnd());
        }
        query = applyFiltersRiservato(filtro, query, map);
        queryPregressi = applyFiltersRiservato(filtro, queryPregressi, mapPregressi);
        PanacheQuery<Ordine> panacheQuery = Ordine.find(query, map);
        PanacheQuery<Ordine> dtoPanacheQuery = Ordine.find(queryPregressi, mapPregressi);
        long count = panacheQuery.count();
        count += dtoPanacheQuery.count();
        List<OrdineDTO> ordineList;
        if (filtro.getSize() > 0) {
            ordineList = panacheQuery.page(filtro.getPage(), filtro.getSize())
                    .project(OrdineDTO.class)
                    .list();
        } else {
            ordineList = panacheQuery
                    .project(OrdineDTO.class)
                    .list();
        }
        List<OrdineDTO> ordinePregressiList;
        if (filtro.getSize() > 0) {
            ordinePregressiList = dtoPanacheQuery.page(filtro.getPage(), filtro.getSize())
                    .project(OrdineDTO.class)
                    .list();
        } else {
            ordinePregressiList = dtoPanacheQuery
                    .project(OrdineDTO.class)
                    .list();
        }
        result.setCount(count);
        result.setList(ordineList);
        result.getList().addAll(ordinePregressiList);
        return result;
    }

    public ConsegneSettimanaliDto consegneSettimanali(FiltroOrdini filtro) throws ParseException {
        ConsegneSettimanaliDto result = new ConsegneSettimanaliDto();

        String query = " SELECT o.anno,  o.serie,  o.progressivo, o.dataConferma,  o.numeroConferma, o.indirdiverse, o.locdiverse, o.provdiverse, " +
                "p.intestazione, p.sottoConto,  o.riferimento,  p.indirizzo,  p.localita, p.cap,  p.provincia, p.latitudine, p.longitudine,  " +
                "p.statoResidenza,  p.statoEstero,  p.telefono,  p.cellulare,  p.email,  p.pec,  go.status, " +
                "go.locked, go.userLock, go.warnNoBolla, go.hasFirma, go.hasProntoConsegna, go.note, go.noteLogistica, v.idVeicolo, ve.descrizione, v.dataConsegna, v.venditore, v.oraConsegna, v.ordine " +
                ", go.dataNote, go.userNote, go.dataNoteLogistica, go.userNoteLogistica " +
                "FROM Ordine o " +
                "LEFT JOIN GoOrdine go ON o.anno = go.anno AND o.serie = go.serie AND o.progressivo = go.progressivo " +
                "LEFT JOIN GoOrdVeicolo v ON v.id.anno = go.anno AND v.id.serie = go.serie AND v.id.progressivo = go.progressivo " +
                "JOIN Veicolo ve ON v.idVeicolo = ve.id " +
                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto WHERE o.dataConferma >= :dataConfig and o.provvisorio <> 'S'";

        String queryPregressi = " SELECT o.anno,  o.serie,  o.progressivo, o.dataConferma,  o.numeroConferma, o.indirdiverse, o.locdiverse, o.provdiverse,  " +
                "p.intestazione, p.sottoConto,  o.riferimento, p.localita, p.provincia, p.latitudine, p.longitudine, " +
                "p.telefono,  p.cellulare, v.idVeicolo, ve.descrizione, v.dataConsegna, v.venditore, v.oraConsegna, v.ordine  " +
                "FROM Ordine o " +
                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto " +
                "JOIN GoOrdVeicolo v ON v.id.anno = o.anno AND v.id.serie = o.serie AND v.id.progressivo = o.progressivo " +
                "JOIN Veicolo ve ON v.idVeicolo = ve.id " +
                "WHERE o.dataConferma <:dataConfig AND o.provvisorio <> 'S' AND " +
                "EXISTS (SELECT 1 FROM OrdineDettaglio od WHERE od.anno = o.anno AND od.serie = o.serie AND od.progressivo = o.progressivo and " +
                "od.saldoAcconto IN ('A', '', ' ') AND od.tipoRigo <> 'C')";

        Map<String, Object> map = new HashMap<>();
        Map<String, Object> mapPregressi = new HashMap<>();

        map.put("dataConfig", sdf.parse(dataCongig));
        mapPregressi.put("dataConfig", sdf.parse(dataCongig));

        if (StringUtils.isNotBlank(filtro.getCodVenditore())) {
            query += " and o.serie = :venditore";
            queryPregressi += " and o.serie = :venditore";
            map.put("venditore", filtro.getCodVenditore());
            mapPregressi.put("venditore", filtro.getCodVenditore());
        }
        if (filtro.getVeicolo() != null) {
            query += " and v.idVeicolo = :v";
            queryPregressi += " and v.idVeicolo = :v";
            map.put("v", filtro.getVeicolo());
            mapPregressi.put("v", filtro.getVeicolo());
        }
        // Calcolo lunedì e sabato della settimana desiderata
        LocalDate monday = LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .plusWeeks(filtro.getDeltaSettimana());
        LocalDate saturday = monday.plusDays(5); // sabato della stessa settimana
        query += " and v.dataConsegna >= :d";
        queryPregressi += " and v.dataConsegna >= :d";
        map.put("d", monday);
        mapPregressi.put("d", monday);
        query += " and v.dataConsegna <= :de";
        queryPregressi += " and v.dataConsegna <= :de";
        map.put("de", saturday);
        mapPregressi.put("de", saturday);
        PanacheQuery<Ordine> panacheQuery = Ordine.find(query, map);
        PanacheQuery<Ordine> dtoPanacheQuery = Ordine.find(queryPregressi, mapPregressi);
        List<OrdineDTO> ordineList;
        ordineList = panacheQuery.project(OrdineDTO.class).list();
        ordineList.addAll(dtoPanacheQuery.project(OrdineDTO.class).list());


        Map<DayOfWeek, ConsegnaGiornalieraDto> mappaGiorni = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek giorno : DayOfWeek.values()) {
            if (giorno != DayOfWeek.SUNDAY) {
                mappaGiorni.put(giorno, new ConsegnaGiornalieraDto());
            }
        }
        // Raggruppa e ordina le consegne per giorno della settimana
        Map<LocalDate, List<OrdineDTO>> consegnePerData = ordineList.stream()
                .filter(o -> o.getDataConsegna() != null)
                .collect(Collectors.groupingBy(OrdineDTO::getDataConsegna));

        for (Map.Entry<LocalDate, List<OrdineDTO>> entry : consegnePerData.entrySet()) {
            LocalDate data = entry.getKey();
            DayOfWeek giorno = data.getDayOfWeek();

            if (giorno != DayOfWeek.SUNDAY && mappaGiorni.containsKey(giorno)) {
                List<OrdineDTO> consegneOrdinate = entry.getValue().stream()
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(OrdineDTO::getOraConsegna,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .toList();
                mappaGiorni.get(giorno).setConsegne(consegneOrdinate);
            }
        }
        result.setLunedi(mappaGiorni.get(DayOfWeek.MONDAY));
        result.setMartedi(mappaGiorni.get(DayOfWeek.TUESDAY));
        result.setMercoledi(mappaGiorni.get(DayOfWeek.WEDNESDAY));
        result.setGiovedi(mappaGiorni.get(DayOfWeek.THURSDAY));
        result.setVenerdi(mappaGiorni.get(DayOfWeek.FRIDAY));
        result.setSabato(mappaGiorni.get(DayOfWeek.SATURDAY));
        return result;
    }

    public PageOrdineDto findAllRiservati(FiltroOrdini filtro) throws ParseException {
        PageOrdineDto result = new PageOrdineDto();
        //checkStatusDettaglio(filtro);
        //checkConsegnati(filtro);
        //checkNoProntaConegna(filtro);

        String query = " SELECT o.anno,  o.serie,  o.progressivo, o.dataConferma,  o.numeroConferma,  " +
                "p.intestazione, p.sottoConto,  o.riferimento,  p.indirizzo,  p.localita, p.cap,  p.provincia, " +
                "p.statoResidenza,  p.statoEstero,  p.telefono,  p.cellulare, go.status, " +
                "go.note, go.noteLogistica, SUM((o2.prezzo*(1-o2.scontoArticolo/100)*(1-o2.scontoC1/100)*(1-o2.scontoC2/100)*(1-o2.scontoP/100))*d.qtaDaConsegnare) " +
                ", go.dataNote, go.userNote, go.dataNoteLogistica, go.userNoteLogistica " +
                "FROM Ordine o " +
                "INNER JOIN GoOrdine go ON o.anno = go.anno AND o.serie = go.serie AND o.progressivo = go.progressivo " +
                "INNER JOIN OrdineDettaglio o2 ON o.anno = o2.anno AND o.serie = o2.serie AND o.progressivo = o2.progressivo " +
                "INNER JOIN GoOrdineDettaglio d ON d.progrGenerale = o2.progrGenerale " +
                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto " +
                "WHERE o.dataConferma >= :dataConfig and o.provvisorio <> 'S' " +
                "AND d.flagConsegnato <> 'T' AND go.status <> 'ARCHIVIATO' AND d.flagRiservato = 'T' ";

        Map<String, Object> map = new HashMap<>();

        if (filtro.getStati() != null && !filtro.getStati().isEmpty()) {
            query += "AND go.status IN (:list)";
            map.put("list", filtro.getStati());
        }
        if (StringUtils.isNotBlank(filtro.getStatus())) {
            query += " AND go.status = :status";
            map.put("status", filtro.getStatus());
        }
        map.put("dataConfig", sdf.parse(dataCongig));
        if (StringUtils.isNotBlank(filtro.getCodVenditore())) {
            query += " and o.serie = :venditore";
            map.put("venditore", filtro.getCodVenditore());
        }
        query += " group by " +
                "   o.anno,  o.serie,  o.progressivo, o.dataConferma,  o.numeroConferma, " +
                "   p.intestazione, p.sottoConto,  o.riferimento,  p.indirizzo,  p.localita, p.cap,  p.provincia, " +
                " p.statoResidenza,  p.statoEstero,  p.telefono,  p.cellulare, go.status,  " +
                "  go.note, go.noteLogistica, go.dataNote, go.userNote, go.dataNoteLogistica, go.userNoteLogistica ";
        query = applyFiltersRiservato(filtro, query, map);
        PanacheQuery<Ordine> panacheQuery = Ordine.find(query, map);
        List<OrdineDTO> ordineDTOList = panacheQuery.project(OrdineDTO.class).list();
        ordineDTOList = ordineDTOList.stream().filter(Objects::nonNull).sorted(Comparator.comparing(OrdineDTO::getImportoRiservati,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed()).toList();
        result.setList(ordineDTOList);
        return result;
    }

    private String applyFiltersRiservato(FiltroOrdini filtro, String query, Map<String, Object> map) throws ParseException {
        if (filtro.getAnno() != null) {
            query += " and o.anno = :a";
            map.put("a", filtro.getAnno());
        }
        if (filtro.getProgressivo() != null) {
            query += " and o.progressivo = :pr";
            map.put("pr", filtro.getProgressivo());
        }
        if (StringUtils.isNotBlank(filtro.getCliente())) {
            query += " and p.intestazione LIKE :c";
            map.put("c", "%" + filtro.getCliente() + "%");
        }
        if (StringUtils.isNotBlank(filtro.getLuogo())) {
            query += " and p.localita LIKE :l";
            map.put("l", "%" + filtro.getLuogo() + "%");
        }
        if (filtro.getDataOrdine() != null) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String format = filtro.getDataOrdine().format(dateTimeFormatter);
            query += " and o.dataConferma = :d";
            map.put("d", sdf.parse(format));
        }
        return query;
    }

    @Transactional
    public boolean updateVeicolo(OrdineDTO dto, String codVenditore) {
        try {
            GoOrdVeicolo goOrdVeicolo = new GoOrdVeicolo();
            goOrdVeicolo.setId(new GoOrdVeicoloPK(dto.getAnno(), dto.getSerie(), dto.getProgressivo()));
            if (dto.getVeicolo() != null) {
                goOrdVeicolo.setIdVeicolo(dto.getVeicolo());
            }
            if (dto.getDataConsegna() != null) {
                goOrdVeicolo.setDataConsegna(dto.getDataConsegna());
            }
            goOrdVeicolo.setOraConsegna(dto.getOraConsegna());
            goOrdVeicolo.setOrdine(dto.getOrdine());
            goOrdVeicolo.setVenditore(StringUtils.isNotBlank(codVenditore));
            long delete = GoOrdVeicolo.delete("id.anno = :anno AND id.serie = :serie AND id.progressivo =:progressivo"
                    , Parameters.with("anno", dto.getAnno()).and("serie", dto.getSerie())
                            .and("progressivo", dto.getProgressivo()));
            if (goOrdVeicolo == null && delete > 0) {
                return true;
            }
            if (goOrdVeicolo != null) {
                goOrdVeicolo.persist();
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.error("Error saving veicoli", e);
            return false;
        }
    }

    public OrdineclienteMonitorDto getOrdiniClienteNonOrdinati() throws ParseException {
        OrdineclienteMonitorDto o = new OrdineclienteMonitorDto();
        List<GoOrdine> listaOrdini = Ordine.find("SELECT go " +
                        "FROM Ordine o " +
                        "LEFT JOIN GoOrdine go ON o.anno = go.anno AND o.serie = go.serie AND o.progressivo = go.progressivo " +
                        "WHERE o.dataConferma >= :dataConfig and o.provvisorio <> 'S' AND go.status IN ('DA_PROCESSARE', 'DA_ORDINARE')",
                Parameters.with("dataConfig", sdf.parse(dataCongig))).list();
        int totDaOrd = listaOrdini.stream().filter(or -> StringUtils.equals(StatoOrdineEnum.DA_ORDINARE.getDescrizione(), or.getStatus())).toList().size();
        int totDaProc = listaOrdini.stream().filter(or -> StringUtils.equals(StatoOrdineEnum.DA_PROCESSARE.getDescrizione(), or.getStatus()))
                .filter(ord ->
                        OrdineDettaglio.find("SELECT o.progrGenerale " +
                                "FROM OrdineDettaglio o " +
                                "LEFT JOIN GoOrdineDettaglio god ON o.progrGenerale = god.progrGenerale " +
                                "WHERE o.anno = god.anno AND o.serie = god.serie AND o.progressivo = god.progressivo AND " +
                                "god.flagRiservato = 'F' AND god.flagNonDisponibile = 'F' AND god.flagOrdinato = 'F'").list().size() > 1).toList().size();
        o.setTotOrdNonDisp((long) totDaOrd);
        o.setTotOrdNonProcessati((long) totDaProc);
        return o;
    }

    public List<OrdineDTO> getAllPregressi(FiltroOrdini filtro) throws ParseException {
        String query = " SELECT o.anno,  o.serie,  o.progressivo, o.dataConferma,  o.numeroConferma,  " +
                "p.intestazione, p.sottoConto,  o.riferimento, p.localita, p.provincia, " +
                "p.telefono,  p.cellulare " +
                "FROM Ordine o " +
                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto " +
                "WHERE o.dataConferma <:dataConfig AND o.provvisorio <> 'S' " +
                "AND NOT EXISTS (SELECT 1 FROM GoOrdVeicolo v " +
                "WHERE v.id.anno = o.anno AND v.id.serie = o.serie AND v.id.progressivo = o.progressivo)";

        Map<String, Object> map = new HashMap<>();
        map.put("dataConfig", sdf.parse(dataCongig));
        if (StringUtils.isNotBlank(filtro.getCodVenditore())) {
            query += " and o.serie = :venditore";
            map.put("venditore", filtro.getCodVenditore());
        }

        return Ordine.find(query, Sort.descending("dataConferma"), map).project(OrdineDTO.class).list();

    }

    @Transactional
    public boolean salvaPregressi(List<OrdineDTO> list, String codVenditore) {
        try {
            List<GoOrdVeicolo> listToSave = new ArrayList<>();
            list.forEach(dto -> {
                GoOrdVeicolo goOrdVeicolo = new GoOrdVeicolo();
                GoOrdVeicoloPK pk = new GoOrdVeicoloPK(dto.getAnno(), dto.getSerie(), dto.getProgressivo());
                goOrdVeicolo.setId(pk);
                goOrdVeicolo.setVenditore(StringUtils.isNotBlank(codVenditore));
                listToSave.add(goOrdVeicolo);
            });

            if (!listToSave.isEmpty()) {
                GoOrdVeicolo.persist(listToSave);
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.error("Error saving veicoli", e);
            return false;
        }
    }

    public List<FatturaAccontoView> findOrdiniPerFatturaAcconto(String sottoConto) {
        List<FatturaAccontoView> result = new ArrayList<>();
        List<FatturaAccontoIvaView> fatturaAccontoIvaViews = new ArrayList<>();

        //recuperare solo articoli ancora da consegnare
        //recuperare acconti
        fatturaAccontoIvaViews = Ordine.find("select o.anno, o.serie, o.progressivo, o2.fCodiceIva, " +
                        "SUM(((CaSE WHEN prezzo is null then 0 ELSE prezzo end)*" +
                        "(CASE WHEN quantita is null then 0 else quantita end))*" +
                        "(1-scontoArticolo/100)*(1-scontoC1/100)*(1-scontoC2/100)*(1-scontoP/100)) " +
                        "from Ordine o " +
                        "join OrdineDettaglio o2 ON o.id = o2.id and o2.tipoRigo <> 'C' " +
                        "join GoOrdine og ON og.progressivo = o.progressivo AND og.serie = o.serie " +
                        "AND o.anno = og.anno AND og.status <> 'ARCHIVIATO' " +
                        "where o.contoCliente = :c " +
                        "group by o.anno, o.serie, o.progressivo, o2.fCodiceIva " +
                        "order by o.anno, o.serie, o.progressivo",
                Parameters.with("c", sottoConto)).project(FatturaAccontoIvaView.class).list();

        // Raggruppamento per anno, serie, progressivo
        Map<String, List<FatturaAccontoIvaView>> groupedMap = fatturaAccontoIvaViews.stream()
                .collect(Collectors.groupingBy(item ->
                        item.getAnno() + "|" + item.getSerie() + "|" + item.getProgressivo()
                ));

        // Creazione dei FatturaAccontoView raggruppati
        result = groupedMap.entrySet().stream()
                .map(entry -> {
                    String[] keys = entry.getKey().split("\\|");
                    FatturaAccontoView view = new FatturaAccontoView();
                    view.setAnno(Integer.parseInt(keys[0]));
                    view.setSerie(keys[1]);
                    view.setProgressivo(Integer.parseInt(keys[2]));
                    view.setFatturaAccontoIvaViewList(entry.getValue());
                    return view;
                })
                .collect(Collectors.toList());

        return result;
    }
}