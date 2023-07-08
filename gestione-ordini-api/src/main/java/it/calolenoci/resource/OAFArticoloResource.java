package it.calolenoci.resource;

import it.calolenoci.dto.FiltroArticoli;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.OrdineFornitoreDettaglioDto;
import it.calolenoci.dto.ResponseDto;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.service.ArticoloService;
import it.calolenoci.service.OAFArticoloService;
import org.apache.commons.lang3.StringUtils;
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

@Path("api/oaf/articoli")
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
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
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
    @POST
    @RolesAllowed({AMMINISTRATIVO, ADMIN})
    @Path("/richiediApprovazione/{anno}/{serie}/{progressivo}")
    public Response richiediApprovazione(Integer anno, String serie, Integer progressivo, List<OrdineFornitoreDettaglioDto> list) {
        if (!list.isEmpty()) {
            articoloService.save(list);
            this.articoloService.richiediApprovazione(anno, serie, progressivo);
            return Response.status(Response.Status.CREATED).entity(new ResponseDto("Salvataggio con successo!", false)).build();
        }
        return Response.status(Response.Status.OK).entity(new ResponseDto("Ordine a fornitore invio richiesta", false)).build();
    }

    @Operation(summary = "Elimina articolo da OAF")
    @DELETE
    @RolesAllowed({AMMINISTRATIVO, ADMIN})
    @Path("/eliminaArticolo/{anno}/{serie}/{progressivo}/{rigo}")
    public Response eliminaArticolo(Integer anno, String serie, Integer progressivo, Integer rigo) {
        return Response.status(Response.Status.OK).entity(articoloService.eliminaArticolo(anno, serie, progressivo, rigo)).build();
    }

    @Operation(summary = "Aggiungi rigo ")
    @POST
    @RolesAllowed({AMMINISTRATIVO, ADMIN})
    @Path("/salvaRigo")
    public Response salvaRigo(OrdineFornitoreDettaglioDto dto) {
        boolean save = articoloService.save(dto);
        if(!save){
            return Response.status(Response.Status.NOT_MODIFIED).entity(new ResponseDto("Errore Salvataggio !", true)).build();
        }
        return Response.status(Response.Status.CREATED).entity(new ResponseDto("Salvataggio con successo!", false)).build();
    }

    @Operation(summary = "Cerca articoli")
    @POST
    @RolesAllowed({AMMINISTRATIVO, ADMIN})
    @Path("/cercaArticoli")
    public Response cercaArticoli(FiltroArticoli filtro) {
        if(StringUtils.isBlank(filtro.getCodice()) && StringUtils.isBlank(filtro.getDescrizione())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDto("Nessun filtro i ricerca presente", true)).build();
        }
        return Response.status(Response.Status.CREATED).entity(articoloService.cercaArticoli(filtro)).build();
    }

    @Operation(summary = "Save dettaglio ordine")
    @PUT
    @RolesAllowed({ADMIN, MAGAZZINIERE, AMMINISTRATIVO})
    public Response saveArticoli(List<OrdineFornitoreDettaglioDto> list) {
        if (!list.isEmpty()) {
            articoloService.save(list);
            return Response.status(Response.Status.CREATED).entity(new ResponseDto("Salvataggio con successo!", false)).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseDto("lista vuota", true)).build();
    }

}
