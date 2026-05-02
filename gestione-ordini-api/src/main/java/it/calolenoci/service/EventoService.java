package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.RegistroAzioniDto;
import it.calolenoci.entity.RegistroAzioni;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class EventoService {

    public List<RegistroAzioniDto> getByAnnoSerieProgressivoRigo(Integer anno, String serie, Integer progressivo, Integer rigo) {
        return RegistroAzioni
                .find("Select " +
                                "r.anno as anno, " +
                                "r.serie as serie, " +
                                "r.progressivo as progressivo, " +
                                "r.rigo as rigo, " +
                                "r.username as username, " +
                                "r.createDate as createDate, " +
                                "r.azione as azione, " +
                                "r.quantita as quantita, " +
                                "r.tono as tono, " +
                                "r.qtaRiservata as qtaRiservata, " +
                                "r.qtaProntoConsegna as qtaProntoConsegna " +
                                "FROM RegistroAzioni r " +
                                "WHERE anno = :anno AND serie = :serie AND progressivo =: progressivo AND rigo = :rigo",
                        Sort.descending("createDate"),
                Parameters.with("anno", anno).and("serie", serie)
                        .and("progressivo", progressivo).and("rigo", rigo)).project(RegistroAzioniDto.class).list();
    }

}
