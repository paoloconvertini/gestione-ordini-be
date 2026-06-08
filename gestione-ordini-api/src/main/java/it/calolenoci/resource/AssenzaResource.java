package it.calolenoci.resource;

import it.calolenoci.dto.AssenzaCalendarioDto;
import it.calolenoci.dto.AssenzaDto;
import it.calolenoci.dto.AssenzaGiornoDto;
import it.calolenoci.service.AssenzaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import java.time.LocalDate;
import java.util.List;

import static it.calolenoci.enums.Ruolo.*;


@Path("api/assenze")
@Consumes("application/json")
@Produces("application/json")
public class AssenzaResource {

    @Inject
    AssenzaService service;

    @POST
    @RolesAllowed({ADMIN, VENDITORE, RECEPTION_CEGLIE, RECEPTION_OSTUNI})
    public AssenzaDto create(AssenzaDto dto) {
        return service.create(dto);
    }

    @PUT
    @Path("/{id}")
    public AssenzaDto update(@PathParam("id") Long id, AssenzaDto dto) {
        return service.update(id, dto);
    }

    @GET
    @Path("/{id}")
    public AssenzaDto getById(@PathParam("id") Long id) {
        return service.getById(id);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        service.delete(id);
    }

    @GET
    @Path("/calendario")
    public List<AssenzaCalendarioDto> getCalendario(@QueryParam("dataDa") LocalDate dataDa, @QueryParam("dataA") LocalDate dataA) {
        return service.getCalendario(dataDa, dataA);
    }

    @GET
    @Path("/giorno/{data}")
    public List<AssenzaGiornoDto> getByDate(@PathParam("data") LocalDate data) {
        return service.getByDate(data);
    }
}
