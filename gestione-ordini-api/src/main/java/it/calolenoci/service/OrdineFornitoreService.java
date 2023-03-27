package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.ArticoloDto;
import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.entity.OrdineFornitore;
import it.calolenoci.entity.OrdineFornitoreDettaglio;
import it.calolenoci.enums.StatoOrdineEnum;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Year;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrdineFornitoreService {


    @Transactional
    public void save(Integer anno, String serie, Integer progressivo) throws Exception {
        try {
            List<OrdineFornitoreDettaglio> ordineFornitoreDettaglios = new ArrayList<>();
            List<OrdineFornitore> fornitoreList = new ArrayList<>();
            Map<String, List<ArticoloDto>> mapArticoli = OrdineDettaglio.find("select a.articolo , a.descrArticolo, a.descrArtSuppl, a.unitaMisura, a.prezzoBase, a.costoBase, " +
                            "m.gruppoMag as gruppoConto, m.contoMag as sottoConto,pc.codPagamento, pc.banca, MAX(m.dataInserimento) as dataInserimento, o.progrGenerale, o.rigo, o.quantita " +
                            " from OrdineDettaglio o " +
                            " left join Articolo a ON a.articolo = o.fArticolo and a.descrArticolo = o.fDescrArticolo " +
                            "          left join Magazzino m ON  o.fArticolo = m.mArticolo and o.fDescrArticolo = a.descrArticolo " +
                            "          left JOIN PianoConti pc ON m.gruppoMag = pc.gruppoConto and m.contoMag = pc.sottoConto " +
                            "          where o.progressivo = :progressivo and o.anno = :anno and o.serie = :serie and o.geFlagNonDisponibile = true " +
                            "           and pc.gruppoConto = 2351 " +
                            "           group by a.articolo , a.descrArticolo, a.descrArtSuppl, a.unitaMisura, a.prezzoBase, a.costoBase, " +
                            "                    m.gruppoMag, m.contoMag,pc.codPagamento, pc.banca, o.progrGenerale, o.rigo",
                    Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)).project(ArticoloDto.class).stream().collect(Collectors.groupingBy(ArticoloDto::getSottoConto));

            int index = 1;
            Integer progressivoForn = OrdineFornitore.find("SELECT CASE WHEN MAX(progressivo) IS NULL THEN 0 ELSE MAX(progressivo) END FROM OrdineFornitore o").project(Integer.class).firstResult();
            Integer progressivoFornDettaglio = OrdineFornitoreDettaglio.find("SELECT CASE WHEN MAX(progrGenerale) IS NULL THEN 0 ELSE MAX(progrGenerale) END FROM OrdineFornitoreDettaglio o").project(Integer.class).firstResult();
            List<ArticoloDto> allArticoloDto = new ArrayList<>();
            for (String sottoConto : mapArticoli.keySet()) {
                List<ArticoloDto> articoloDtoList = mapArticoli.get(sottoConto);
                allArticoloDto.addAll(articoloDtoList);
                if (!articoloDtoList.isEmpty()) {
                    ArticoloDto articoloDto = articoloDtoList.get(0);
                    OrdineFornitore ordineFornitore = new OrdineFornitore();
                    ordineFornitore.setProgressivo(progressivoForn + index);
                    ordineFornitore.setAnno(Year.now().getValue());
                    ordineFornitore.setSerie("B");
                    ordineFornitore.setDataOrdine(new Date());
                    ordineFornitore.setCreateDate(new Date());
                    ordineFornitore.setDataModifica(new Date());
                    ordineFornitore.setMagazzino("B");
                    ordineFornitore.setNumRevisione(0);
                    ordineFornitore.setGruppo(articoloDto.getGruppoConto());
                    ordineFornitore.setConto(sottoConto);
                    ordineFornitore.setCodicePagamento(articoloDto.getCodPagamento());
                    ordineFornitore.setBancaPagamento(articoloDto.getBanca());
                    fornitoreList.add(ordineFornitore);
                }
                index++;
            }
            allArticoloDto.forEach(a -> {
                OrdineFornitoreDettaglio fornitoreDettaglio = new OrdineFornitoreDettaglio();
                fornitoreDettaglio.setProgressivo(progressivoForn);
                fornitoreDettaglio.setProgrGenerale(progressivoFornDettaglio + allArticoloDto.indexOf(a) + 1);
                fornitoreDettaglio.setAnno(Year.now().getValue());
                fornitoreDettaglio.setSerie("B");
                fornitoreDettaglio.setRigo(allArticoloDto.indexOf(a) + 1);
                fornitoreDettaglio.setPid(a.getProgrGenerale());
                fornitoreDettaglio.setOArticolo(a.getArticolo());
                fornitoreDettaglio.setODescrArticolo(a.getDescrArticolo());
                fornitoreDettaglio.setOPrezzo(a.getPrezzoBase());
                fornitoreDettaglio.setOQuantita(a.getQuantita());
                fornitoreDettaglio.setOUnitaMisura(a.getUnitaMisura());
                fornitoreDettaglio.setOColli(a.getColli());
                fornitoreDettaglio.setOCodiceIva("22");
                String nota = "Riferimento n. " + anno + "/" + serie + "/" + progressivo + "-" + a.getRigo();
                fornitoreDettaglio.setNota(nota);
                ordineFornitoreDettaglios.add(fornitoreDettaglio);
                OrdineDettaglio.update("geFlagNonDisponibile = 'F', geFlagOrdinato = 'T' where anno = :anno " +
                        "and serie = :serie and progressivo = :progressivo and rigo = :rigo", Parameters.with("anno", anno)
                        .and("serie", serie).and("progressivo", progressivo).and("rigo", a.getRigo()));
            });
            OrdineFornitoreDettaglio.persist(ordineFornitoreDettaglios);
            OrdineFornitore.persist(fornitoreList);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

}
