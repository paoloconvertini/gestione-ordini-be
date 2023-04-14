package it.calolenoci.resource;

import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.PianoConti;
import it.calolenoci.enums.StatoOrdineEnum;
import it.calolenoci.scheduler.FetchScheduler;
import it.calolenoci.service.*;
import net.sf.jasperreports.engine.JRException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jfree.util.Log;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

import static it.calolenoci.enums.Ruolo.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Produces(APPLICATION_JSON)
@Path("api/pianoconti")
@RequestScoped
public class PianocontiResource {



    @Operation(summary = "Returns all fornitori")
    @GET
    @Path("/getFornitori")
    @RolesAllowed({ADMIN, AMMINISTRATIVO})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PianoConti.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Fornitori")
    @Consumes(APPLICATION_JSON)
    public Response getFornitori() {
        return Response.ok(PianoConti.find("select p.gruppoConto, p.sottoConto, p.intestazione FROM PianoConti p where p.gruppoConto = 2351", Sort.ascending("intestazione"))
                .project(PianoContiDto.class).list()).build();
    }


}
