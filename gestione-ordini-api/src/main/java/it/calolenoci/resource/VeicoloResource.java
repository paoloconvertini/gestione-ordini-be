package it.calolenoci.resource;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.*;
import it.calolenoci.entity.GoOrdine;
import it.calolenoci.entity.GoOrdineDettaglio;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.entity.Veicolo;
import it.calolenoci.service.ArticoloService;
import it.calolenoci.service.DropBoxService;
import it.calolenoci.service.FatturaService;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;

import static it.calolenoci.enums.Ruolo.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Path("api/veicoli")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
public class VeicoloResource {

    @Operation(summary = "Returns all the articoli from the database")
    @GET
    @PermitAll
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Veicolo.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Articoli")
    @Transactional
    public Response getVeicoli() {
        return Response.ok(Veicolo.findAll().list()).build();
    }

}
