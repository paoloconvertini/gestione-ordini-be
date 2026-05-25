package it.calolenoci.resource;

import it.calolenoci.dto.OperaioDto;
import it.calolenoci.service.OperaioService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;

import static it.calolenoci.enums.Ruolo.ADMIN;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/operai")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
public class OperaioResource {

    @Inject
    OperaioService service;


    @GET
    @RolesAllowed({ADMIN})
    public Response getOperai() {
        List<OperaioDto> result = service.getOperai();
        return Response.ok(result).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ADMIN})
    public Response getById(@PathParam("id") Long id) {
        OperaioDto result = service.getOperaioById(id);
        return Response.ok(result).build();
    }

    @POST
    @Transactional
    @RolesAllowed({ADMIN})
    public Response create(OperaioDto dto) {
        OperaioDto result = service.createOperaio(dto);
        return Response.status(Response.Status.CREATED)
                .entity(result)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed({ADMIN})
    public Response update(@PathParam("id") Long id, OperaioDto dto) {
        OperaioDto result = service.updateOperaio(id, dto);
        return Response.ok(result).build();
    }

    @PUT
    @Path("/{id}/disattiva")
    @Transactional
    @RolesAllowed({ADMIN})
    public Response disattiva(@PathParam("id") Long id) {
        service.disattivaOperaio(id);
        return Response.noContent().build();
    }
}


