package it.calolenoci.resource;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;
import io.smallrye.mutiny.Uni;
import it.calolenoci.dto.EmailDto;
import it.calolenoci.service.EventoService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static it.calolenoci.enums.Ruolo.*;

@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Path("api/registro")
public class RegistroEventoResource {

    @Inject
    EventoService service;

    @GET
    @Path("/{anno}/{serie}/{progressivo}/{rigo}")
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO})
    public Response getEventoById(Integer anno, String serie, Integer progressivo, Integer rigo) {
        return Response.ok(service.getByAnnoSerieProgressivoRigo(anno, serie, progressivo, rigo)).build();
    }

}
