package it.calolenoci.service;

import it.calolenoci.entity.RegistroAzioni;
import it.calolenoci.enums.AzioneEnum;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.Date;

@ApplicationScoped
public class EventoService {

    @Transactional
    public void save(Integer anno, String serie, Integer progressivo, String username, AzioneEnum azioneEnum, Integer rigo) {
        RegistroAzioni r = new RegistroAzioni();
        r.setAnno(anno);
        r.setSerie(serie);
        r.setProgressivo(progressivo);
        r.setAzione(azioneEnum.getDesczrizione());
        r.setUser(username);
        r.setData(new Date());
        if(rigo != null) {
            r.setRigo(rigo);
        }
        r.persist();
    }


}
