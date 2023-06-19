package it.calolenoci.mapper;

import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.OrdineFornitoreDettaglioDto;
import it.calolenoci.entity.FornitoreArticolo;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.entity.OrdineFornitoreDettaglio;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OafArticoloMapper {
    public void viewToEntity(OrdineFornitoreDettaglio entity, OrdineFornitoreDettaglioDto dto) {
       entity.setOQuantita(dto.getOQuantita());
       entity.setOPrezzo(dto.getOPrezzo());
       entity.setScontoF1(dto.getScontoF1());
       entity.setScontoF2(dto.getScontoF2());
       entity.setFScontoP(dto.getFScontoP());
    }

    public OrdineFornitoreDettaglio viewToEntity(Integer anno, String serie, Integer progressivo, OrdineFornitoreDettaglioDto dto) {
        OrdineFornitoreDettaglio entity = new OrdineFornitoreDettaglio();

        entity.setAnno(anno);
        entity.setSerie(serie);
        entity.setProgressivo(progressivo);
        entity.setRigo(dto.getRigo());
        entity.setTipoRigo(dto.getTipoRigo());
        entity.setOArticolo(dto.getOArticolo());
        entity.setODescrArticolo(dto.getODescrArticolo());
        entity.setOQuantita(dto.getOQuantita());
        entity.setOUnitaMisura(dto.getOUnitaMisura());
        entity.setOPrezzo(dto.getOPrezzo());
        entity.setScontoF1(dto.getScontoF1());
        entity.setScontoF2(dto.getScontoF2());
        entity.setFScontoP(dto.getFScontoP());
        entity.setProgrGenerale(0);
        return entity;
    }
}
