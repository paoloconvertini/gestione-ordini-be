package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.AccontoDto;
import it.calolenoci.dto.FatturaDto;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.FattureDettaglio;
import it.calolenoci.entity.OrdineDettaglio;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@ApplicationScoped
public class FatturaService {

    @Inject
    EntityManager em;

    public List<OrdineDettaglioDto> getBolle() {
        long inizio = System.currentTimeMillis();
        List<OrdineDettaglioDto> list = OrdineDettaglio.find("select o2.anno,o2.serie,o2.progressivo," +
                        " o2.progrGenerale, o2.rigo, " +
                        " (CASE WHEN o2.quantitaV IS NOT NULL AND o2.quantita <> o2.quantitaV THEN o2.quantitaV ELSE o2.quantita END ) as quantita " +
                        " from OrdineDettaglio o2" +
                        " join Ordine o ON o.anno = o2.anno AND o.serie = o2.serie AND o.progressivo = o2.progressivo " +
                        " INNER JOIN GoOrdine go ON o.anno = go.anno AND o.serie = go.serie AND o.progressivo = go.progressivo" +
                        " where (go.status <> 'ARCHIVIATO' AND go.status <> null AND go.status <> '') " +
                        " AND EXISTS (SELECT 1 FROM GoOrdineDettaglio god WHERE o2.progrGenerale = god.progrGenerale)")
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
            if (map.containsKey(o.getProgrGenerale())) {
                o.setQtaBolla(map.get(o.getProgrGenerale()));
            }
        });
        long fine = System.currentTimeMillis();
        Log.debug("Query getBolle: " + (fine - inizio) + " msec");
        return list;
        //return fatturaDtos;
    }

    public List<FatturaDto> getBolle(Integer progrCliente) {
        return FattureDettaglio
                .find("Select f.numeroBolla, f.dataBolla, f2.quantita as qta FROM FattureDettaglio f2 " +
                        "INNER JOIN Fatture f ON f.anno = f2.anno and f.serie = f2.serie and f.progressivo = f2.progressivo " +
                        " WHERE f2.progrOrdCli = :progCliente", Parameters.with("progCliente", progrCliente))
                .project(FatturaDto.class)
                .list();
    }

    public List<AccontoDto> getAcconti(String sottoConto) {
        List<AccontoDto> resultList = new ArrayList<>();
        List<AccontoDto> listaAcconto = em.createNamedQuery("AccontoDto").setParameter("sottoConto", sottoConto).getResultList();
        List<AccontoDto> listaStorno = em.createNamedQuery("StornoDto").setParameter("sottoConto", sottoConto).getResultList();

        if (listaAcconto.isEmpty() && listaStorno.isEmpty()) {
            Log.debug("Entrambe le liste sono vuote");
            return resultList;
        }
        if (listaAcconto.isEmpty()) {
            Log.debug("La lista acconti è vuota");
            return listaStorno;
        } else {
            Map<String, List<AccontoDto>> mapByNumFatt = listaAcconto.stream().collect(Collectors.groupingBy(AccontoDto::getNumeroFattura));
            Log.debug("Lista acconti size: " + mapByNumFatt.size());
            for (String numFatt : mapByNumFatt.keySet()) {
                int rowACC = 0;
                //ciclo sulle righe della singola fattura
                List<AccontoDto> righeFattura = mapByNumFatt.get(numFatt);
                if (righeFattura.isEmpty()) {
                    continue;
                }
                long acc = righeFattura.stream().filter(a -> StringUtils.equals("*ACC", a.getFArticolo())).count();
                long ord = righeFattura.stream().filter(a -> StringUtils.containsIgnoreCase(a.getOperazione(), "ord")).count();
                // situazione con acconti e ordine cliente uguale
                if ((acc == ord)) {
                    Log.debug("situazione con acconti e ordine cliente uguale per fattura " + numFatt);
                    for (int i = 0; i < righeFattura.size(); i++) {
                        AccontoDto rigaFattura = righeFattura.get(i);
                        if (StringUtils.equals("*ACC", rigaFattura.getFArticolo())) {
                            rowACC = i;
                            continue;
                        }
                        if (i == rowACC + 2) {
                            List<String> rifCliList = new ArrayList<>();
                            rifCliList.add(rigaFattura.getOperazione());
                            righeFattura.get(rowACC).setRifOrdClienteList(rifCliList);
                        }
                    }
                    // situazione con 2 acconti e unico ordine cliente
                } else if (acc > 1 && ord == 1) {
                    List<Integer> rowACCs = new ArrayList<>();
                    List<String> rifCliList = new ArrayList<>();
                    Log.debug("situazione con 2 acconti e unico ordine cliente per fattura " + numFatt);
                    for (int i = 0; i < righeFattura.size(); i++) {
                        AccontoDto rigaFattura = righeFattura.get(i);
                        if (StringUtils.equals("*ACC", rigaFattura.getFArticolo())) {
                            rowACCs.add(i);
                            continue;
                        }
                        if (StringUtils.containsIgnoreCase(rigaFattura.getOperazione(), "ord")) {
                            rifCliList.add(rigaFattura.getOperazione());
                        }
                    }
                    rowACCs.forEach(r -> righeFattura.get(r).setRifOrdClienteList(rifCliList));
                } else if (acc == 1 && ord > 1) {
                    List<String> rifCliList = new ArrayList<>();
                    Log.debug("situazione con unico acconto e più ordini clienti per fattura " + numFatt);
                    for (int i = 0; i < righeFattura.size(); i++) {
                        AccontoDto rigaFattura = righeFattura.get(i);
                        if (StringUtils.equals("*ACC", rigaFattura.getFArticolo())) {
                            rowACC = i;
                            continue;
                        }
                        if (StringUtils.containsIgnoreCase(rigaFattura.getOperazione(), "ord")) {
                            rifCliList.add(rigaFattura.getOperazione());
                        }
                    }
                    righeFattura.get(rowACC).setRifOrdClienteList(rifCliList);
                }
            }

            final List<AccontoDto> listaAcconti = new ArrayList<>();
            mapByNumFatt.values().forEach(list -> listaAcconti.addAll(list.stream().filter(a -> a.getRifOrdClienteList() !=null && !a.getRifOrdClienteList().isEmpty()).toList()));

            listaAcconti.sort(Comparator.comparing(AccontoDto::getDataFattura));
            Log.debug("Acconti post elaborazione: " +listaAcconti.size());
            Log.debug("Lista storni: " + listaStorno.size());
            for (AccontoDto a : listaAcconti) {
                resultList.add(a);
                for (AccontoDto s : listaStorno) {
                    Log.debug("Acconto n." + a.getNumeroFattura() + " contiene storno operazione " + s.getOperazione() + "? " + StringUtils.contains(s.getOperazione(), a.getNumeroFattura()));
                    if (StringUtils.contains(s.getOperazione(), a.getNumeroFattura())) {
                        a.getRifOrdClienteList().forEach(r -> {
                            String ordCli2 = StringUtils.replace(s.getOrdineCliente(), "/", ".");
                            if(StringUtils.contains(r, s.getOrdineCliente()) ||
                                    StringUtils.contains(r, ordCli2)) {
                                List<String> rifList = new ArrayList<>();
                                rifList.add(s.getRifOrdCliente());
                                s.setRifOrdClienteList(rifList);
                                resultList.add(s);
                            }});
                    }
                }
            }
        }

        return resultList;
    }
}
