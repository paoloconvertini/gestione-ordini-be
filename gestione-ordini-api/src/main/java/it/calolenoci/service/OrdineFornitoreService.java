package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
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
                        settaCampi(user, fornitoreDettaglio);
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
                        GoOrdineDettaglio.update("flagNonDisponibile = 'F', flagOrdinato = 'T' where anno = :anno " +
                                "and serie = :serie and progressivo = :progressivo and rigo = :rigo", Parameters.with("anno", anno)
                                .and("serie", serie).and("progressivo", progressivo).and("rigo", a.getRigo()));
                        registroAzioniList.add(registroAzioniMapper.fromDtoToEntity(anno, serie,
                                progressivo, user, AzioneEnum.ORDINATO.getDesczrizione()
                                , a.getRigo(), null, null, null, null));
                    }
                    OrdineFornitoreDettaglio rigoRiferimento = createRigoRiferimento(serieOAF, prog, articoloDto.getIntestazioneCliente(),
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

    @Transactional
    public void unisciOrdini(List<OrdineFornitoreDto> dtoList) {
        try {
            dtoList.sort(Comparator.comparing(OrdineFornitoreDto::getAnno).reversed());
            OrdineFornitoreDto ordineDaTenere = dtoList.get(0);
            Log.debug("Ordine da tenere:" + ordineDaTenere.getProgressivo());
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
                Log.debug("Aggiorno progressivo: " + o.getProgressivo() + " con nuovo progressivo: " + ordineDaTenere.getProgressivo());
                update += OrdineFornitoreDettaglio.update("anno=:a, rigo = :nuovoRigo, progressivo =: nuovoProgressivo " +
                                "WHERE anno = :anno and serie =:serie and progressivo =:progressivo and rigo=:rigo",
                        Parameters.with("anno", o.getAnno()).and("a", ordineDaTenere.getAnno()).and("serie", o.getSerie())
                                .and("progressivo", o.getProgressivo()).and("rigo", o.getRigo())
                                .and("nuovoRigo", count).and("nuovoProgressivo", ordineDaTenere.getProgressivo()));
                count++;
            }
            if (update != 0) {
                Log.info("Aggiornati " + update + " articoli");
                List<OrdineFornitoreDettaglio> listaDettaglioDaEliminare = new ArrayList<>();
                ordiniDaUnire.forEach(o -> {
                listaDettaglioDaEliminare.addAll(OrdineFornitoreDettaglio.find("anno = :anno AND serie = :serie AND progressivo = :progressivo",
                            Parameters.with("anno", o.getAnno()).and("serie", o.getSerie()).and("progressivo", o.getProgressivo())).list());
                     OrdineFornitore ordineFornitore = OrdineFornitore.find("anno = :anno AND serie = :serie AND progressivo = :progressivo",
                            Parameters.with("anno", o.getAnno()).and("serie", o.getSerie()).and("progressivo", o.getProgressivo())).firstResult();
                     GoOrdineFornitoreBK.persist(ordineFornitoreMapper.copyOAF(ordineFornitore));
                    OrdineFornitore.deleteById(new FornitoreId(o.getAnno(), o.getSerie(), o.getProgressivo()));
                });
                if(!listaDettaglioDaEliminare.isEmpty()) {
                    Log.debug("Trovati " + listaDettaglioDaEliminare.size() + " articoli di OAF orfani! INIZIO CANCELLAZIONE DA DB...");
                    listaDettaglioDaEliminare.forEach( d -> {
                        Log.debug("Elimino progressivo/anno: " + d.getProgressivo()+"/"+d.getAnno());
                        GoOrdineFornitoreDettaglioBK.persist(oafArticoloMapper.copyOAFDettaglio(d));
                        OrdineFornitoreDettaglio.delete("anno = :anno AND serie = :serie AND progressivo = :progressivo AND rigo =:rigo",
                                Parameters.with("anno", d.getAnno()).and("serie", d.getSerie()).and("progressivo", d.getProgressivo())
                                        .and("rigo", d.getRigo()));
                    });
                    Log.debug("CANCELLAZIONE TERMINATA!!");
                }

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

    private OrdineFornitoreDettaglio createRigoRiferimento(String serie, Integer progressivo, String intestazione, Integer rigo, Integer progrGenerale, String user) {
        OrdineFornitoreDettaglio ordineFornitoreDettaglio = new OrdineFornitoreDettaglio();
        ordineFornitoreDettaglio.setTipoRigo("C");
        ordineFornitoreDettaglio.setRigo(rigo + 1);
        ordineFornitoreDettaglio.setAnno(Year.now().getValue());
        ordineFornitoreDettaglio.setSerie(serie);
        ordineFornitoreDettaglio.setProgressivo(progressivo);
        ordineFornitoreDettaglio.setODescrArticolo("Rif. " + intestazione);
        ordineFornitoreDettaglio.setProgrGenerale(progrGenerale + 1);
        ordineFornitoreDettaglio.setNota(" ");
        ordineFornitoreDettaglio.setOQuantita(0D);
        ordineFornitoreDettaglio.setOQuantitaV(0D);
        ordineFornitoreDettaglio.setOquantita2(0D);
        ordineFornitoreDettaglio.setOUnitaMisura(" ");
        ordineFornitoreDettaglio.setOColli(0);
        ordineFornitoreDettaglio.setOCodiceIva(" ");
        ordineFornitoreDettaglio.setProvenienza(" ");
        ordineFornitoreDettaglio.setMagazzino(" ");
        ordineFornitoreDettaglio.setPid(0);
        ordineFornitoreDettaglio.setOArticolo(" ");
        ordineFornitoreDettaglio.setCampoUser5(" ");
        settaCampi(user, ordineFornitoreDettaglio);

        return ordineFornitoreDettaglio;
    }

    private void settaCampi(String user, OrdineFornitoreDettaglio ordineFornitoreDettaglio) {
        ordineFornitoreDettaglio.setSaldo(" ");
        ordineFornitoreDettaglio.setSinonimo1(0);
        ordineFornitoreDettaglio.setVariante1(" ");
        ordineFornitoreDettaglio.setVariante2(" ");
        ordineFornitoreDettaglio.setVariante3(" ");
        ordineFornitoreDettaglio.setVariante4(" ");
        ordineFornitoreDettaglio.setVariante5(" ");
        ordineFornitoreDettaglio.setCodiceean(" ");
        ordineFornitoreDettaglio.setOcoefficiente(0D);
        ordineFornitoreDettaglio.setPrezzoextra(0D);
        ordineFornitoreDettaglio.setDataconfconseg(new Date());
        ordineFornitoreDettaglio.setDatarichconseg(new Date());
        ordineFornitoreDettaglio.setOlottomagf(" ");
        ordineFornitoreDettaglio.setOpallet(0D);
        ordineFornitoreDettaglio.setOcommessa(" ");
        ordineFornitoreDettaglio.setOcentrocostor(" ");
        ordineFornitoreDettaglio.setImpprovvfisso(0D);
        ordineFornitoreDettaglio.setOprovvarticolo(0D);
        ordineFornitoreDettaglio.setOprovvfornitore(0D);
        ordineFornitoreDettaglio.setQtyuser1(0D);
        ordineFornitoreDettaglio.setQtyuser2(0D);
        ordineFornitoreDettaglio.setQtyuser3(0D);
        ordineFornitoreDettaglio.setQtyuser4(0D);
        ordineFornitoreDettaglio.setQtyuser5(0D);
        ordineFornitoreDettaglio.setCampouser1(" ");
        ordineFornitoreDettaglio.setCampouser2(" ");
        ordineFornitoreDettaglio.setCampouser3(" ");
        ordineFornitoreDettaglio.setCampouser4(" ");
        ordineFornitoreDettaglio.setPidPrimanota(0);
        ordineFornitoreDettaglio.setUsername(user);
        ordineFornitoreDettaglio.setSysCreatedate(new Date());
        ordineFornitoreDettaglio.setSysUpdatedate(new Date());
        ordineFornitoreDettaglio.setSysCreateuser(user);
        ordineFornitoreDettaglio.setSysUpdateuser(user);
        ordineFornitoreDettaglio.setOvocespesa(" ");
        ordineFornitoreDettaglio.setOPrezzo(0D);
        ordineFornitoreDettaglio.setValoreTotale(0D);
        ordineFornitoreDettaglio.setFScontoArticolo(0D);
        ordineFornitoreDettaglio.setScontoF1(0D);
        ordineFornitoreDettaglio.setScontoF2(0D);
        ordineFornitoreDettaglio.setFScontoP(0D);
    }

    public List<OrdineFornitoreDto> findAllByStatus(String status) throws ParseException {
        String query = " SELECT o.anno,  o.serie,  o.progressivo, o.dataOrdine,  " +
                "p.intestazione,  o.dataConfOrdine, o.numConfOrdine, o.provvisorio, o.updateDate, go.note";
        if (StringUtils.isBlank(status)) {
            query += " , go.flInviato, go.dataInvio ";
        }
        query += " FROM OrdineFornitore o " +
                "JOIN PianoConti p ON o.gruppo = p.gruppoConto AND o.conto = p.sottoConto "
                + " LEFT JOIN GoOrdineFornitore go ON o.anno = go.anno AND go.serie = o.serie AND o.progressivo = go.progressivo "
                + " WHERE o.dataOrdine >= :dataConfig ";
        Map<String, Object> params = new HashMap<>();
        params.put("dataConfig", sdf.parse(dataCongig));
        if (StringUtils.isNotBlank(status)) {
            query += " AND  o.provvisorio =:stato";
            params.put("stato", status);
            return OrdineFornitore.find(query, Sort.descending("o.updateDate", "dataOrdine")
                            , params)
                    .project(OrdineFornitoreDto.class).list();
        } else {
            query += " AND  (o.provvisorio is null OR o.provvisorio = '' OR o.provvisorio = ' ')";
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
                ordineFornitore.persist();
            } else {
                GoOrdineFornitore.update("flInviato =:flInv" +
                                " WHERE anno =:anno AND serie =:serie AND progressivo =:progressivo",
                        Parameters.with("anno", dto.getAnno()).and("serie", dto.getSerie())
                                .and("progressivo", dto.getProgressivo())
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
            if(oaf.isEmpty()) {
                result.setError(Boolean.TRUE);
                result.setMsg("Ordine a fornitore non trovato con questo identificativo: " +
                        dto.getAnnoOAF() + "/" + dto.getSerieOAF() + "/" + dto.getProgressivoOAF());
                result.setCode(Response.Status.NO_CONTENT);
                return result;
            }
            if(!oaf.get().getOQuantita().equals(dto.getQta())) {
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

    @Transactional
    public ResponseDto collegaOAF(CollegaOAFDto dto) {
        ResponseDto result = new ResponseDto();
        try {
            String campoUser5 = "VS.ART." + dto.getDescrArtSuppl();
            String nota = "Riferimento n. " + dto.getAnno() + "/" + dto.getSerie() + "/" + dto.getProgressivo() + "-" + dto.getRigo();
            int update = OrdineFornitoreDettaglio.update("nota = :n, campoUser5 = :c, provenienza = :p, pid = :g " +
                            "WHERE anno =:a and serie =:s AND progressivo = :pr AND oArticolo = :art",
                    Parameters.with("n", nota).and("c", StringUtils.truncate(campoUser5, 25))
                            .and("p", "C").and("a", dto.getAnnoOAF()).and("s", dto.getSerieOAF())
                            .and("pr", dto.getProgressivoOAF()).and("g", dto.getProgrGenerale()).and("art", dto.getCodice()));
            if (update != 0) {
                Log.debug("Collega OAF: Aggiornati " + update + " record");
            }
            result.setError(Boolean.FALSE);
            result.setMsg("Articolo cliente collegato all'ordine a fornitore " + dto.getAnnoOAF() + "/" + dto.getSerieOAF() + "/" + dto.getProgressivoOAF());
            return result;
        } catch (Exception e) {
            Log.error("Collega OAF: ERROR! ", e);
            result.setError(Boolean.FALSE);
            result.setMsg("Collega OAF: ERROR! " + e.getMessage());
            return result;
        }
    }
    public List<OAFMonitorDto> getOrdiniByOperatore() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d = sdf.parse("2024-02-21");
        return OrdineFornitore.find("SELECT createUser, COUNT(*) FROM OrdineFornitore WHERE createDate>=:d GROUP BY createUser",
                Parameters.with("d", d)).project(OAFMonitorDto.class).list();
    }
}
