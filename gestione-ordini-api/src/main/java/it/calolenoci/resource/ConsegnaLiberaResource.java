package it.calolenoci.resource;

import it.calolenoci.dto.ConsegnaLiberaDto;
import it.calolenoci.service.ConsegnaLiberaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import static it.calolenoci.enums.Ruolo.*;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("api/consegne-libere")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
@RequestScoped
public class ConsegnaLiberaResource {
    @Inject
    ConsegnaLiberaService service;

    @POST
    public Response crea(ConsegnaLiberaDto dto) {
        return Response.status(Response.Status.CREATED).entity(service.salva(null, dto)).build();
    }

    @PUT
    @Path("/{id}")
    public Response modifica(@PathParam("id") Long id, ConsegnaLiberaDto dto) {
        return Response.ok(service.salva(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response elimina(@PathParam("id") Long id) {
        service.elimina(id);
        return Response.noContent().build();
    }
}
