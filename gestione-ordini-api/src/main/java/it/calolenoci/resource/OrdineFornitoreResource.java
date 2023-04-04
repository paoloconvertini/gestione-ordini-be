package it.calolenoci.resource;

import it.calolenoci.dto.ResponseDto;
import it.calolenoci.entity.Ordine;
import it.calolenoci.service.OrdineFornitoreService;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("api/v1/oaf")
@RequestScoped
public class OrdineFornitoreResource {

    @Inject
    OrdineFornitoreService service;

    @Inject
    @Claim(standard = Claims.upn)
    String user;

    @Operation(summary = "Crea ordine a fornitore")
    @GET
    @RolesAllowed({"Admin", "Amministrativo"})
    @Path("/{anno}/{serie}/{progressivo}")
    public Response createOAF(Integer anno, String serie, Integer progressivo) {

        try {
            service.save(anno, serie, progressivo, user);
            return Response.status(Response.Status.CREATED).entity(new ResponseDto("Salvataggio con successo!", false)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseDto(e.getMessage(), true)).build();
        }
    }

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @RolesAllowed({"Admin", "Venditore", "Magazziniere", "Amministrativo"})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    public Response getAllOrdini(@QueryParam("status") String status) {
        return Response.ok(service.findAllByStatus(status)).build();
    }


}
