package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.RegistroAzioniDto;
import it.calolenoci.entity.RegistroAzioni;
import it.calolenoci.enums.AzioneEnum;
import it.calolenoci.mapper.RegistroAzioniMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class EventoService {


    @Transactional
    public void save(Integer anno, String serie, Integer progressivo, String username, AzioneEnum azioneEnum, Integer rigo) {
        RegistroAzioni r = new RegistroAzioni();
        r.setAnno(anno);
        r.setSerie(serie);
        r.setProgressivo(progressivo);
        r.setAzione(azioneEnum.getDesczrizione());
        r.setUsername(username);
        r.setCreateDate(new Date());
        if (rigo != null) {
            r.setRigo(rigo);
        }
        r.persist();
    }

    public List<RegistroAzioniDto> getByAnnoSerieProgressivoRigo(Integer anno, String serie, Integer progressivo, Integer rigo) {
        return RegistroAzioni
                .find("anno = :anno AND serie = :serie AND progressivo =: progressivo AND rigo = :rigo",
                        Sort.descending("createDate"),
                Parameters.with("anno", anno).and("serie", serie)
                        .and("progressivo", progressivo).and("rigo", rigo)).project(RegistroAzioniDto.class).list();
    }

}
