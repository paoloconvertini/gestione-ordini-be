package it.calolenoci.scheduler;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import it.calolenoci.dto.FatturaDto;
import it.calolenoci.service.ArticoloService;
import it.calolenoci.service.FatturaService;
import it.calolenoci.service.OrdineService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class FetchScheduler {


    @Inject
    FatturaService fatturaService;

    @Inject
    OrdineService ordineService;

    @Inject
    ArticoloService articoloService;


    @Scheduled(cron = "{cron.expr}")
    @Transactional
    public void update() {
        Log.info("INIZIO UPDATE CHECK BOLLE");
        Integer update;
        List<Integer> list = fatturaService.getBolle().stream().map(FatturaDto::getProgrOrdCli).collect(Collectors.toList());
        update = articoloService.updateFlagConsegnato(list);
        if(update != null){
            Log.debug("Aggiornati " + update + " articoli");
            ordineService.checkConsegnati();
        }
    }

}
