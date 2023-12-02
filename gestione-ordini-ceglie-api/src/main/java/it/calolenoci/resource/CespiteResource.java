package it.calolenoci.resource;

import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.CespiteRequest;
import it.calolenoci.dto.CespiteView;
import it.calolenoci.dto.FiltroCespite;
import it.calolenoci.dto.ResponseDto;
import it.calolenoci.entity.Cespite;
import it.calolenoci.service.AmmortamentoCespiteService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static it.calolenoci.enums.Ruolo.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("api/cespiti")
@RequestScoped
public class CespiteResource {


    @Inject
    AmmortamentoCespiteService service;

    @Operation(summary = "Returns all the ordini from the database")
    @POST
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = CespiteView.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ammortamenti")
    public Response getAll(FiltroCespite filtroCespite) {
        try {
            return Response.ok().entity(service.getAll(filtroCespite)).build();
        } catch (Exception e) {
            return Response.status(400).entity(new ResponseDto("Error getting registro cespiti", true)).build();
        }

    }

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @RolesAllowed({ADMIN})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Cespite.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Path("/cespiti")
    public Response getAllCespiti() {
        return Response.ok(Cespite.find("attivo <> 'F'", Sort.ascending("tipoCespite, progressivo1, progressivo2")).list()).build();
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
    }


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

    @DELETE
    @Transactional
    @Path("/{id}")
    public Response delete(String id) {
        Cespite.deleteById(id);
        return Response.ok().entity(new ResponseDto("Cespite eliminato", false)).build();
    }

}