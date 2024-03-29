package it.calolenoci.resource;

import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import io.vertx.core.file.OpenOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.file.AsyncFile;
import it.calolenoci.dto.*;
import it.calolenoci.entity.GoOrdine;
import it.calolenoci.entity.Ordine;
import it.calolenoci.enums.StatoOrdineEnum;
import it.calolenoci.scheduler.FetchScheduler;
import it.calolenoci.service.ArticoloService;
import it.calolenoci.service.FirmaService;
import it.calolenoci.service.JasperService;
import it.calolenoci.service.OrdineService;
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
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static it.calolenoci.enums.Ruolo.*;
import static java.util.stream.Collectors.groupingBy;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Produces(APPLICATION_JSON)
@Path("api/ordini-clienti")
@RequestScoped
public class OrdineResource {

    @Inject
    Vertx vertx;

    @Inject
    JasperService service;


    @Inject
    OrdineService ordineService;

    @Inject
    FirmaService firmaService;

    @ConfigProperty(name = "firma.store.path")
    String pathFirma;

    @ConfigProperty(name = "firma.venditore.path")
    String pathFirmaVendtore;

    @Inject
    ArticoloService articoloService;

    @Inject
    FetchScheduler scheduler;

    @Inject
    @Claim(standard = Claims.nickname)
    String codVenditore;

    @Inject
    @Claim(standard = Claims.upn)
    String user;

    @ConfigProperty(name = "ordini.path")
    String pathReport;

