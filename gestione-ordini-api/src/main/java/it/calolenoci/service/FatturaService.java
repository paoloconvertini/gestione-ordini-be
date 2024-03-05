package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.AccontoDto;
import it.calolenoci.dto.FatturaDto;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.*;
import it.calolenoci.mapper.FattureMapper;
import it.calolenoci.mapper.MagazzinoMapper;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class FatturaService {

    @Inject
    EntityManager em;

    @Inject
    FattureMapper fattureMapper;

    @Inject
    MagazzinoMapper magazzinoMapper;

    public List<OrdineDettaglioDto> getBolle() {
        long inizio = System.currentTimeMillis();
        List<OrdineDettaglioDto> list = OrdineDettaglio.find("select o2.anno,o2.serie,o2.progressivo," +
                        " o2.progrGenerale, o2.rigo, " +
                        " (CASE WHEN o2.quantitaV IS NOT NULL AND o2.quantita <> o2.quantitaV THEN o2.quantitaV ELSE o2.quantita END ) as quantita " +
                        " from OrdineDettaglio o2" +
                        " join Ordine o ON o.anno = o2.anno AND o.serie = o2.serie AND o.progressivo = o2.progressivo " +
                        " INNER JOIN GoOrdine go ON o.anno = go.anno AND o.serie = go.serie AND o.progressivo = go.progressivo" +
                        " where (go.status <> 'ARCHIVIATO' OR go.status <> null OR go.status <> '') " +
                        " AND EXISTS (SELECT 1 FROM GoOrdineDettaglio god WHERE o2.progrGenerale = god.progrGenerale )"
                          //      + "AND o2.fArticolo = god.fArticolo)"
                )
                .project(OrdineDettaglioDto.class).list();
        Map<Integer, Double> map = new HashMap<>();
        List<Integer> integers = list.stream().map(OrdineDettaglioDto::getProgrGenerale).toList();
        Log.debug("getBolle: Trovati " + integers.size() + " integers");
        if (integers.size() >= 1000) {
            List<List<Integer>> partition = ListUtils.partition(integers, 1000);
            for (List<Integer> integerList : partition) {
                List<FatturaDto> fatturas = FattureDettaglio
                        .find("Select f.progrOrdCli, SUM(f.quantita) as qta " +
                                        "FROM FattureDettaglio f " +
                                        "WHERE f.progrOrdCli in (:list) GROUP BY f.progrOrdCli",
                                Parameters.with("list", integerList))
                        .project(FatturaDto.class).list();
                fatturas.forEach(fatturaDto -> map.put(fatturaDto.getProgrOrdCli(), fatturaDto.getQta()));
            }
        } else {
            List<FatturaDto> fatturaDtos = FattureDettaglio
                    .find("Select f.progrOrdCli, SUM(f.quantita) as qta " +
                                    "FROM FattureDettaglio f " +
                                    "WHERE f.progrOrdCli in (:list) GROUP BY f.progrOrdCli",
                            Parameters.with("list", integers))
                    .project(FatturaDto.class).list();
            fatturaDtos.forEach(fatturaDto -> map.put(fatturaDto.getProgrOrdCli(), fatturaDto.getQta()));
        }
        list.forEach(o -> {
            if (map.containsKey(o.getProgrGenerale())) {
                o.setQtaBolla(map.get(o.getProgrGenerale()));
            }
        });
        long fine = System.currentTimeMillis();
        Log.debug("Query getBolle: " + (fine - inizio) + " msec");
        return list;
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
        return getAcconti(sottoConto, null);
    }

    public List<AccontoDto> getAcconti(String sottoConto, List<OrdineDettaglioDto> lista) {
        List<AccontoDto> resultList = new ArrayList<>();
        List<AccontoDto> listaAcconto = em.createNamedQuery("AccontoDto").setParameter("sottoConto", sottoConto).getResultList();
        List<AccontoDto> listaStorno = em.createNamedQuery("StornoDto").setParameter("sottoConto", sottoConto).getResultList();
        Log.debug("Lista storni: " + listaStorno.size());
        if (listaAcconto.isEmpty() && listaStorno.isEmpty()) {
            Log.debug("Entrambe le liste sono vuote");
            return resultList;
        }
        if (listaAcconto.isEmpty()) {
            Log.debug("La lista acconti è vuota");
            return listaStorno;
        } else {
            List<AccontoDto> listaAcconti = settaRifOrdCliente(listaAcconto, lista);
            for (AccontoDto a : listaAcconti) {
                a.setUuidAS("" + UUID.randomUUID());
                resultList.add(a);
                for (AccontoDto s : listaStorno) {
                    Log.debug("Acconto n." + a.getNumeroFattura() + " contiene storno operazione " + s.getOperazione() + "? " + StringUtils.contains(s.getOperazione(), StringUtils.trim(a.getNumeroFattura())));
                    if (StringUtils.contains(s.getOperazione(), StringUtils.trim(a.getNumeroFattura()))) {
                        a.getRifOrdClienteList().forEach(r -> {
                            String ordCli2 = StringUtils.replace(s.getOrdineCliente(), "/", ".");
                            String ordCli3 = StringUtils.replace(s.getOrdineCliente(), "/", "-");
                            if (StringUtils.contains(r, s.getOrdineCliente()) ||
                                    StringUtils.contains(r, ordCli2)
                            || StringUtils.contains(r, ordCli3)) {
                                List<String> rifList = new ArrayList<>();
                                rifList.add(s.getRifOrdCliente());
                                s.setRifOrdClienteList(rifList);
                                s.setUuidAS("" + a.getUuidAS());
                                Log.debug("AccontoDto :" + s);
                                resultList.add(s);
                            }
                        });
                    }
                }
            }
        }

        if (lista != null && !lista.isEmpty()) {
            List<AccontoDto> accontoDtoList = new ArrayList<>();
            Map<String, List<AccontoDto>> mapAccontoStorno = resultList.stream().collect(Collectors.groupingBy(AccontoDto::getUuidAS));
            Log.debug("Mappa acconti storno size: " + mapAccontoStorno.size());
            for (String uuid : mapAccontoStorno.keySet()) {
                double sum = mapAccontoStorno.get(uuid).stream().mapToDouble(AccontoDto::getPrezzo).sum();
                Log.debug("Somma saldo " + uuid + ":" + sum);
                if (sum > 0) {
                    AccontoDto dto = mapAccontoStorno.get(uuid).stream().filter(a -> a.getPrezzo() > 0).findFirst().get();
                    dto.setPrezzo(sum);
                    accontoDtoList.add(dto);
                }
            }
            return accontoDtoList;
        }

        return resultList;
    }

    @Transactional
    public String creaBolla(List<OrdineDettaglioDto> list, List<AccontoDto> accontoDtos, String user) {
        String result = null;
        try {
            Integer progressivoFatt = OrdineFornitore.find("SELECT CASE WHEN MAX(progressivo) IS NULL THEN 0 ELSE MAX(progressivo) END FROM Fatture o WHERE anno = :anno and serie = 'B'", Parameters.with("anno", Year.now().getValue())).project(Integer.class).firstResult();
            Integer progressivoFattDettaglio = OrdineFornitoreDettaglio.find("SELECT CASE WHEN MAX(progrGenerale) IS NULL THEN 0 ELSE MAX(progrGenerale) END FROM FattureDettaglio o").project(Integer.class).firstResult();
            Ordine ordine = Ordine.findByOrdineId(list.get(0).getAnno(), list.get(0).getSerie(), list.get(0).getProgressivo());
            Fatture f = fattureMapper.buildFatture(progressivoFatt, ordine);
            Log.debug("*** CREA BOLLA --- creata fattura n. " + f.getAnno() + "/" + f.getSerie() + "/" + f.getProgressivo());
            f.persist();
            Map<OrdineId, List<OrdineDettaglioDto>> map = list.stream().collect(Collectors.groupingBy(o ->
                    new OrdineId(o.getAnno(), o.getSerie(), o.getProgressivo())));

            Log.debug("*** CREA BOLLA --- mappa lista ordine dettaglio: " + map.size());
            for (OrdineId id : map.keySet()) {
                Log.debug("*** CREA BOLLA, ciclio sulla mappa --- ordine n. " + id.getAnno() + "/" + id.getSerie() + "/" + id.getProgressivo());
                if (accontoDtos != null && !accontoDtos.isEmpty()) {
                    Log.debug("*** CREA BOLLA, acconti selezionati: " + accontoDtos.size());
                    final List<OrdineDettaglioDto> dtos = map.get(id);
                    accontoDtos.stream().filter(a -> AccontoDto.checkOrdineEsiste(a, id)).toList()
                            .forEach(a -> {
                                OrdineDettaglioDto dto = fattureMapper.fromAccontoToOrdineDettaglio(a, id);
                                dtos.add(0, dto);
                                Log.debug("*** CREA BOLLA, creata voce storno: " + dto.getFDescrArticolo());
                            });
                }
            }

            List<OrdineDettaglioDto> listaDaTrasformare = new ArrayList<>();
            map.values().forEach(listaDaTrasformare::addAll);

            List<FattureDettaglio> fattureDaSalvare = new ArrayList<>();
            List<OrdineDettaglio> ordineDettaglioList = new ArrayList<>();
            List<Magazzino> magazzinoList = new ArrayList<>();
            List<SaldiMagazzino> saldiMagazzinoList = new ArrayList<>();
            List<GoTmpScarico> goTmpScaricoList = new ArrayList<>();
            FattureDettaglio fd;
            Integer progressivo = Magazzino.find("select ISNULL(MAX(m.magazzinoId.progressivo)+1, 1) from Magazzino m WHERE m.magazzinoId.anno=:anno and m.magazzinoId.serie = 'B'",
                    Parameters.with("anno", Year.now().getValue())).project(Integer.class).firstResult();
            Log.debug("*** CREA BOLLA, Magazzino progressivo: " + progressivo);
            Integer progressivoGen = Magazzino.find("select MAX(m.progrgenerale) from Magazzino m").project(Integer.class).firstResult();
            Log.debug("*** CREA BOLLA, Magazzino progressivo generale: " + progressivoGen);
            for (int i = 0; i < listaDaTrasformare.size(); i++) {
                Log.debug("*** CREA BOLLA, lista da trasformare: " + listaDaTrasformare.size());
                OrdineDettaglioDto dto = listaDaTrasformare.get(i);
                Log.debug("*** CREA BOLLA, dto della lista da trasformare : " + dto.getAnno() + "/" + dto.getSerie() + "/" + dto.getProgressivo()
                + ", articolo: " + dto.getFArticolo());
                Magazzino m;
                if (StringUtils.containsIgnoreCase(dto.getFDescrArticolo(), "Storno")) {
                    Log.debug("*** CREA BOLLA, lista da trasformare, riga storno : " + dto.getRigo());
                            fd = fattureMapper.buildStorno(dto, f, progressivoFattDettaglio, i, user);
                    MagazzinoId id = new MagazzinoId(Year.now().getValue(), "B", progressivo, " ", i+1);
                    m = magazzinoMapper.buildMagazzino(id, ++progressivoGen, fd, f, ordine);
                    Optional<Magazzino> opt = Magazzino.find("progrgenerale = :p", Parameters.with("p", m.getProgrgenerale())).singleResultOptional();
                    if (opt.isPresent()) {
                        Log.error("*** CREA BOLLA, Errore trovato record con progrGen: " + progressivoGen);
                    }
                } else {
                    Log.debug("*** CREA BOLLA, lista da trasformare, riga articolo : " + dto.getRigo());
                    OrdineDettaglio o = OrdineDettaglio.getById(dto.getAnno(), dto.getSerie(), dto.getProgressivo(), dto.getRigo());
                    fd = fattureMapper.buildFattureDettaglio(dto, f, o, progressivoFattDettaglio, i, user);
                    o.setSaldoAcconto("S");
                    ordineDettaglioList.add(o);
                    Optional<SaldiMagazzino> optional = SaldiMagazzino.find("marticolo =:art and  mmagazzino = :mag",
                            Parameters.with("art", o.getFArticolo()).and("mag", o.getMagazz())).firstResultOptional();
                    if (optional.isPresent()) {
                        Log.debug("*** CREA BOLLA, TmpScarico creato per articolo: " + o.getFArticolo() + ". Qta: " + o.getQuantita());
                        SaldiMagazzino saldiMagazzino = optional.get();
                        Double qtaScarico = (saldiMagazzino.getQscarichi()==null?0: saldiMagazzino.getQscarichi()) + (o.getQuantita()==null?0:o.getQuantita());
                        qtaScarico = Math.round(qtaScarico * 100.0) / 100.0;
                        Double qtaGiacenza = (saldiMagazzino.getQcarichi()==null?0: saldiMagazzino.getQcarichi()) - qtaScarico;
                        saldiMagazzino.setQscarichi(qtaScarico);
                        saldiMagazzino.setQgiacenza(qtaGiacenza);
                        saldiMagazzinoList.add(saldiMagazzino);
                    } else {
                        if(StringUtils.equals(o.getTipoRigo(), "" ) || StringUtils.equals(o.getTipoRigo(), " ")) {
                            GoTmpScarico goTmpScarico = new GoTmpScarico();
                            goTmpScarico.setId(new GoTmpScaricoPK(o.getFArticolo(), o.getMagazz(), fd.getProgrGenerale()));
                            goTmpScarico.setAttivo(Boolean.TRUE);
                            goTmpScaricoList.add(goTmpScarico);
                            Log.debug("*** CREA BOLLA, TmpScarico creato per articolo: " + o.getFArticolo() + ". Qta: " + o.getQuantita());
                        }

                    }

                    MagazzinoId id = new MagazzinoId(Year.now().getValue(), "B", progressivo, " ", i+1);
                    m = magazzinoMapper.buildMagazzino(id, ++progressivoGen, o, fd, f, ordine);
                    Optional<Magazzino> opt = Magazzino.find("progrgenerale = :p", Parameters.with("p", m.getProgrgenerale())).singleResultOptional();
                    if (opt.isPresent()) {
                        Log.error("*** CREA BOLLA, Errore trovato record con progrGen: " + progressivoGen);
                    }

                }
                magazzinoList.add(m);
                fattureDaSalvare.add(fd);
            }
            Log.debug("*** CREA BOLLA, fatture da salvare: " + fattureDaSalvare.size());
            FattureDettaglio.persist(fattureDaSalvare);
            OrdineDettaglio.persist(ordineDettaglioList);
            if (!magazzinoList.isEmpty()) {
                Magazzino.persist(magazzinoList);
            }
            if (!saldiMagazzinoList.isEmpty()) {
                SaldiMagazzino.persist(saldiMagazzinoList);
            }
            if(!goTmpScaricoList.isEmpty()){
                GoTmpScarico.persist(goTmpScaricoList);
            }
            result = StringUtils.join("Creata bolla n. ", f.getAnno(), "/", f.getSerie(), "/", f.getProgressivo());
        } catch (Exception e) {
            Log.error("Errore nella creazione della bolla: " + e.getMessage(), e);
        }
        return result;
    }

    private List<AccontoDto> settaRifOrdCliente(List<AccontoDto> listaAcconto, List<OrdineDettaglioDto> lista) {
        boolean check = lista != null;
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
                        checkOrdCli(lista, check, rifCliList, rigaFattura);
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
                        checkOrdCli(lista, check, rifCliList, rigaFattura);
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
                        checkOrdCli(lista, check, rifCliList, rigaFattura);
                    }
                }
                righeFattura.get(rowACC).setRifOrdClienteList(rifCliList);
            }
        }

        final List<AccontoDto> listaAcconti = new ArrayList<>();
        mapByNumFatt.values().forEach(list -> listaAcconti.addAll(list.stream().filter(a -> a.getRifOrdClienteList() != null && !a.getRifOrdClienteList().isEmpty()).toList()));

        listaAcconti.sort(Comparator.comparing(AccontoDto::getDataFattura));
        Log.debug("Acconti post elaborazione: " + listaAcconti.size());
        return listaAcconti;
    }

    private void checkOrdCli(List<OrdineDettaglioDto> list, boolean check, List<String> rifCliList, AccontoDto rigaFattura) {
        if (check) {
            for (OrdineDettaglioDto l : list) {
                if (StringUtils.containsIgnoreCase(rigaFattura.getOperazione(), StringUtils.join(l.getAnno(), "/", l.getSerie(), "/", l.getProgressivo()))
                || StringUtils.containsIgnoreCase(rigaFattura.getOperazione(), StringUtils.join(l.getAnno(), "-", l.getSerie(), "-", l.getProgressivo()))) {
                    rifCliList.add(rigaFattura.getOperazione());
                    break;
                }
            }
        } else {
            rifCliList.add(rigaFattura.getOperazione());
        }
    }
}
