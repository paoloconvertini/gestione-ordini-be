package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import it.calolenoci.mapper.FattureMapper;
import it.calolenoci.mapper.MagazzinoMapper;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;


@ApplicationScoped
public class FatturaService {

    // Esempi validi:
    // "ordine n 2025/AB/123", "ord. n. 2025-AB-123", "ORD: 2025/ab/123",
    // "n 2025/AB/123", "n. 2025-AB-123", "n° 2025/AB/123", "nº 2025/AB/123"
    private static final Pattern ORDER_WORDY = Pattern.compile(
            "(?i)(?:\\b(?:ns|nostr[oi])\\.?\\s*)?" +        // opz. ns / nostro
                    "(?:conf(?:\\.|erma)\\s*)?" +                   // opz. conf./conferma
                    "ord(?:ine)?\\s*" +                             // ord / ordine
                    "(?:n[°.\\s]*)?" +                              // opz. n / n. / n°
                    "(\\d{4})\\s*[/\\-\\s]\\s*([A-Z0-9]+)\\s*[/\\-\\s]\\s*(\\d+)"
    );

    private static final Pattern ORDER_TRIPLE = Pattern.compile(
            "(?i)\\b(\\d{4})\\s*[/\\-\\s]\\s*([A-Z0-9]+)\\s*[/\\-\\s]\\s*(\\d+)\\b"
    );

    @Inject
    EntityManager em;

    @ConfigProperty(name = "data.inizio")
    String dataCongig;

    public SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");

    public SimpleDateFormat annoYY = new SimpleDateFormat("dd/MM/yy");

    @Inject
    FattureMapper fattureMapper;

    @Inject
    MagazzinoMapper magazzinoMapper;

    public List<OrdineDettaglioDto> getBolle() {
        try {
            long inizio = System.currentTimeMillis();
            LocalDate data = LocalDate.parse(dataCongig);

            // 1) Prelevo i righi degli ordini che hanno fatture associate
            List<OrdineDettaglioDto> list = OrdineDettaglio.find(
                            "select o2.anno, o2.serie, o2.progressivo, " +
                                    "o2.progrGenerale, o2.rigo, " +
                                    "(CASE WHEN o2.quantitaV IS NOT NULL AND o2.quantita <> o2.quantitaV " +
                                    "      THEN o2.quantitaV ELSE o2.quantita END) as quantita, " +
                                    "SUM(COALESCE(f.quantita,0)) as qtaBolla " +
                                    "from OrdineDettaglio o2 " +
                                    "join Ordine o ON o.anno = o2.anno AND o.serie = o2.serie AND o.progressivo = o2.progressivo " +
                                    "join GoOrdine go ON go.anno = o.anno AND go.serie = o.serie AND go.progressivo = o.progressivo " +
                                    "join FattureDettaglio f ON f.progrOrdCli = o2.progrGenerale " +
                                    "where go.status <> 'ARCHIVIATO' " +
                                    "and o.dataConferma >= :data " +
                                    "and exists (select 1 from GoOrdineDettaglio god where god.progrGenerale = o2.progrGenerale) " +
                                    "group by o2.anno, o2.serie, o2.progressivo, " +
                                    "o2.progrGenerale, o2.rigo, o2.quantita, o2.quantitaV",
                            Parameters.with("data", data)
                    )
                    .project(OrdineDettaglioDto.class)
                    .list();

            if (list.isEmpty()) {
                Log.debug("Nessuna bolla trovata");
                return list;
            }

            Log.debug("Trovate " + list.size() + " bolle");
            long fine = System.currentTimeMillis();
            Log.debug("Query getBolle ottimizzata: " + (fine - inizio) + " ms");
            return list;
        } catch (Exception e) {
            Log.error("Errore getBolle ", e);
            return new ArrayList<>();
        }
    }

    public List<FatturaDto> getBolle(Integer progrCliente) {
        return FattureDettaglio
                .find("Select f.numeroBolla, f.dataBolla, f2.quantita as qta FROM FattureDettaglio f2 " +
                        "INNER JOIN Fatture f ON f.anno = f2.anno and f.serie = f2.serie and f.progressivo = f2.progressivo " +
                        " WHERE f2.progrOrdCli = :progCliente", Parameters.with("progCliente", progrCliente))
                .project(FatturaDto.class)
                .list();
    }

