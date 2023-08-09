package it.calolenoci.scheduler;

import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.quarkus.panache.common.Parameters;
import io.quarkus.scheduler.Scheduled;
import it.calolenoci.dto.Coordinate;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.PianoContiDto;
import it.calolenoci.entity.PianoConti;
import it.calolenoci.service.*;
import net.sf.jasperreports.components.iconlabel.IconLabelElementOdsHandler;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

@ApplicationScoped
public class FetchScheduler {

    @ConfigProperty(name = "bing.map.url")
    String baseUrl;

    @ConfigProperty(name = "bing.map.apiKey")
    String apiKey;

    @Inject
    FatturaService fatturaService;

    @Inject
    OrdineService ordineService;

    @Inject
    ArticoloService articoloService;

    @Inject
    MailService mailService;

    @Inject
    GeoLocationService geoLocationService;


    @Scheduled(every = "${cron.expr:1m}")
    @Transactional
    @TransactionConfiguration(timeout = 500)
    public void update() throws ParseException {
        long inizio = System.currentTimeMillis();
        Integer update;
        List<OrdineDettaglioDto> list = fatturaService.getBolle();
        if (list != null && !list.isEmpty()) {
            Log.debug("Trovate " + list.size() + " bolle");
            update = articoloService.updateArticoliBolle(list);
            Log.info("Aggiornati " + update + " articoli");
            if (update != null && update != 0) {
                ordineService.checkConsegnati();
            }
        }
        articoloService.checkNoBolle();
        ordineService.checkNoProntaConegna();
        long fine = System.currentTimeMillis();
        Log.info("FINE UPDATE CHECK BOLLE: " + (fine - inizio) / 1000 + " sec");
    }

    @Scheduled(every = "${cron.expr.nuovi.ordini:10m}")
    @Transactional
    public void findNuoviOrdini() throws ParseException {
        ordineService.addNuoviOrdini();
    }

    @Scheduled(cron = "${cron.expr.invio.mail}")
    @Transactional
    public void invioMail() {
        mailService.invioMailOrdini();
    }


    @Scheduled(cron = "${cron.expr.geolocation}")
    @TransactionConfiguration(timeout = 50000)
    public void geoLocation() throws ParseException {
        List<PianoContiDto> clienti = ordineService.findClienti();
        for (PianoContiDto pianoContiDto : clienti) {
            updateLatLon(pianoContiDto);
        }
    }

    @Transactional
    public void updateLatLon(PianoContiDto pianoContiDto) {
        try {
            String url = baseUrl + encodeValue(StringUtils.trim(pianoContiDto.getCap()) + " " + pianoContiDto.getLocalita() + " " + pianoContiDto.getIndirizzo()) + "&key=" + apiKey;
            Coordinate lonLat = geoLocationService.getLonLat(url);
            if (lonLat != null) {
                PianoConti.update("latitudine = :lat, longitudine =:lon" +
                                " WHERE gruppoConto = 1231 AND sottoConto =:sottoConto",
                        Parameters.with("lat", lonLat.getLatitudine()).and("lon", lonLat.getLongitudine())
                                .and("sottoConto", pianoContiDto.getSottoConto()));
            }
        } catch (Exception e) {
            Log.error(e.getStackTrace());
        }
    }

    private String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
}
