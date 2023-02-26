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

@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Path("api/v1/registro")
public class RegistroEventoResource {

    @Inject
    EventoService service;

    @GET
    @Path("/{anno}/{serie}/{progressivo}/{rigo}")
    @RolesAllowed({"Admin", "Venditore", "Magazziniere", "Amministrativo"})
    public Response getEventoById(Integer anno, String serie, Integer progressivo, Integer rigo) {
        return Response.ok(service.getByAnnoSerieProgressivoRigo(anno, serie, progressivo, rigo)).build();
    }

}
