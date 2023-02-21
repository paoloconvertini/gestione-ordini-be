package it.calolenoci.service;

import it.calolenoci.entity.RegistroAzioni;
import it.calolenoci.enums.AzioneEnum;

import javax.enterprise.context.ApplicationScoped;
import java.util.Date;

@ApplicationScoped
public class EventoService {

    public void save(Integer anno, String serie, Integer progressivo, AzioneEnum azioneEnum) {
        RegistroAzioni r = new RegistroAzioni();
        r.setAnno(anno);
        r.setSerie(serie);
        r.setProgressivo(progressivo);
        r.setAzione(azioneEnum.getDesczrizione());
        r.setData(new Date());
        r.persist();
    }


}