    @TransactionConfiguration(timeout = 50000)
    public List<AccontoDto> getAcconti(String sottoConto) {
        List<AccontoDto> resultList = new ArrayList<>();
        List<AccontoDto> listaAcconto = em.createNamedQuery("AccontoDto").setParameter("sottoConto", sottoConto).getResultList();
        if (listaAcconto.isEmpty()) {
            Log.debug("La lista acconti è vuota");
            return resultList;
        } else {
            List<AccontoDto> listaAcconti = settaRifOrdCliente(listaAcconto).stream()
                    .filter(a -> StringUtils.isNotBlank(a.getNumeroFattura()) && a.getDataFattura() != null)
                    .toList();

            // ✅ deduplica subito
            return getAccontoDtos(sottoConto, resultList, new ArrayList<>(new HashSet<>(listaAcconti)));

        }
    }

    @TransactionConfiguration(timeout = 50000)
    public List<AccontoDto> getAccontiPerOrdiniClienti(String sottoConto, List<OrdineDettaglioDto> lista) {
        List<AccontoDto> resultList = new ArrayList<>();
        List<AccontoDto> listaAcconti;
        Set<AccontoDto> listaDto = new HashSet<>();
        List<AccontoDto> listaAcconto = em.createNamedQuery("AccontoDto").setParameter("sottoConto", sottoConto).getResultList();
        if (listaAcconto.isEmpty()) {
            Log.debug("La lista acconti è vuota");
            return resultList;
        } else {
            listaAcconti = settaRifOrdCliente(listaAcconto);

            listaAcconti = listaAcconti.stream()
                    .filter(a -> StringUtils.isNotBlank(a.getNumeroFattura()) && a.getDataFattura() != null)
                    .toList();
            for (AccontoDto a : listaAcconti) {
                for (OrdineDettaglioDto o : lista) {
                    if(a.getRifOrdCliente().equals(StringUtils.join(o.getAnno(), "/", o.getSerie(), "/", o.getProgressivo()))
                            && a.getIva().equals(o.getFCodiceIva())){
                        listaDto.add(a);
                    }
                }
            }

        }

        if (listaDto.isEmpty()) {
            Log.debug("La lista acconti è vuota");
            return resultList;
        } else {
            return getAccontoDtos(sottoConto, resultList, new ArrayList<>(listaDto)).stream()
                    .filter(a -> a.getImportoResiduo() > 0)
                    .toList();
        }

    }

    // dentro FatturaService

    public long countAccontiNonValidatiByOrdine(Integer annoOrd, String serieOrd, Integer progOrd) {
        String chiaveSlash = annoOrd + "/" + serieOrd + "/" + progOrd;
        String chiaveDash  = annoOrd + "-" + serieOrd + "-" + progOrd;

        String hql =
                "select distinct f.anno, f.serie, f.progressivo " +
                        "from Fatture f, FattureDettaglio dAcc, FattureDettaglio dDesc " +
                        "where f.anno = dAcc.anno and f.serie = dAcc.serie and f.progressivo = dAcc.progressivo " +
                        "  and f.anno = dDesc.anno and f.serie = dDesc.serie and f.progressivo = dDesc.progressivo " +
                        "  and (f.numeroFattura is null or f.numeroFattura = '') " +
                        "  and f.dataFattura is null " +
                        "  and dAcc.fArticolo = '*ACC' " +
                        "  and ( lower(dDesc.fDescrArticolo) like concat('%', lower(?1), '%') " +
                        "     or lower(dDesc.fDescrArticolo) like concat('%', lower(?2), '%') )";

        // Nota: contiamo la size della lista di triple distinte (anno/serie/progr)
        @SuppressWarnings("unchecked")
        List<AccontoLightDto> triples = Fatture.find(hql, chiaveSlash, chiaveDash).project(AccontoLightDto.class).list();
        return triples.size();
    }


