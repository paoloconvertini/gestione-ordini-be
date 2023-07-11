package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.AccontoDto;
import it.calolenoci.dto.FatturaDto;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.FattureDettaglio;
import it.calolenoci.entity.OrdineDettaglio;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@ApplicationScoped
public class FatturaService {

    @Inject
    EntityManager em;

    public List<OrdineDettaglioDto> getBolle()  {
        long inizio = System.currentTimeMillis();
        List<OrdineDettaglioDto> list = OrdineDettaglio.find("select o2.anno,o2.serie,o2.progressivo," +
                        " o2.progrGenerale, o2.rigo, " +
                        " (CASE WHEN o2.quantitaV IS NOT NULL AND o2.quantita <> o2.quantitaV THEN o2.quantitaV ELSE o2.quantita END ) as quantita " +
                        " from OrdineDettaglio o2" +
                        " join Ordine o ON o.anno = o2.anno AND o.serie = o2.serie AND o.progressivo = o2.progressivo " +
                        " INNER JOIN GoOrdine go ON o.anno = go.anno AND o.serie = go.serie AND o.progressivo = go.progressivo" +
                        " where (go.status <> 'ARCHIVIATO' AND go.status <> null AND go.status <> '') " +
                        " AND EXISTS (SELECT 1 FROM GoOrdineDettaglio god WHERE o.anno = god.anno AND o.serie = god.serie AND o.progressivo = god.progressivo AND o2.rigo = god.rigo)")
                .project(OrdineDettaglioDto.class).list();
        Map<Integer, Double> map = new HashMap<>();
        List<Integer> integers = list.stream().map(OrdineDettaglioDto::getProgrGenerale).toList();
        List<FatturaDto> fatturaDtos = FattureDettaglio
                .find("Select f.progrOrdCli, SUM(f.quantita) as qta " +
                                "FROM FattureDettaglio f " +
                                "WHERE f.progrOrdCli in (:list) GROUP BY f.progrOrdCli",
                        Parameters.with("list", integers))
                .project(FatturaDto.class).list();
               fatturaDtos.forEach(fatturaDto -> map.put(fatturaDto.getProgrOrdCli(), fatturaDto.getQta()));
                  list.forEach(o -> {
            if(map.containsKey(o.getProgrGenerale())){
                o.setQtaBolla(map.get(o.getProgrGenerale()));
            }
        });
        long fine = System.currentTimeMillis();
        Log.info("Query getBolle: " + (fine - inizio)/1000);
        return list;
        //return fatturaDtos;
    }

    public List<FatturaDto> getBolle(Integer progrCliente){
        return FattureDettaglio
                .find("Select f.numeroBolla, f.dataBolla, f2.quantita as qta FROM FattureDettaglio f2 " +
                        "INNER JOIN Fatture f ON f.anno = f2.anno and f.serie = f2.serie and f.progressivo = f2.progressivo " +
                        " WHERE f2.progrOrdCli = :progCliente", Parameters.with("progCliente", progrCliente))
                .project(FatturaDto.class)
                .list();
    }

    public List<AccontoDto> getAcconti(String sottoConto) {
        return em.createNamedQuery("AccontoDto").setParameter("sottoConto", sottoConto).getResultList();
    }
}
