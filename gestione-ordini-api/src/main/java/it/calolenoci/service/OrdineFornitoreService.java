package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.ArticoloDto;
import it.calolenoci.dto.OrdineFornitoreDto;
import it.calolenoci.entity.*;
import it.calolenoci.enums.AzioneEnum;
import it.calolenoci.mapper.RegistroAzioniMapper;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrdineFornitoreService {

    @Inject
    RegistroAzioniMapper registroAzioniMapper;

    @Transactional
    public List<String> save(Integer anno, String serie, Integer progressivo, String user) throws Exception {
        List<String> fornitori = new ArrayList<>();
        try {
            List<OrdineFornitoreDettaglio> ordineFornitoreDettaglios = new ArrayList<>();
            List<OrdineFornitore> fornitoreList = new ArrayList<>();
            Map<String, List<ArticoloDto>> mapArticoli = OrdineDettaglio.find("select pcc.intestazione as intestazioneCliente, pc.intestazione, a.articolo , " +
                            " a.descrArticolo, a.descrArtSuppl, a.unitaMisura, a.prezzoBase, a.costoBase, " +
                            " fa.fornitoreArticoloId.gruppo as gruppoConto, fa.fornitoreArticoloId.conto as sottoConto,pc.codPagamento," +
                            " pc.banca, o.progrGenerale, o.rigo, o.quantita, o.fColli " +
                            " from OrdineDettaglio o " +
                            " INNER JOIN GoOrdineDettaglio god ON god.anno = o.anno AND god.serie = o.serie AND god.progressivo = o.progressivo AND o.rigo = god.rigo " +
                            " inner join Ordine o2 ON  o2.anno = o.anno AND o2.serie = o.serie AND o2.progressivo = o.progressivo " +
                            " inner join PianoConti pcc ON  pcc.gruppoConto =  o2.gruppoCliente AND pcc.sottoConto = o2.contoCliente " +
                            " inner join Articolo a ON a.articolo = o.fArticolo " +
                            " inner join FornitoreArticolo fa ON  o.fArticolo = fa.fornitoreArticoloId.articolo " +
                            " inner JOIN PianoConti pc ON fa.fornitoreArticoloId.gruppo = pc.gruppoConto and fa.fornitoreArticoloId.conto = pc.sottoConto " +
                            " where o.progressivo = :progressivo and o.anno = :anno and o.serie = :serie and god.flagNonDisponibile = 'T' " +
                            " group by pcc.intestazione, pc.intestazione, a.articolo , a.descrArticolo, a.descrArtSuppl, a.unitaMisura, a.prezzoBase, a.costoBase, " +
                            "                    fa.fornitoreArticoloId.gruppo, fa.fornitoreArticoloId.conto,pc.codPagamento, pc.banca, o.progrGenerale, o.rigo, o.quantita, o.fColli",
                    Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)).project(ArticoloDto.class).stream().collect(Collectors.groupingBy(ArticoloDto::getIntestazione));
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
                    Optional<OrdineFornitoreDto> optOrdine = OrdineFornitore.find("Select f.anno, f.serie, f.progressivo, f.conto FROM OrdineFornitore f " +
                                    "WHERE (f.provvisorio IS NULL OR f.provvisorio = '') and f.conto = :sottoconto ORDER BY f.progressivo DESC",
                            Parameters.with("sottoconto", articoloDto.getSottoConto())).project(OrdineFornitoreDto.class).firstResultOptional();
                    if(optOrdine.isPresent()) {
                        OrdineFornitoreDto ordineFornitoreDto = optOrdine.get();
                        serieOAF = ordineFornitoreDto.getSerie();
                        prog = ordineFornitoreDto.getProgressivo();
                        OrdineFornitore.update("updateDate = :date, dataModifica = :date WHERE anno =:anno AND serie = :serie AND " +
                                " progressivo =:progressivo", Parameters.with("anno", ordineFornitoreDto.getAnno()).and("date", new Date())
                                .and("serie", ordineFornitoreDto.getSerie()).and("progressivo", ordineFornitoreDto.getProgressivo()));
                    } else {
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
                        fornitoreList.add(ordineFornitore);
                    }
                    for (ArticoloDto a : articoloDtoList) {
                        Log.debug("Creo ordine per articolo: " + a.getArticolo() + " - " + a.getDescrArticolo());
                        OrdineFornitoreDettaglio fornitoreDettaglio = new OrdineFornitoreDettaglio();
                        fornitoreDettaglio.setProgressivo(prog);
                        fornitoreDettaglio.setProgrGenerale(++progressivoFornDettaglio);
                        fornitoreDettaglio.setAnno(Year.now().getValue());
                        fornitoreDettaglio.setSerie(serieOAF);
                        Integer rigo = articoloDtoList.indexOf(a);
                        if(optOrdine.isPresent()) {
                             rigo = findRigo(serieOAF, prog);
                        }
                        fornitoreDettaglio.setRigo(rigo + 1);
                        fornitoreDettaglio.setPid(a.getProgrGenerale());
                        fornitoreDettaglio.setOArticolo(a.getArticolo());
                        fornitoreDettaglio.setODescrArticolo(a.getDescrArticolo());
                        fornitoreDettaglio.setOPrezzo(a.getCostoBase());
                        fornitoreDettaglio.setOQuantita(a.getQuantita());
                        fornitoreDettaglio.setOUnitaMisura(a.getUnitaMisura());
                        fornitoreDettaglio.setOColli(a.getColli());
                        fornitoreDettaglio.setOCodiceIva("22");
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
                    ordineFornitoreDettaglios.add(createRigoRiferimento(serieOAF, prog, articoloDto.getIntestazioneCliente(), ordineFornitoreDettaglios.get(ordineFornitoreDettaglios.size()-1).getRigo()));
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
            throw new Exception(e.getMessage());
        }
        return fornitori;
    }

    private Integer findRigo(String serie, Integer progressivo) {
       return OrdineFornitoreDettaglio.find("SELECT MAX(f.rigo) FROM OrdineFornitoreDettaglio f " +
                                " WHERE f.anno = :anno AND f.serie = :serie AND f.progressivo = :progressivo ",
                        Parameters.with("anno", Year.now().getValue())
                                .and("serie", serie).and("progressivo", progressivo))
                .project(Integer.class).singleResult();
    }

    private OrdineFornitoreDettaglio createRigoRiferimento(String serie, Integer progressivo, String intestazione, Integer rigo) {
        OrdineFornitoreDettaglio ordineFornitoreDettaglio = new OrdineFornitoreDettaglio();
        ordineFornitoreDettaglio.setTipoRigo("C");
        ordineFornitoreDettaglio.setRigo(rigo +1);
        ordineFornitoreDettaglio.setAnno(Year.now().getValue());
        ordineFornitoreDettaglio.setSerie(serie);
        ordineFornitoreDettaglio.setProgressivo(progressivo);
        ordineFornitoreDettaglio.setODescrArticolo("Rif. " +  intestazione);
        ordineFornitoreDettaglio.setProgrGenerale(0);
        return ordineFornitoreDettaglio;
    }

    public List<OrdineFornitoreDto> findAllByStatus(String status) {
        String query = " SELECT o.anno,  o.serie,  o.progressivo, o.dataOrdine,  " +
                "p.intestazione,  o.dataConfOrdine, o.numConfOrdine, o.provvisorio " +
                "FROM OrdineFornitore o " +
                "JOIN PianoConti p ON o.gruppo = p.gruppoConto AND o.conto = p.sottoConto ";
        if (StringUtils.isNotBlank(status)) {
            query += " where o.provvisorio =:stato";
            return OrdineFornitore.find(query, Sort.descending("dataOrdine")
                            , Parameters.with("stato", status))
                    .project(OrdineFornitoreDto.class).list();
        } else {
            query += " where o.provvisorio is null";
            return OrdineFornitore.find(query, Sort.descending("dataOrdine"))
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

}
