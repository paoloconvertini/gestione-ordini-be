package it.calolenoci.resource;

import it.calolenoci.common.service.ComuneService;
import it.calolenoci.dto.ComuneFiltroDto;
import it.calolenoci.dto.FiltroShowroom;
import it.calolenoci.dto.PageShowroomDto;
import it.calolenoci.dto.ShowroomVisitDto;
import it.calolenoci.service.ShowroomService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.List;
import static it.calolenoci.enums.Ruolo.*;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


@Path("/api/showroom")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class ShowroomResource {

    @Inject
    ShowroomService showroomService;

    @Inject
    ComuneService comuneService;

    @POST
    @Path("/search")
    @RolesAllowed({ADMIN, RECEPTION, VENDITORE})
    public Response search(FiltroShowroom filtro) {

        PageShowroomDto result = showroomService.search(filtro);

        return Response.ok(result).build();
    }

    @POST
    @RolesAllowed({ADMIN, RECEPTION})
    @Transactional
    public Response create(ShowroomVisitDto dto) {

        ShowroomVisitDto result = showroomService.create(dto);

        return Response.status(Response.Status.CREATED)
                .entity(result)
                .build();
    }

    @GET
    @Path("/province")
    @RolesAllowed({ADMIN, RECEPTION, VENDITORE})
    public Response getProvince() {

        List<String> province = comuneService.findProvince();

        return Response.ok(province).build();
    }

    @GET
    @Path("/comuni")
    @RolesAllowed({ADMIN, RECEPTION, VENDITORE})
    public Response getComuni(
            @QueryParam("provincia") String provincia,
            @QueryParam("q") String testo
    ) {

        List<ComuneFiltroDto> result = comuneService
                .searchComuni(provincia, testo)
                .stream()
                .map(c -> new ComuneFiltroDto(
                        c.getCodiceIstat(),
                        c.getNomeComune(),
                        c.getSiglaProvincia()
                ))
                .toList();

        return Response.ok(result).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ADMIN, RECEPTION})
    @Transactional
    public Response update(@PathParam("id") Long id,
                           ShowroomVisitDto dto) {

        ShowroomVisitDto result = showroomService.update(id, dto);

        return Response.ok(result).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN"})
    @Transactional
    public Response delete(@PathParam("id") Long id) {

        showroomService.delete(id);

        return Response.noContent().build();
    }
}
