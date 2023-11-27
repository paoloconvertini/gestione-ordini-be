package it.calolenoci.resource;

import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.PianoContiDto;
import it.calolenoci.entity.PianoConti;
import it.calolenoci.enums.Ruolo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Produces(APPLICATION_JSON)
@Path("api/pianoconti")
@RequestScoped
public class PianocontiResource {



    @Operation(summary = "Returns all fornitori")
    @GET
    @Path("/getFornitori")
    @RolesAllowed({Ruolo.ADMIN, Ruolo.AMMINISTRATIVO})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PianoConti.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Fornitori")
    @Consumes(APPLICATION_JSON)
    public Response getFornitori() {
        return Response.ok(PianoConti.find("select p.gruppoConto, p.sottoConto, p.intestazione FROM PianoConti p where p.gruppoConto = 2351", Sort.ascending("intestazione"))
                .project(PianoContiDto.class).list()).build();
    }


}
