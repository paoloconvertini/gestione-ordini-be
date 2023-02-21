package it.calolenoci.service;

import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.PianoConti;
import it.calolenoci.mapper.OrdineTestataMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class OrdineService {

    @Inject
    OrdineTestataMapper ordineTestataMapper;

    public List<OrdineDTO> findAllByStatus(String status) {
        List<OrdineDTO> list = new ArrayList<>();
        List<Ordine> ordini = Ordine.findOrdiniByStatus(status);
        ordini.forEach(o -> list.add(ordineTestataMapper
                .fromEntityToDto(o, PianoConti.findByGruppoAndSottoConto(o.getGruppoCliente(), o.getContoCliente()))));
        return list;
    }

    public OrdineDTO findById(Integer anno, String serie, Integer progressivo) {
        Ordine.findByOrdineId(anno, serie,  progressivo);
        Ordine o = Ordine.findByOrdineId(anno, serie, progressivo);
        return ordineTestataMapper.fromEntityToDto(o,
                PianoConti.findByGruppoAndSottoConto(o.getGruppoCliente(), o.getContoCliente()));
    }

}
