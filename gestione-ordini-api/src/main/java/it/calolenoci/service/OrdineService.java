package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.entity.Ordine;
import it.calolenoci.enums.StatoOrdineEnum;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class OrdineService {

    @ConfigProperty(name = "data.inizio")
    String dataCongig;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Inject
    ArticoloService articoloService;

    public List<OrdineDTO> findAllByStatus(String status) throws ParseException {
        if(!StatoOrdineEnum.DA_PROCESSARE.getDescrizione().equals(status) &&
                !StatoOrdineEnum.ARCHIVIATO.getDescrizione().equals(status)) {
            checkStatusDettaglio();
        }
        if(StatoOrdineEnum.ARCHIVIATO.getDescrizione().equals(status)) {
            checkConsegnati();
        }
        String query = " SELECT o.anno,  o.serie,  o.progressivo, o.dataOrdine,  o.numeroConferma,  " +
                "p.intestazione,  p.sottoConto, p.continuaIntest,  p.indirizzo,  p.localita, p.cap,  p.provincia,  " +
                "p.statoResidenza,  p.statoEstero,  p.telefono,  p.cellulare,  p.email,  p.pec,  o.geStatus, " +
                "o.geLocked as locked, o.geUserLock as userLock, o.geWarnNoBolla as warnBolla " +
                "FROM Ordine o " +
                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto ";
        if(StringUtils.isBlank(status)) {
            query += "WHERE o.geStatus is null and o.dataOrdine >= :dataCongig  and o.provvisorio <> 'S' ";
            return Ordine.find(query, Sort.descending("dataOrdine"), Parameters.with("dataCongig", sdf.parse(dataCongig)))
                    .project(OrdineDTO.class).list();
        } else {
            query += "WHERE o.geStatus = :status";
            return Ordine.find(query, Sort.descending("dataOrdine"), Parameters.with("status", status))
                    .project(OrdineDTO.class).list();
        }
    }

    public List<OrdineDTO> findAllByStatus(String status, String venditore) throws ParseException {
        if(!StatoOrdineEnum.DA_PROCESSARE.getDescrizione().equals(status) &&
                !StatoOrdineEnum.ARCHIVIATO.getDescrizione().equals(status)) {
            checkStatusDettaglio();
        }
        if(StatoOrdineEnum.ARCHIVIATO.getDescrizione().equals(status)) {
            checkConsegnati();
        }
        String query = " SELECT o.anno,  o.serie,  o.progressivo, o.dataOrdine,  o.numeroConferma,  " +
                "p.intestazione, p.sottoConto,  p.continuaIntest,  p.indirizzo,  p.localita, p.cap,  p.provincia,  " +
                "p.statoResidenza,  p.statoEstero,  p.telefono,  p.cellulare,  p.email,  p.pec,  o.geStatus, " +
                "o.geLocked as locked, o.geUserLock as userLock, o.geWarnNoBolla as warnBolla " +
                "FROM Ordine o " +
                "JOIN PianoConti p ON o.gruppoCliente = p.gruppoConto AND o.contoCliente = p.sottoConto ";
        if(StringUtils.isBlank(status)) {
            query += "WHERE o.geStatus is null and o.dataOrdine >= :dataConfig and o.provvisorio <> 'S' and o.serie = :venditore" ;
            return Ordine.find(query, Sort.descending("dataOrdine"), Parameters.with("dataConfig", sdf.parse(dataCongig))
                            .and("venditore", venditore)).project(OrdineDTO.class).list();
        } else {
            query += "WHERE o.geStatus = :status and o.serie = :venditore";
            return Ordine.find(query, Sort.descending("dataOrdine"), Parameters.with("status", status)
                    .and("venditore", venditore)).project(OrdineDTO.class).list();
        }
    }

    @Transactional
    public void checkStatusDettaglio() {
        List<String> list = new ArrayList<>();
        list.add(StatoOrdineEnum.COMPLETO.getDescrizione());
        list.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
        List<Ordine> ordineList = Ordine.findOrdiniByStatus(list);
        ordineList.forEach(o-> {
            if(articoloService.findAnyNoStatus(o.getAnno(), o.getSerie(), o.getProgressivo())) {
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
        ordineList.forEach(o-> {
            if(articoloService.findNoConsegnati(o.getAnno(), o.getSerie(), o.getProgressivo())) {
                o.setGeStatus(StatoOrdineEnum.ARCHIVIATO.getDescrizione());
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

}
