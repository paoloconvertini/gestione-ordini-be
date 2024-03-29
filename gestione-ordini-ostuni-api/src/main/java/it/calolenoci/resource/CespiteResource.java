package it.calolenoci.resource;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.Cespite;
import it.calolenoci.service.AmmortamentoCespiteService;
import it.calolenoci.service.PrimanotaService;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ThreadContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static it.calolenoci.enums.Ruolo.ADMIN;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Consumes(APPLICATION_JSON)
@Path("api/cespiti")
@RequestScoped
public class CespiteResource {


    @Inject
    AmmortamentoCespiteService service;

    @Inject
    PrimanotaService primanotaService;

    @Operation(summary = "Returns all the ordini from the database")
    @POST
    @RolesAllowed({ADMIN})
    @Produces(APPLICATION_JSON)
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RegistroCespitiDto.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ammortamenti")
    public Response getRegistroCespiti(FiltroCespite filtroCespite) {
        try {
            return Response.ok().entity(service.getRegistroCespiti(filtroCespite)).build();
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
    @Produces(APPLICATION_JSON)
    public Response getAllCespiti() {
        return Response.ok(Cespite.find("attivo <> 'F'", Sort.ascending("tipoCespite, progressivo1, progressivo2")).list()).build();
    }

    @RolesAllowed({ADMIN})
    @Operation(summary = "calcola ammortamenti")
    @Path("/calcola")
    @POST
    @Produces(APPLICATION_JSON)
    public Response calcola(FiltroCespite filtroCespite) {
        service.calcola(filtroCespite);
        return getRegistroCespiti(filtroCespite);
    }


    @Operation(summary = "salva cespite")
    @PUT
    @RolesAllowed({ADMIN})
    @Produces(APPLICATION_JSON)
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
    @RolesAllowed(ADMIN)
    @Produces(APPLICATION_JSON)
    public Response delete(String id) {
        Cespite.deleteById(id);
        return Response.ok().entity(new ResponseDto("Cespite eliminato", false)).build();
    }

    @POST
    @Path("/scaricaRegistroCespiti")
    @Produces(MediaType.TEXT_PLAIN)
    @PermitAll
    public Response scaricaRegistroCespiti(RegistroCespitiDto view) {
        File pdf;
        try {
            pdf = service.scaricaRegistroCespiti(view);
            if(pdf != null){
                return Response.ok(pdf).header("Content-Disposition", "attachment;filename=" + pdf.getName()).build();
            } else {
                return Response.noContent().build();
            }
        } catch (Exception e) {
            Log.error("Errore scarica registro cespite", e);
        }
        return Response.noContent().build();
    }

    @Operation(summary = "salva quadratura cespite")
    @POST
    @RolesAllowed({ADMIN})
    @Produces(APPLICATION_JSON)
    @Path("/aggiornaAmmortamenti/{date}")
    public Response aggiornaAmmortamenti(AmmortamentoCespite a, String date) {
        if (a == null) {
            return Response.status(Response.Status.NOT_MODIFIED).entity(new ResponseDto("no save", true)).build();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        LocalDate localDate = LocalDate.parse(date, formatter);
        service.aggiornaAmmortamenti(a, localDate);
        return Response.status(Response.Status.CREATED).entity(new ResponseDto("Record salvati", false)).build();
    }

    @Operation(summary = "contabilizza")
    @GET
    @RolesAllowed({ADMIN})
    @Produces(APPLICATION_JSON)
    @Path("/contabilizzaAmm/{date}")
    public Response contabilizzaAmm(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            LocalDate localDate = LocalDate.parse(date, formatter);
            primanotaService.contabilizzaAmm(localDate);
            return Response.ok().entity(new ResponseDto("contabilizzazione completata con successo", false)).build();
        } catch (Exception e) {
            Log.error("Errore contabilizzaAmm", e);
            return Response.status(500).entity(new ResponseDto(e.getCause().getMessage(), true)).build();
        }
    }
}
