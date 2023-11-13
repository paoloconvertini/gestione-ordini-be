package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.EmailDto;
import it.calolenoci.entity.PianoConti;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

@ApplicationScoped
public class PianoContiService {

    @Transactional
    public void update(EmailDto dto) {
        PianoConti.update("email = :email WHERE gruppoConto = 1231 AND sottoConto = :sottoConto"
                , Parameters.with("email", dto.getTo()).and("sottoConto", dto.getSottoConto()));
    }
}
