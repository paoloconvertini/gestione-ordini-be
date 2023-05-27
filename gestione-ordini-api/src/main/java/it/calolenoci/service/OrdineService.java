package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.FiltroOrdini;
import it.calolenoci.dto.FiltroStati;
import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.entity.Ordine;
import it.calolenoci.enums.StatoOrdineEnum;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.logging.Log;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrdineService {

    @ConfigProperty(name = "data.inizio")
    String dataCongig;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @ConfigProperty(name = "ordini.path")
    String path;

    @Inject
    ArticoloService articoloService;

    public List<OrdineDTO> findAllByStatus(FiltroOrdini filtro) {
        if (!StatoOrdineEnum.DA_PROCESSARE.getDescrizione().equals(filtro.getStatus()) &&
                !StatoOrdineEnum.ARCHIVIATO.getDescrizione().equals(filtro.getStatus())) {
            checkStatusDettaglio();
        }
        if (!StatoOrdineEnum.ARCHIVIATO.getDescrizione().equals(filtro.getStatus())) {
            checkConsegnati();
        }
        String query = " SELECT o.anno,  o.serie,  o.progressivo, o.dataOrdine,  o.numeroConferma,  " +
                "p.intestazione, p.sottoConto,  p.continuaIntest,  p.indirizzo,  p.localita, p.cap,  p.provincia,  " +
                "p.statoResidenza,  p.statoEstero,  p.telefono,  p.cellulare,  p.email,  p.pec,  o.geStatus, " +
                "o.geLocked as locked, o.geUserLock as userLock, o.geWarnNoBolla as warnBolla, o.hasFirma, o.note " +
                "FROM Ordine o " +
                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto WHERE o.dataOrdine >= :dataConfig and o.provvisorio <> 'S' ";

        Map<String, Object> map = new HashMap<>();
        map.put("dataConfig", dataCongig);
        if(StringUtils.isNotBlank(filtro.getStatus())) {
            query += " AND o.geStatus = :status";
            map.put("status", filtro.getStatus());
        }
        if (StringUtils.isNotBlank(filtro.getCodVenditore())) {
            query += " and o.serie = :venditore";
            map.put("venditore", filtro.getCodVenditore());
        }
        return Ordine.find(query, Sort.descending("dataOrdine"), map).project(OrdineDTO.class).list();
    }

    @Transactional
    public void checkStatusDettaglio() {
        List<String> list = new ArrayList<>();
        list.add(StatoOrdineEnum.COMPLETO.getDescrizione());
        list.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
        List<Ordine> ordineList = Ordine.findOrdiniByStatus(list);
        ordineList.forEach(o -> {
            if (articoloService.findAnyNoStatus(o.getAnno(), o.getSerie(), o.getProgressivo())) {
                o.setGeStatus(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
                o.persist();
            }

        });
    }

    @Transactional
    public void checkConsegnati() {
        List<String> list = new ArrayList<>();
        list.add(StatoOrdineEnum.COMPLETO.getDescrizione());
        list.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
        List<Ordine> ordineList = Ordine.findOrdiniByStatus(list);
        ordineList.forEach(o -> {
            if (articoloService.findNoConsegnati(o.getAnno(), o.getSerie(), o.getProgressivo()) && !o.getGeWarnNoBolla() && o.getHasFirma()) {
                o.setGeStatus(StatoOrdineEnum.ARCHIVIATO.getDescrizione());
                String filename = "ordine_" + o.getAnno() + "_" + o.getSerie() + "_" + o.getProgressivo() + ".pdf";
                File fileSrc = new File(path + filename);
                File folderDest = new File(path + "archiviato/" + o.getAnno() + "/" + o.getSerie());
                if (folderDest.mkdirs()) {
                    try {
                        FileUtils.moveFile(fileSrc, new File(folderDest.getAbsolutePath() + "/" + filename), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                ;

                o.persist();
            }
        });
    }

    public OrdineDTO findById(Integer anno, String serie, Integer progressivo) {
        return Ordine.find(" SELECT o.anno,  o.serie,  o.progressivo, o.dataOrdine,  o.numeroConferma,  " +
                                "p.intestazione, p.sottoConto,  p.continuaIntest,  p.indirizzo,  p.localita, p.cap,  p.provincia,  " +
                                "p.statoResidenza,  p.statoEstero,  p.telefono,  p.cellulare,  p.email,  p.pec,  o.geStatus, o.geLocked as locked, o.geUserLock as userLock, o.geWarnNoBolla as warnBolla " +
                                "FROM Ordine o " +
                                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto " +
                                "WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo",
                        Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo))
                .project(OrdineDTO.class).firstResult();
    }

    @Transactional
    public void changeStatus(Integer anno, String serie, Integer progressivo, String status) {
        Ordine.update("geStatus =:stato where anno = :anno and progressivo = :progressivo and serie = :serie",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)
                        .and("stato", status));
    }

    @Transactional
    public void updateStatus(String stato) throws ParseException {
        Ordine.update("geStatus = :stato WHERE (geStatus is null OR geStatus = '') and dataOrdine >= :dataConfig and provvisorio <> 'S'",
                Parameters.with("dataConfig", sdf.parse(dataCongig)).and("stato", stato));
    }

    public List<OrdineDTO> findAllNuoviOrdini() throws ParseException {
        return Ordine.find("SELECT o.anno, o.serie, o.progressivo FROM Ordine o WHERE (o.geStatus is null OR o.geStatus = '') and o.dataOrdine >= :dataConfig and o.provvisorio <> 'S'",
                Parameters.with("dataConfig", sdf.parse(dataCongig))).project(OrdineDTO.class).list();
    }

    public List<FiltroStati> getStati() {
        return Arrays.stream(StatoOrdineEnum.values()).map(s -> {
            if (s.getDescrizione().equals(StatoOrdineEnum.TUTTI.getDescrizione())) {
                return new FiltroStati(s.getDescrizione(), "", true);
            }
            return new FiltroStati(s.getDescrizione(), s.getDescrizione(), false);
        }) .collect(Collectors.toList());
    }
}