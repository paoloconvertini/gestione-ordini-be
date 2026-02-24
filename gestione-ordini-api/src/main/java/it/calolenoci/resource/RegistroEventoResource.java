package it.calolenoci.resource;

import it.calolenoci.enums.Ruolo;
import it.calolenoci.service.EventoService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

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
