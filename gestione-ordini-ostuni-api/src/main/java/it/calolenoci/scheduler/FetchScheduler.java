package it.calolenoci.scheduler;

import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.quarkus.scheduler.Scheduled;
import it.calolenoci.service.AmmortamentoCespiteService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.text.ParseException;
import java.time.LocalDate;

@ApplicationScoped
public class FetchScheduler {


    @Inject
    AmmortamentoCespiteService ammortamentoCespiteService;

    @Scheduled(cron = "${cron.expr.cespiti}")
    @TransactionConfiguration(timeout = 5000000)
    public void calcolaAmmortamentoCespiti() throws ParseException {
        ammortamentoCespiteService.calcola(LocalDate.now());
    }

}
