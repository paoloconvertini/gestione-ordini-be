package it.calolenoci.resource;

import it.calolenoci.dto.*;
import it.calolenoci.entity.Ordine;
import it.calolenoci.enums.AzioneEnum;
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

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Produces(APPLICATION_JSON)
@Path("api/v1/ordini-clienti")
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
    @PermitAll
    @APIResponse(responseCode = "200", description = "Pdf generato con successo")
    @Consumes(MULTIPART_FORM_DATA)
    @Path("/upload")
    @RolesAllowed({"Admin", "Venditore"})
    public Response upload(MultipartBody data) throws IOException, JRException {

        final Integer anno = Integer.valueOf(data.orderId.split("_")[0]);
        final String serie = data.orderId.split("_")[1];
        final Integer progressivo = Integer.valueOf(data.orderId.split("_")[2]);
        String filename = "firma_" + data.orderId + ".png";
        String name = pathFirma + filename;
        firmaService.save(anno, serie, progressivo, filename);
        eventoService.save(anno, serie, progressivo, data.username, AzioneEnum.FIRMA, null);

        String encodedImage = data.file.split(",")[1];
        byte[] decodedImage = Base64.getDecoder().decode(encodedImage);
        FileOutputStream fos = new FileOutputStream(name);
        fos.write(decodedImage);
        fos.close();

        OrdineDTO ordineDTO = ordineService.findById(anno, serie, progressivo);
        if (ordineDTO != null) {
            ordineService.changeStatus(anno, serie, progressivo, StatoOrdineEnum.DA_PROCESSARE);
            List<OrdineDettaglioDto> articoli = articoloService.findAndChangeStatusById(anno, serie, progressivo, StatoOrdineEnum.DA_PROCESSARE);
            List<OrdineReportDto> dtoList = service.getOrdiniReport(ordineDTO, articoli, name);
            if (!dtoList.isEmpty()) {
                service.createReport(dtoList);
            }
        }
        return Response.ok().entity(new ResponseDto("Firma creata con successo!", false)).build();
    }


    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @RolesAllowed({"Admin", "Venditore", "Magazziniere", "Amministrativo"})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    public Response getAllOrdini(@QueryParam("status") String status) {
        return Response.ok(ordineService.findAllByStatus(status)).build();
    }

    @GET
    @RolesAllowed({"Admin", "Venditore", "Magazziniere", "Amministrativo"})
    @Path("/updateConsegne")
    public Response updateConsegne(@QueryParam("status") String status) {
        scheduler.update();
        return Response.ok(ordineService.findAllByStatus(status)).build();
    }

}
