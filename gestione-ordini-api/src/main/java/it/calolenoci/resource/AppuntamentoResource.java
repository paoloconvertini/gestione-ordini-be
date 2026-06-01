package it.calolenoci.resource;

import it.calolenoci.dto.AppuntamentoDto;
import it.calolenoci.dto.FiltroAppuntamentoDto;
import it.calolenoci.dto.PageAppuntamentoDto;
import it.calolenoci.service.AppuntamentoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static it.calolenoci.enums.Ruolo.ADMIN;
import static it.calolenoci.enums.Ruolo.VENDITORE;

@Path("api/appuntamenti")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AppuntamentoResource {

    @Inject
    AppuntamentoService service;

    @POST
    @Path("/search")
    @RolesAllowed({ADMIN, VENDITORE})
    public PageAppuntamentoDto search(FiltroAppuntamentoDto filtro) {
        return service.search(filtro);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ADMIN, VENDITORE})
    public AppuntamentoDto getById(@PathParam("id") Long id) {
        return service.getById(id);
    }

    @POST
    @RolesAllowed({ADMIN, VENDITORE})
    public AppuntamentoDto create(AppuntamentoDto dto) {
        return service.create(dto);
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ADMIN, VENDITORE})
    public AppuntamentoDto update(@PathParam("id") Long id, AppuntamentoDto dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ADMIN, VENDITORE})
    public void delete(@PathParam("id") Long id) {
        service.delete(id);
    }

    @POST
    @Path("/export-ics")
    @Produces("text/calendar")
    @RolesAllowed({ADMIN, VENDITORE})
    public Response exportIcs(FiltroAppuntamentoDto filtro) {

        String content = service.exportIcs(filtro);

        return Response.ok(content)
                .header("Content-Disposition", "attachment; filename=appuntamenti.ics")
                .build();
    }
}
