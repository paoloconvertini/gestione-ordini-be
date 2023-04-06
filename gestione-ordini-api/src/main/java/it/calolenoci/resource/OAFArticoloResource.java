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
import static it.calolenoci.enums.Ruolo.*;

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
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = OrdineDettaglio.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Articoli")
    @Path("/{anno}/{serie}/{progressivo}")
    public Response getArticoliByIdOrdine(Integer anno, String serie, Integer progressivo) {
        return Response.ok(articoloService.findById(anno, serie, progressivo)).build();
    }


    @Operation(summary = "Approva dettaglio ordine a fornitore")
    @GET
    @RolesAllowed({ADMIN})
    @Path("/approva/{anno}/{serie}/{progressivo}")
    public Response approva(Integer anno, String serie, Integer progressivo) {
        this.articoloService.approva(anno, serie, progressivo);
        return Response.status(Response.Status.OK).entity(new ResponseDto("Ordine a fornitore approvato", false)).build();
    }

    @Operation(summary = "Richiedi approvazione ordine a fornitore")
    @GET
    @RolesAllowed({AMMINISTRATIVO, ADMIN})
    @Path("/richiediApprovazione/{anno}/{serie}/{progressivo}")
    public Response richiediApprovazione(Integer anno, String serie, Integer progressivo) {
        this.articoloService.richiediApprovazione(anno, serie, progressivo);
        return Response.status(Response.Status.OK).entity(new ResponseDto("Ordine a fornitore invio richiesta", false)).build();
    }

}
