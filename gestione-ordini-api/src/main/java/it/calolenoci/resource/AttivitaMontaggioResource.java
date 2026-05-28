package it.calolenoci.resource;

import it.calolenoci.dto.AttivitaMontaggioDto;
import it.calolenoci.dto.FiltroAttivitaMontaggioDto;
import it.calolenoci.dto.PageAttivitaMontaggioDto;
import it.calolenoci.service.AttivitaMontaggioService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import static it.calolenoci.enums.Ruolo.*;
import static it.calolenoci.enums.Ruolo.AMMINISTRATIVO;
import static it.calolenoci.enums.Ruolo.LOGISTICA;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/attivita-montaggio")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class AttivitaMontaggioResource {

    @Inject
    AttivitaMontaggioService service;

    @POST
    @Path("/search")
    @RolesAllowed({ADMIN})
    public Response search(FiltroAttivitaMontaggioDto filtro) {
        PageAttivitaMontaggioDto result = service.search(filtro);
        return Response.ok(result).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ADMIN})
    public Response getById(@PathParam("id") Long id) {
        AttivitaMontaggioDto result = service.getById(id);
        return Response.ok(result).build();
    }

    @POST
    @Transactional
    @RolesAllowed({ADMIN})
    public Response create(AttivitaMontaggioDto dto) {
        AttivitaMontaggioDto result = service.create(dto);
        return Response.status(Response.Status.CREATED)
                .entity(result)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed({ADMIN})
    public Response update(@PathParam("id") Long id, AttivitaMontaggioDto dto) {
        AttivitaMontaggioDto result = service.update(id, dto);
        return Response.ok(result).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed({ADMIN})
    public Response delete(@PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/export-ics")
    @Produces("text/calendar")
    @RolesAllowed({ADMIN})
    public Response exportIcs(FiltroAttivitaMontaggioDto filtro) {

        String ics = service.exportIcs(filtro);

        return Response.ok(ics)
                .header(
                        "Content-Disposition",
                        "attachment; filename=agenda-montaggi.ics"
                )
                .build();
    }
}