    private List<AccontoDto> getAccontoDtos(String sottoConto, List<AccontoDto> resultList, List<AccontoDto> listaAcconto) {
        for (AccontoDto a : listaAcconto) {
            String data1 = sdf2.format(a.getDataFattura());
            String data2 = annoYY.format(a.getDataFattura());
            List<AccontoDto> listaStorno = em.createNamedQuery("StornoDto")
                    .setParameter("sottoConto", sottoConto)
                    .setParameter("numeroFattura", StringUtils.trim(a.getNumeroFattura()))
                    .setParameter("iva", a.getIva())
                    .setParameter("data1", data1)
                    .setParameter("data2", data2)
                    .getResultList();
            a.setStorni(listaStorno.stream()
                    .filter(s -> {
                        String ordA = normalize(a.getRifOrdCliente());
                        String ordS = normalize(s.getOrdineCliente());
                        return ordS.equals(ordA);
                    })
                    .toList());        }
        Set<AccontoDto> unici = listaAcconto.stream()
                .filter(a -> a.getPrezzo() > 0)
                .collect(Collectors.toSet());

        resultList.addAll(unici);
        for (AccontoDto dto : resultList) {
            double sommaStorni = dto.getStorni().stream().mapToDouble(AccontoDto::getPrezzo).sum();
            dto.setImportoResiduo(dto.getPrezzo() + sommaStorni);
        }
        return resultList.stream()
                .distinct() // sicurezza extra
                .sorted(Comparator.comparing(AccontoDto::getRifOrdCliente))
                .toList();    }

    private String normalize(String s) {
        return s == null ? "" : s.replaceAll("[^0-9/]", "").trim();
    }

