package it.calolenoci.mapper;

import it.calolenoci.entity.RegistroAzioni;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import java.util.Date;


@ApplicationScoped
public class RegistroAzioniMapper {

    public RegistroAzioni fromDtoToEntity(Integer anno, String serie, Integer progressivo, String username, String azione, Integer rigo, String tono, Double quantita, Double qtaRiservata, Double qtaProntoConsegna) {
        RegistroAzioni r = new RegistroAzioni();
        r.setAnno(anno);
        r.setProgressivo(progressivo);
        if (rigo != null) {
            r.setRigo(rigo);
        }
        r.setSerie(serie);
        r.setCreateDate(new Date());
        r.setUsername(username);
        r.setAzione(azione);
        if (quantita != null) {
            r.setQuantita(quantita);
        }
        if (StringUtils.isNotBlank(tono)) {
            r.setTono(tono);
        }
        if(qtaProntoConsegna != null) {
            r.setQtaProntoConsegna(qtaProntoConsegna);
        }
        if(qtaRiservata != null) {
            r.setQtaRiservata(qtaRiservata);
        }
        return r;
    }
}
