package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import it.calolenoci.enums.StatoOrdineEnum;
import it.calolenoci.mapper.FattureMapper;
import it.calolenoci.mapper.GoOrdineDettaglioMapper;
import it.calolenoci.mapper.GoOrdineMapper;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static it.calolenoci.enums.StatoOrdineEnum.ARCHIVIATO;

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
    FattureMapper fattureMapper;
    @Inject
    ArticoloService articoloService;

    @Inject
    JasperService service;

    @ConfigProperty(name = "firma.venditore.path")
    String pathFirmaVendtore;

    @Inject
    FatturaService fatturaService;

    @Inject
    EntityManager em;

    @Inject
    AuditService auditService;

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
        // Costruisco la lista degli stati da considerare
        List<String> stati = new ArrayList<>();
        if (StringUtils.isBlank(filtroOrdini.getStatus())) {
            stati.add(StatoOrdineEnum.COMPLETO.getDescrizione());
            stati.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
        } else {
            stati.add(filtroOrdini.getStatus());
        }
        long inizio = System.currentTimeMillis();

        String jpql =
                "SELECT o.anno, o.serie, o.progressivo, o.status " +
                        "FROM GoOrdine o " +
                        "WHERE o.status IN (:stati) " +
                        "AND EXISTS (" +
                        "   SELECT 1 FROM OrdineDettaglio od " +
                        "   WHERE od.anno = o.anno " +
                        "     AND od.serie = o.serie " +
                        "     AND od.progressivo = o.progressivo " +
                        "     AND od.tipoRigo = ' ' " +
                        "     AND NOT EXISTS (" +
                        "         SELECT 1 FROM GoOrdineDettaglio god " +
                        "         WHERE god.progrGenerale = od.progrGenerale" +
                        "     )" +
                        ")";

        List<GoOrdineDto> ordineList = GoOrdine.find(
                        jpql,
                        Parameters.with("stati", stati)
                )
                .project(GoOrdineDto.class)
                .list();

        if (!ordineList.isEmpty()) {
            for (GoOrdineDto o : ordineList) {
                // ======== AUDIT ========
                String newStatus = StatoOrdineEnum.DA_PROCESSARE.getDescrizione();

                if (!Objects.equals(o.getStatus(), newStatus)) {
                    auditService.logChange(
                            "GO_ORDINE",
                            o.getAnno(),
                            o.getSerie(),
                            o.getProgressivo(),
                            null,  // rigo
                            null,  // progrGenerale
                            "status",
                            o.getStatus(),
                            newStatus,
                            "checkStatusDettaglio",
                            null
                    );
                }

                // ======== UPDATE ORIGINALE ========
                GoOrdine.update(
                        "status = :status " +
                                "WHERE anno = :anno AND serie = :serie AND progressivo = :progressivo",
                        Parameters.with("status", newStatus)
                                .and("anno", o.getAnno())
                                .and("serie", o.getSerie())
                                .and("progressivo", o.getProgressivo())
                );
            }
        }

        long fine = System.currentTimeMillis();
        Log.debug("check nuovi articoli: " + (fine - inizio) + " msec");

        // ======== FLUSH AUDIT ========
        auditService.flush();
    }


    @Transactional
    public void checkConsegnati(FiltroOrdini filtro) {
        long inizio = System.currentTimeMillis();

        List<String> statiValidi = new ArrayList<>();
        if (StringUtils.isBlank(filtro.getStatus())) {
            statiValidi.add(StatoOrdineEnum.COMPLETO.getDescrizione());
            statiValidi.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
            statiValidi.add(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
            statiValidi.add(StatoOrdineEnum.DA_ORDINARE.getDescrizione());
        } else {
            statiValidi.add(filtro.getStatus());
        }

        Map<String, Object> params = new HashMap<>();
        params.put("stati", statiValidi);
        params.put("warn", Boolean.FALSE);

        String query =
                "SELECT go.anno, go.serie, go.progressivo, go.status, go.hasProntoConsegna, go.hasCarico " +
                        "FROM GoOrdine go " +
                        "WHERE go.status IN (:stati) " +
                        "AND go.warnNoBolla = :warn " +
                        "AND NOT EXISTS ( " +
                        "    SELECT 1 FROM OrdineDettaglio o " +
                        "    WHERE o.anno = go.anno " +
                        "      AND o.serie = go.serie " +
                        "      AND o.progressivo = go.progressivo " +
                        "      AND o.tipoRigo = ' ' " +
                        "      AND o.saldoAcconto <> 'S' " +
                        ")";

        query = applyFiltersConsegna(filtro, query, params);

        long t0 = System.currentTimeMillis();
        List<GoOrdineDto> ordini = GoOrdine.find(query, params)
                .project(GoOrdineDto.class)
                .list();
        long t1 = System.currentTimeMillis();
        Log.debug("checkConsegnati - query ricerca: " + (t1 - t0) + " ms");

        if (!ordini.isEmpty()) {
            for (GoOrdineDto o : ordini) {
                // ========== AUDIT (prima della update) ==========
                String entity = "GO_ORDINE";
                Integer anno = o.getAnno();
                String serie = o.getSerie();
                Integer progressivo = o.getProgressivo();

                // 1) STATUS → ARCHIVIATO
                if (!Objects.equals(o.getStatus(), StatoOrdineEnum.ARCHIVIATO.getDescrizione())) {
                    auditService.logChange(
                            entity, anno, serie, progressivo,
                            null, null,
                            "status",
                            o.getStatus(),
                            StatoOrdineEnum.ARCHIVIATO.getDescrizione(),
                            "checkConsegnati",
                            null
                    );
                }

                // 2) hasProntoConsegna → false
                if (!Objects.equals(o.getHasProntoConsegna(), Boolean.FALSE)) {
                    auditService.logChange(
                            entity, anno, serie, progressivo,
                            null, null,
                            "hasProntoConsegna",
                            o.getHasProntoConsegna(),
                            Boolean.FALSE,
                            "checkConsegnati",
                            null
                    );
                }

                // 3) hasCarico → false
                if (!Objects.equals(o.getHasCarico(), Boolean.FALSE)) {
                    auditService.logChange(
                            entity, anno, serie, progressivo,
                            null, null,
                            "hasCarico",
                            o.getHasCarico(),
                            Boolean.FALSE,
                            "checkConsegnati",
                            null
                    );
                }

                // ========== UPDATE DB ==========

                GoOrdine.update(
                        "status = :st, hasProntoConsegna = :pc, hasCarico = :hc " +
                                "WHERE anno = :a AND serie = :s AND progressivo = :p",
                        Parameters.with("st", StatoOrdineEnum.ARCHIVIATO.getDescrizione())
                                .and("pc", Boolean.FALSE)
                                .and("hc", Boolean.FALSE)
                                .and("a", anno)
                                .and("s", serie)
                                .and("p", progressivo)
                );
            }
        }

        long fine = System.currentTimeMillis();
        Log.debug("checkConsegnati - totale: " + (fine - inizio) + " ms");
        auditService.flush();
    }


    @Transactional
    public void checkNoProntaConegna(FiltroOrdini filtro) {

        // 1) Stati da considerare
        List<String> stati = new ArrayList<>();
        if (StringUtils.isBlank(filtro.getStatus())) {
            stati.add(StatoOrdineEnum.COMPLETO.getDescrizione());
            stati.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
            stati.add(StatoOrdineEnum.DA_ORDINARE.getDescrizione());
            stati.add(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
            stati.add(StatoOrdineEnum.ARCHIVIATO.getDescrizione());
        } else {
            stati.add(filtro.getStatus());
        }

        long t0 = System.currentTimeMillis();

        String query =
                "SELECT go.anno, go.serie, go.progressivo, go.status, go.hasProntoConsegna " +
                        "FROM GoOrdine go " +
                        "WHERE go.hasProntoConsegna = TRUE " +
                        "AND EXISTS ( " +
                        "   SELECT 1 FROM GoOrdineDettaglio god " +
                        "   WHERE god.anno = go.anno " +
                        "     AND god.serie = go.serie " +
                        "     AND god.progressivo = go.progressivo " +
                        "     AND god.flProntoConsegna = FALSE " +
                        ") " +
                        "AND EXISTS ( " +
                        "   SELECT 1 FROM OrdineDettaglio o " +
                        "   WHERE o.anno = go.anno " +
                        "     AND o.serie = go.serie " +
                        "     AND o.progressivo = go.progressivo " +
                        "     AND o.tipoRigo = ' ' " +
                        ") " +
                        "AND go.status IN (:stati)";

        Map<String, Object> params = new HashMap<>();
        params.put("stati", stati);

        query = applyFiltersConsegna(filtro, query, params);

        List<GoOrdineDto> ordini = GoOrdine
                .find(query, params)
                .project(GoOrdineDto.class)
                .list();

        long t1 = System.currentTimeMillis();
        Log.debug("checkNoProntaConsegna - query: " + (t1 - t0) + " msec");

        // 3) Aggiornamento dei flag
        if (!ordini.isEmpty()) {
            for (GoOrdineDto o : ordini) {

                // ======== AUDIT ========
                if (!Objects.equals(o.getHasProntoConsegna(), Boolean.FALSE)) {
                    auditService.logChange(
                            "GO_ORDINE",
                            o.getAnno(),
                            o.getSerie(),
                            o.getProgressivo(),
                            null,               // rigo
                            null,               // progrGenerale
                            "hasProntoConsegna",
                            o.getHasProntoConsegna(),
                            Boolean.FALSE,
                            "checkNoProntaConsegna",
                            null
                    );
                }

                // ======== UPDATE ========
                GoOrdine.update(
                        "hasProntoConsegna = FALSE " +
                                "WHERE anno = :anno AND serie = :serie AND progressivo = :progressivo",
                        Parameters.with("anno", o.getAnno())
                                .and("serie", o.getSerie())
                                .and("progressivo", o.getProgressivo())
                );
            }
        }

        long t2 = System.currentTimeMillis();
        Log.debug("checkNoProntaConsegna - aggiornamento: " + (t2 - t1) + " msec");

        // ======== FLUSH ========
        auditService.flush();
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
        // 1) Totali per ordine/IVA
        List<FatturaAccontoIvaView> fatturaAccontoIvaViews = Ordine.find(
                "select o.anno, o.serie, o.progressivo, o2.fCodiceIva, " +
                        " SUM( (COALESCE(o2.prezzo,0) * COALESCE(o2.quantita,0)) * " +
                        "     (1 - COALESCE(o2.scontoArticolo, 0)/100) * " +
                        "     (1 - COALESCE(o2.scontoC1, 0)/100) * " +
                        "     (1 - COALESCE(o2.scontoC2, 0)/100) * " +
                        "     (1 - COALESCE(o2.scontoP, 0)/100) ) " +
                        "from Ordine o " +
                        " join OrdineDettaglio o2 on o.anno = o2.anno and o.serie = o2.serie and " +
                        " o.progressivo = o2.progressivo and o2.tipoRigo <> 'C' " +
                        " join GoOrdine og on og.progressivo = o.progressivo and og.serie = o.serie and o.anno = og.anno and og.status <> 'ARCHIVIATO' " +
                        "where o.contoCliente = :c " +
                        "group by o.anno, o.serie, o.progressivo, o2.fCodiceIva " +
                        "order by o.anno, o.serie, o.progressivo",
                Parameters.with("c", sottoConto)
        ).project(FatturaAccontoIvaView.class).list();

        // 2) PRE-CALCOLO: prendo tutti gli acconti, setto rif ordine, poi filtro SUBITO i NON validati
        List<AccontoDto> listaAcconto = em.createNamedQuery("AccontoDto", AccontoDto.class)
                .setParameter("sottoConto", sottoConto)
                .getResultList();

        List<AccontoDto> listaAcconti = fatturaService.settaRifOrdCliente(listaAcconto).stream()
                .filter(a -> StringUtils.isNotBlank(a.getRifOrdCliente()))                 // deve avere rif ordine
                .filter(a -> StringUtils.isNotBlank(a.getNumeroFattura()))                 // deve avere numero fattura
                .filter(a -> a.getDataFattura() != null)                                   // deve avere data fattura
                .filter(a -> StringUtils.isNotBlank(a.getIva()))                           // IVA presente
                .toList();

        // 2b) indicizzo per (ordine|iva) → lookup O(1)
        Map<String, List<AccontoDto>> accontiByOrdIva = listaAcconti.stream()
                .collect(Collectors.groupingBy(a -> a.getRifOrdCliente() + "|" + a.getIva()));

        // 3) Raggruppo i totali per anno/serie/progressivo
        Map<String, List<FatturaAccontoIvaView>> groupedMap = fatturaAccontoIvaViews.stream()
                .collect(Collectors.groupingBy(item -> item.getAnno() + "/" + item.getSerie() + "/" + item.getProgressivo()));

        List<FatturaAccontoView> result = new ArrayList<>();

        // 4) Per ogni ORDINE
        for (Map.Entry<String, List<FatturaAccontoIvaView>> entry : groupedMap.entrySet()) {
            String ordKey = entry.getKey(); // es. "2025/A/123"
            String[] keys = ordKey.split("/");
            int anno = Integer.parseInt(keys[0]);
            String serie = keys[1];
            int progressivo = Integer.parseInt(keys[2]);

            FatturaAccontoView view = new FatturaAccontoView();
            view.setAnno(anno);
            view.setSerie(serie);
            view.setProgressivo(progressivo);

            List<FatturaAccontoIvaView> ivaViews = entry.getValue();

            // 5) Per ogni IVA del medesimo ordine
            for (FatturaAccontoIvaView ivaView : ivaViews) {
                String codiceIva = ivaView.getFCodiceIva();
                double ivaPerc = NumberUtils.toDouble(codiceIva, 0d); // evita NumberFormatException

                // Acconti VALIDATI per (ordine, iva)
                List<AccontoDto> accontoDtos = accontiByOrdIva.getOrDefault(ordKey + "|" + codiceIva, Collections.emptyList());

                // Completo dati acconti (storni, residui)
                for (AccontoDto a : accontoDtos) {
                    // a.getDataFattura() e numeroFattura sono non-null per filtro precedente
                    List<AccontoDto> listaStorno = em.createNamedQuery("StornoDto", AccontoDto.class)
                            .setParameter("sottoConto", sottoConto)
                            .setParameter("numeroFattura", a.getNumeroFattura())
                            .setParameter("iva", a.getIva())
                            .setParameter("dataAcconto", fatturaService.sdf2.format(a.getDataFattura()))
                            .getResultList();

                    a.setStorni(listaStorno.stream()
                            .filter(s -> ordKey.equals(s.getOrdineCliente()))
                            .toList());

                    double sommaStorni = a.getStorni().stream()
                            .mapToDouble(AccontoDto::getPrezzo)
                            .sum();

                    double residuo = a.getPrezzo() + sommaStorni;
                    a.setImportoResiduo(residuo);
                    a.setImportoResiduoIvato(residuo + (residuo * ivaPerc / 100d));
                }
                ivaView.setAcconti(accontoDtos);

                // DDT netti per (ordine, iva)
                List<DdtNettoDto> listaDdt = em.createNamedQuery("DdtNettiPerOrdine", DdtNettoDto.class)
                        .setParameter("anno", anno)
                        .setParameter("serie", serie)
                        .setParameter("progressivo", progressivo)
                        .setParameter("fCodiceIva", codiceIva)
                        .getResultList();
                ivaView.setDdtList(listaDdt);

                double sommaDtt = listaDdt.stream().mapToDouble(DdtNettoDto::getImportoDdtNetto).sum();
                double sommaAccontiStornati = ivaView.getAcconti().stream().mapToDouble(AccontoDto::getImportoResiduo).sum();
                double sommaAccontiStornatiIvato = ivaView.getAcconti().stream().mapToDouble(AccontoDto::getImportoResiduoIvato).sum();

                double importo = ivaView.getImporto(); // già imponibile aggregato
                double residuo = (importo - sommaDtt);

                ivaView.setResiduoAcconti(sommaAccontiStornati);
                ivaView.setResiduoAccontiIvato(sommaAccontiStornatiIvato);
                ivaView.setImportoResiduo(residuo);
                ivaView.setImportoResiduoIvato(residuo + (residuo * ivaPerc / 100d));
                ivaView.setImportoIvato(importo + (importo * ivaPerc / 100d));
                ivaView.setResiduoFatturabile(ivaView.getImportoResiduo() - ivaView.getResiduoAcconti());
                ivaView.setResiduoFatturabileIvato(
                        ivaView.getResiduoFatturabile() + (ivaView.getResiduoFatturabile() * ivaPerc / 100d)
                );
            }

            view.setFatturaAccontoIvaViewList(ivaViews);
            result.add(view);
        }

        return result;
    }

    @Transactional
    public String creaFatturaAcconto(List<FatturaAccontoDto> fatturaAccontoDtoList, String user) {
        String result = null;
        try {
            Integer progressivoFatt = Fatture.find("SELECT CASE WHEN MAX(progressivo) IS NULL THEN 1 ELSE (MAX(progressivo)+1) END FROM Fatture o WHERE anno = :anno and serie = 'A'", Parameters.with("anno", Year.now().getValue())).project(Integer.class).firstResult();
            Integer progressivoFattDettaglio = FattureDettaglio.find("SELECT CASE WHEN MAX(progrGenerale) IS NULL THEN 1 ELSE (MAX(progrGenerale)+1) END FROM FattureDettaglio o").project(Integer.class).firstResult();
            Ordine ordine = Ordine.findByOrdineId(fatturaAccontoDtoList.get(0).getAnno(), fatturaAccontoDtoList.get(0).getSerie(), fatturaAccontoDtoList.get(0).getProgressivo());
            Fatture fatture = fattureMapper.buildFatturaAcconto(progressivoFatt, ordine, user);
            Fatture.persist(fatture);
            Map<String, List<FatturaAccontoDto>> mappaFattureAccontoByOrdCliente = fatturaAccontoDtoList.stream()
                    .collect(Collectors.groupingBy(item -> item.getAnno() + "/" + item.getSerie() + "/" + item.getProgressivo()));

            List<FattureDettaglio> fattureDaSalvare = new ArrayList<>();
            int rigo =1;
            for (Map.Entry<String, List<FatturaAccontoDto>> entry : mappaFattureAccontoByOrdCliente.entrySet()) {
                String[] keys = entry.getKey().split("/");
                int anno = Integer.parseInt(keys[0]);
                String serie = keys[1];
                int progressivo = Integer.parseInt(keys[2]);
                List<FatturaAccontoDto> dtoByOrdine = entry.getValue();
                FattureDettaglio fattureDettaglio;
                for (FatturaAccontoDto d : dtoByOrdine) {
                    fattureDettaglio = fattureMapper.buildFatturaAccontoDettaglio(d.getIva(), fatture,
                            progressivoFattDettaglio, rigo, user,
                            "V", "*ACC", d.isASaldo() ? "A SALDO" : "Acconto",
                            d.getNuovoAcconto(), ".", "B");
                    fattureDaSalvare.add(fattureDettaglio);
                    rigo++;
                    progressivoFattDettaglio++;
                }
                FattureDettaglio c1 = fattureMapper.buildFatturaAccontoDettaglio("", fatture, progressivoFattDettaglio, rigo, user,
                        "C", "", "Su prossima fornitura di materiale come da",
                        0D, "", "");
                fattureDaSalvare.add(c1);
                rigo++;
                progressivoFattDettaglio++;
                Ordine ord = Ordine.findByOrdineId(anno, serie, progressivo);
                String desc = "ordine n." + anno + "/" + serie + "/" + progressivo + " del " + fatturaService.sdf2.format(ord.getDataOrdine());
                FattureDettaglio c2 = fattureMapper.buildFatturaAccontoDettaglio("", fatture, progressivoFattDettaglio, rigo, user,
                        "C", "", desc,
                        0D, "", "");
                fattureDaSalvare.add(c2);
                rigo++;
                progressivoFattDettaglio++;
            }
            if(!fattureDaSalvare.isEmpty()) {
                FattureDettaglio.persist(fattureDaSalvare);
                result = StringUtils.join("Creata fattura acconto n. ", fatture.getAnno(), "/", fatture.getSerie(),
                        "/", fatture.getProgressivo());
            }
            return result;
        } catch (Exception e) {
            Log.error("Errore nella creazione della fattura di acconto: " + e.getMessage(), e);
        }
        return "Errore nella creazione della fattura di acconto";
    }
}