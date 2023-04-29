package it.calolenoci.resource;

import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.*;
import it.calolenoci.entity.Ordine;
import it.calolenoci.enums.StatoOrdineEnum;
import it.calolenoci.scheduler.FetchScheduler;
import it.calolenoci.service.*;
import net.sf.jasperreports.engine.JRException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jfree.util.Log;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Base64;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static it.calolenoci.enums.Ruolo.*;

@Produces(APPLICATION_JSON)
@Path("api/ordini-clienti")
@RequestScoped
public class OrdineResource {

    @Inject
    JasperService service;


    @Inject
    OrdineService ordineService;

    @Inject
    FirmaService firmaService;

    @ConfigProperty(name = "firma.store.path")
    String pathFirma;

    @Inject
    ArticoloService articoloService;

    @Inject
    FetchScheduler scheduler;

    @Operation(summary = "Returns all the roles from the database")
    @POST
    @APIResponse(responseCode = "200", description = "Pdf generato con successo")
    @Consumes(MULTIPART_FORM_DATA)
    @Path("/upload")
    @RolesAllowed({ADMIN, VENDITORE})
    public Response upload(MultipartBody data) {

        final Integer anno = Integer.valueOf(data.orderId.split("_")[0]);
        final String serie = data.orderId.split("_")[1];
        final Integer progressivo = Integer.valueOf(data.orderId.split("_")[2]);
        String filename = "firma_" + data.orderId + ".png";
        String name = pathFirma + filename;
        firmaService.save(anno, serie, progressivo, filename);

        String encodedImage = data.file.split(",")[1];
        byte[] decodedImage = Base64.getDecoder().decode(encodedImage);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(name);
            fos.write(decodedImage);
            fos.close();
        } catch (IOException e) {
            Log.error("Errore nella creazione del file di firma " + filename, e);
            throw new RuntimeException(e);
        }

        OrdineDTO ordineDTO = ordineService.findById(anno, serie, progressivo);
        if (ordineDTO != null) {
            ResponseOrdineDettaglio responseOrdineDettaglio = articoloService.findById(new FiltroArticoli(anno, serie, progressivo));
            List<OrdineReportDto> dtoList = service.getOrdiniReport(ordineDTO, responseOrdineDettaglio.getArticoli(), name);
            if (!dtoList.isEmpty()) {
                try {
                    service.createReport(dtoList);
                    ordineService.changeStatus(anno, serie, progressivo, StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
                    articoloService.changeAllStatus(anno, serie, progressivo, StatoOrdineEnum.DA_PROCESSARE);
                } catch (JRException | IOException e) {
                    Log.error("Errore nella creazione del report per l'ordine " + data.orderId, e);
                    throw new RuntimeException(e);
                }
            }
        }
        return Response.ok().entity(new ResponseDto("Firma creata con successo!", false)).build();
    }


    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    public Response getAllOrdini(@QueryParam("status") String status) throws ParseException {
        return Response.ok(ordineService.findAllByStatus(status, null)).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @Path("/{venditore}")
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    public Response getAllOrdiniByVenditore(@QueryParam("status") String status, String venditore) throws ParseException {
        return Response.ok(ordineService.findAllByStatus(status, venditore)).build();
    }

    @Consumes(APPLICATION_JSON)
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @Path("/unlock/{anno}/{serie}/{progressivo}")
    @GET
    @Transactional
    public Response unlock(Integer anno, String serie, Integer progressivo) {
        Ordine.update("geLocked = 'F', geUserLock = null where anno =:anno and serie =:serie and progressivo = :progressivo",
                Parameters.with("anno", anno)
                        .and("serie", serie)
                        .and("progressivo", progressivo));
        return Response.status(Response.Status.OK).entity(new ResponseDto("ordine sbloccato", false)).build();
    }

    @GET
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @Path("/updateConsegne")
    public Response updateConsegne(@QueryParam("status") String status) throws ParseException {
        scheduler.update();
        return Response.ok(ordineService.findAllByStatus(status, null)).build();
    }

    @GET
    @Path("/apriOrdine/{anno}/{serie}/{progressivo}/{status}")
    @RolesAllowed({ADMIN, MAGAZZINIERE})
    public Response apriOrdine(Integer anno, String serie, Integer progressivo, String status){
        ordineService.changeStatus(anno, serie, progressivo, status);
        return Response.ok(new ResponseDto("Ordine riaperto", false)).build();
    }

}
