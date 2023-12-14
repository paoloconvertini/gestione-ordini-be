package it.calolenoci.resource;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.CategoriaCespite;
import it.calolenoci.service.CategoriaCespiteService;
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

import static it.calolenoci.enums.Ruolo.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("api/tipocespite")
@RequestScoped
public class TipoCespiteResource {


    @Inject
    CategoriaCespiteService service;

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RegistroCespitiDto.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ammortamenti")
    public Response getAll() {
        return Response.ok(CategoriaCespite.find("SELECT t.tipoCespite, t.descrizione, t.costoGruppo, t.costoConto " +
                " FROM CategoriaCespite t", Sort.ascending("t.tipoCespite")).project(CategoriaCespiteDto.class).list()).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @RolesAllowed({ADMIN})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = CategoriaCespite.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Path("/tipocespiti")
    public Response getAllTipoCespiti() {
        return Response.ok(CategoriaCespite.find("SELECT t.tipoCespite, t.descrizione " +
                " FROM CategoriaCespite t", Sort.ascending("t.tipoCespite")).project(FiltroCespite.class).list()).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @POST
    @RolesAllowed({ADMIN})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = CategoriaCespite.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    public Response save(CategoriaCespiteResponse categoriaCespiteResponse) {
        service.save(categoriaCespiteResponse);
        return Response.ok().entity(new ResponseDto("Categoria cespite salvata correttamente", false)).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @RolesAllowed({ADMIN})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = CategoriaCespite.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Path("/{id}")
    public Response getById(String id) {
        return Response.ok(CategoriaCespite.find("SELECT t.id, t.tipoCespite, t.codice, t.descrizione, t.percAmmortamento, " +
                "t.costoGruppo, t.costoConto, t.ammGruppo, t.ammConto, t.fondoGruppo, t.fondoConto, " +
                "t.plusGruppo, t.plusConto, t.minusGruppo, t.minusConto " +
                " FROM CategoriaCespite t WHERE t.tipoCespite=:id", Parameters.with("id", id)).project(CategoriaCespiteResponse.class).firstResult()).build();
    }

}
