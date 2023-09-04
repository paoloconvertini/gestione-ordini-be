package it.calolenoci.resource;

import com.dropbox.core.DbxException;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.*;
import it.calolenoci.entity.GoOrdine;
import it.calolenoci.entity.GoOrdineDettaglio;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.service.ArticoloService;
import it.calolenoci.service.DropBoxService;
import it.calolenoci.service.FatturaService;
import it.calolenoci.service.OrdineService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;

import static it.calolenoci.enums.Ruolo.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("api/articoli")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
public class ArticoloResource {

    @Inject
    ArticoloService articoloService;

    @Inject
    FatturaService fatturaService;

    @Inject
    OrdineService ordineService;

    @Inject
    @Claim(standard = Claims.upn)
    String user;

    @Inject
    @Claim(standard = Claims.email)
    String email;

    @Inject
    DropBoxService dropBoxService;

    @Operation(summary = "Crea ordine a fornitore")
    @POST
    @RolesAllowed({ADMIN, AMMINISTRATIVO})
    @Path("/codificaArticoli")
    public Response codificaArticoli(List<OrdineDettaglioDto> list) {
        try {
            if(!list.isEmpty()) {
                List<String> errors = articoloService.codificaArticoli(list, user);
                if(!errors.isEmpty()) {
                    return Response.status(Response.Status.CREATED).entity(errors).build();
                } else {
                    return Response.status(Response.Status.CREATED).entity(new ResponseDto("Articoli codificati ", false)).build();
                }
            } else {
                return Response.status(Response.Status.CREATED).entity(new ResponseDto("Nessun elemento salvato!", false)).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseDto(e.getMessage(), true)).build();
        }
    }

    @Operation(summary = "Returns all the articoli from the database")
    @POST
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = OrdineDettaglio.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Articoli")
    @Transactional
    public Response getArticoliByIdOrdine(FiltroArticoli filtro) {
        if(!filtro.getView()){
            GoOrdine.update("locked = 'T', userLock = :user where " +
                            "anno =:anno and serie =:serie and progressivo = :progressivo",
                    Parameters.with("user", user).and("anno", filtro.getAnno())
                            .and("serie", filtro.getSerie())
                            .and("progressivo", filtro.getProgressivo()));
        }
        return Response.ok(articoloService.findById(filtro)).build();
    }

    @Operation(summary = "Returns all the articoli from the database")
    @GET
    @RolesAllowed({ADMIN, VENDITORE, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = OrdineDettaglio.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Articoli")
    @Transactional
    @Path("/getArticoli/{bolla}/{anno}/{serie}/{progressivo}")
    public Response getArticoli(String bolla, Integer anno, String serie, Integer progressivo) {
        return Response.ok(articoloService.getArticoli(StringUtils.equals("Y",bolla), anno, serie, progressivo)).build();
    }

    @Operation(summary = "Returns all the articoli from the database")
    @GET
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = OrdineDettaglio.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Articoli")
    @Path("/getBolle/{progrCliente}")
    public Response getBolle(Integer progrCliente) {
        return Response.ok(fatturaService.getBolle(progrCliente)).build();
    }

    @Operation(summary = "Creazione bolle a partire da articoli in pronta consegna")
    @POST
    @RolesAllowed({ADMIN, LOGISTICA})
    @Path("/creaBolla")
    public Response creaBolla(Body body) {
        String result = fatturaService.creaBolla(body.getList(), body.getAccontoDtos());
        return Response.ok(new ResponseDto(result, StringUtils.isBlank(result))).build();
    }

    @Operation(summary = "Returns all the articoli from the database")
    @POST
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = OrdineDettaglio.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Articoli")
    @Path("/cercaAcconti/{sottoConto}")
    public Response cercaAcconti(String sottoConto, List<OrdineDettaglioDto> list) {
        return Response.ok(fatturaService.getAcconti(sottoConto, list)).build();
    }

    @Operation(summary = "Returns all the articoli from the database")
    @GET
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = OrdineDettaglio.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Articoli")
    @Path("/getAcconti/{sottoConto}")
    public Response getAcconti(String sottoConto) {
        return Response.ok(fatturaService.getAcconti(sottoConto)).build();
    }

    @Operation(summary = "Save dettaglio ordine")
    @PUT
    @RolesAllowed({ADMIN, MAGAZZINIERE, AMMINISTRATIVO})
    public Response saveArticoli(List<OrdineDettaglioDto> list) {
        if (!list.isEmpty()) {
            articoloService.save(list, user);
            return Response.status(Response.Status.CREATED).entity(new ResponseDto("Salvataggio con successo!", false)).build();
        }
        return Response.status(Response.Status.OK).entity(new ResponseDto("lista vuota", true)).build();
    }

    @Operation(summary = "Save dettaglio ordine")
    @POST
    @RolesAllowed({ADMIN, MAGAZZINIERE, AMMINISTRATIVO})
    @Path("/chiudi")
    public Response chiudi(List<OrdineDettaglioDto> list) {
        if (!list.isEmpty()) {
            return Response.status(Response.Status.CREATED).entity(new ResponseDto(articoloService.save(list, user, true), false)).build();
        }
        return Response.status(Response.Status.CREATED).entity(new ResponseDto("lista vuota", true)).build();
    }

    @Operation(summary = "Save dettaglio ordine")
    @POST
    @RolesAllowed({ADMIN, AMMINISTRATIVO})
    @Path("/addFornitore")
    public Response addFornitore(PianoContiDto dto) {
        boolean ok = this.articoloService.addFornitore(dto, user);
        if(!ok) {
            return Response.status(Response.Status.OK).entity(new ResponseDto("Errore! Non è stato possibile aggiungere il fornitore " + dto.getIntestazione() + " all'articolo " + dto.getCodiceArticolo(), true)).build();
        }
        return Response.status(Response.Status.CREATED).entity(new ResponseDto("Fornitore aggiunto all'articolo " + dto.getCodiceArticolo(), false)).build();
    }

    @POST
    @Path("/addNotes")
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @Transactional
    @Consumes(APPLICATION_JSON)
    public Response addNotes(OrdineDettaglioDto dto){
        GoOrdineDettaglio.update("note = :note WHERE anno =:anno and serie =:serie and progressivo = :progressivo and rigo = :rigo",
                Parameters.with("note", dto.getNote()).and("anno", dto.getAnno())
                        .and("serie", dto.getSerie())
                        .and("progressivo", dto.getProgressivo())
                        .and("rigo", dto.getRigo()));
        return Response.ok(new ResponseDto("Nota aggiunta", false)).build();
    }

    @POST
    @Path("/scaricaSchedeTecniche")
    @Produces(MediaType.TEXT_PLAIN)
    @PermitAll
    public Response dpx(List<OrdineDettaglioDto> list) {
        File zip;
        try {
            zip = dropBoxService.list(list);
            if(zip != null){
                return Response.ok(zip).header("Content-Disposition", "attachment;filename=" + zip.getName()).build();
            } else {
                return Response.noContent().build();
            }
        } catch (Exception e) {
            Log.error("Errore scarica dropbox file", e);
        }
        return null;
    }

}
