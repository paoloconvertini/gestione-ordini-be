package it.calolenoci.resource;

import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.ResponseDto;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.service.ArticoloService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("api/v1/articoli")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class ArticoloResource {

    @Inject
    ArticoloService articoloService;

    @Operation(summary = "Returns all the articoli from the database")
    @GET
    @RolesAllowed({"Admin", "Venditore", "Magazziniere", "Amministrativo"})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = OrdineDettaglio.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Articoli")
    @Path("/{anno}/{serie}/{progressivo}")
    public Response getArticoliByIdOrdine(Integer anno, String serie, Integer progressivo) {
        return Response.ok(articoloService.findById(anno, serie, progressivo, false)).build();
    }

    @Operation(summary = "Returns all the articoli from the database")
    @GET
    @RolesAllowed({"Admin", "Venditore", "Magazziniere", "Amministrativo"})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = OrdineDettaglio.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Articoli")
    @Path("/{anno}/{serie}/{progressivo}/{filtro}")
    public Response getArticoliByIdOrdine(Integer anno, String serie, Integer progressivo, Boolean filtro) {
        return Response.ok(articoloService.findById(anno, serie, progressivo, filtro)).build();
    }

    @Operation(summary = "Save dettaglio ordine")
    @PUT
    @RolesAllowed({"Admin", "Magazziniere", "Amministrativo"})
    public Response saveArticoli(List<OrdineDettaglioDto> list) {
        if (!list.isEmpty()) {
            articoloService.save(list);
            return Response.status(Response.Status.CREATED).entity(new ResponseDto("Salvataggio con successo!", false)).build();

        }
        return Response.status(Response.Status.OK).entity(new ResponseDto("lista vuota", true)).build();
    }

    @Operation(summary = "Save dettaglio ordine")
    @GET
    @RolesAllowed({"Admin", "Magazziniere", "Amministrativo"})
    @Path("/chiudi/{anno}/{serie}/{progressivo}")
    public Response chiudi(Integer anno, String serie, Integer progressivo) {
        return Response.status(Response.Status.CREATED).entity(new ResponseDto(articoloService.chiudi(anno, serie, progressivo), false)).build();
    }

}
