package it.calolenoci.resource;

import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.ResponseDto;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.service.ArticoloService;
import it.calolenoci.service.OAFArticoloService;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
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
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("api/v1/oaf/articoli")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
public class OAFArticoloResource {

    @Inject
    OAFArticoloService articoloService;

    @Inject
    @Claim(standard = Claims.upn)
    String user;

    @Operation(summary = "Returns all the articoli from the database")
    @GET
    @RolesAllowed({"Admin", "Venditore", "Magazziniere", "Amministrativo"})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = OrdineDettaglio.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Articoli")
    @Path("/{anno}/{serie}/{progressivo}")
    public Response getArticoliByIdOrdine(Integer anno, String serie, Integer progressivo) {
        return Response.ok(articoloService.findById(anno, serie, progressivo)).build();
    }


    @Operation(summary = "Save dettaglio ordine a fornitore")
    @POST
    @RolesAllowed({"Admin"})
    @Path("/approva")
    public Response approva(List<OrdineDettaglioDto> list) {
        if (!list.isEmpty()) {
            return Response.status(Response.Status.CREATED).entity(new ResponseDto(articoloService.save(list, true), false)).build();
        }
        return Response.status(Response.Status.CREATED).entity(new ResponseDto("lista vuota", true)).build();
    }

}
