package it.calolenoci.service;

import it.calolenoci.entity.AttivitaMontaggio;
import it.calolenoci.entity.AttivitaMontaggioDett;
import it.calolenoci.entity.TipoAttivitaMontaggio;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class CalendarLabelService {

    public String buildClienteLabel(AttivitaMontaggio entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.getNomeCliente());
        return sb.toString();
    }

    public String buildIndirizzoLabel(AttivitaMontaggio entity) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(entity.getVia())) {
            sb.append(entity.getVia());
            if (StringUtils.isNotBlank(entity.getCivico())) {
                sb.append(" ").append(entity.getCivico());
            }
        }

        if (StringUtils.isNotBlank(entity.getComune())) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(entity.getComune());
        }

        return sb.toString();
    }

    public String buildAttivitaLabel(AttivitaMontaggio entity) {
        List<AttivitaMontaggioDett> dettagli = AttivitaMontaggioDett.list("idAttivitaMontaggio", entity.getId());
        if (dettagli.isEmpty()) {
            return "";
        }
        List<Long> idsTipo = dettagli.stream()
                .map(AttivitaMontaggioDett::getIdTipoAttivita)
                .distinct()
                .toList();

        List<TipoAttivitaMontaggio> tipi =
                TipoAttivitaMontaggio.list(
                        "id in ?1",
                        idsTipo
                );

        Map<Long, String> tipiMap = new HashMap<>();

        for (TipoAttivitaMontaggio t : tipi) {
            tipiMap.put(t.getId(), t.getDescrizione());
        }

        return dettagli.stream()
                .map(d -> tipiMap.get(d.getIdTipoAttivita()))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(", "));
    }

    public String buildDataOraLabel(AttivitaMontaggio entity) {
        if (entity.getDataOraDa() == null || entity.getDataOraA() == null) {
            return "";
        }
        DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter oraFormatter = DateTimeFormatter.ofPattern("HH:mm");
        return entity.getDataOraDa().format(dataFormatter)
                + " "
                + entity.getDataOraDa().format(oraFormatter)
                + " - "
                + entity.getDataOraA().format(oraFormatter);
    }

}
