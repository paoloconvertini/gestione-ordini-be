package it.calolenoci.resource;

import io.quarkus.logging.Log;
import it.calolenoci.dto.CollegaOAFDto;
import it.calolenoci.dto.OrdineFornitoreDto;
import it.calolenoci.dto.ResponseDto;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.enums.Ruolo;
import it.calolenoci.service.JasperService;
import it.calolenoci.service.OrdineFornitoreService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
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
import javax.ws.rs.core.Response;
import java.io.File;
import java.text.ParseException;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

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

    @Inject
    JasperService jasperService;

    @ConfigProperty(name = "ordini.tmp")
    String tmpFolder;


    @Operation(summary = "Returns all the roles from the database")
    @GET
    @APIResponse(responseCode = "200", description = "Pdf generato con successo")
    @Consumes(APPLICATION_JSON)
    @Produces("application/pdf")
    @Path("/scaricaOrdine/{anno}/{serie}/{progressivo}")
    @PermitAll
    public Response scaricaOrdine(Integer anno, String serie, Integer progressivo) {

        try {
            jasperService.createReport(anno, serie, progressivo);
            File report = new File(tmpFolder + anno + "_" + serie + "_" + progressivo + ".pdf");
            if (!report.exists()) {
                Log.error("Errore: Report non trovato!");
                return Response.noContent().build();
            }
            Response.ResponseBuilder response = Response.ok(report);
            response.header("Content-Disposition", "attachment; filename=" + report.getName());
            Log.info("report OK");
            return response.build();
        } catch (Exception e) {
            Log.error("Errore scarica Ordine a fornitore ", e);
            return Response.status(Response.Status.NO_CONTENT).build();
        }

    }

    @Operation(summary = "Crea ordine a fornitore")
    @POST
    @RolesAllowed({Ruolo.ADMIN, Ruolo.AMMINISTRATIVO})
    public Response createOAF(List<OrdineDettaglio> articoli) {
        try {
            if (articoli.isEmpty()) {
                return Response.status(Response.Status.CREATED).entity(new ResponseDto("Nessun articolo da ordinare!", true)).build();
            }
            List<String> list = service.save(articoli, user);
            if (!list.isEmpty()) {
                return Response.status(Response.Status.CREATED).entity(list).build();
            } else {
                return Response.status(Response.Status.CREATED).entity(new ResponseDto("Nessun elemento salvato!", true)).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseDto(e.getMessage(), true)).build();
        }
    }

    @GET
    @Path("/apriOrdine/{anno}/{serie}/{progressivo}")
    @RolesAllowed({Ruolo.ADMIN, Ruolo.AMMINISTRATIVO})
    public Response apriOrdine(Integer anno, String serie, Integer progressivo) {
        service.changeStatus(anno, serie, progressivo);
        return Response.ok(new ResponseDto("Ordine riaperto", false)).build();
    }

    @Operation(summary = "Richiedi approvazione ordine a fornitore")
    @DELETE
    @RolesAllowed({Ruolo.AMMINISTRATIVO, Ruolo.ADMIN})
    @Path("/eliminaOrdine/{anno}/{serie}/{progressivo}")
    public Response eliminaOrdine(Integer anno, String serie, Integer progressivo) {
        return Response.status(Response.Status.OK).entity(service.eliminaOrdine(anno, serie, progressivo)).build();
    }

    @Operation(summary = "Unisci ordine a fornitore")
    @POST
    @Path("/unisciOrdini")
    @RolesAllowed({Ruolo.ADMIN, Ruolo.AMMINISTRATIVO})
    public Response unisciOrdini(List<OrdineFornitoreDto> ordini) {
        try {
            if (ordini.isEmpty()) {
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
    @RolesAllowed({Ruolo.ADMIN, Ruolo.VENDITORE, Ruolo.MAGAZZINIERE, Ruolo.AMMINISTRATIVO, Ruolo.LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    public Response getAllOrdini(String status) throws ParseException {
        return Response.ok(service.findAllByStatus(status)).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @Path("/byOperatore")
    @RolesAllowed({Ruolo.ADMIN})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    public Response getOrdiniByOperatore() throws ParseException {
        return Response.ok(service.getOrdiniByOperatore()).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @RolesAllowed({Ruolo.ADMIN, Ruolo.VENDITORE, Ruolo.MAGAZZINIERE, Ruolo.AMMINISTRATIVO, Ruolo.LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    public Response getAllOrdini() throws ParseException {
        return Response.ok(service.findAllByStatus(null)).build();
    }

    @Operation(summary = "Richiedi approvazione ordine a fornitore")
    @GET
    @RolesAllowed({Ruolo.AMMINISTRATIVO, Ruolo.ADMIN})
    @Path("/richiediApprovazione/{anno}/{serie}/{progressivo}")
    public Response richiediApprovazione(Integer anno, String serie, Integer progressivo) {
        this.service.richiediApprovazione(anno, serie, progressivo);
        return Response.status(Response.Status.OK).entity(new ResponseDto("Ordine a fornitore invio richiesta", false)).build();
    }

    @Operation(summary = "Richiedi approvazione ordine a fornitore")
    @POST
    @RolesAllowed({Ruolo.AMMINISTRATIVO, Ruolo.ADMIN})
    @Path("/richiediApprovazione")
    public Response richiediApprovazione(List<OrdineFornitoreDto> list) {
        if (list.isEmpty()) {
            return Response.status(Response.Status.OK).entity(new ResponseDto("Lista vuota", true)).build();
        }
        this.service.richiediApprovazione(list);
        return Response.status(Response.Status.OK).entity(new ResponseDto("Ordine a fornitore invio richiesta", false)).build();
    }

    @Operation(summary = "Richiedi approvazione ordine a fornitore")
    @PUT
    @RolesAllowed({Ruolo.AMMINISTRATIVO, Ruolo.ADMIN})
    @Path("/inviato")
    public Response inviato(List<OrdineFornitoreDto> list) {
        if (list.isEmpty()) {
            return Response.status(Response.Status.OK).entity(new ResponseDto("Lista vuota", true)).build();
        }
        this.service.inviato(list);
        return Response.status(Response.Status.OK).entity(new ResponseDto("Ordine a fornitore invio richiesta", false)).build();
    }

    @POST
    @Path("/addNotes")
    @RolesAllowed({Ruolo.ADMIN, Ruolo.VENDITORE, Ruolo.MAGAZZINIERE, Ruolo.AMMINISTRATIVO, Ruolo.LOGISTICA})
    @Transactional
    @Consumes(APPLICATION_JSON)
    public Response addNotes(OrdineFornitoreDto dto) {
        this.service.salvaNota(dto);
        return Response.ok(new ResponseDto("Nota aggiunta", false)).build();
    }

    @Operation(summary = "Unisci ordine a fornitore")
    @POST
    @Path("/collegaOAF")
    @RolesAllowed({Ruolo.ADMIN, Ruolo.AMMINISTRATIVO})
    public Response collegaOAF(CollegaOAFDto dto) {
        try {
            if (dto == null) {
                return Response.status(Response.Status.NOT_MODIFIED).entity(new ResponseDto("Nessun collegamento creativo", false)).build();
            }
            return Response.status(Response.Status.OK).entity(service.collegaOAF(dto)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseDto(e.getMessage(), true)).build();
        }
    }

    @Operation(summary = "Unisci ordine a fornitore")
    @POST
    @Path("/verificaOAF")
    @RolesAllowed({Ruolo.ADMIN, Ruolo.AMMINISTRATIVO})
    public Response verificaOAF(CollegaOAFDto dto) {
        try {
            if (dto == null) {
                return Response.status(Response.Status.NOT_MODIFIED).entity(new ResponseDto("Nessun collegamento creativo", false)).build();
            }
            return Response.status(Response.Status.OK).entity(service.verificaOAF(dto)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseDto(e.getMessage(), true)).build();
        }
    }
}
