package it.calolenoci.mapper;

import it.calolenoci.dto.OrdineFornitoreDettaglioDto;
import it.calolenoci.entity.OrdineFornitoreDettaglio;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OafArticoloMapper {

    public void viewToEntity(OrdineFornitoreDettaglio entity, OrdineFornitoreDettaglioDto dto) {
        entity.setODescrArticolo(dto.getODescrArticolo());
        entity.setOQuantita(dto.getOQuantita());
       entity.setOPrezzo(dto.getOPrezzo());
       entity.setFScontoArticolo(dto.getFScontoArticolo());
       entity.setScontoF1(dto.getScontoF1());
       entity.setScontoF2(dto.getScontoF2());
       entity.setFScontoP(dto.getFScontoP());
    }

    public void aggiornaRigo(OrdineFornitoreDettaglio entity, OrdineFornitoreDettaglioDto dto) {

        entity.setTipoRigo(dto.getTipoRigo());
        entity.setOArticolo(StringUtils.isNotBlank(dto.getOArticolo())?dto.getOArticolo():"");
        entity.setODescrArticolo(dto.getODescrArticolo());
        if("C".equals(dto.getTipoRigo())){
            entity.setPid(null);
            entity.setOCodiceIva(null);
            entity.setNota(null);
        }
        entity.setOQuantita(dto.getOQuantita());
        entity.setOUnitaMisura(dto.getOUnitaMisura());
        entity.setOPrezzo(dto.getOPrezzo());
        entity.setFScontoArticolo(dto.getFScontoArticolo());
        entity.setScontoF1(dto.getScontoF1());
        entity.setScontoF2(dto.getScontoF2());
        entity.setFScontoP(dto.getFScontoP());
        Integer progrGenerale = OrdineFornitoreDettaglio.find("SELECT MAX(progrGenerale) FROM OrdineFornitoreDettaglio").project(Integer.class).singleResult();
        entity.setProgrGenerale(progrGenerale + 1);
    }
}
