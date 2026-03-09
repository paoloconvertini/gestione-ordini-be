package it.calolenoci.resource;

import it.calolenoci.scheduler.FetchScheduler;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.inject.Inject;

@Path("/admin/scheduler")
public class SchedulerManualResource {

    @Inject
    FetchScheduler fetchScheduler;

    @GET
    @Path("/run-bolle")
    @Produces(MediaType.TEXT_PLAIN)
    public String runBolle() throws Exception {
        fetchScheduler.update();
        return "Scheduler eseguito manualmente.";
    }
}
