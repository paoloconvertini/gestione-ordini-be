package it.calolenoci.resource;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.PianoContiDto;
import it.calolenoci.dto.UpdateCoordsDto;
import it.calolenoci.entity.PianoConti;
import it.calolenoci.enums.Ruolo;
import it.calolenoci.service.PianoContiService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Response;

import java.util.List;

import static it.calolenoci.enums.Ruolo.ADMIN;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Produces(APPLICATION_JSON)
@Path("api/pianoconti")
@RequestScoped
public class PianocontiResource {

    @Inject
    PianoContiService pianoContiService;

    @Operation(summary = "Returns all fornitori")
    @GET
    @Path("/getFornitori")
    @RolesAllowed({ADMIN, Ruolo.AMMINISTRATIVO})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PianoConti.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Fornitori")
    @Consumes(APPLICATION_JSON)
    public Response getFornitori() {
        return Response.ok(PianoConti.find("select p.gruppoConto, p.sottoConto, p.intestazione FROM PianoConti p where p.gruppoConto = 2351", Sort.ascending("intestazione"))
                .project(PianoContiDto.class).list()).build();
    }

    @POST
    @Path("/update-coordinates")
    @Transactional
    @PermitAll
    public void updateCoordinates(UpdateCoordsDto dto) {
        String coords = dto.coords;

        if (coords == null || !coords.contains(",")) {
            throw new RuntimeException("Formato coordinate non valido");
        }

        String[] parts = coords.split(",");

        double lat = Double.parseDouble(parts[0].trim());
        double lon = Double.parseDouble(parts[1].trim());
        PianoConti.update(
                "latitudine = :lat, longitudine = :lon WHERE sottoConto = :sottoConto",
                Parameters.with("lat", lat)
                        .and("lon", lon)
                        .and("sottoConto", dto.getSottoConto())
        );
    }

    @GET
    @Path("/search-clienti")
    @RolesAllowed({ADMIN})
    public Response searchClienti(@QueryParam("q") String q) {
        List<PianoContiDto> result = pianoContiService.searchClienti(q);
        return Response.ok(result).build();
    }

}
