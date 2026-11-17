package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import it.calolenoci.enums.AzioneEnum;
import it.calolenoci.mapper.GoOrdineFornitoreMapper;
import it.calolenoci.mapper.OafArticoloMapper;
import it.calolenoci.mapper.OrdineFornitoreMapper;
import it.calolenoci.mapper.RegistroAzioniMapper;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrdineFornitoreService {



    @Inject
    OrdineFornitoreMapper ordineFornitoreMapper;

    @Inject
    OafArticoloMapper oafArticoloMapper;
    @Inject
    GoOrdineFornitoreMapper goOrdineFornitoreMapper;
    @Inject
    RegistroAzioniMapper registroAzioniMapper;

    @Inject
    OAFArticoloService articoloService;

    @ConfigProperty(name = "data.inizio")
    String dataCongig;


    @Transactional
    public List<String> save(List<OrdineDettaglio> articoli, String user) throws Exception {
        List<String> fornitori = new ArrayList<>();
        try {
            List<OrdineFornitoreDettaglio> ordineFornitoreDettaglios = new ArrayList<>();
            List<OrdineFornitore> fornitoreList = new ArrayList<>();
            List<ArticoloDto> articoloDtos = new ArrayList<>();
            final Integer anno = articoli.get(0).getAnno();
            final String serie = articoli.get(0).getSerie();
            final Integer progressivo = articoli.get(0).getProgressivo();
            articoli.forEach(a -> OrdineDettaglio.find("select pcc.intestazione as intestazioneCliente, pc.intestazione, a.articolo , " +
                                    " a.descrArticolo, a.descrArtSuppl, a.unitaMisura, a.prezzoBase, a.costoBase, " +
                                    " fa.fornitoreArticoloId.gruppo as gruppoConto, fa.fornitoreArticoloId.conto as sottoConto,pc.codPagamento," +
                                    " pc.banca, o.progrGenerale, o.rigo, o.quantita, o.quantitaV, o.fColli " +
                                    " from OrdineDettaglio o " +
                                    " inner join Ordine o2 ON  o2.anno = o.anno AND o2.serie = o.serie AND o2.progressivo = o.progressivo " +
                                    " inner join PianoConti pcc ON  pcc.gruppoConto =  o2.gruppoCliente AND pcc.sottoConto = o2.contoCliente " +
                                    " inner join Articolo a ON a.articolo = o.fArticolo " +
                                    " inner join FornitoreArticolo fa ON  o.fArticolo = fa.fornitoreArticoloId.articolo " +
                                    " inner JOIN PianoConti pc ON fa.fornitoreArticoloId.gruppo = pc.gruppoConto and fa.fornitoreArticoloId.conto = pc.sottoConto " +
                                    " where o.progressivo = :progressivo and o.anno = :anno and o.serie = :serie and o.rigo = :rigo ",
                            Parameters.with("anno", a.getAnno()).and("serie", a.getSerie()).and("progressivo", a.getProgressivo()).and("rigo", a.getRigo()))
                    .project(ArticoloDto.class)
                    .singleResultOptional()
                    .ifPresent(articoloDtos::add));
            Map<String, List<ArticoloDto>> mapArticoli = articoloDtos.stream().collect(Collectors.groupingBy(ArticoloDto::getIntestazione));
            if (mapArticoli.isEmpty()) {
                return fornitori;
            }
            fornitori.addAll(mapArticoli.keySet());
            int index = 1;
            Integer progressivoForn = OrdineFornitore.find("SELECT CASE WHEN MAX(progressivo) IS NULL THEN 0 ELSE MAX(progressivo) END FROM OrdineFornitore o WHERE o.anno = :a", Parameters.with("a", LocalDate.now().getYear())).project(Integer.class).firstResult();
            Integer progressivoFornDettaglio = OrdineFornitoreDettaglio.find("SELECT CASE WHEN MAX(progrGenerale) IS NULL THEN 0 ELSE MAX(progrGenerale) END FROM OrdineFornitoreDettaglio").project(Integer.class).firstResult();
            List<RegistroAzioni> registroAzioniList = new ArrayList<>();
            for (String sottoConto : mapArticoli.keySet()) {
                List<ArticoloDto> articoloDtoList = mapArticoli.get(sottoConto);
                Log.debug("Trovati " + articoloDtoList.size() + " articoli per il fornitore " + sottoConto);
                int prog = progressivoForn + index;
                String serieOAF = "B";
                if (!articoloDtoList.isEmpty()) {
                    ArticoloDto articoloDto = articoloDtoList.get(0);
                    fornitoreList.add(ordineFornitoreMapper.buildOAF(articoloDto, prog, serieOAF, user));
                    for (ArticoloDto a : articoloDtoList) {
                        Log.debug("Creo ordine per articolo: " + a.getArticolo() + " - " + a.getDescrArticolo());
                        OrdineFornitoreDettaglio fornitoreDettaglio = new OrdineFornitoreDettaglio();
                        fornitoreDettaglio.setProgressivo(prog);
                        fornitoreDettaglio.setProgrGenerale(++progressivoFornDettaglio);
                        fornitoreDettaglio.setAnno(Year.now().getValue());
                        fornitoreDettaglio.setSerie(serieOAF);
                        fornitoreDettaglio.setRigo(articoloDtoList.indexOf(a) + 1);
                        fornitoreDettaglio.setTipoRigo(" ");
                        fornitoreDettaglio.setPid(a.getProgrGenerale());
                        fornitoreDettaglio.setOArticolo(a.getArticolo());
                        fornitoreDettaglio.setODescrArticolo(a.getDescrArticolo());
                        Magazzino.find("Select valoreUnitario FROM Magazzino " +
                                                "WHERE mArticolo = :codArticolo ORDER BY dataMagazzino desc",
                                        Parameters.with("codArticolo", a.getArticolo()))
                                .project(Double.class)
                                .firstResultOptional()
                                .ifPresent(fornitoreDettaglio::setOPrezzo);
                        fornitoreDettaglio.setOQuantita(a.getQuantita());
                        fornitoreDettaglio.setOQuantitaV(a.getQuantitaV());
                        fornitoreDettaglio.setOquantita2(0D);
                        fornitoreDettaglio.setOUnitaMisura(a.getUnitaMisura());
                        fornitoreDettaglio.setOColli(a.getColli());
                        fornitoreDettaglio.setOCodiceIva("22");
                        fornitoreDettaglio.setProvenienza("C");
                        fornitoreDettaglio.setMagazzino("B");
                        oafArticoloMapper.settaCampi(user, fornitoreDettaglio);
                        if (fornitoreDettaglio.getOQuantita() != null && fornitoreDettaglio.getOPrezzo() != null) {
                            fornitoreDettaglio.setValoreTotale(fornitoreDettaglio.getOQuantita() * fornitoreDettaglio.getOPrezzo());
                        }
                        String campoUser5 = "VS.ART." + a.getDescrArtSuppl();
                        fornitoreDettaglio.setCampoUser5(StringUtils.truncate(campoUser5, 25));
                        String nota = "Riferimento n. " + anno + "/" + serie + "/" + progressivo + "-" + a.getRigo();
                        fornitoreDettaglio.setNota(nota);

                        Log.debug("Chiave per articolo " + fornitoreDettaglio.getOArticolo() + ": " + fornitoreDettaglio.getAnno() + fornitoreDettaglio.getSerie()
                                + fornitoreDettaglio.getProgressivo() + fornitoreDettaglio.getRigo());
                        ordineFornitoreDettaglios.add(fornitoreDettaglio);
                        int update = GoOrdineDettaglio.update("flagNonDisponibile = 'F', flagOrdinato = 'T' where anno = :anno " +
                                "and serie = :serie and progressivo = :progressivo and rigo = :rigo", Parameters.with("anno", anno)
                                .and("serie", serie).and("progressivo", progressivo).and("rigo", a.getRigo()));
                        Log.debug("Cambio flag ordinato per articolo: " + anno + "/" + serie + "/" + progressivo + "-" + a.getRigo() +
                                ". Aggiornati " + update + " articoli");
                        registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(anno, serie, progressivo, user, AzioneEnum.ORDINATO.getDescrizione()
                                , a.getRigo(), null, null, null, null));
                    }
                    OrdineFornitoreDettaglio rigoRiferimento = oafArticoloMapper.createRigoRiferimento(serieOAF, prog, articoloDto.getIntestazioneCliente(),
                            ordineFornitoreDettaglios.get(ordineFornitoreDettaglios.size() - 1).getRigo(), progressivoFornDettaglio, user);
                    ordineFornitoreDettaglios.add(rigoRiferimento);
                    progressivoFornDettaglio = rigoRiferimento.getProgrGenerale();
                }

                index++;
            }
            RegistroAzioni.persist(registroAzioniList);
            long count = GoOrdineDettaglio.count("flagNonDisponibile = 'T' and anno = :anno " +
                    " and serie = :serie and progressivo = :progressivo ", Parameters.with("anno", anno)
                    .and("serie", serie).and("progressivo", progressivo));
            if (count == 0) {
                GoOrdine.update("userLock = null, locked = 'F', status = 'INCOMPLETO' where anno = :anno " +
                        " and serie = :serie and progressivo = :progressivo", Parameters.with("anno", anno)
                        .and("serie", serie).and("progressivo", progressivo));
            }
            OrdineFornitoreDettaglio.persist(ordineFornitoreDettaglios);
            OrdineFornitore.persist(fornitoreList);
        } catch (Exception e) {
            Log.error("Errore creazione ordine a fornitore: " + e.getMessage());
            throw new Exception(e.getMessage());
        }
        return fornitori;
    }

    public AnteprimaUnioneOafDto anteprimaUnione(List<OrdineFornitoreDto> richieste) {
        return preparaUnione(richieste, false);
    }

    private AnteprimaUnioneOafDto preparaUnione(List<OrdineFornitoreDto> richieste, boolean blocca) {
        if (richieste == null || richieste.size() < 2)
            throw new it.calolenoci.exception.BusinessException("Selezionare almeno due ordini distinti");
        List<OrdineFornitoreDto> ordini = new ArrayList<>();
        Set<FornitoreId> chiavi = new HashSet<>();
        StringBuilder snapshot = new StringBuilder();
        for (OrdineFornitoreDto dto : richieste) {
            if (dto == null || dto.getAnno() == null || StringUtils.isBlank(dto.getSerie()) || dto.getProgressivo() == null)
                throw new it.calolenoci.exception.BusinessException("Riferimento ordine non valido");
            FornitoreId id = new FornitoreId(dto.getAnno(), dto.getSerie(), dto.getProgressivo());
            if (!chiavi.add(id)) throw new it.calolenoci.exception.BusinessException("Lo stesso ordine è selezionato più volte");
            OrdineFornitore o = blocca ? OrdineFornitore.findById(id, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
                    : OrdineFornitore.findById(id);
            if (o == null) throw new it.calolenoci.exception.BusinessException("Un ordine non esiste più: aggiornare la lista");
            if (!"F".equals(o.getProvvisorio()))
                throw new it.calolenoci.exception.BusinessException("Si possono unire soltanto ordini sospesi");
            if (o.getGruppo() == null || StringUtils.isBlank(o.getConto()))
                throw new it.calolenoci.exception.BusinessException("Fornitore mancante nell'ordine " + o.getProgressivo());
            OrdineFornitoreDto attuale = new OrdineFornitoreDto();
            attuale.setAnno(o.getAnno()); attuale.setSerie(o.getSerie()); attuale.setProgressivo(o.getProgressivo());
            attuale.setGruppo(o.getGruppo()); attuale.setConto(o.getConto().trim());
            PianoConti fornitore = PianoConti.find("gruppoConto=?1 and sottoConto=?2", o.getGruppo(), o.getConto()).firstResult();
            attuale.setIntestazione(fornitore == null ? o.getGruppo() + "/" + o.getConto().trim() : fornitore.getIntestazione());
            ordini.add(attuale);
            snapshot.append(o.getAnno()).append('|').append(o.getSerie()).append('|').append(o.getProgressivo())
                    .append('|').append(o.getGruppo()).append('|').append(o.getConto().trim()).append('|').append(o.getUpdateDate()).append(';');
            var queryRighe = OrdineFornitoreDettaglio.find(
                    "anno=?1 and serie=?2 and progressivo=?3 ORDER BY rigo", o.getAnno(), o.getSerie(), o.getProgressivo());
            if (blocca) queryRighe.withLock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
            List<OrdineFornitoreDettaglio> righe = queryRighe.list();
            for (OrdineFornitoreDettaglio r : righe)
                snapshot.append(r.getProgrGenerale()).append('|').append(r.getRigo()).append('|')
                        .append(r.getOArticolo()).append('|').append(r.getOQuantita()).append('|').append(r.getOPrezzo()).append(';');
        }
        // Mantiene la scelta storica: anno più recente, primo selezionato a parità di anno.
        ordini.sort(Comparator.comparing(OrdineFornitoreDto::getAnno).reversed());
        boolean diversi = ordini.stream().anyMatch(o -> !Objects.equals(o.getGruppo(), ordini.get(0).getGruppo())
                || !Objects.equals(o.getConto(), ordini.get(0).getConto()));
        String versione;
        try {
            versione = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(snapshot.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
        return new AnteprimaUnioneOafDto(ordini, diversi, versione);
    }

    @Transactional
    public void unisciOrdini(List<OrdineFornitoreDto> ordini) {
        unisciOrdini(ordini, false, null);
    }

    @Transactional(rollbackOn = Exception.class)
    public void unisciOrdini(List<OrdineFornitoreDto> richieste, boolean confermaFornitoriDiversi, String versione) {
        AnteprimaUnioneOafDto anteprima = preparaUnione(richieste, true);
        if (versione != null && !versione.equals(anteprima.getVersione()))
            throw new it.calolenoci.exception.BusinessException("Gli ordini sono cambiati: aggiornare la lista e confermare nuovamente");
        if (anteprima.isFornitoriDiversi() && (!confermaFornitoriDiversi || versione == null))
            throw new it.calolenoci.exception.BusinessException("Fornitori diversi: è richiesta la conferma esplicita dell'anteprima");
        OrdineFornitoreDto destinazione = anteprima.getOrdini().get(0);
        Integer ultimo = findRigo(destinazione.getAnno(), destinazione.getSerie(), destinazione.getProgressivo());
        int prossimo = ultimo == null ? 1 : ultimo + 1;
        for (OrdineFornitoreDto sorgente : anteprima.getOrdini().subList(1, anteprima.getOrdini().size())) {
            OrdineFornitore testata = OrdineFornitore.findById(new FornitoreId(sorgente.getAnno(), sorgente.getSerie(), sorgente.getProgressivo()));
            List<OrdineFornitoreDettaglio> righe = OrdineFornitoreDettaglio.find(
                    "anno=?1 and serie=?2 and progressivo=?3 ORDER BY rigo", sorgente.getAnno(), sorgente.getSerie(), sorgente.getProgressivo()).list();
            if (GoOrdineFornitoreBK.findById(new FornitoreId(sorgente.getAnno(), sorgente.getSerie(), sorgente.getProgressivo())) == null)
                GoOrdineFornitoreBK.persist(ordineFornitoreMapper.copyOAF(testata));
            for (OrdineFornitoreDettaglio r : righe) {
                if (GoOrdineFornitoreDettaglioBK.findById(new FornitoreDettaglioId(r.getAnno(), r.getSerie(), r.getProgressivo(), r.getRigo())) == null)
                    GoOrdineFornitoreDettaglioBK.persist(oafArticoloMapper.copyOAFDettaglio(r));
            }
            // Salva il backup prima di spostare le righe con update bulk delle chiavi ERP.
            OrdineFornitore.getEntityManager().flush();
            for (OrdineFornitoreDettaglio r : righe) {
                int spostate = OrdineFornitoreDettaglio.update(
                        "anno=:a, serie=:s, progressivo=:p, rigo=:r WHERE anno=:oa and serie=:os and progressivo=:op and rigo=:orr",
                        Parameters.with("a", destinazione.getAnno()).and("s", destinazione.getSerie()).and("p", destinazione.getProgressivo())
                                .and("r", prossimo++).and("oa", r.getAnno()).and("os", r.getSerie()).and("op", r.getProgressivo()).and("orr", r.getRigo()));
                if (spostate != 1) throw new IllegalStateException("Riga non trasferita durante l'unione");
            }
            if (!OrdineFornitore.deleteById(new FornitoreId(sorgente.getAnno(), sorgente.getSerie(), sorgente.getProgressivo())))
                throw new IllegalStateException("Testata non rimossa durante l'unione");
        }
        OrdineFornitore.getEntityManager().flush();
    }

    private Integer findRigo(Integer anno, String serie, Integer progressivo) {
        return OrdineFornitoreDettaglio.find("SELECT MAX(f.rigo) FROM OrdineFornitoreDettaglio f " +
                                " WHERE f.anno = :anno AND f.serie = :serie AND f.progressivo = :progressivo ",
                        Parameters.with("anno", anno)
                                .and("serie", serie).and("progressivo", progressivo))
                .project(Integer.class).singleResult();
    }

    public List<OrdineFornitoreDto> findAllByStatus(FiltroOrdini filtro) throws ParseException {
        String query = " SELECT o.anno,  o.serie,  o.progressivo, o.dataOrdine,  " +
                "p.intestazione,  o.dataConfOrdine, o.numConfOrdine, o.provvisorio, o.updateDate, go.note, o.gruppo, o.conto";
        if (filtro != null && StringUtils.isBlank(filtro.getFiltroStatus())) {
            query += " , go.flInviato, go.dataInvio ";
        }
        query += " FROM OrdineFornitore o " +
                "JOIN PianoConti p ON o.gruppo = p.gruppoConto AND o.conto = p.sottoConto "
                + " LEFT JOIN GoOrdineFornitore go ON o.anno = go.anno AND go.serie = o.serie AND o.progressivo = go.progressivo "
                + " WHERE o.dataOrdine >= :dataConfig ";
        Map<String, Object> params = new HashMap<>();
        LocalDate data = LocalDate.parse(dataCongig);
        params.put("dataConfig", data);
        if (filtro != null && StringUtils.isNotBlank(filtro.getFiltroInvio())) {
            if ("da_inviare".equals(filtro.getFiltroInvio())) {
                query += " AND (go.flInviato IS null OR go.flInviato = false) ";
            }
            if ("inviati".equals(filtro.getFiltroInvio())) {
                query += " AND go.flInviato = true ";
            }
        }
        if (filtro != null && StringUtils.isNotBlank(filtro.getFiltroStatus())) {
            query += " AND  o.provvisorio =:stato";
            params.put("stato", filtro.getFiltroStatus());
        } else {
            query += " AND  (o.provvisorio is null OR o.provvisorio = '' OR o.provvisorio = ' ')";
        }
        Log.debug("query: " + query);
    return OrdineFornitore.find(query, Sort.descending("o.updateDate").and( "dataOrdine"), params)
                .project(OrdineFornitoreDto.class).list();

    }

    @Transactional
    public void richiediApprovazione(Integer anno, String serie, Integer progressivo) {
        OrdineFornitore.update("provvisorio = 'T' where anno = :anno AND serie = :serie AND progressivo = :progressivo",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo));
    }

    @Transactional
    public void richiediApprovazione(List<OrdineFornitoreDto> list) {
        list.forEach(o -> this.richiediApprovazione(o.getAnno(), o.getSerie(), o.getProgressivo()));
    }

    @Transactional
    public void changeStatus(Integer anno, String serie, Integer progressivo) {
        int update = OrdineFornitore.update("provvisorio = 'F' where anno = :anno and progressivo = :progressivo and serie = :serie",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo));
        if (update > 0)
            GoOrdineFornitore.deleteById(new FornitoreId(anno, serie, progressivo));
    }

    @Transactional
    public ResponseDto eliminaOrdine(Integer anno, String serie, Integer progressivo) {
        ResponseDto dto = new ResponseDto();
        try {
            OrdineFornitore ordine = OrdineFornitore.findById(new FornitoreId(anno, serie, progressivo));
            if (ordine == null) {
                Log.error("ordine fornitore non trovato");
                dto.setError(Boolean.TRUE);
                dto.setMsg("ordine fornitore non trovato");
                return dto;
            }
            List<OrdineFornitoreDettaglio> list = OrdineFornitoreDettaglio.find("anno =:anno AND serie =:serie AND progressivo =:progressivo",
                    Parameters.with("anno", ordine.getAnno()).and("serie", ordine.getSerie()).and("progressivo", ordine.getProgressivo())).list();

            for (OrdineFornitoreDettaglio dettaglio : list) {
                articoloService.eliminaArticolo(dettaglio.getAnno(), dettaglio.getSerie(), dettaglio.getProgressivo(), dettaglio.getRigo());
            }
            OrdineFornitore.deleteById(new FornitoreId(anno, serie, progressivo));
            GoOrdineFornitore.deleteById(new FornitoreId(anno, serie, progressivo));
            dto.setError(Boolean.FALSE);
            dto.setMsg("Articolo eliminato");
        } catch (Exception e) {
            Log.error("Errore elimina ordine fornitore ", e);
            dto.setError(Boolean.TRUE);
            dto.setMsg("Errore elimina ordine fornitore");
        }
        return dto;
    }

    @Transactional
    public void inviato(List<OrdineFornitoreDto> list) {
        for (OrdineFornitoreDto dto : list) {
            Optional<GoOrdineFornitore> opt = GoOrdineFornitore.findByIdOptional(new FornitoreId(dto.getAnno(), dto.getSerie(), dto.getProgressivo()));
            GoOrdineFornitore ordineFornitore;
            if (opt.isEmpty()) {
                ordineFornitore = goOrdineFornitoreMapper.creaEntity(dto.getAnno(), dto.getSerie(), dto.getProgressivo());
                ordineFornitore.setFlInviato(dto.getFlInviato());
                ordineFornitore.setDataInvio(LocalDate.now());
                ordineFornitore.persist();
            } else {
                GoOrdineFornitore.update("flInviato =:flInv, dataInvio =:d" +
                                " WHERE anno =:anno AND serie =:serie AND progressivo =:progressivo",
                        Parameters.with("anno", dto.getAnno()).and("serie", dto.getSerie())
                                .and("progressivo", dto.getProgressivo()).and("d", LocalDate.now())
                                .and("flInv", dto.getFlInviato()));
            }
        }
    }

    public List<OrdineFornitoreDto> findForReport(Integer anno, String serie, Integer progressivo) {
        List<OrdineFornitoreDto> list = OrdineFornitore.find("select f.anno, f.serie, f.progressivo, f.dataOrdine, f.numConfOrdine, f.dataConfOrdine, f2.oArticolo, f2.oDescrArticolo, f2.campoUser5, f2.nota, " +
                        " f2.oPrezzo, f2.oUnitaMisura,f2.oQuantita, f2.fScontoArticolo, f2.scontoF1, f2.scontoF2, f2.fScontoP, f2.oCodiceIva, " +
                        " b.descrBanca, b.abiBanca, pa.codice, pa.descrizione, f.createUser as user, a.descrArtSuppl, " +
                        " p.intestazione, p.telefono, p.fax, p.indirizzo, p.localita, p.cap, p.provincia, f2.tipoRigo" +
                        " from OrdineFornitore f " +
                        " JOIN OrdineFornitoreDettaglio f2 ON f.anno = f2.anno AND f.serie = f2.serie AND f.progressivo = f2.progressivo " +
                        " JOIN PianoConti p ON f.gruppo = p.gruppoConto ANd f.conto = p.sottoConto " +
                        " JOIN InfoBanca b ON f.bancaPagamento = b.bancaPres " +
                        " JOIN ModalitaPagamento pa ON f.codicePagamento = pa.codice " +
                        " LEFT JOIN Articolo a ON a.articolo = f2.oArticolo " +
                        " WHERE f.anno =:anno AND f.serie =:serie AND f.progressivo =:progressivo",
                Parameters.with("anno", anno).and("serie", serie)
                        .and("progressivo", progressivo)).project(OrdineFornitoreDto.class).list();
        list.forEach(e -> {
            if (StringUtils.isBlank(e.getTipoRigo())) {
                e.setValoreTotale(calcolaValoreTotale(e));
            }
        });
        return list;
    }

    private Double calcolaValoreTotale(OrdineFornitoreDto dto) {
        double valoreTotale = dto.getQuantita() * dto.getPrezzo();
        if (dto.getScontoArticolo() != null && dto.getScontoArticolo() != 0) {
            valoreTotale -= (valoreTotale * dto.getScontoArticolo() / 100);
        }
        if (dto.getScontoF1() != null && dto.getScontoF1() != 0) {
            valoreTotale -= (valoreTotale * dto.getScontoF1() / 100);
        }
        if (dto.getScontoF2() != null && dto.getScontoF2() != 0) {
            valoreTotale -= (valoreTotale * dto.getScontoF2() / 100);
        }
        if (dto.getScontoP() != null && dto.getScontoP() != 0) {
            valoreTotale -= (valoreTotale * dto.getScontoP() / 100);
        }

        return valoreTotale;
    }

    public void salvaNota(OrdineFornitoreDto dto) {
        Optional<GoOrdineFornitore> opt = GoOrdineFornitore.findByIdOptional(new FornitoreId(dto.getAnno(), dto.getSerie(), dto.getProgressivo()));
        GoOrdineFornitore ordineFornitore;
        if (opt.isEmpty()) {
            ordineFornitore = goOrdineFornitoreMapper.creaEntity(dto.getAnno(), dto.getSerie(), dto.getProgressivo());
            ordineFornitore.setNote(dto.getNote());
            ordineFornitore.persist();
        } else {
            GoOrdineFornitore.update("note = :note WHERE anno =:anno and serie =:serie and progressivo = :progressivo",
                    Parameters.with("note", dto.getNote()).and("anno", dto.getAnno())
                            .and("serie", dto.getSerie())
                            .and("progressivo", dto.getProgressivo()));
        }
    }

    @Transactional
    public ResponseDto verificaOAF(CollegaOAFDto dto) {
        ResponseDto result = new ResponseDto();
        try {
            Optional<OrdineFornitoreDettaglio> oaf = OrdineFornitoreDettaglio.find("anno =:a and serie =:s AND progressivo = :pr AND oArticolo = :art",
                    Parameters.with("a", dto.getAnnoOAF()).and("s", dto.getSerieOAF())
                            .and("pr", dto.getProgressivoOAF()).and("art", dto.getCodice())).firstResultOptional();
            if (oaf.isEmpty()) {
                result.setError(Boolean.TRUE);
                result.setMsg("Ordine a fornitore non trovato con questo identificativo: " +
                        dto.getAnnoOAF() + "/" + dto.getSerieOAF() + "/" + dto.getProgressivoOAF());
                result.setCode(Response.Status.NO_CONTENT);
                return result;
            }
            if (!oaf.get().getOQuantita().equals(dto.getQta())) {
                result.setError(Boolean.TRUE);
                result.setMsg("ATTENZIONE: Le quantità tra articolo cliente e ordine a fornitore non corrispondono. ");
                result.setCode(Response.Status.CONFLICT);
                return result;
            }
            result.setError(Boolean.FALSE);
            result.setMsg("Verifica Ok: procedi al collegamento con OAF");
            return result;
        } catch (Exception e) {
            Log.error("Verifica OAF: ERROR! ", e);
            result.setMsg("Verifica OAF: ERROR! " + e.getMessage());
            result.setError(Boolean.TRUE);
            return result;
        }
    }

    public List<OAFMonitorDto> getOrdiniByOperatore() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d = sdf.parse("2024-02-21");
        return OrdineFornitore.find("SELECT createUser, COUNT(*) FROM OrdineFornitore WHERE createDate>=:d GROUP BY createUser",
                Parameters.with("d", d)).project(OAFMonitorDto.class).list();
    }

    @Transactional
    public ResponseDto inserisciDataConsegna(AggiornaDataDto dto) {
        ResponseDto result = new ResponseDto();
        try {
            Optional<OrdineDettaglio> opt = OrdineDettaglio.find("progrGenerale = :pid",
                    Parameters.with("pid", dto.getPid())).firstResultOptional();
            if (opt.isPresent()) {
                OrdineDettaglio ordineDettaglio = opt.get();
                boolean dtInserita = false;
                if (dto.getDataConsegna() != null) {
                    dtInserita = true;
                }
                if (dto.getSettimana() != null) {
                    dtInserita = true;
                }
                ordineDettaglio.setDatauser1(dto.getDataConsegna());
                ordineDettaglio.setQtyuser1(dto.getSettimana());
                ordineDettaglio.persist();
                result.setError(Boolean.FALSE);
                result.setMsg("Articolo cliente aggiornato");
                Optional<OrdineFornitoreDettaglio> optOAF = OrdineFornitoreDettaglio.find("pid = :pid",
                        Parameters.with("pid", dto.getPid())).firstResultOptional();
                if (optOAF.isPresent()) {
                    OrdineFornitoreDettaglio ordineFornitoreDettaglio = optOAF.get();
                    if(dtInserita) {
                        ordineFornitoreDettaglio.setCampouser1("S");
                        ordineFornitoreDettaglio.persist();
                    } else {
                        ordineFornitoreDettaglio.setCampouser1(" ");
                        ordineFornitoreDettaglio.persist();
                    }
                }
                return result;
            }
            result.setError(Boolean.FALSE);
            result.setMsg("Articolo non trovato!");
            return result;
        } catch (Exception e) {
            Log.error("Aggiorna data consegna: ERROR! ", e);
            result.setError(Boolean.TRUE);
            result.setMsg("Aggiorna data consegna: ERROR! " + e.getMessage());
            return result;
        }
    }

    public AggiornaDataDto getDataOrdineCliente(Integer pid) {
        AggiornaDataDto dto = new AggiornaDataDto();
        try {
            Optional<OrdineDettaglio> opt = OrdineDettaglio.find("progrGenerale = :pid",
                    Parameters.with("pid", pid)).firstResultOptional();
            if (opt.isPresent()) {
                OrdineDettaglio ordineDettaglio = opt.get();
                dto.setPid(pid);
                if (ordineDettaglio != null && ordineDettaglio.getDatauser1() != null) {
                    dto.setDataConsegna(ordineDettaglio.getDatauser1());
                }
                if (ordineDettaglio != null && ordineDettaglio.getQtyuser1() != null) {
                    dto.setSettimana(ordineDettaglio.getQtyuser1());
                }
            }
        } catch (Exception e) {
            Log.error("Aggiorna data consegna: ERROR! ", e);
        }
        return dto;
    }
}
