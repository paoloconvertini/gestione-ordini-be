package resource;

import it.calolenoci.ostuni.entity.Cespite;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("ostuni/api/cespiti")
@RequestScoped
public class CespiteResource {

/*
    @Inject
    AmmortamentoCespiteService service;*/

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @PermitAll
    //@RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Cespite.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ammortamenti")
    public Response getAll() {
        return Response.ok(Cespite.findAll().list()).build();
    }

/*    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @RolesAllowed({ADMIN})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Path("/cespiti")
    public Response getAllCespiti() {
        return Response.ok(Cespite.find("attivo <> 'T'").list()).build();
    }

    @RolesAllowed({ADMIN})
    @Operation(summary = "calcola ammortamenti")
    @Path("/calcola/{date}")
    @GET
    public Response calcola(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        LocalDate localDate = LocalDate.parse(date, formatter);
        service.calcola(localDate);
        return Response.ok(new ResponseDto("Ammortamento calcolato correttamente", false)).build();
    }*/

/*

    @Operation(summary = "salva cespite")
    @PUT
    @RolesAllowed({ADMIN})
    public Response salva(CespiteRequest cespite) {
        if (cespite == null) {
            return Response.status(Response.Status.NOT_MODIFIED).entity(new ResponseDto("no save", true)).build();
        }
        service.updateCespiti(cespite);
        return Response.status(Response.Status.CREATED).entity(new ResponseDto("Record salvati", false)).build();
    }
*/

}
