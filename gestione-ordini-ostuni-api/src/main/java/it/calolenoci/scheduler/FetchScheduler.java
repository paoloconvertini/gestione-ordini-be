package it.calolenoci.scheduler;

import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.quarkus.scheduler.Scheduled;
import it.calolenoci.dto.FiltroCespite;
import it.calolenoci.service.AmmortamentoCespiteService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class FetchScheduler {


    @Inject
    AmmortamentoCespiteService ammortamentoCespiteService;

    @Scheduled(cron = "${cron.expr.cespiti}")
    @TransactionConfiguration(timeout = 5000000)
    public void calcolaAmmortamentoCespiti() throws ParseException {
        FiltroCespite filtro = new FiltroCespite();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        String date = formatter.format(now);
        filtro.setData(date);
        ammortamentoCespiteService.calcola(filtro);
    }

}
