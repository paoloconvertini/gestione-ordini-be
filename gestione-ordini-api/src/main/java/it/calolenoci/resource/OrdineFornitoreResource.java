package it.calolenoci.resource;

import it.calolenoci.dto.OrdineFornitoreDto;
import it.calolenoci.dto.ResponseDto;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.OrdineDettaglio;
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

import java.util.List;

import static it.calolenoci.enums.Ruolo.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static it.calolenoci.enums.Ruolo.*;

@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("api/oaf")
@RequestScoped
public class OrdineFornitoreResource {

    @Inject
    OrdineFornitoreService service;

    @Inject
    @Claim(standard = Claims.upn)
    String user;

    @Operation(summary = "Crea ordine a fornitore")
    @POST
    @RolesAllowed({ADMIN, AMMINISTRATIVO})
    public Response createOAF(List<OrdineDettaglio> articoli) {
        try {
            if(articoli.isEmpty()){
                return Response.status(Response.Status.CREATED).entity(new ResponseDto("Nessun articolo da ordinare!", false)).build();
            }
            List<String> list = service.save(articoli, user);
            if(!list.isEmpty()) {
                return Response.status(Response.Status.CREATED).entity(list).build();
            } else {
                return Response.status(Response.Status.CREATED).entity(new ResponseDto("Nessun elemento salvato!", false)).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseDto(e.getMessage(), true)).build();
        }
    }

    @GET
    @Path("/apriOrdine/{anno}/{serie}/{progressivo}")
    @RolesAllowed({ADMIN, AMMINISTRATIVO})
    public Response apriOrdine(Integer anno, String serie, Integer progressivo){
        service.changeStatus(anno, serie, progressivo);
        return Response.ok(new ResponseDto("Ordine riaperto", false)).build();
    }

    @Operation(summary = "Richiedi approvazione ordine a fornitore")
    @DELETE
    @RolesAllowed({AMMINISTRATIVO, ADMIN})
    @Path("/eliminaOrdine/{anno}/{serie}/{progressivo}")
    public Response eliminaOrdine(Integer anno, String serie, Integer progressivo) {
        return Response.status(Response.Status.OK).entity(service.eliminaOrdine(anno, serie, progressivo)).build();
    }

    @Operation(summary = "Unisci ordine a fornitore")
    @POST
    @Path("/unisciOrdini")
    @RolesAllowed({ADMIN, AMMINISTRATIVO})
    public Response unisciOrdini(List<OrdineFornitoreDto> ordini) {
        try {
            if(ordini.isEmpty()){
                return Response.status(Response.Status.CREATED).entity(new ResponseDto("Nessun ordine presente!", false)).build();
            }
            service.unisciOrdini(ordini);
            return Response.status(Response.Status.CREATED).entity(new ResponseDto("Ordini unificati!", false)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseDto(e.getMessage(), true)).build();
        }
    }

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @Path("/{status}")
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    public Response getAllOrdini(String status) {
        return Response.ok(service.findAllByStatus(status)).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    public Response getAllOrdini() {
        return Response.ok(service.findAllByStatus(null)).build();
    }

    @Operation(summary = "Richiedi approvazione ordine a fornitore")
    @GET
    @RolesAllowed({AMMINISTRATIVO, ADMIN})
    @Path("/richiediApprovazione/{anno}/{serie}/{progressivo}")
    public Response richiediApprovazione(Integer anno, String serie, Integer progressivo) {
        this.service.richiediApprovazione(anno, serie, progressivo);
        return Response.status(Response.Status.OK).entity(new ResponseDto("Ordine a fornitore invio richiesta", false)).build();
    }

    @Operation(summary = "Richiedi approvazione ordine a fornitore")
    @POST
    @RolesAllowed({AMMINISTRATIVO, ADMIN})
    @Path("/richiediApprovazione")
    public Response richiediApprovazione(List<OrdineFornitoreDto> list) {
        if(list.isEmpty()){
            return Response.status(Response.Status.OK).entity(new ResponseDto("Lista vuota", true)).build();
        }
        this.service.richiediApprovazione(list);
        return Response.status(Response.Status.OK).entity(new ResponseDto("Ordine a fornitore invio richiesta", false)).build();
    }


}
