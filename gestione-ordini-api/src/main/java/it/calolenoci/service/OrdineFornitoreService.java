package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.ArticoloDto;
import it.calolenoci.dto.OrdineFornitoreDto;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.entity.OrdineFornitore;
import it.calolenoci.entity.OrdineFornitoreDettaglio;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.time.Year;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrdineFornitoreService {

    @Transactional
    public List<String> save(Integer anno, String serie, Integer progressivo, String user) throws Exception {
        List<String> fornitori = new ArrayList<>();
        try {
            List<OrdineFornitoreDettaglio> ordineFornitoreDettaglios = new ArrayList<>();
            List<OrdineFornitore> fornitoreList = new ArrayList<>();
            Map<String, List<ArticoloDto>> mapArticoli = OrdineDettaglio.find("select pc.intestazione, a.articolo , a.descrArticolo, a.descrArtSuppl, a.unitaMisura, a.prezzoBase, a.costoBase, " +
                            "fa.fornitoreArticoloId.gruppo as gruppoConto, fa.fornitoreArticoloId.conto as sottoConto,pc.codPagamento, pc.banca, o.progrGenerale, o.rigo, o.quantita, o.fColli " +
                            " from OrdineDettaglio o " +
                            " inner join Articolo a ON a.articolo = o.fArticolo " +
                            "          inner join FornitoreArticolo fa ON  o.fArticolo = fa.fornitoreArticoloId.articolo " +
                            "          inner JOIN PianoConti pc ON fa.fornitoreArticoloId.gruppo = pc.gruppoConto and fa.fornitoreArticoloId.conto = pc.sottoConto " +
                            "          where o.progressivo = :progressivo and o.anno = :anno and o.serie = :serie and o.geFlagNonDisponibile = 'T' " +
                            "           group by pc.intestazione, a.articolo , a.descrArticolo, a.descrArtSuppl, a.unitaMisura, a.prezzoBase, a.costoBase, " +
                            "                    fa.fornitoreArticoloId.gruppo, fa.fornitoreArticoloId.conto,pc.codPagamento, pc.banca, o.progrGenerale, o.rigo, o.quantita, o.fColli",
                    Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)).project(ArticoloDto.class).stream().collect(Collectors.groupingBy(ArticoloDto::getIntestazione));
            if (mapArticoli.isEmpty()) {
                return fornitori;
            }
            fornitori.addAll(mapArticoli.keySet());
            int index = 1;
            Integer progressivoForn = OrdineFornitore.find("SELECT CASE WHEN MAX(progressivo) IS NULL THEN 0 ELSE MAX(progressivo) END FROM OrdineFornitore o").project(Integer.class).firstResult();
            Integer progressivoFornDettaglio = OrdineFornitoreDettaglio.find("SELECT CASE WHEN MAX(progrGenerale) IS NULL THEN 0 ELSE MAX(progrGenerale) END FROM OrdineFornitoreDettaglio o").project(Integer.class).firstResult();
            for (String sottoConto : mapArticoli.keySet()) {
                List<ArticoloDto> articoloDtoList = mapArticoli.get(sottoConto);
                Log.debug("Trovati " + articoloDtoList.size() + " articoli per il fornitore " + sottoConto);
                int prog = progressivoForn + index;
                if (!articoloDtoList.isEmpty()) {
                    ArticoloDto articoloDto = articoloDtoList.get(0);
                    OrdineFornitore ordineFornitore = new OrdineFornitore();
                    ordineFornitore.setProgressivo(prog);
                    ordineFornitore.setAnno(Year.now().getValue());
                    ordineFornitore.setSerie("B");
                    ordineFornitore.setDataOrdine(new Date());
                    ordineFornitore.setCreateDate(new Date());
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
                articoloDtoList.forEach(a -> {
                    String nota = "Riferimento n. " + anno + "/" + serie + "/" + progressivo + "-" + a.getRigo();
                   if(OrdineFornitoreDettaglio.count("nota LIKE :nota", Parameters.with("nota", nota)) == 0){
                       Log.debug("Creo ordine per articolo: " + a.getArticolo() + " - " + a.getDescrArticolo());
                       OrdineFornitoreDettaglio fornitoreDettaglio = new OrdineFornitoreDettaglio();
                        fornitoreDettaglio.setProgressivo(prog);
                        fornitoreDettaglio.setProgrGenerale(progressivoFornDettaglio + articoloDtoList.indexOf(a) + 1);
                        fornitoreDettaglio.setAnno(Year.now().getValue());
                        fornitoreDettaglio.setSerie("B");
                        fornitoreDettaglio.setRigo(articoloDtoList.indexOf(a) + 1);
                        fornitoreDettaglio.setPid(a.getProgrGenerale());
                        fornitoreDettaglio.setOArticolo(a.getArticolo());
                        fornitoreDettaglio.setODescrArticolo(a.getDescrArticolo());
                        fornitoreDettaglio.setOPrezzo(a.getPrezzoBase());
                        fornitoreDettaglio.setOQuantita(a.getQuantita());
                        fornitoreDettaglio.setOUnitaMisura(a.getUnitaMisura());
                        fornitoreDettaglio.setOColli(a.getColli());
                        fornitoreDettaglio.setOCodiceIva("22");
                        fornitoreDettaglio.setNota(nota);
                        Log.debug("Chiave per articolo " + fornitoreDettaglio.getOArticolo() + ": " + fornitoreDettaglio.getAnno()+fornitoreDettaglio.getSerie()
                        + fornitoreDettaglio.getProgressivo() + fornitoreDettaglio.getRigo());
                        ordineFornitoreDettaglios.add(fornitoreDettaglio);
                        OrdineDettaglio.update("geFlagNonDisponibile = 'F', geFlagOrdinato = 'T' where anno = :anno " +
                                "and serie = :serie and progressivo = :progressivo and rigo = :rigo", Parameters.with("anno", anno)
                                .and("serie", serie).and("progressivo", progressivo).and("rigo", a.getRigo()));
                  }
                });
                index++;
            }
            long count = OrdineDettaglio.count("geFlagNonDisponibile = 'T' and anno = :anno " +
                    " and serie = :serie and progressivo = :progressivo ", Parameters.with("anno", anno)
                    .and("serie", serie).and("progressivo", progressivo));
            if(count == 0) {
                Ordine.update("geStatus = 'INCOMPLETO' where anno = :anno " +
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
