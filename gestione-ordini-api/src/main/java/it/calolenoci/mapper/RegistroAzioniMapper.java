package it.calolenoci.mapper;

import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.RegistroAzioniDto;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.entity.RegistroAzioni;

import javax.enterprise.context.ApplicationScoped;
import java.util.Date;


@ApplicationScoped
public class RegistroAzioniMapper {

    public RegistroAzioni fromDtoToEntity (Integer anno, String serie, Integer progressivo, String username, String azione, Integer rigo) {
        RegistroAzioni r = new RegistroAzioni();
        r.setAnno(anno);
        r.setProgressivo(progressivo);
        if(rigo != null) {
            r.setRigo(rigo);
        }
        r.setSerie(serie);
        r.setData(new Date());
        r.setUser(username);
        r.setAzione(azione);
        return r;
    }

}
