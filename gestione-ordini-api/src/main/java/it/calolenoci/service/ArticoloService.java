package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.mapper.ArticoloMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ArticoloService {


    @Inject
    ArticoloMapper mapper;

    public List<OrdineDettaglioDto> findById(Integer anno, String serie, Integer progressivo) {
        List<OrdineDettaglioDto> list = new ArrayList<>();
        List<OrdineDettaglio> articoli = OrdineDettaglio.find("anno = :anno AND serie = :serie AND progressivo = :progressivo", Sort.ascending("rigo"),
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)).list();
        articoli.forEach(a -> list.add(mapper.fromEntityToDto(a)));
        return list;
    }
}
