package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.EmailDto;
import it.calolenoci.dto.PianoContiDto;
import it.calolenoci.entity.PianoConti;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PianoContiService {

    @Transactional
    public void update(EmailDto dto) {
        PianoConti.update("email = :email WHERE gruppoConto = 1231 AND sottoConto = :sottoConto"
                , Parameters.with("email", dto.getTo()).and("sottoConto", dto.getSottoConto()));
    }

    public List<PianoContiDto> searchClienti(String q) {
        if (StringUtils.isBlank(q)) {
            return List.of();
        }
        String query = """
        SELECT p.gruppoConto,
            p.sottoConto,
            p.intestazione,
            p.indirizzo,
            p.localita,
            p.cap,
            p.provincia,
            p.latitudine,
            p.longitudine,
            p.telefono,
            p.cellulare,
            p.email
        FROM PianoConti p
        WHERE p.cliFor = 'C'
        AND (
            LOWER(p.intestazione) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(p.sottoConto) LIKE LOWER(CONCAT('%', :q, '%'))
        )
        ORDER BY p.intestazione
        """;
        Map<String, Object> params = new HashMap<>();
        params.put("q", q);
        return PianoConti.find(query, params).range(0, 19).project(PianoContiDto.class).list();
    }
}
