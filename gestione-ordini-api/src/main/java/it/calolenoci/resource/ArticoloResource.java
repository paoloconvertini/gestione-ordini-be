package it.calolenoci.resource;

import it.calolenoci.entity.OrdineCliente;
import it.calolenoci.service.ArticoloService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("api/v1/articoli")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class ArticoloResource {

    @Inject
    ArticoloService articoloService;

    @Operation(summary = "Returns all the roles from the database")
    @GET
    @RolesAllowed("Admin")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = OrdineCliente.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Articoli")
    @Path("/{anno}/{serie}/{progressivo}")
    public Response getArticoliByIdOrdine(Integer anno, String serie, Integer progressivo) {
        return Response.ok(articoloService.findById(anno, serie, progressivo)).build();
    }

}
