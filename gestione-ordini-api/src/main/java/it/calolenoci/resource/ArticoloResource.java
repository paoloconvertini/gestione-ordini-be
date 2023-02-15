/*
package it.calolenoci.resource;

import io.quarkus.panache.common.Sort;
import it.calolenoci.entity.Articolo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("api/v1/articoli")
public class ArticoloResource {

    @Operation(summary = "Returns all the roles from the database")
    @GET
    @RolesAllowed("Admin")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Articolo.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Articoli")
    public Response getArticoliByIdOrdine(Long id) {
        return Response.ok(Articolo.findByOrdineId(id)).build();
    }

}
*/
