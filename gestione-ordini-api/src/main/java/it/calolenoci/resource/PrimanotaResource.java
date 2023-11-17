package it.calolenoci.resource;

import it.calolenoci.dto.*;
import it.calolenoci.entity.Cespite;
import it.calolenoci.entity.Ordine;
import it.calolenoci.service.AmmortamentoCespiteService;
import it.calolenoci.service.PrimanotaService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static it.calolenoci.enums.Ruolo.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("api/primanota")
@RequestScoped
public class PrimanotaResource {


    @Inject
    PrimanotaService service;

    @Operation(summary = "Returns all the ordini from the database")
    @POST
    @RolesAllowed({ADMIN})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = CespiteView.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ammortamenti")
    public Response getById(FiltroPrimanota filtroPrimanota) {
        return Response.ok(service.getById(filtroPrimanota)).build();
    }
    @Operation(summary = "salva cespite")
    @POST
    @RolesAllowed({ADMIN})
    @Path("/salva")
    public Response salva(PrimanotaDto dto) {
        if (dto == null) {
            return Response.status(Response.Status.NOT_MODIFIED).entity(new ResponseDto("no save", true)).build();
        }
        try {
            service.salva(dto);
        } catch (Exception e) {
            return Response.status(400).entity(new ResponseDto("Errore salva primanota", true)).build();
        }
        return Response.status(Response.Status.CREATED).entity(new ResponseDto("Primanota salvata con successo", false)).build();
    }
}
