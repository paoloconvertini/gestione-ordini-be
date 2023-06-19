package it.calolenoci.scheduler;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import it.calolenoci.dto.FatturaDto;
import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.enums.StatoOrdineEnum;
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
    public void update() throws ParseException {
        Log.info("INIZIO UPDATE CHECK BOLLE");
        Integer update;
        List<OrdineDettaglioDto> list = fatturaService.getBolle();
        if (list != null && !list.isEmpty()) {
            update = articoloService.updateArticoliBolle(list);
            Log.debug("Aggiornati " + update + " articoli");
            if (update != null && update != 0) {
                ordineService.checkConsegnati();
            }
        }
        articoloService.checkNoBolle();
    }

    @Scheduled(every = "${cron.expr.nuovi.ordini:10m}")
    @Transactional
    public void findNuoviOrdini() throws ParseException {
        ordineService.addNuoviOrdini();
    }

}
