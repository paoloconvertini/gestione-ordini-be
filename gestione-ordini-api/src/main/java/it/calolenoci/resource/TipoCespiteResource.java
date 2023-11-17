package it.calolenoci.resource;

import it.calolenoci.dto.*;
import it.calolenoci.entity.CategoriaCespite;
import it.calolenoci.entity.Cespite;
import it.calolenoci.entity.Ordine;
import it.calolenoci.service.AmmortamentoCespiteService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static it.calolenoci.enums.Ruolo.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("api/tipocespite")
@RequestScoped
public class TipoCespiteResource {


    @Inject
    AmmortamentoCespiteService service;

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = CespiteView.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ammortamenti")
    public Response getAll() {
        return Response.ok(CategoriaCespite.find("SELECT t.tipoCespite, t.descrizione, t.costoGruppo, t.costoConto " +
                " FROM CategoriaCespite t").project(CategoriaCespiteDto.class).list()).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @RolesAllowed({ADMIN})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Path("/tipocespiti")
    public Response getAllTipoCespiti() {
        return Response.ok(CategoriaCespite.find("SELECT t.tipoCespite, t.descrizione " +
                " FROM CategoriaCespite t").project(FiltroCespite.class).list()).build();
    }
}
