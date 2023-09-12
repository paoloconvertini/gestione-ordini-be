package it.calolenoci.mapper;

import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.GoOrdineDettaglio;
import it.calolenoci.entity.Magazzino;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class MagazzinoMapper {

    public Magazzino buildMagazzino(){
        Magazzino m = new Magazzino();

        return m;
    }
}
