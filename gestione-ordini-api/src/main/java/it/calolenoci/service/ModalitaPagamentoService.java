package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.AccontoDto;
import it.calolenoci.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ModalitaPagamentoService {
    @Inject EntityManager em;
    @Inject FatturaService fatturaService;

    private static final String ATTIVI = """
            FROM Ordine o JOIN GoOrdine go ON go.anno=o.anno AND go.serie=o.serie AND go.progressivo=o.progressivo
            WHERE go.status IS NOT NULL AND go.status <> 'ARCHIVIATO' AND o.provvisorio <> 'S'
            """;
    private static final String CON_PAGAMENTO = """
            AND EXISTS (SELECT 1 FROM OrdineDettaglio p WHERE p.anno=o.anno AND p.serie=o.serie
                AND p.progressivo=o.progressivo AND p.fArticolo IN ('*ACCONTO','*SALDO'))
            """;

    /** Scansione in background: include timestamp null, cambi di data e righe rimosse da Plurima. */
    @Transactional
    public void riconcilia() {
        riconcilia(null);
    }

    @Transactional
    public void riconciliaOrdine(Integer anno, String serie, Integer progressivo) {
        riconcilia(new OrdineId(anno,serie,progressivo));
    }

    private void riconcilia(OrdineId filtro) {
        // SQL Server avvia la transazione JDBC implicita con una lettura di tabella,
        // non con il solo EXEC sp_getapplock. Serve anche con snapshot inizialmente vuoto.
        em.createNativeQuery("SELECT TOP (1) PROGRGENERALE FROM GO_PAGAMENTO_ORDINE").getResultList();
        Number lock = (Number) em.createNativeQuery("""
                DECLARE @r int;
                EXEC @r = sys.sp_getapplock @Resource='GO_MODALITA_PAGAMENTO',
                    @LockMode='Exclusive', @LockOwner='Transaction', @LockTimeout=0;
                SELECT @r;
                """).getSingleResult();
        if (lock.intValue() < 0) throw new IllegalStateException("Riconciliazione pagamenti: lock non disponibile (codice "+lock.intValue()+")");

        List<Object[]> ordini = leggi("SELECT o, go.status " + ATTIVI + CON_PAGAMENTO + filtroOrdine("o",filtro),Object[].class,filtro);
        if (ordini.isEmpty()) {
            if (filtro==null) GoPagamentoOrdine.deleteAll();
            else GoPagamentoOrdine.delete("anno=?1 AND serie=?2 AND progressivo=?3",
                    filtro.getAnno(),filtro.getSerie(),filtro.getProgressivo());
            return;
        }
        List<OrdineDettaglio> dettagli = leggi("""
                SELECT d FROM OrdineDettaglio d WHERE d.fArticolo IN ('*ACCONTO','*SALDO')
                AND EXISTS (SELECT 1 FROM Ordine o JOIN GoOrdine go ON go.anno=o.anno
                    AND go.serie=o.serie AND go.progressivo=o.progressivo
                    WHERE d.anno=o.anno AND d.serie=o.serie AND d.progressivo=o.progressivo
                    AND go.status IS NOT NULL AND go.status <> 'ARCHIVIATO' AND o.provvisorio <> 'S')
                """ + filtroOrdine("d",filtro) + " ORDER BY d.anno,d.serie,d.progressivo,d.rigo",
                OrdineDettaglio.class,filtro);
        Map<OrdineId,List<OrdineDettaglio>> perOrdine = dettagli.stream().collect(Collectors.groupingBy(this::chiave));
        Map<Integer,GoPagamentoOrdine> salvate = leggi("SELECT p FROM GoPagamentoOrdine p WHERE 1=1"
                +filtroOrdine("p",filtro),GoPagamentoOrdine.class,filtro).stream()
                .collect(Collectors.toMap(GoPagamentoOrdine::getProgrGenerale, p -> p));
        Set<Integer> presenti = new HashSet<>();
        Map<OrdineId,BigDecimal> totali = totaliOrdini(filtro);
        RisultatoFatture verificaFatture = accontiEmessi(ordini,filtro);
        Map<String,String> clienti = new HashMap<>();
        List<Object[]> anagrafiche = leggi("""
                SELECT DISTINCT p.gruppoConto,p.sottoConto,p.intestazione FROM PianoConti p
                WHERE EXISTS (SELECT 1 FROM Ordine o JOIN GoOrdine go ON go.anno=o.anno
                    AND go.serie=o.serie AND go.progressivo=o.progressivo
                    WHERE o.gruppoCliente=p.gruppoConto AND o.contoCliente=p.sottoConto
                    AND go.status <> 'ARCHIVIATO'
                """+filtroOrdine("o",filtro)+")",Object[].class,filtro);
        anagrafiche.forEach(p -> clienti.put(p[0]+"/"+p[1].toString().trim(), Objects.toString(p[2],"")));
        int lunghezzaDescrizione = ((Number)em.createNativeQuery("""
                SELECT CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA='dbo' AND TABLE_NAME='ORDCLI2' AND COLUMN_NAME='FDESCRARTICOLO'
                """).getSingleResult()).intValue();
        LocalDate oggi = LocalDate.now(java.time.ZoneId.of("Europe/Rome"));
        LocalDateTime ora = LocalDateTime.now(java.time.ZoneId.of("Europe/Rome"));
        for (Object[] coppia : ordini) {
            Ordine ordine = (Ordine)coppia[0];
            String status = (String)coppia[1];
            OrdineId id = new OrdineId(ordine.getAnno(),ordine.getSerie().trim(),ordine.getProgressivo());
            List<OrdineDettaglio> righeDb = perOrdine.getOrDefault(id,List.of());
            List<RegolePagamento.Riga> righe = righeDb.stream().map(this::riga).toList();
            Map<Integer,String> anomalie = RegolePagamento.anomalie(righe,ordine.getDataConferma());
            BigDecimal totale = totali.getOrDefault(id, BigDecimal.ZERO);
            List<RiscontroAcconti.Previsto> previsti = soglieCumulative(righe,ordine.getDataConferma(),totale);
            List<RiscontroAcconti.Emesso> fatture = verificaFatture.emessi().getOrDefault(id,List.of());
            Map<Integer,RiscontroAcconti.Esito> riscontri = RiscontroAcconti.verifica(previsti,fatture);
            String tutteFatture = fatture.stream().map(f -> f.riferimento()+" (€ "+f.importo()+")").collect(Collectors.joining(", "));
            for (OrdineDettaglio d : righeDb) {
                RegolePagamento.Riga r = riga(d);
                GoPagamentoOrdine p = salvate.get(r.id());
                if (p == null) { p = new GoPagamentoOrdine(); p.setProgrGenerale(r.id()); }
                presenti.add(r.id());
                String descrizioneAttuale = Objects.toString(d.getFDescrArticolo(),"").stripTrailing();
                String base = Objects.equals(descrizioneAttuale,p.getDescrizioneScritta())
                        ? Objects.toString(p.getDescrizioneBase(),descrizioneAttuale) : descrizioneAttuale;
                if (!RegolePagamento.dataPredefinita(r,ordine.getDataConferma())) base = RegolePagamento.senzaMese(base);
                RegolePagamento.Riga conBase = new RegolePagamento.Riga(r.id(),r.rigo(),r.articolo(),base,r.percentuale(),r.data());
                String nuovaDescrizione = RegolePagamento.descrizione(conBase,ordine.getDataConferma(),oggi.getYear());
                String anomalia = anomalie.getOrDefault(r.id(),"");
                if (verificaFatture.clientiDaVerificare().contains(ordine.getContoCliente().trim()))
                    anomalia = unisci(anomalia,"Il cliente ha fatture acconto senza un riferimento ordine riconoscibile: verificare gli abbinamenti");
                if (totale.signum() <= 0) anomalia = unisci(anomalia,"Imponibile ordine nullo o negativo: verificare gli importi");
                RiscontroAcconti.Esito riscontro = riscontri.get(r.id());
                if (lunghezzaDescrizione > 0 && nuovaDescrizione.length() > lunghezzaDescrizione) {
                    anomalia = unisci(anomalia,"Descrizione troppo lunga per aggiungere il mese: abbreviare il testo in Plurima");
                    nuovaDescrizione = descrizioneAttuale;
                }
                if (!nuovaDescrizione.equals(descrizioneAttuale)) aggiornaDescrizione(d,nuovaDescrizione);
                p.setAnno(d.getAnno()); p.setSerie(d.getSerie()); p.setProgressivo(d.getProgressivo());
                p.setRigo(d.getRigo()); p.setArticolo(r.articolo()); p.setPercentuale(r.percentuale());
                p.setDataPrevista(r.data()); p.setDataScadenza(RegolePagamento.scadenza(r,ordine.getDataConferma()));
                p.setDescrizioneBase(base); p.setDescrizioneScritta(nuovaDescrizione);
                p.setCliente(clienti.getOrDefault(ordine.getGruppoCliente()+"/"+ordine.getContoCliente().trim(),ordine.getContoCliente()));
                p.setStatoOrdine(status); p.setAnomalia(anomalia);
                p.setImportoPrevisto(r.percentuale()==null ? null : importo(totale,r.percentuale()));
                boolean coperto = totale.signum()>0 && riscontro != null && riscontro.coperto();
                if (coperto) { anomalia=""; p.setAnomalia(""); }
                p.setImportoFatturato(coperto ? p.getImportoPrevisto() : BigDecimal.ZERO);
                p.setFatture(tutteFatture.length()>2000 ? tutteFatture.substring(0,2000) : tutteFatture);
                p.setStato(!anomalia.isEmpty() ? "ANOMALIA"
                        : p.getImportoPrevisto()!=null && p.getImportoPrevisto().signum()==0 ? "NON_DOVUTO"
                        : coperto ? "EMESSO"
                        : p.getDataScadenza()==null ? ("COMPLETO".equals(status) ? "DA_FATTURARE" : "AL_PRONTO")
                        : !p.getDataScadenza().isAfter(oggi) ? "DA_FATTURARE" : "PROGRAMMATO");
                p.setControllatoIl(ora);
                if (!p.isPersistent()) p.persist();
            }
        }
        for (var p : salvate.values()) if (!presenti.contains(p.getProgrGenerale())) p.delete();
        em.flush();
    }

    private void aggiornaDescrizione(OrdineDettaglio d, String descrizione) {
        String filtro = "anno=:a AND serie=:s AND progressivo=:p AND rigo=:r AND progrGenerale=:id";
        var params = Parameters.with("a",d.getAnno()).and("s",d.getSerie()).and("p",d.getProgressivo())
                .and("r",d.getRigo()).and("id",d.getProgrGenerale()).and("nuova",descrizione);
        if (d.getFDescrArticolo()==null) filtro += " AND fDescrArticolo IS NULL";
        else { filtro += " AND fDescrArticolo=:vecchia"; params.and("vecchia",d.getFDescrArticolo()); }
        if (d.getDataConfConsegna()==null) filtro += " AND dataConfConsegna IS NULL";
        else { filtro += " AND dataConfConsegna=:data"; params.and("data",d.getDataConfConsegna()); }
        if (d.getQuantita()==null) filtro += " AND quantita IS NULL";
        else { filtro += " AND quantita=:q"; params.and("q",d.getQuantita()); }
        if (OrdineDettaglio.update("fDescrArticolo=:nuova WHERE "+filtro,params)!=1)
            throw new IllegalStateException("Riga pagamento modificata durante il controllo; verrà riprovata al prossimo ciclo");
    }

    private Map<OrdineId,BigDecimal> totaliOrdini(OrdineId filtro) {
        List<Object[]> risultati = leggi("""
                SELECT d.anno,d.serie,d.progressivo,
                    SUM(COALESCE(d.prezzo,0)*COALESCE(d.quantita,0)
                      *(1-COALESCE(d.scontoArticolo,0)/100)*(1-COALESCE(d.scontoC1,0)/100)
                      *(1-COALESCE(d.scontoC2,0)/100)*(1-COALESCE(d.scontoP,0)/100))
                FROM OrdineDettaglio d
                WHERE d.tipoRigo <> 'C' AND d.fArticolo NOT IN ('*ACCONTO','*SALDO')
                  AND EXISTS (SELECT 1 FROM GoOrdine go WHERE go.anno=d.anno AND go.serie=d.serie
                    AND go.progressivo=d.progressivo AND go.status <> 'ARCHIVIATO')
                  AND EXISTS (SELECT 1 FROM OrdineDettaglio p WHERE p.anno=d.anno AND p.serie=d.serie
                    AND p.progressivo=d.progressivo AND p.fArticolo IN ('*ACCONTO','*SALDO'))
                """+filtroOrdine("d",filtro)+" GROUP BY d.anno,d.serie,d.progressivo",Object[].class,filtro);
        Map<OrdineId,BigDecimal> totali = new HashMap<>();
        risultati.forEach(r -> totali.put(new OrdineId((Integer)r[0],((String)r[1]).trim(),(Integer)r[2]),
                r[3]==null ? BigDecimal.ZERO : new BigDecimal(r[3].toString())));
        return totali;
    }

    private record RisultatoFatture(Map<OrdineId,List<RiscontroAcconti.Emesso>> emessi, Set<String> clientiDaVerificare) {}

    @SuppressWarnings("unchecked")
    private RisultatoFatture accontiEmessi(List<Object[]> ordini, OrdineId filtro) {
        Map<OrdineId,Ordine> testate = new HashMap<>();
        ordini.forEach(c -> { Ordine o=(Ordine)c[0]; testate.put(new OrdineId(o.getAnno(),o.getSerie().trim(),o.getProgressivo()),o); });
        if (testate.isEmpty()) return new RisultatoFatture(Map.of(),Set.of());
        List<AccontoDto> righe = parametri(em.createNativeQuery("""
                SELECT f.CONTOCLIENTE contoCliente,f.ANNO anno,f.SERIE serie,f.PROGRESSIVO progressivo,
                    f.DATABOLLA dataBolla,f.DATAFATTURA dataFattura,f.NUMEROFATTURA numeroFattura,
                    f2.FDESCRARTICOLO operazione,f2.PREZZO prezzo,f2.FCODICEIVA iva,
                    RTRIM(f2.FARTICOLO) fArticolo,f.NUMEROBOLLA numeroBolla
                FROM FATTURE f JOIN FATTURE2 f2 ON f.ANNO=f2.ANNO AND f.SERIE=f2.SERIE AND f.PROGRESSIVO=f2.PROGRESSIVO
                WHERE f.SERIE='A' AND f.PROGRESSIVO>0 AND EXISTS (
                    SELECT 1 FROM ORDCLI o JOIN GO_ORDINE go ON go.ANNO=o.ANNO AND go.SERIE=o.SERIE AND go.PROGRESSIVO=o.PROGRESSIVO
                    WHERE o.CONTOCLIENTE=f.CONTOCLIENTE AND o.GRUPPOCLIENTE=f.GRUPPOCLIENTE
                      AND go.STATUS <> 'ARCHIVIATO' AND EXISTS (
                        SELECT 1 FROM ORDCLI2 p WHERE p.ANNO=o.ANNO AND p.SERIE=o.SERIE AND p.PROGRESSIVO=o.PROGRESSIVO
                          AND p.FARTICOLO IN ('*ACCONTO','*SALDO'))
                """+filtroOrdine("o",filtro)+") ORDER BY f2.ANNO,f2.SERIE,f2.PROGRESSIVO,f2.RIGO",
                "AccontoDto"),filtro).getResultList();
        List<AccontoDto> associati = fatturaService.settaRifOrdCliente(righe);
        Set<String> nonAssociati = righe.stream()
                .filter(a -> "*ACC".equalsIgnoreCase(Objects.toString(a.getFArticolo(),"").trim()) && a.getRifOrdCliente()==null)
                .map(a -> a.getContoCliente().trim()).collect(Collectors.toSet());
        Map<OrdineId,Map<String,BigDecimal>> documenti = new HashMap<>();
        for (AccontoDto a : associati) {
            String[] parti = a.getRifOrdCliente().split("/");
            if (parti.length!=3) continue;
            OrdineId id = new OrdineId(Integer.valueOf(parti[0]),parti[1],Integer.valueOf(parti[2]));
            Ordine ordine = testate.get(id);
            if (ordine==null || !ordine.getContoCliente().trim().equals(a.getContoCliente().trim())) continue;
            String documento = a.getAnno()+"/"+a.getSerie()+"/"+a.getProgressivo();
            documenti.computeIfAbsent(id,k->new LinkedHashMap<>()).merge(documento,
                    BigDecimal.valueOf(a.getPrezzo()==null ? 0 : a.getPrezzo()),BigDecimal::add);
        }
        Map<OrdineId,List<RiscontroAcconti.Emesso>> risultato = new HashMap<>();
        documenti.forEach((id,docs) -> risultato.put(id,docs.entrySet().stream()
                .map(d -> new RiscontroAcconti.Emesso(d.getKey(),RiscontroAcconti.euro(d.getValue()))).toList()));
        return new RisultatoFatture(risultato,nonAssociati);
    }

    private List<RiscontroAcconti.Previsto> soglieCumulative(List<RegolePagamento.Riga> righe,
                                                           LocalDate conferma, BigDecimal totale) {
        if (righe.stream().anyMatch(r -> r.percentuale()==null || r.percentuale().signum()<0
                || r.percentuale().compareTo(BigDecimal.valueOf(100))>0)) return List.of();
        if (righe.stream().map(RegolePagamento.Riga::percentuale).reduce(BigDecimal.ZERO,BigDecimal::add)
                .compareTo(BigDecimal.valueOf(100))!=0) return List.of();
        List<RegolePagamento.Riga> ordinate = righe.stream().sorted(
                Comparator.comparingInt((RegolePagamento.Riga r) -> r.acconto() ? 0 : 1)
                    .thenComparing(r -> Optional.ofNullable(RegolePagamento.scadenza(r,conferma)).orElse(LocalDate.MAX))
                    .thenComparingInt(RegolePagamento.Riga::rigo)).toList();
        List<RiscontroAcconti.Previsto> soglie = new ArrayList<>();
        BigDecimal percentuale = BigDecimal.ZERO;
        for (RegolePagamento.Riga r : ordinate) {
            percentuale=percentuale.add(r.percentuale());
            soglie.add(new RiscontroAcconti.Previsto(r.id(),importo(totale,percentuale)));
        }
        return soglie;
    }

    private String filtroOrdine(String alias, OrdineId filtro) {
        return filtro==null ? "" : " AND "+alias+".anno=:annoFiltro AND "+alias+".serie=:serieFiltro AND "
                +alias+".progressivo=:progressivoFiltro";
    }

    private jakarta.persistence.Query parametri(jakarta.persistence.Query query, OrdineId filtro) {
        if (filtro!=null) query.setParameter("annoFiltro",filtro.getAnno())
                .setParameter("serieFiltro",filtro.getSerie()).setParameter("progressivoFiltro",filtro.getProgressivo());
        return query;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> leggi(String jpql, Class<T> tipo, OrdineId filtro) {
        return parametri(em.createQuery(jpql,tipo),filtro).getResultList();
    }

    private RegolePagamento.Riga riga(OrdineDettaglio d) {
        return new RegolePagamento.Riga(d.getProgrGenerale(),d.getRigo(),d.getFArticolo().trim(),
                d.getFDescrArticolo(),d.getQuantita()==null ? null : BigDecimal.valueOf(d.getQuantita()),d.getDataConfConsegna());
    }
    private OrdineId chiave(OrdineDettaglio d) { return new OrdineId(d.getAnno(),d.getSerie().trim(),d.getProgressivo()); }
    private BigDecimal importo(BigDecimal totale, BigDecimal percentuale) {
        return totale.multiply(percentuale).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP);
    }
    private String unisci(String a, String b) { return b.isEmpty() ? a : a.isEmpty() ? b : a+"; "+b; }
}
