package it.calolenoci.resource;

import it.calolenoci.dto.TipoAttivitaMontaggioDto;
import it.calolenoci.service.TipoAttivitaMontaggioService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;

import static it.calolenoci.enums.Ruolo.ADMIN;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/tipi-attivita-montaggio")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class TipoAttivitaMontaggioResource {

    @Inject
    TipoAttivitaMontaggioService service;

    @GET
    @RolesAllowed({ADMIN})
    public Response getTipiAttivita() {
        List<TipoAttivitaMontaggioDto> result = service.getTipiAttivita();
        return Response.ok(result).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ADMIN})
    public Response getById(@PathParam("id") Long id) {
        TipoAttivitaMontaggioDto result = service.getById(id);
        return Response.ok(result).build();
    }

    @POST
    @Transactional
    @RolesAllowed({ADMIN})
    public Response create(TipoAttivitaMontaggioDto dto) {
        TipoAttivitaMontaggioDto result = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed({ADMIN})
    public Response update(@PathParam("id") Long id, TipoAttivitaMontaggioDto dto) {
        TipoAttivitaMontaggioDto result = service.update(id, dto);
        return Response.ok(result).build();
    }

    @PUT
    @Path("/{id}/disattiva")
    @Transactional
    @RolesAllowed({ADMIN})
    public Response disattiva(@PathParam("id") Long id) {
        service.disattiva(id);
        return Response.noContent().build();
    }
}
