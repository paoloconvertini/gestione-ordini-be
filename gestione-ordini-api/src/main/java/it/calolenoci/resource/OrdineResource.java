package it.calolenoci.resource;

import it.calolenoci.dto.*;
import it.calolenoci.entity.Ordine;
import it.calolenoci.enums.AzioneEnum;
import it.calolenoci.enums.StatoOrdineEnum;
import it.calolenoci.scheduler.FetchScheduler;
import it.calolenoci.service.*;
import net.sf.jasperreports.engine.JRException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jfree.util.Log;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static it.calolenoci.enums.Ruolo.*;

@Produces(APPLICATION_JSON)
@Path("api/v1/ordini-clienti")
@RequestScoped
public class OrdineResource {

    @Inject
    JasperService service;


    @Inject
    OrdineService ordineService;

    @Inject
    FirmaService firmaService;

    @Inject
    EventoService eventoService;

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
            List<OrdineDettaglioDto> articoli = articoloService.findById(anno, serie, progressivo, false);
            List<OrdineReportDto> dtoList = service.getOrdiniReport(ordineDTO, articoli, name);
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
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    public Response getAllOrdini(@QueryParam("status") String status) {
        return Response.ok(ordineService.findAllByStatus(status)).build();
    }

    @GET
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO})
    @Path("/updateConsegne")
    public Response updateConsegne(@QueryParam("status") String status) {
        scheduler.update();
        return Response.ok(ordineService.findAllByStatus(status)).build();
    }

    @GET
    @Path("/apriOrdine/{anno}/{serie}/{progressivo}/{status}")
    @RolesAllowed({ADMIN, MAGAZZINIERE})
    public Response apriOrdine(Integer anno, String serie, Integer progressivo, String status){
        ordineService.changeStatus(anno, serie, progressivo, status);
        return Response.ok(new ResponseDto("Ordine riaperto", false)).build();
    }

}
