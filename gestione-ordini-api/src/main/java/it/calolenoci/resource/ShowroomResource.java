package it.calolenoci.resource;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import it.calolenoci.common.service.ComuneService;
import it.calolenoci.dto.*;
import it.calolenoci.entity.Sede;
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
    @RolesAllowed({ADMIN, RECEPTION_OSTUNI, RECEPTION_CEGLIE, VENDITORE})
    public Response search(FiltroShowroom filtro) {

        PageShowroomDto result = showroomService.search(filtro);

        return Response.ok(result).build();
    }

    @POST
    @RolesAllowed({ADMIN, RECEPTION_CEGLIE, RECEPTION_OSTUNI})
    @Transactional
    public Response create(ShowroomVisitDto dto) {

        ShowroomVisitDto result = showroomService.create(dto);

        return Response.status(Response.Status.CREATED)
                .entity(result)
                .build();
    }

    @GET
    @Path("/province")
    @RolesAllowed({ADMIN, RECEPTION_CEGLIE, RECEPTION_OSTUNI, VENDITORE})
    public Response getProvince() {

        List<String> province = comuneService.findProvince();

        return Response.ok(province).build();
    }

    @GET
    @Path("/comuni")
    @RolesAllowed({ADMIN, RECEPTION_CEGLIE, RECEPTION_OSTUNI, VENDITORE})
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
    @RolesAllowed({ADMIN, RECEPTION_CEGLIE, RECEPTION_OSTUNI})
    @Transactional
    public Response update(@PathParam("id") Long id,
                           ShowroomVisitDto dto) {

        ShowroomVisitDto result = showroomService.update(id, dto);

        return Response.ok(result).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ADMIN, RECEPTION_CEGLIE, RECEPTION_OSTUNI})
    @Transactional
    public Response delete(@PathParam("id") Long id) {

        showroomService.delete(id);

        return Response.noContent().build();
    }

    @GET
    @RolesAllowed({ADMIN, RECEPTION_CEGLIE, RECEPTION_OSTUNI})
    @Path("/motivi/root")
    public Response getRoot() {
        return Response.ok(showroomService.getMotiviRoot()).build();
    }

    @GET
    @RolesAllowed({ADMIN, RECEPTION_CEGLIE, RECEPTION_OSTUNI})
    @Path("/motivi/{parentId}/figli")
    public Response getFigli(@PathParam("parentId") Long parentId) {
        return Response.ok(showroomService.getFigli(parentId)).build();
    }

    @GET
    @RolesAllowed({ADMIN, RECEPTION_CEGLIE, RECEPTION_OSTUNI})
    @Path("/motivi/{id}")
    public Response getMotivoById(@PathParam("id") Long id) {
        return Response.ok(showroomService.getMotivoById(id)).build();
    }

    @GET
    @RolesAllowed({ADMIN, RECEPTION_CEGLIE, RECEPTION_OSTUNI})
    @Path("/clienti/search")
    public List<ClienteLightDto> searchClienti(@QueryParam("q") String q) {
        return showroomService.searchClienti(q);
    }

    @PUT
    @RolesAllowed({ADMIN, RECEPTION_CEGLIE, RECEPTION_OSTUNI})
    @Path("/{id}/associa-cliente")
    public Response associaCliente(@PathParam("id") Long id,
                                   ClienteLinkRequest request) {

        showroomService.associaCliente(id, request.getCodiceCliente());

        return Response.ok().build();
    }

    @GET
    @RolesAllowed({ADMIN, RECEPTION_CEGLIE, RECEPTION_OSTUNI})
    @Path("/sedi")
    public List<SedeDto> getSedi() {
        List<Sede> list = Sede.findAll().list();
        return list.stream()
                .map(s -> new SedeDto(s.getId(), s.getDescrizione()))
                .toList();
    }

    @POST
    @Path("/motivi")
    @RolesAllowed({ADMIN})
    @Transactional
    public Response createMotivo(ShowroomMotivoDto dto) {

        ShowroomMotivoDto result = showroomService.createMotivo(dto);

        return Response.status(Response.Status.CREATED)
                .entity(result)
                .build();
    }

    @PUT
    @Path("/motivi/{id}")
    @RolesAllowed({ADMIN})
    @Transactional
    public Response updateMotivo(@PathParam("id") Long id,
                                 ShowroomMotivoDto dto) {

        ShowroomMotivoDto result = showroomService.updateMotivo(id, dto);

        return Response.ok(result).build();
    }

    @PUT
    @Path("/motivi/{id}/disattiva")
    @RolesAllowed({ADMIN})
    @Transactional
    public Response disattivaMotivo(@PathParam("id") Long id) {

        showroomService.disattivaMotivo(id);

        return Response.noContent().build();
    }
}
