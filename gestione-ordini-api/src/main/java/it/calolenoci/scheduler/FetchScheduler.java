package it.calolenoci.scheduler;

import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.quarkus.scheduler.Scheduled;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.service.ArticoloService;
import it.calolenoci.service.FatturaService;
import it.calolenoci.service.OrdineService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.util.List;

@ApplicationScoped
public class FetchScheduler {


    @Inject
    FatturaService fatturaService;

    @Inject
    OrdineService ordineService;

    @Inject
    ArticoloService articoloService;


    @Scheduled(every = "${cron.expr:1m}")
    @Transactional
    @TransactionConfiguration(timeout = 500)
    public void update() throws ParseException {
        long inizio = System.currentTimeMillis();
        Integer update;
        List<OrdineDettaglioDto> list = fatturaService.getBolle();
        if (list != null && !list.isEmpty()) {
            update = articoloService.updateArticoliBolle(list);
            Log.info("Aggiornati " + update + " articoli");
            if (update != null && update != 0) {
                ordineService.checkConsegnati();
            }
        }
        articoloService.checkNoBolle();
        ordineService.checkNoProntaConegna();
        long fine = System.currentTimeMillis();
        Log.info("FINE UPDATE CHECK BOLLE: " + (fine - inizio)/1000 + " sec");
    }

    @Scheduled(every = "${cron.expr.nuovi.ordini:10m}")
    @Transactional
    public void findNuoviOrdini() throws ParseException {
        ordineService.addNuoviOrdini();
    }

}
