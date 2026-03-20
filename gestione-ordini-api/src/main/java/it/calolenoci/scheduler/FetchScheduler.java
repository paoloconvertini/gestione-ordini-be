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
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class FetchScheduler {

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
    SaldiMagazzinoService saldiMagazzinoService;

    @Inject
    AmmortamentoCespiteService ammortamentoCespiteService;

    @Scheduled(cron = "${cron.expr}")
    public void update() {
        long inizio = System.currentTimeMillis();

        try {
            runUpdateBolle();
            runCheckConsegnati();
            runSyncHasBolla();
            runSyncProntoConsegna();
            runSyncProntoTestata();
        } catch (Exception e) {
            Log.error("Errore scheduler update", e);
        }

        long fine = System.currentTimeMillis();
        Log.error("FINE UPDATE CHECK BOLLE: " + (fine - inizio) / 1000 + " sec");
    }

    @Transactional
    public void runSyncProntoTestata() {
        ordineService.syncProntoTestata();
    }

    @Transactional
    public void runUpdateBolle() {

        List<OrdineDettaglioDto> list = fatturaService.getBolle();

        if (list != null && !list.isEmpty()) {
            boolean update = articoloService.updateArticoliBolle(list);

            if (update) {
                Log.error("Aggiornamento riuscito.");
            } else {
                throw new RuntimeException("Update bolle fallito");
            }
        }
    }

    @Transactional
    public void runCheckConsegnati() {
        ordineService.checkConsegnati(new FiltroOrdini());
    }

    @Transactional
    public void runSyncHasBolla() {
        articoloService.syncHasBolla();
    }

    @Transactional
    public void runSyncProntoConsegna() {
        ordineService.syncProntoConsegna();
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

    @Scheduled(cron = "${cron.expr.cespiti}")
    @TransactionConfiguration(timeout = 500000)
    public void calcolaAmmortamentoCespiti() throws ParseException {
        ammortamentoCespiteService.calcola(null);
    }

}
