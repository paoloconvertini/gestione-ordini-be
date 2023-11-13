package it.calolenoci.resource;

import it.calolenoci.enums.Ruolo;
import it.calolenoci.service.EventoService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Path("api/registro")
public class RegistroEventoResource {

    @Inject
    EventoService service;

    @GET
    @Path("/{anno}/{serie}/{progressivo}/{rigo}")
    @RolesAllowed({Ruolo.ADMIN, Ruolo.VENDITORE, Ruolo.MAGAZZINIERE, Ruolo.AMMINISTRATIVO, Ruolo.LOGISTICA})
    public Response getEventoById(Integer anno, String serie, Integer progressivo, Integer rigo) {
        return Response.ok(service.getByAnnoSerieProgressivoRigo(anno, serie, progressivo, rigo)).build();
    }

}
