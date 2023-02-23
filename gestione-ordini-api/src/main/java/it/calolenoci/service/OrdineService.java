package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.PianoConti;
import it.calolenoci.enums.StatoOrdineEnum;
import it.calolenoci.mapper.OrdineTestataMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class OrdineService {

    @Inject
    OrdineTestataMapper ordineTestataMapper;

    @Inject
    ArticoloService articoloService;

    public List<OrdineDTO> findAllByStatus(String status) {
        List<OrdineDTO> list = new ArrayList<>();
        if(StatoOrdineEnum.INCOMPLETO.getDesczrizione().equals(status) ||
        StatoOrdineEnum.COMPLETO.getDesczrizione().equals(status)) {
            checkStatusDettaglio();
        }
        List<Ordine> ordini = Ordine.findOrdiniByStatus(status);
        ordini.forEach(o -> list.add(ordineTestataMapper
                .fromEntityToDto(o, PianoConti.findByGruppoAndSottoConto(o.getGruppoCliente(), o.getContoCliente()))));
        return list;
    }

    private void checkStatusDettaglio() {
        List<Ordine> ordineList = Ordine.findOrdiniByStatus();
        ordineList.forEach(o-> {
            if(articoloService.findAnyNoStatus(o.getAnno(), o.getSerie(), o.getProgressivo())) {
                o.setGeStatus(null);
            }
        });
    }

    public OrdineDTO findById(Integer anno, String serie, Integer progressivo) {
        Ordine o = Ordine.findByOrdineId(anno, serie, progressivo);
        return ordineTestataMapper.fromEntityToDto(o,
                PianoConti.findByGruppoAndSottoConto(o.getGruppoCliente(), o.getContoCliente()));
    }

    @Transactional
    public void changeStatus(Integer anno, String serie, Integer progressivo, StatoOrdineEnum statoOrdineEnum) {
        Ordine ordine = Ordine.findByOrdineId(anno, serie, progressivo);
        ordine.setGeStatus(statoOrdineEnum.getDesczrizione());
        ordine.persist();
    }

}
