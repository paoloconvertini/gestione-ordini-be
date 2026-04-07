package it.calolenoci.service;

import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.ResiduoDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ResiduoService {

    @PersistenceContext
    EntityManager em;

    /**
     * Metodo CORE: calcola residui SAFE
     */
    @Transactional
    public List<ResiduoDto> calcolaResidui(List<OrdineDettaglioDto> righeOrdine) {

        if (righeOrdine == null || righeOrdine.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> ids = righeOrdine.stream()
                .map(OrdineDettaglioDto::getProgrGenerale)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Integer, Double> mappaBollato = getQtaBollata(ids);

        List<ResiduoDto> result = new ArrayList<>();

        for (OrdineDettaglioDto dto : righeOrdine) {

            Integer id = dto.getProgrGenerale();
            if (id == null) continue;

            double bollato = mappaBollato.getOrDefault(id, 0D);
            double ordinato = dto.getQuantita() != null ? dto.getQuantita() : 0D;

            double residuo = ordinato - bollato;
            if (residuo < 0) residuo = 0;

            ResiduoDto r = new ResiduoDto();

            // identificativi
            r.setAnno(dto.getAnno());
            r.setSerie(dto.getSerie());
            r.setProgressivo(dto.getProgressivo());
            r.setRigo(dto.getRigo());
            r.setProgrGenerale(id);

            // articolo
            r.setFArticolo(dto.getFArticolo());
            r.setFDescrArticolo(dto.getFDescrArticolo());

            // quantità
            r.setQtaOrdinata(ordinato);
            r.setQtaBollata(bollato);
            r.setResiduo(residuo);

            result.add(r);
        }

        return result;
    }

    /**
     * Metodo comodo: restituisce MAP COMPLETA e SAFE
     */
    public Map<Integer, ResiduoDto> calcolaResiduiMap(List<OrdineDettaglioDto> righeOrdine) {
        return calcolaResidui(righeOrdine).stream()
                .collect(Collectors.toMap(
                        ResiduoDto::getProgrGenerale,
                        r -> r,
                        (r1, r2) -> r1 // fallback sicurezza
                ));
    }

    /**
     * Recupera quantità bollata dalle fatture (serie B)
     */
    private Map<Integer, Double> getQtaBollata(List<Integer> ids) {

        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> rows = em.createQuery(
                "SELECT f2.progrOrdCli, SUM(f2.quantita) " +
                        "FROM FattureDettaglio f2, Fatture f " +
                        "WHERE f.anno = f2.anno " +
                        "AND f.serie = f2.serie " +
                        "AND f.progressivo = f2.progressivo " +
                        "AND f.serie = 'B' " +
                        "AND f2.progrOrdCli IN :ids " +
                        "GROUP BY f2.progrOrdCli",
                Object[].class
        ).setParameter("ids", ids).getResultList();

        Map<Integer, Double> map = new HashMap<>();

        for (Object[] row : rows) {
            Integer id = (Integer) row[0];
            Double sum = (Double) row[1];
            map.put(id, sum != null ? sum : 0D);
        }

        return map;
    }
}