    @Operation(summary = "Returns all the roles from the database")
    @POST
    @APIResponse(responseCode = "200", description = "Pdf generato con successo")
    @Consumes(MULTIPART_FORM_DATA)
    @Path("/upload")
    @RolesAllowed({ADMIN, VENDITORE})
    @Transactional
    public Response upload(MultipartBody data) {

        final Integer anno = Integer.valueOf(data.orderId.split("_")[0]);
        final String serie = data.orderId.split("_")[1];
        final Integer progressivo = Integer.valueOf(data.orderId.split("_")[2]);
        String filename = "firma_" + data.orderId + ".png";
        String name = pathFirma + filename;
        String firmaVenditore = pathFirmaVendtore + serie + ".png";
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

        OrdineDTO ordineDTO = ordineService.findForReport(anno, serie, progressivo);
        GoOrdine.update("hasFirma = 'T' WHERE anno = :anno AND serie = :serie AND progressivo = :progressivo",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo));
        if (ordineDTO != null) {
            List<OrdineDettaglioDto> articoli = articoloService.findForReport(anno, serie, progressivo);
            List<OrdineReportDto> dtoList = service.getOrdiniReport(ordineDTO, articoli, name, firmaVenditore);
            if (!dtoList.isEmpty()) {
                try {
                    service.createReport(dtoList, ordineDTO.getSottoConto(), anno, serie, progressivo);
                } catch (JRException | IOException e) {
                    Log.error("Errore nella creazione del report per l'ordine " + data.orderId, e);
                    throw new RuntimeException(e);
                }
            }
        }
        return Response.ok().entity(new ResponseDto("Firma creata con successo!", false)).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @POST
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    public Response getAllOrdini(FiltroOrdini filtro) throws ParseException {
        long i =  System.currentTimeMillis();
        if(StatoOrdineEnum.TUTTI.getDescrizione().equals(filtro.getStatus())){
            filtro.setStatus(null);
        }
        List<OrdineDTO> allByStatus = ordineService.findAllByStatus(filtro);
        long fine = System.currentTimeMillis();
        io.quarkus.logging.Log.info("get all ordini: " + (fine - i) + " msec");
        return Response.ok(allByStatus).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @POST
    @RolesAllowed({ADMIN, VENDITORE, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    @Path("/consegne")
    public Response getAllOrdiniByStati(FiltroOrdini filtro) throws ParseException {
        if(StatoOrdineEnum.TUTTI.getDescrizione().equals(filtro.getStatus())){
            List<String> stati = new ArrayList<>();
            stati.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
            stati.add(StatoOrdineEnum.COMPLETO.getDescrizione());
            stati.add(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
            filtro.setStati(stati);
            filtro.setStatus(null);
        }
        return Response.ok(ordineService.findAllByStati(filtro)).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @POST
    @RolesAllowed({ADMIN, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    @Path("/pregressi")
    public Response getAllPregressi(FiltroOrdini filtro) throws ParseException {
        return Response.ok(ordineService.getAllPregressi(filtro)).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @POST
    @RolesAllowed({ADMIN, VENDITORE, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    @Path("/riservati")
    public Response getAllOrdiniRiservati(FiltroOrdini filtro) throws ParseException {
        if(StatoOrdineEnum.TUTTI.getDescrizione().equals(filtro.getStatus())){
            List<String> stati = new ArrayList<>();
            stati.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
            stati.add(StatoOrdineEnum.COMPLETO.getDescrizione());
            stati.add(StatoOrdineEnum.DA_ORDINARE.getDescrizione());
            stati.add(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
            filtro.setStati(stati);
            filtro.setStatus(null);
        }
        List<OrdineDTO> allRiservati = ordineService.findAllRiservati(filtro);

        allRiservati.forEach(ordineDTO -> {
            if(ordineDTO.getImportoRiservati() == null) {
                ordineDTO.setImportoRiservati(0D);
            }
        });
        OrdineResponseDto dto = new OrdineResponseDto();
        dto.setOrdineDTOList(allRiservati);
        Map<String, Double> map = new HashMap<>();
        Map<String, List<OrdineDTO>> listMap = allRiservati.stream().collect(groupingBy(OrdineDTO::getSerie));
        for (String venditore : listMap.keySet()) {
           map.put(venditore, listMap.get(venditore).stream().mapToDouble(OrdineDTO::getImportoRiservati).sum());
        }
        dto.setImportoRiservatiMap(map);
        return Response.ok(dto).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @Path("/getStati")
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    public Response getStati() {
        return Response.ok(ordineService.getStati()).build();
    }

    @Consumes(APPLICATION_JSON)
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @Path("/unlock/{anno}/{serie}/{progressivo}")
    @GET
    @Transactional
    public Response unlock(Integer anno, String serie, Integer progressivo) {
        GoOrdine.update("locked = 'F', userLock = null where anno =:anno and serie =:serie and progressivo = :progressivo",
                Parameters.with("anno", anno)
                        .and("serie", serie)
                        .and("progressivo", progressivo));
        return Response.status(Response.Status.OK).entity(new ResponseDto("ordine sbloccato", false)).build();
    }

    @GET
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @Path("/aggiornaListaOrdini")
    public Response aggiornaListaOrdini() throws ParseException {
        scheduler.findNuoviOrdini();
        return Response.ok().build();
    }

    @GET
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @Path("/aggiornaBolle")
    public Response aggiornaBolle() throws ParseException {
        scheduler.update();
        return Response.ok(ordineService.findAllByStatus(new FiltroOrdini())).build();
    }

    @GET
    @Path("/apriOrdine/{anno}/{serie}/{progressivo}/{status}")
    @RolesAllowed({ADMIN, MAGAZZINIERE, AMMINISTRATIVO})
    public Response apriOrdine(Integer anno, String serie, Integer progressivo, String status) {
        ordineService.changeStatus(anno, serie, progressivo, status);
        return Response.ok(new ResponseDto("Ordine riaperto", false)).build();
    }

    @POST
    @Path("/addNotes/{from}")
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @Transactional
    @Consumes(APPLICATION_JSON)
    public Response addNotes(OrdineDTO dto, Integer from) {
        String query;
        Map<String, Object> param = new HashMap<>();
        if(from == 0) {
          query = "dataNote =:d, userNote =:u,  note ";
          param.put("d", LocalDateTime.now());
          param.put("u", user);
        } else {
            query = "dataNoteLogistica =:d, userNoteLogistica =:u, noteLogistica ";
            param.put("d", LocalDateTime.now());
            param.put("u", user);
        }
        param.put("note", dto.getNote());
        param.put("anno", dto.getAnno());
        param.put("serie", dto.getSerie());
        param.put("progressivo", dto.getProgressivo());
        query += "= :note WHERE anno =:anno and serie =:serie and progressivo = :progressivo";
        GoOrdine.update(query, param);
        return Response.ok(new ResponseDto("Nota aggiunta", false)).build();
    }


    // we need a Vertx instance for accessing filesystem

    @GET
    @Path("/downloadOrdine/{sottoConto}/{anno}/{serie}/{progressivo}")
    @Produces(MediaType.TEXT_PLAIN)
    @PermitAll
    public Uni<Response> streamDataFromFile(String sottoConto, Integer anno, String serie, Integer progressivo) {
        final OpenOptions openOptions = (new OpenOptions()).setCreate(false).setWrite(false);
        String ordineId = sottoConto + "_" + anno + "_" + serie + "_" + progressivo + ".pdf";

        Uni<AsyncFile> uni1 = vertx.fileSystem()
                .open(pathReport + anno + "/" + serie + "/" + ordineId, openOptions);

        return uni1.onItem()
                .transform(asyncFile -> Response.ok(asyncFile)
                        .header("Content-Disposition", "attachment;filename=" + ordineId)
                        .build());
    }

    @Transactional
    @Operation(summary = "salva testata ordini")
    @PUT
    @Produces(APPLICATION_JSON)
    @RolesAllowed({ADMIN})
    @Consumes(APPLICATION_JSON)
    public Response salva(OrdineDTO dto) {
        if (dto == null) {
            return Response.status(Response.Status.NOT_MODIFIED).entity(new ResponseDto("lista vuota", true)).build();
        }
        GoOrdine.update("status =:status WHERE anno=:anno AND serie=:serie AND progressivo =:progressivo",
                Parameters.with("status", dto.getStatus()).and("anno", dto.getAnno())
                        .and("serie", dto.getSerie()).and("progressivo", dto.getProgressivo()));
        return Response.status(Response.Status.CREATED).entity(new ResponseDto("Lista salvata", false)).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    @Path("/cercaAltriOrdiniCliente/{anno}/{serie}/{progressivo}/{sottoConto}")
    public Response findAltriOrdiniCliente(Integer anno, String serie, Integer progressivo, String sottoConto) throws ParseException {
        return Response.ok(ordineService.findAltriOrdiniCliente(anno, serie, progressivo, sottoConto)).build();
    }

    @RolesAllowed({ADMIN, LOGISTICA})
    @PUT
    @Path("/updateVeicolo")
    @Consumes(APPLICATION_JSON)
    public Response update(OrdineDTO dto) {
        if(ordineService.updateVeicolo(dto)){
            return Response.ok(new ResponseDto("Veicoli aggiornati con successo", false)).build();
        } else {
            return Response.notModified().build();
        }
    }

    @RolesAllowed({ADMIN, LOGISTICA})
    @POST
    @Path("/salvaPregressi")
    @Consumes(APPLICATION_JSON)
    public Response salvaPregressi(List<OrdineDTO> dto) {
        if(ordineService.salvaPregressi(dto)){
            return Response.ok(new ResponseDto("Veicoli aggiornati con successo", false)).build();
        } else {
            return Response.notModified().build();
        }
    }

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @Path("/getOrdiniClienteNonOrdinati")
    @RolesAllowed({ADMIN})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    public Response getOrdiniClienteNonOrdinati() throws ParseException {
        return Response.ok(ordineService.getOrdiniClienteNonOrdinati()).build();
    }


}
