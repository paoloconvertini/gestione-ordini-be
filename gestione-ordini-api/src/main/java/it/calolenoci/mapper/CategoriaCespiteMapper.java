package it.calolenoci.mapper;

import it.calolenoci.dto.CategoriaCespiteResponse;
import it.calolenoci.entity.CategoriaCespite;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CategoriaCespiteMapper {

    public void fromDtoToEntity(CategoriaCespite categoriaCespite, CategoriaCespiteResponse response) {
        categoriaCespite.setTipoCespite(response.getTipoCespite());
        categoriaCespite.setDescrizione(response.getDescrizione());
        categoriaCespite.setCodice(response.getCodice());
        categoriaCespite.setAmmConto(response.getAmmConto());
        categoriaCespite.setAmmGruppo(response.getAmmGruppo());
        categoriaCespite.setPercAmmortamento(response.getPercAmmortamento());
        categoriaCespite.setFondoGruppo(response.getFondoGruppo());
        categoriaCespite.setFondoConto(response.getFondoConto());
        categoriaCespite.setCostoGruppo(response.getCostoGruppo());
        categoriaCespite.setCostoConto(response.getCostoConto());
        categoriaCespite.setPlusGruppo(response.getPlusGruppo());
        categoriaCespite.setPlusConto(response.getPlusConto());
        categoriaCespite.setMinusGruppo(response.getMinusGruppo());
        categoriaCespite.setMinusConto(response.getMinusConto());
    }
}
