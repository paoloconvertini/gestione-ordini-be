package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.FiltroOrdini;
import it.calolenoci.dto.FiltroStati;
import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.entity.GoOrdine;
import it.calolenoci.entity.Ordine;
import it.calolenoci.enums.StatoOrdineEnum;
import it.calolenoci.mapper.GoOrdineMapper;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrdineService {

    @Inject
    GoOrdineMapper goOrdineMapper;
    @ConfigProperty(name = "data.inizio")
    String dataCongig;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @ConfigProperty(name = "ordini.path")
    String path;

    @Inject
    ArticoloService articoloService;

    public List<OrdineDTO> findAllByStatus(FiltroOrdini filtro) throws ParseException {
        if (!StatoOrdineEnum.DA_PROCESSARE.getDescrizione().equals(filtro.getStatus()) &&
                !StatoOrdineEnum.ARCHIVIATO.getDescrizione().equals(filtro.getStatus())) {
            checkStatusDettaglio();
        }
        if (!StatoOrdineEnum.ARCHIVIATO.getDescrizione().equals(filtro.getStatus())) {
            checkConsegnati();
            checkNoProntaConegna();
        }
        String query = " SELECT o.anno,  o.serie,  o.progressivo, o.dataConferma,  o.numeroConferma,  " +
                "p.intestazione, p.sottoConto,  p.continuaIntest,  p.indirizzo,  p.localita, p.cap,  p.provincia,  " +
                "p.statoResidenza,  p.statoEstero,  p.telefono,  p.cellulare,  p.email,  p.pec,  go.status, " +
                "go.locked, go.userLock, go.warnNoBolla, go.hasFirma, go.hasProntoConsegna, go.note " +
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
        if (StringUtils.isNotBlank(filtro.getCodVenditore())) {
            query += " and o.serie = :venditore";
            map.put("venditore", filtro.getCodVenditore());
        }
        return Ordine.find(query, Sort.descending("dataConferma"), map).project(OrdineDTO.class).list();
    }

    @Transactional
    public void checkStatusDettaglio() {
        List<String> list = new ArrayList<>();
        list.add(StatoOrdineEnum.COMPLETO.getDescrizione());
        list.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
        List<GoOrdine> ordineList = GoOrdine.findOrdiniByStatus(list);
        ordineList.forEach(o -> {
            if (articoloService.findAnyNoStatus(o.getAnno(), o.getSerie(), o.getProgressivo())) {
                o.setStatus(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
                o.persist();
            }

        });
    }

    @Transactional
    public void checkConsegnati() {
        List<String> list = new ArrayList<>();
        list.add(StatoOrdineEnum.COMPLETO.getDescrizione());
        list.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
        List<GoOrdine> ordineList = GoOrdine.findOrdiniByStatus(list);
        ordineList.forEach(o -> {
            if (articoloService.findNoConsegnati(o.getAnno(), o.getSerie(), o.getProgressivo()) && !o.getWarnNoBolla() && (o.getHasFirma() != null && o.getHasFirma())) {
                o.setStatus(StatoOrdineEnum.ARCHIVIATO.getDescrizione());
                o.persist();
            }
        });
    }

    @Transactional
    public void checkNoProntaConegna() {
        List<String> list = new ArrayList<>();
        list.add(StatoOrdineEnum.COMPLETO.getDescrizione());
        list.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
        list.add(StatoOrdineEnum.DA_ORDINARE.getDescrizione());
        list.add(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
        List<GoOrdine> ordineList = GoOrdine.findOrdiniByStatus(list);
        ordineList.forEach(o -> {
            if (articoloService.findNoProntaConsegna(o.getAnno(), o.getSerie(), o.getProgressivo())) {
                o.setHasProntoConsegna(Boolean.FALSE);
                o.persist();
            }
        });
    }

    public OrdineDTO findById(Integer anno, String serie, Integer progressivo) {
        return Ordine.find(" SELECT o.anno,  o.serie,  o.progressivo, o.dataConferma,  o.numeroConferma,  " +
                                "p.intestazione, p.sottoConto,  p.continuaIntest,  p.indirizzo,  p.localita, p.cap,  p.provincia,  " +
                                "p.statoResidenza,  p.statoEstero,  p.telefono,  p.cellulare,  p.email,  p.pec,  go.status, go.locked, go.userLock, go.warnNoBolla " +
                                "FROM Ordine o " +
                                "LEFT JOIN GoOrdine go ON o.anno = go.anno AND o.serie = go.serie AND o.progressivo = go.progressivo " +
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
        return Arrays.stream(StatoOrdineEnum.values()).map(s -> {
            if (s.getDescrizione().equals(StatoOrdineEnum.TUTTI.getDescrizione())) {
                return new FiltroStati(s.getDescrizione(), "", true);
            }
            return new FiltroStati(s.getDescrizione(), s.getDescrizione(), false);
        }).collect(Collectors.toList());
    }

    public void addNuoviOrdini() throws ParseException {
        List<Ordine> list = Ordine.find("SELECT o FROM Ordine o " +
                "WHERE o.dataConferma >= :dataConfig and o.provvisorio <> 'S' AND NOT EXISTS (SELECT 1 FROM GoOrdine god WHERE god.anno =  o.anno" +
                " AND god.serie = o.serie AND god.progressivo = o.progressivo)", Parameters.with("dataConfig", sdf.parse(dataCongig))).list();
        if (!list.isEmpty()) {
            List<GoOrdine> listToSave = new ArrayList<>();
            list.forEach(o -> listToSave.add(goOrdineMapper.fromOrdineToGoOrdine(o)));
            GoOrdine.persist(listToSave);
        }
    }
}