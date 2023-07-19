package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import it.calolenoci.enums.AzioneEnum;
import it.calolenoci.mapper.RegistroAzioniMapper;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrdineFornitoreService {

    @Inject
    RegistroAzioniMapper registroAzioniMapper;

    @Inject
    OAFArticoloService articoloService;

    @ConfigProperty(name = "data.inizio")
    String dataCongig;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

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
            Integer progressivoForn = OrdineFornitore.find("SELECT CASE WHEN MAX(progressivo) IS NULL THEN 0 ELSE MAX(progressivo) END FROM OrdineFornitore o").project(Integer.class).firstResult();
            Integer progressivoFornDettaglio = OrdineFornitoreDettaglio.find("SELECT CASE WHEN MAX(progrGenerale) IS NULL THEN 0 ELSE MAX(progrGenerale) END FROM OrdineFornitoreDettaglio o").project(Integer.class).firstResult();
            List<RegistroAzioni> registroAzioniList = new ArrayList<>();
            for (String sottoConto : mapArticoli.keySet()) {
                List<ArticoloDto> articoloDtoList = mapArticoli.get(sottoConto);
                Log.debug("Trovati " + articoloDtoList.size() + " articoli per il fornitore " + sottoConto);
                int prog = progressivoForn + index;
                String serieOAF = "B";
                if (!articoloDtoList.isEmpty()) {
                    ArticoloDto articoloDto = articoloDtoList.get(0);
                    OrdineFornitore ordineFornitore = new OrdineFornitore();
                    ordineFornitore.setProgressivo(prog);
                    ordineFornitore.setAnno(Year.now().getValue());
                    ordineFornitore.setSerie(serieOAF);
                    ordineFornitore.setDataOrdine(new Date());
                    ordineFornitore.setCreateDate(new Date());
                    ordineFornitore.setUpdateDate(new Date());
                    ordineFornitore.setDataModifica(new Date());
                    ordineFornitore.setMagazzino("B");
                    ordineFornitore.setNumRevisione(0);
                    ordineFornitore.setGruppo(articoloDto.getGruppoConto());
                    ordineFornitore.setConto(articoloDto.getSottoConto());
                    ordineFornitore.setCodicePagamento(articoloDto.getCodPagamento());
                    ordineFornitore.setBancaPagamento(articoloDto.getBanca());
                    ordineFornitore.setCreateUser(user);
                    ordineFornitore.setProvvisorio("F");
                    fornitoreList.add(ordineFornitore);
                    for (ArticoloDto a : articoloDtoList) {
                        Log.debug("Creo ordine per articolo: " + a.getArticolo() + " - " + a.getDescrArticolo());
                        OrdineFornitoreDettaglio fornitoreDettaglio = new OrdineFornitoreDettaglio();
                        fornitoreDettaglio.setProgressivo(prog);
                        fornitoreDettaglio.setProgrGenerale(++progressivoFornDettaglio);
                        fornitoreDettaglio.setAnno(Year.now().getValue());
                        fornitoreDettaglio.setSerie(serieOAF);
                        //Integer rigo = articoloDtoList.indexOf(a);
                      /*  if(optOrdine.isPresent()) {
                             rigo = findRigo(serieOAF, prog);
                        }*/
                        fornitoreDettaglio.setRigo(articoloDtoList.indexOf(a) + 1);
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
                        fornitoreDettaglio.setOUnitaMisura(a.getUnitaMisura());
                        fornitoreDettaglio.setOColli(a.getColli());
                        fornitoreDettaglio.setOCodiceIva("22");
                        fornitoreDettaglio.setProvenienza("C");
                        fornitoreDettaglio.setMagazzino("B");
                        fornitoreDettaglio.setSaldo("");
                        fornitoreDettaglio.setTipoRigo("");
                        if(fornitoreDettaglio.getOQuantita() != null && fornitoreDettaglio.getOPrezzo() != null){
                            fornitoreDettaglio.setValoreTotale(fornitoreDettaglio.getOQuantita()*fornitoreDettaglio.getOPrezzo());
                        }
                        String campoUser5 = "VS.ART." + a.getDescrArtSuppl();
                        fornitoreDettaglio.setCampoUser5(StringUtils.truncate(campoUser5, 25));
                        String nota = "Riferimento n. " + anno + "/" + serie + "/" + progressivo + "-" + a.getRigo();
                        fornitoreDettaglio.setNota(nota);

                        Log.debug("Chiave per articolo " + fornitoreDettaglio.getOArticolo() + ": " + fornitoreDettaglio.getAnno() + fornitoreDettaglio.getSerie()
                                + fornitoreDettaglio.getProgressivo() + fornitoreDettaglio.getRigo());
                        ordineFornitoreDettaglios.add(fornitoreDettaglio);
                        GoOrdineDettaglio.update("flagNonDisponibile = 'F', flagOrdinato = 'T' where anno = :anno " +
                                "and serie = :serie and progressivo = :progressivo and rigo = :rigo", Parameters.with("anno", anno)
                                .and("serie", serie).and("progressivo", progressivo).and("rigo", a.getRigo()));
                        registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(anno, serie,
                                progressivo, user, AzioneEnum.ORDINATO.getDesczrizione()
                                , a.getRigo(), null, null, null, null));
                    }
                    OrdineFornitoreDettaglio rigoRiferimento = createRigoRiferimento(serieOAF, prog, articoloDto.getIntestazioneCliente(),
                            ordineFornitoreDettaglios.get(ordineFornitoreDettaglios.size() - 1).getRigo(), progressivoFornDettaglio);
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

    @Transactional
    public void unisciOrdini(List<OrdineFornitoreDto> dtoList) {
        try {
            OrdineFornitoreDto ordineDaTenere = dtoList.get(0);
            Integer ultimoRigo = findRigo(ordineDaTenere.getAnno(), ordineDaTenere.getSerie(), ordineDaTenere.getProgressivo());
            List<OrdineFornitoreDto> ordiniDaUnire = dtoList.stream().skip(1).toList();
            int count = ultimoRigo + 1;
            int update = 0;
            List<OrdineFornitoreDettaglio> list = new ArrayList<>();
            ordiniDaUnire.forEach(dto ->
                    list.addAll(OrdineFornitoreDettaglio.find("anno = :anno AND serie = :serie AND progressivo = :progressivo",
                                    Parameters.with("anno", dto.getAnno()).and("serie", dto.getSerie()).and("progressivo", dto.getProgressivo()))
                            .list()));
            for (OrdineFornitoreDettaglio o : list) {
                update += OrdineFornitoreDettaglio.update("rigo = :nuovoRigo, progressivo =: nuovoProgressivo " +
                                "WHERE anno = :anno and serie =:serie and progressivo =:progressivo and rigo=:rigo",
                        Parameters.with("anno", o.getAnno()).and("serie", o.getSerie())
                                .and("progressivo", o.getProgressivo()).and("rigo", o.getRigo())
                                .and("nuovoRigo", count).and("nuovoProgressivo", ordineDaTenere.getProgressivo()));
                count++;
            }
            if (update != 0) {
                Log.info("Aggiornati " + update + "articoli");
                ordiniDaUnire.forEach(o -> OrdineFornitore.deleteById(new FornitoreId(o.getAnno(), o.getSerie(), o.getProgressivo())));
            }

        } catch (Exception e) {
            Log.error("Errore nell'unione degli OAF: " + e.getMessage());
        }

    }

    private Integer findRigo(Integer anno, String serie, Integer progressivo) {
        return OrdineFornitoreDettaglio.find("SELECT MAX(f.rigo) FROM OrdineFornitoreDettaglio f " +
                                " WHERE f.anno = :anno AND f.serie = :serie AND f.progressivo = :progressivo ",
                        Parameters.with("anno", anno)
                                .and("serie", serie).and("progressivo", progressivo))
                .project(Integer.class).singleResult();
    }

    private OrdineFornitoreDettaglio createRigoRiferimento(String serie, Integer progressivo, String intestazione, Integer rigo, Integer progrGenerale) {
        OrdineFornitoreDettaglio ordineFornitoreDettaglio = new OrdineFornitoreDettaglio();
        ordineFornitoreDettaglio.setTipoRigo("C");
        ordineFornitoreDettaglio.setRigo(rigo + 1);
        ordineFornitoreDettaglio.setAnno(Year.now().getValue());
        ordineFornitoreDettaglio.setSerie(serie);
        ordineFornitoreDettaglio.setProgressivo(progressivo);
        ordineFornitoreDettaglio.setODescrArticolo("Rif. " + intestazione);
        ordineFornitoreDettaglio.setProgrGenerale(progrGenerale + 1);
        return ordineFornitoreDettaglio;
    }

    public List<OrdineFornitoreDto> findAllByStatus(String status) throws ParseException {
        String query = " SELECT o.anno,  o.serie,  o.progressivo, o.dataOrdine,  " +
                "p.intestazione,  o.dataConfOrdine, o.numConfOrdine, o.provvisorio, o.updateDate";
        if (StringUtils.isBlank(status)) {
            query +=  " , go.flInviato, go.note, go.dataInvio ";
        }
        query +=" FROM OrdineFornitore o " +
                "JOIN PianoConti p ON o.gruppo = p.gruppoConto AND o.conto = p.sottoConto ";
        if (StringUtils.isBlank(status)) {
            query += " LEFT JOIN GoOrdineFornitore go ON o.anno = go.anno AND go.serie = o.serie AND o.progressivo = go.progressivo ";
        }
        query += " WHERE o.dataOrdine >= :dataConfig ";
        Map<String, Object> params = new HashMap<>();
        params.put("dataConfig", sdf.parse(dataCongig));
        if (StringUtils.isNotBlank(status)) {
            query += " AND  o.provvisorio =:stato";
            params.put("stato", status);
            return OrdineFornitore.find(query, Sort.descending("o.updateDate", "dataOrdine")
                            , params)
                    .project(OrdineFornitoreDto.class).list();
        } else {
            query += " AND  (o.provvisorio is null OR o.provvisorio = '')";
            return OrdineFornitore.find(query, Sort.descending("o.updateDate", "dataOrdine"), params)
                    .project(OrdineFornitoreDto.class).list();
        }
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
        if(update > 0)
            GoOrdineFornitore.deleteById(new FornitoreId(anno, serie, progressivo));
    }

    @Transactional
    public ResponseDto eliminaOrdine(Integer anno, String serie, Integer progressivo) {
        ResponseDto dto = new ResponseDto();
        try {
        OrdineFornitore ordine = OrdineFornitore.findById(new FornitoreId(anno, serie, progressivo));
        if(ordine == null){
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
        int update = 0;
        for (OrdineFornitoreDto e : list) {
            update += GoOrdineFornitore.update("flInviato =:flInv" +
                            " WHERE anno =:anno AND serie =:serie AND progressivo =:progressivo",
                    Parameters.with("anno", e.getAnno()).and("serie", e.getSerie())
                            .and("progressivo", e.getProgressivo())
                            .and("flInv", e.getFlInviato()));
        }
        Log.info("FlInvio aggionati: " + update + " ordini");
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
        list.forEach(e-> {
            if(StringUtils.isBlank(e.getTipoRigo())) {
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

}
