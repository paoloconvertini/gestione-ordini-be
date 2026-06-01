package it.calolenoci.service;

import it.calolenoci.dto.AttivitaMontaggioSearchDto;
import it.calolenoci.dto.PageAttivitaMontaggioDto;
import it.calolenoci.entity.AttivitaMontaggio;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class IcsService {

    private static final String NL = "\r\n";

    @Inject
    CalendarLabelService calendarLabelService;

    public String buildMontaggiCalendar(PageAttivitaMontaggioDto result) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR").append(NL);
        sb.append("VERSION:2.0").append(NL);
        sb.append("PRODID:-//GESTIONE_ORDINI//Agenda Montaggi//IT").append(NL);
        sb.append("CALSCALE:GREGORIAN").append(NL);
        sb.append("METHOD:PUBLISH").append(NL);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

        for (AttivitaMontaggioSearchDto dto : result.getList()) {
            AttivitaMontaggio entity = AttivitaMontaggio.findById(dto.getId());
            if (entity == null) {
                continue;
            }
            if (entity.getDataOraDa() == null || entity.getDataOraA() == null) {
                continue;
            }
            if (entity.getDataOraA().isBefore(entity.getDataOraDa())) {
                continue;
            }
            sb.append("BEGIN:VEVENT").append(NL);
            sb.append("UID:")
                    .append(entity.getId())
                    .append("@GO_")
                    .append(NL);
            sb.append("DTSTAMP:")
                    .append(LocalDateTime.now().format(formatter))
                    .append(NL);
            sb.append("DTSTART:")
                    .append(entity.getDataOraDa().format(formatter))
                    .append(NL);
            sb.append("DTEND:")
                    .append(entity.getDataOraA().format(formatter))
                    .append(NL);
            sb.append("SUMMARY:")
                    .append(escape(buildSummary(entity)))
                    .append(NL);
            String location = calendarLabelService.buildIndirizzoLabel(entity);
            if (StringUtils.isNotBlank(location)) {
                sb.append("LOCATION:")
                        .append(escape(location))
                        .append(NL);
            }
            StringBuilder description = new StringBuilder();
            if (StringUtils.isNotBlank(entity.getTelefono())) {
                description.append("Telefono: ")
                        .append(entity.getTelefono())
                        .append("\\n");
            }
            String attivita = calendarLabelService.buildAttivitaLabel(entity);
            if (StringUtils.isNotBlank(attivita)) {
                description.append("Attività: ")
                        .append(attivita)
                        .append("\\n");
            }
            if (StringUtils.isNotBlank(entity.getNote())) {
                description.append("Note: ").append(entity.getNote());
            }
            if (!description.isEmpty()) {
                sb.append("DESCRIPTION:")
                        .append(escape(description.toString()))
                        .append(NL);
            }
            sb.append("END:VEVENT").append(NL);
        }
        sb.append("END:VCALENDAR").append(NL);
        return sb.toString();
    }

    private String buildSummary(AttivitaMontaggio entity) {

        if (StringUtils.isNotBlank(entity.getNomeCliente())) {
            return entity.getNomeCliente();
        }

        return "Montaggio";
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r", "")
                .replace("\n", "\\n");
    }
}