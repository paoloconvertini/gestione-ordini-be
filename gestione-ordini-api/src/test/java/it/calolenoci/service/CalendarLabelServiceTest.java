package it.calolenoci.service;

import it.calolenoci.entity.AttivitaMontaggio;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalendarLabelServiceTest {

    private final CalendarLabelService service = new CalendarLabelService();

    @Test
    void componeClienteEIndirizzoCompleto() {
        AttivitaMontaggio attivita = new AttivitaMontaggio();
        attivita.setNomeCliente("Mario Rossi");
        attivita.setVia("Via Roma");
        attivita.setCivico("10");
        attivita.setComune("Ceglie Messapica");

        assertEquals("Mario Rossi", service.buildClienteLabel(attivita));
        assertEquals("Via Roma 10, Ceglie Messapica", service.buildIndirizzoLabel(attivita));
    }

    @Test
    void componeIndirizzoConIParzialiDisponibili() {
        AttivitaMontaggio soloVia = new AttivitaMontaggio();
        soloVia.setVia("Via Bari");
        soloVia.setCivico(" ");
        assertEquals("Via Bari", service.buildIndirizzoLabel(soloVia));

        AttivitaMontaggio soloComune = new AttivitaMontaggio();
        soloComune.setComune("Ostuni");
        assertEquals("Ostuni", service.buildIndirizzoLabel(soloComune));

        assertEquals("", service.buildIndirizzoLabel(new AttivitaMontaggio()));
    }

    @Test
    void formattaDataEOraDellAttivita() {
        AttivitaMontaggio attivita = new AttivitaMontaggio();
        attivita.setDataOraDa(LocalDateTime.of(2026, 9, 8, 9, 5));
        attivita.setDataOraA(LocalDateTime.of(2026, 9, 8, 11, 30));

        assertEquals("08/09 09:05 - 11:30", service.buildDataOraLabel(attivita));
    }

    @Test
    void nonFormattaIntervalliIncompleti() {
        AttivitaMontaggio attivita = new AttivitaMontaggio();
        attivita.setDataOraDa(LocalDateTime.of(2026, 9, 8, 9, 0));

        assertEquals("", service.buildDataOraLabel(attivita));
    }

}
