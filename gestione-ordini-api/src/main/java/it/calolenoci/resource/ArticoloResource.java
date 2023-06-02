package it.calolenoci.resource;

import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.*;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.service.ArticoloService;
import it.calolenoci.service.FatturaService;
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
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static it.calolenoci.enums.Ruolo.*;

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
    @Claim(standard = Claims.upn)
    String user;

    @Inject
    @Claim(standard = Claims.email)
    String email;

    @Operation(summary = "Returns all the articoli from the database")
    @POST
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = OrdineDettaglio.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Articoli")
    @Transactional
    public Response getArticoliByIdOrdine(FiltroArticoli filtro) {
        if(!filtro.getView()){
            Ordine.update("geLocked = 'T', geUserLock = :user where " +
                            "anno =:anno and serie =:serie and progressivo = :progressivo",
                    Parameters.with("user", user).and("anno", filtro.getAnno())
                            .and("serie", filtro.getSerie())
                            .and("progressivo", filtro.getProgressivo()));
        }
        return Response.ok(articoloService.findById(filtro)).build();
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
            articoloService.save(list, user, email);
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
            return Response.status(Response.Status.CREATED).entity(new ResponseDto(articoloService.save(list, user, true, email), false)).build();
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
        OrdineDettaglio.update("note = :note WHERE anno =:anno and serie =:serie and progressivo = :progressivo and rigo = :rigo",
                Parameters.with("note", dto.getNote()).and("anno", dto.getAnno())
                        .and("serie", dto.getSerie())
                        .and("progressivo", dto.getProgressivo())
                        .and("rigo", dto.getRigo()));
        return Response.ok(new ResponseDto("Nota aggiunta", false)).build();
    }

}
