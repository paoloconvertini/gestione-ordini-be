package it.calolenoci.scheduler;

import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.quarkus.panache.common.Parameters;
import io.quarkus.scheduler.Scheduled;
import it.calolenoci.dto.Coordinate;
import it.calolenoci.dto.FiltroOrdini;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.PianoContiDto;
import it.calolenoci.entity.PianoConti;
import it.calolenoci.service.*;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;

@ApplicationScoped
public class FetchScheduler {

    @ConfigProperty(name = "bing.map.url")
    String baseUrl;

    @ConfigProperty(name = "bing.map.apiKey")
    String apiKey;

    @ConfigProperty(name = "admin.email")
    String adminEmail;

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

    @Inject
    SaldiMagazzinoService saldiMagazzinoService;

    @Inject
    AmmortamentoCespiteService ammortamentoCespiteService;

    @Scheduled(cron = "${cron.expr}")
    @Transactional
    @TransactionConfiguration(timeout = 500)
    public void update() throws ParseException {
        long inizio = System.currentTimeMillis();
        List<OrdineDettaglioDto> list = fatturaService.getBolle();
        if (list != null && !list.isEmpty()) {
            Log.debug("Trovate " + list.size() + " bolle");
            int tentativi = 0;
            boolean successo = false;
            boolean update = false;
            while (tentativi < 2 && !successo) {
                try {
                    update = articoloService.updateArticoliBolle(list);
                    if (update) {
                        successo = true;
                        Log.error("Aggiornamento riuscito.");
                    } else {
                        throw new RuntimeException("L'aggiornamento non ha avuto successo.");
                    }
                } catch (Exception e) {
                    tentativi++;
                    Log.error("Tentativo " + tentativi + " fallito: " + e.getMessage());

                    if (tentativi >= 2) {
                        Log.error("Aggiornamento fallito dopo 2 tentativi.");
                    }
                }
            }

            if (update) {
                ordineService.checkConsegnati(new FiltroOrdini());
            }
        }
        articoloService.checkNoBolle();
        ordineService.checkNoProntaConegna(new FiltroOrdini());
        long fine = System.currentTimeMillis();
        Log.error("FINE UPDATE CHECK BOLLE: " + (fine - inizio) / 1000 + " sec");
    }

    @Scheduled(every = "${cron.expr.nuovi.ordini:10m}")
    @Transactional
    public void findNuoviOrdini() throws ParseException {
        ordineService.checkStatusDettaglio(new FiltroOrdini());
        ordineService.addNuoviOrdini();
    }

    @Scheduled(every = "${cron.expr.find.carichi}")
    @Transactional
    public void findCarichi(){
        articoloService.findCarichi();
    }

    @Scheduled(cron = "${cron.expr.find.carico.mag}")
    @Transactional
    @TransactionConfiguration(timeout = 50000)
    public void findCaricoMagazzino(){
        saldiMagazzinoService.findCaricoMagazzino();
    }

    @Scheduled(cron = "${cron.expr.invio.mail}")
    @Transactional
    public void invioMail() {
        mailService.invioMailOrdini(this.adminEmail);
    }

    @Scheduled(cron = "${cron.expr.invio.mail.da.consegnare}")
    @Transactional
    public void invioMailDaConsegnare() {
        mailService.invioMailOrdiniDaConsegnare(this.adminEmail);
    }


    @Scheduled(cron = "${cron.expr.geolocation}")
    @TransactionConfiguration(timeout = 50000)
    public void geoLocation() throws ParseException {
        List<PianoContiDto> clienti = ordineService.findClienti();
        for (PianoContiDto pianoContiDto : clienti) {
            updateLatLon(pianoContiDto);
        }
    }

    @Scheduled(cron = "${cron.expr.cespiti}")
    @TransactionConfiguration(timeout = 500000)
    public void calcolaAmmortamentoCespiti() throws ParseException {
        ammortamentoCespiteService.calcola(null);
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
