package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.RegistroAzioniDto;
import it.calolenoci.entity.RegistroAzioni;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class EventoService {

    public List<RegistroAzioniDto> getByAnnoSerieProgressivoRigo(Integer anno, String serie, Integer progressivo, Integer rigo) {
        return RegistroAzioni
                .find("anno = :anno AND serie = :serie AND progressivo =: progressivo AND rigo = :rigo",
                        Sort.descending("createDate"),
                Parameters.with("anno", anno).and("serie", serie)
                        .and("progressivo", progressivo).and("rigo", rigo)).project(RegistroAzioniDto.class).list();
    }

}
