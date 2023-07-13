package it.calolenoci.mapper;

import it.calolenoci.dto.ArticoloDto;
import it.calolenoci.dto.OrdineFornitoreDettaglioDto;
import it.calolenoci.entity.OrdineFornitoreDettaglio;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OafArticoloMapper {

    public void viewToEntity(OrdineFornitoreDettaglio entity, OrdineFornitoreDettaglioDto dto) {
        entity.setODescrArticolo(dto.getODescrArticolo());
        entity.setOQuantita(dto.getOQuantita());
        entity.setOQuantitaV(dto.getOQuantita());
       entity.setOPrezzo(dto.getOPrezzo());
       entity.setFScontoArticolo(dto.getFScontoArticolo());
       entity.setScontoF1(dto.getScontoF1());
       entity.setScontoF2(dto.getScontoF2());
       entity.setFScontoP(dto.getFScontoP());
        if(dto.getOQuantita() != null && dto.getOPrezzo() != null){
            entity.setValoreTotale(dto.getOQuantita()*dto.getOPrezzo());
        }
    }

    public OrdineFornitoreDettaglio fromDtoToEntity(ArticoloDto dto) {
        OrdineFornitoreDettaglio entity = new OrdineFornitoreDettaglio();
        entity.setAnno(dto.getAnno());
        entity.setSerie(dto.getSerie());
        entity.setProgressivo(dto.getProgressivo());
        entity.setRigo(dto.getRigo());
        entity.setTipoRigo(dto.getTipoRigo());
        entity.setOArticolo(StringUtils.isNotBlank(dto.getArticolo())?dto.getArticolo():"");
        entity.setODescrArticolo(dto.getDescrArticolo());
        if("C".equals(dto.getTipoRigo())){
            entity.setPid(null);
            entity.setOCodiceIva(null);
            entity.setNota(null);
        } else {
            entity.setMagazzino("B");
            entity.setProvenienza("C");
        }
        entity.setOQuantita(dto.getQuantita());
        entity.setOQuantitaV(dto.getQuantita());
        entity.setOUnitaMisura(dto.getUnitaMisura());
        entity.setOPrezzo(dto.getPrezzoBase());
        entity.setFScontoArticolo(dto.getFScontoArticolo());
        entity.setScontoF1(dto.getScontoF1());
        entity.setScontoF2(dto.getScontoF2());
        entity.setFScontoP(dto.getFScontoP());
        Integer progrGenerale = OrdineFornitoreDettaglio.find("SELECT MAX(progrGenerale) FROM OrdineFornitoreDettaglio").project(Integer.class).singleResult();
        entity.setProgrGenerale(progrGenerale + 1);
        if(dto.getQuantita() != null && dto.getPrezzoBase() != null){
            entity.setValoreTotale(dto.getQuantita()*dto.getPrezzoBase());
        }
        return entity;
    }
}