    @Transactional
    public String creaBolla(List<OrdineDettaglioDto> list, List<AccontoDto> accontoDtos, String user) {
        String result = null;
        try {
            Integer progressivoFatt = Fatture.find("SELECT CASE WHEN MAX(progressivo) IS NULL THEN 0 ELSE MAX(progressivo) END FROM Fatture o WHERE anno = :anno and serie = 'B'", Parameters.with("anno", Year.now().getValue())).project(Integer.class).firstResult();
            Integer progressivoFattDettaglio = FattureDettaglio.find("SELECT CASE WHEN MAX(progrGenerale) IS NULL THEN 0 ELSE MAX(progrGenerale) END FROM FattureDettaglio o").project(Integer.class).firstResult();
            Ordine ordine = Ordine.findByOrdineId(list.get(0).getAnno(), list.get(0).getSerie(), list.get(0).getProgressivo());
            Fatture f = fattureMapper.buildFatture(progressivoFatt, ordine, user);
            Log.debug("*** CREA BOLLA --- creata fattura n. " + f.getAnno() + "/" + f.getSerie() + "/" + f.getProgressivo());
            f.persist();
            Map<OrdinePerIva, List<OrdineDettaglioDto>> map = list.stream().collect(Collectors.groupingBy(o ->
                    new OrdinePerIva(o.getAnno(), o.getSerie(), o.getProgressivo(), o.getFCodiceIva())));
            Log.debug("*** CREA BOLLA --- mappa lista ordine dettaglio: " + map.size());
            for (OrdinePerIva id : map.keySet()) {
                Log.debug("*** CREA BOLLA, ciclio sulla mappa --- ordine n. " + id.getAnno() + "/" + id.getSerie() + "/" + id.getProgressivo());
                if (accontoDtos != null && !accontoDtos.isEmpty()) {
                    final List<OrdineDettaglioDto> dtos = map.get(id);
                    Map<String, List<AccontoDto>> accontiPerIvaMap = accontoDtos.stream().filter(a -> AccontoDto.checkOrdineEsiste(a, id)).collect(Collectors.groupingBy(AccontoDto::getIva));
                    for (String s : accontiPerIvaMap.keySet()) {
                        List<AccontoDto> accontiPerIva = accontiPerIvaMap.get(s);
                        Log.debug("*** CREA BOLLA, acconti selezionati per Iva e : " + accontiPerIva.size());
                        accontiPerIva.sort(Comparator.comparing(AccontoDto::getDataFattura));
                        double diffAccontoSommaArticoli = dtos.stream().filter(d -> StringUtils.isNotBlank(d.getFCodiceIva()) && d.getFCodiceIva().equals(s))
                                .mapToDouble(dto -> dto.getPrezzoScontato()*dto.getQtaProntoConsegna()).sum();
                        for (AccontoDto a : accontiPerIva) {

                            if (diffAccontoSommaArticoli <= 0) {
                                break;
                            }

                            double residuo = a.getImportoResiduo();

                            if (residuo <= 0) {
                                continue; // acconto già consumato
                            }

                            double prezzo = Math.min(residuo, diffAccontoSommaArticoli);

                            if (prezzo > 0) {

                                double prezzoArrotondato = BigDecimal.valueOf(prezzo)
                                        .setScale(2, RoundingMode.HALF_UP)
                                        .doubleValue();

                                OrdineDettaglioDto ordineDettaglio =
                                        fattureMapper.fromAccontoToOrdineDettaglio(a, id, prezzoArrotondato);

                                dtos.add(ordineDettaglio);

                                Log.debug("*** CREA BOLLA, creata voce storno: "
                                        + ordineDettaglio.getFDescrArticolo()
                                        + " di " + prezzoArrotondato + " euro");

                                // 🔥 SCALO RESIDUO ACconto
                                a.setImportoResiduo(residuo - prezzoArrotondato);

                                // 🔥 SCALO MERCE DA STORNARE
                                diffAccontoSommaArticoli -= prezzoArrotondato;
                            }
                        }
                    }
                }
            }

            List<OrdineDettaglioDto> listaDaTrasformare = new ArrayList<>();
            map.values().forEach(listaDaTrasformare::addAll);
            listaDaTrasformare.sort(Comparator
                    .comparing(OrdineDettaglioDto::getAnno)
                    .thenComparing(OrdineDettaglioDto::getSerie)
                    .thenComparing(OrdineDettaglioDto::getProgressivo)
                    .thenComparing(OrdineDettaglioDto::getRigo, Comparator.nullsFirst(Integer::compareTo)));

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
                dto.setQtaProntoConsegna(dto.getQtaProntoConsegna() == null ? 0 : dto.getQtaProntoConsegna());
                Magazzino m;
                if (StringUtils.containsIgnoreCase(dto.getFDescrArticolo(), "Storno")) {
                    Log.debug("*** CREA BOLLA, lista da trasformare, riga storno : " + dto.getRigo());
                    fd = fattureMapper.buildStorno(dto, f, progressivoFattDettaglio, i, user);
                    MagazzinoId id = new MagazzinoId(Year.now().getValue(), "B", progressivo, " ", i + 1);
                    m = magazzinoMapper.buildMagazzino(id, ++progressivoGen, fd, f, ordine);
                    Optional<Magazzino> opt = Magazzino.find("progrgenerale = :p", Parameters.with("p", m.getProgrgenerale())).singleResultOptional();
                    if (opt.isPresent()) {
                        Log.error("*** CREA BOLLA, Errore trovato record con progrGen: " + progressivoGen);
                    }
                } else {
                    Log.debug("*** CREA BOLLA, lista da trasformare, riga articolo : " + dto.getRigo());
                    OrdineDettaglio o = OrdineDettaglio.getById(dto.getAnno(), dto.getSerie(), dto.getProgressivo(), dto.getRigo());
                    fd = fattureMapper.buildFattureDettaglio(dto, f, o, progressivoFattDettaglio, i, user);
                    if (dto.getQtaDaConsegnare() != null) {
                        List<FattureDettaglio> fatture = FattureDettaglio.find("Select f " +
                                        "FROM FattureDettaglio f " +
                                        "WHERE f.progrOrdCli = :id ",
                                Parameters.with("id", dto.getProgrGenerale())).list();
                        if (!fatture.isEmpty()) {
                            double sum = fatture.stream().mapToDouble(FattureDettaglio::getQuantita).sum();
                            dto.setQtaDaConsegnare(dto.getQuantita() - sum);
                        } else {
                            dto.setQtaDaConsegnare(dto.getQuantita());
                        }
                        Log.error("*** CREA BOLLA, qta prontoConsegna = " + dto.getQtaProntoConsegna());
                        Log.error("*** CREA BOLLA, qta ordinata = " + dto.getQuantita());
                        Log.error("*** CREA BOLLA, qta da consegnare = " + dto.getQtaDaConsegnare());
                        Double qtaDaCons = ((dto.getQtaDaConsegnare() == null || (dto.getQtaDaConsegnare() != null && dto.getQtaDaConsegnare() < 0)) ? 0 : dto.getQtaDaConsegnare());
                        Double qta = (qtaDaCons == 0) ? dto.getQuantita() : dto.getQtaDaConsegnare();
                        if (qta - dto.getQtaProntoConsegna() == 0) {
                            o.setSaldoAcconto("S");
                        } else {
                            o.setSaldoAcconto("A");
                        }
                    }
                    ordineDettaglioList.add(o);
                    Optional<SaldiMagazzino> optional = SaldiMagazzino.find("marticolo =:art and  mmagazzino = :mag",
                            Parameters.with("art", o.getFArticolo()).and("mag", o.getMagazz())).firstResultOptional();
                    if (optional.isPresent()) {
                        Log.debug("*** CREA BOLLA, TmpScarico creato per articolo: " + o.getFArticolo() + ". Qta: " + o.getQuantita());
                        SaldiMagazzino saldiMagazzino = optional.get();
                        Double qtaScarico = (saldiMagazzino.getQscarichi() == null ? 0 : saldiMagazzino.getQscarichi()) + (o.getQuantita() == null ? 0 : o.getQuantita());
                        qtaScarico = Math.round(qtaScarico * 100.0) / 100.0;
                        Double qtaGiacenza = (saldiMagazzino.getQcarichi() == null ? 0 : saldiMagazzino.getQcarichi()) - qtaScarico;
                        saldiMagazzino.setQscarichi(qtaScarico);
                        saldiMagazzino.setQgiacenza(qtaGiacenza);
                        saldiMagazzinoList.add(saldiMagazzino);
                    } else {
                        if (StringUtils.equals(o.getTipoRigo(), "") || StringUtils.equals(o.getTipoRigo(), " ")) {
                            GoTmpScaricoPK pk = new GoTmpScaricoPK(o.getFArticolo(), o.getMagazz(), fd.getProgrGenerale());
                            GoTmpScarico goTmpScarico = GoTmpScarico.findById(pk);
                            if(goTmpScarico == null) {
                                goTmpScarico = new GoTmpScarico();
                            }
                            goTmpScarico.setId(pk);
                            goTmpScarico.setAttivo(Boolean.TRUE);
                            goTmpScaricoList.add(goTmpScarico);
                            Log.debug("*** CREA BOLLA, TmpScarico creato per articolo: " + o.getFArticolo() + ". Qta: " + o.getQuantita());
                        }

                    }

                    MagazzinoId id = new MagazzinoId(Year.now().getValue(), "B", progressivo, " ", i + 1);
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
            if (!goTmpScaricoList.isEmpty()) {
                GoTmpScarico.persist(goTmpScaricoList);
            }
            result = StringUtils.join("Creata bolla n. ", f.getAnno(), "/", f.getSerie(), "/", f.getProgressivo());
        } catch (Exception e) {
            Log.error("Errore nella creazione della bolla: " + e.getMessage(), e);
        }
        return result;
    }

    public List<AccontoDto> settaRifOrdCliente(List<AccontoDto> listaAcconto) {
        if (listaAcconto == null || listaAcconto.isEmpty()) return Collections.emptyList();

        // Raggruppo per (anno, serie, progressivo) e preservo l’ordine di incontro
        Map<FattKey, List<AccontoDto>> mapByDoc =
                listaAcconto.stream()
                        .filter(a -> a.getAnno() != null && a.getSerie() != null && a.getProgressivo() != null)
                        .collect(Collectors.groupingBy(
                                a -> new FattKey(a.getAnno(), a.getSerie(), a.getProgressivo()),
                                LinkedHashMap::new, // preserva l’ordine dei gruppi
                                Collectors.toList() // preserva l’ordine degli elementi (ordine dello stream)
                        ));

        // Per ciascun “documento” (anno/serie/progr) applico la logica *ACC -> riga +2 = riferimento ordine
        for (List<AccontoDto> righeFattura : mapByDoc.values()) {
            if (righeFattura.isEmpty()) continue;

            List<AccontoDto> accBlock = new ArrayList<>();
            int lastAccIndex = -1;

            for (int i = 0; i < righeFattura.size(); i++) {
                AccontoDto dto = righeFattura.get(i);

                // Riga di acconto: FARTICOLO = "*ACC" (dal CSV è lì)
                if ("*ACC".equalsIgnoreCase(dto.getFArticolo())) {
                    accBlock.add(dto);
                    lastAccIndex = i;
                    continue;
                }

                // Due righe dopo l’ultimo *ACC trovo la descrizione con l’ordine
                if (lastAccIndex != -1 && i == lastAccIndex + 2) {
                    String descr = dto.getOperazione();
                    String ordine = estraiNumeroOrdine(descr);
                    if (ordine != null) {
                        for (AccontoDto acc : accBlock) {
                            acc.setRifOrdCliente(ordine);
                        }
                    }
                    accBlock.clear();
                    lastAccIndex = -1;
                }
            }
        }

        // Ritorno SOLO le righe con riferimento ordine non nullo
        final List<AccontoDto> listaAcconti = mapByDoc.values().stream()
                .flatMap(List::stream)
                .filter(a -> a.getRifOrdCliente() != null).sorted(Comparator
                        .comparing(AccontoDto::getAnno, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(AccontoDto::getSerie, Comparator.nullsLast(String::compareTo))
                        .thenComparing(AccontoDto::getProgressivo, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(AccontoDto::getDataFattura, Comparator.nullsLast(Date::compareTo)))
                .collect(Collectors.toCollection(ArrayList::new));

        // Ordinamento sicuro anche senza numero/data fattura:
        // prima per anno, poi serie, poi progressivo, poi (se presente) per data

        Log.debug("Acconti post elaborazione: " + listaAcconti.size());
        return listaAcconti;
    }

    // Chiave di raggruppamento (anno, serie, progressivo)
    private static final class FattKey {
        final Integer anno;
        final String serie;
        final Integer progressivo;

        FattKey(Integer anno, String serie, Integer progressivo) {
            this.anno = anno;
            this.serie = serie;
            this.progressivo = progressivo;
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FattKey)) return false;
            FattKey fk = (FattKey) o;
            return Objects.equals(anno, fk.anno)
                    && Objects.equals(serie, fk.serie)
                    && Objects.equals(progressivo, fk.progressivo);
        }
        @Override public int hashCode() {
            return Objects.hash(anno, serie, progressivo);
        }
        @Override public String toString() {
            return anno + "/" + serie + "/" + progressivo;
        }
    }

    private static String estraiNumeroOrdine(String descr) {
        if (StringUtils.isBlank(descr)) return null;

        // Fast guard: se non ci sono né slash né trattini né “ord”, scarta subito
        if (!(descr.indexOf('/') >= 0 || descr.indexOf('-') >= 0 ||
                StringUtils.containsIgnoreCase(descr, "ord"))) {
            return null;
        }

        // 1) Se c'è “ord” (ord., ordine, ord…), prova il pattern “parlato”
        if (StringUtils.containsIgnoreCase(descr, "ord")) {
            Matcher m = ORDER_WORDY.matcher(descr);
            if (m.find()) return m.group(1) + "/" + m.group(2) + "/" + m.group(3);
        }

        // 2) In ogni caso, fallback sul tripletto nudo
        Matcher g = ORDER_TRIPLE.matcher(descr);
        if (g.find()) return g.group(1) + "/" + g.group(2) + "/" + g.group(3);

        return null;
    }


    @TransactionConfiguration(timeout = 5000)
    public Double getSaldoContabile(String sottoConto) {
        return Primanota.find("SELECT ISNULL(SUM(importo), 0) " +
                "FROM Primanota " +
                "WHERE gruppoconto = 1231 AND sottoconto = :s " +
                "GROUP BY gruppoconto, sottoconto", Parameters.with("s", sottoConto)).project(Double.class).firstResult();
    }

    @TransactionConfiguration(timeout = 5000)
    public Double getOrdiniAperti(String sottoConto) {
        return Ordine.find(
                "SELECT COALESCE(SUM(" +
                        " o2.prezzo" +
                        " *(1-o2.scontoArticolo/100)" +
                        " *(1-o2.scontoC1/100)" +
                        " *(1-o2.scontoC2/100)" +
                        " *(1-o2.scontoP/100)" +
                        " * god.qtaDaConsegnare" +
                        " * (1 + CAST(o2.fCodiceIva AS double)/100)" +
                        "),0) " +
                        "FROM Ordine o " +
                        "JOIN OrdineDettaglio o2 ON o.anno = o2.anno AND o.serie = o2.serie AND o.progressivo = o2.progressivo " +
                        "JOIN GoOrdineDettaglio god ON o2.progrGenerale = god.progrGenerale " +
                        "WHERE o2.saldoAcconto <> 'S' " +
                        "AND o.gruppoCliente = 1231 " +
                        "AND o.contoCliente = :s",
                Parameters.with("s", sottoConto)
        ).project(Double.class).firstResult();
    }

    @TransactionConfiguration(timeout = 5000)
    public Double getAccontiFatturati(String sottoConto) {

        return Fatture.find(
                "SELECT COALESCE(SUM(" +
                        " f2.prezzo * (1 + CAST(f2.iva AS double)/100)" +
                        "),0) " +
                        "FROM Fatture f " +
                        "JOIN FattureDettaglio f2 " +
                        "ON f.anno = f2.anno AND f.serie = f2.serie AND f.progressivo = f2.progressivo " +
                        "WHERE f.gruppoCliente = 1231 " +
                        "AND f.contoCliente = :s " +
                        "AND f2.fArticolo = '*ACC'",
                Parameters.with("s", sottoConto)
        ).project(Double.class).firstResult();
    }

    @TransactionConfiguration(timeout = 5000)
    public Double getBolleNonFatturate(String sottoConto) {

        return Fatture.find(
                "SELECT COALESCE(SUM(" +
                        " f2.prezzo" +
                        " *(1-f2.scontoarticolo/100)" +
                        " *(1-f2.scontoc1/100)" +
                        " *(1-f2.scontoc2/100)" +
                        " *(1-f2.scontop/100)" +
                        " * f2.quantita" +
                        " * (1 + CAST(f2.iva AS double)/100)" +
                        "),0) " +
                        "FROM Fatture f " +
                        "JOIN FattureDettaglio f2 " +
                        "ON f.anno = f2.anno AND f.serie = f2.serie AND f.progressivo = f2.progressivo " +
                        "WHERE f.gruppoCliente = 1231 " +
                        "AND f.contoCliente = :s " +
                        "AND f.flagfattura <> 'S'",
                Parameters.with("s", sottoConto)
        ).project(Double.class).firstResult();
    }

    /**
     * Elenco fatture di acconto NON validate (senza numero/data) che,
     * nelle righe descrittive, contengono il riferimento all'ORDINE indicato.
     * Matching robusto: sia "YYYY/SSS/PPPP" che "YYYY-SSS-PPPP".
     */
    public List<AccontoLightDto> findAccontiNonValidatiByOrdine(Integer annoOrd, String serieOrd, Integer progOrd) {
        String chiaveSlash = annoOrd + "/" + serieOrd + "/" + progOrd;
        String chiaveDash  = annoOrd + "-" + serieOrd + "-" + progOrd;

        String hql =
                "select distinct f.anno, f.serie, f.progressivo "  +
                        "from Fatture f, FattureDettaglio dAcc, FattureDettaglio dDesc " +
                        "where f.anno = dAcc.anno and f.serie = dAcc.serie and f.progressivo = dAcc.progressivo " +
                        "  and f.anno = dDesc.anno and f.serie = dDesc.serie and f.progressivo = dDesc.progressivo " +
                        "  and (f.numeroFattura is null or f.numeroFattura = '') " +
                        "  and f.dataFattura is null " +
                        "  and dAcc.fArticolo = '*ACC' " +
                        "  and ( lower(dDesc.fDescrArticolo) like concat('%', lower(?1), '%') " +
                        "     or lower(dDesc.fDescrArticolo) like concat('%', lower(?2), '%') ) " +
                        "order by f.anno, f.serie, f.progressivo";

        @SuppressWarnings("unchecked")
        List<AccontoLightDto> list = Fatture.find(hql, chiaveSlash, chiaveDash)
                .project(AccontoLightDto.class)
                .list();
        return list;
    }

    /**
     * True se la fattura è validata (numero e data presenti).
     */
    public boolean isValidata(Integer anno, String serie, Integer progressivo) {
        long cnt = Fatture.count(
                "anno = ?1 and serie = ?2 and progressivo = ?3 " +
                        "and (numeroFattura is not null and numeroFattura <> '') " +
                        "and dataFattura is not null",
                anno, serie, progressivo
        );
        return cnt > 0;
    }
}
