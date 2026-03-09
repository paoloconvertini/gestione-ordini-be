package it.calolenoci.resource;

import it.calolenoci.scheduler.FetchScheduler;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.inject.Inject;

@Path("/scheduler")
public class SchedulerManualResource {

    @Inject
    FetchScheduler fetchScheduler;

    @GET
    @Path("/run-bolle")
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public String runBolle() throws Exception {
        fetchScheduler.update();
        return "Scheduler eseguito manualmente.";
    }
}
