package it.calolenoci.resource;

import it.calolenoci.dto.*;
import it.calolenoci.service.PrimanotaService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static it.calolenoci.enums.Ruolo.ADMIN;
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
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RegistroCespitiDto.class, type = SchemaType.ARRAY)))
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
            return Response.status(500).entity(new ResponseDto(e.getCause().getMessage(), true)).build();
        }
        return Response.status(Response.Status.CREATED).entity(new ResponseDto("Primanota salvata con successo", false)).build();
    }

    @Operation(summary = "salva cespite")
    @POST
    @RolesAllowed({ADMIN})
    @Path("/registraVendita")
    public Response registraVendita(VenditaCespiteDto dto) {
        if (dto == null) {
            return Response.status(Response.Status.NOT_MODIFIED).entity(new ResponseDto("no save", true)).build();
        }
        try {
            service.registraVendita(dto);
        } catch (Exception e) {
            return Response.status(500).entity(new ResponseDto(e.getCause().getMessage(), true)).build();
        }
        return Response.status(Response.Status.CREATED).entity(new ResponseDto("Registrato vendita con successo", false)).build();
    }

}
