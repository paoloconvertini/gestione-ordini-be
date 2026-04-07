package it.calolenoci.resource;

import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import io.vertx.core.file.OpenOptions;
import io.vertx.mutiny.core.Vertx;
import it.calolenoci.dto.*;
import it.calolenoci.entity.GoOrdVeicolo;
import it.calolenoci.entity.GoOrdine;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.enums.StatoOrdineEnum;
import it.calolenoci.scheduler.FetchScheduler;
import it.calolenoci.service.*;
import jakarta.annotation.security.PermitAll;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jfree.util.Log;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;

import static it.calolenoci.enums.Ruolo.*;
import static java.util.stream.Collectors.groupingBy;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Produces(APPLICATION_JSON)
@Path("api/ordini-clienti")
@RequestScoped
public class OrdineResource {

    @Inject
    Vertx vertx;

    @Inject
    JasperService service;

    @Inject
    FetchScheduler fetchScheduler;

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

    @Inject
    FatturaService fatturaService;

    @Inject
    ResiduoService residuoService;

    @Operation(summary = "Returns all the roles from the database")
    @POST
    @APIResponse(responseCode = "200", description = "Pdf generato con successo")
    @Consumes(MULTIPART_FORM_DATA)
    @Path("/upload")
    @RolesAllowed({ADMIN, VENDITORE, LOGISTICA, AMMINISTRATIVO, MAGAZZINIERE})
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
        if(StatoOrdineEnum.TUTTI.getDescrizione().equals(filtro.getFiltroStatus())){
            filtro.setFiltroStatus(null);
        }
        PageOrdineDto allByStatus = ordineService.findAllByStatus(filtro);
        long fine = System.currentTimeMillis();
        io.quarkus.logging.Log.info("get all ordini: " + (fine - i) + " msec");
        return Response.ok(allByStatus).build();
    }

    @Operation(summary = "Lista degli ordini per la pagina delle consegne")
    @POST
    @RolesAllowed({ADMIN, VENDITORE, LOGISTICA, AMMINISTRATIVO, MAGAZZINIERE})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    @Path("/consegne")
    public Response getAllOrdiniByStati(FiltroOrdini filtro) throws ParseException {
        if(StatoOrdineEnum.TUTTI.getDescrizione().equals(filtro.getFiltroStatus())){
            List<String> stati = new ArrayList<>();
            stati.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
            stati.add(StatoOrdineEnum.COMPLETO.getDescrizione());
            stati.add(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
            stati.add(StatoOrdineEnum.DA_ORDINARE.getDescrizione());
            filtro.setStati(stati);
            filtro.setFiltroStatus(null);
        }
        return Response.ok(ordineService.findAllByStati(filtro)).build();
    }

    @POST
    @Path("/consegne/map")
    @RolesAllowed({ADMIN, VENDITORE, LOGISTICA, AMMINISTRATIVO, MAGAZZINIERE})
    public Response getOrdiniMappa(FiltroOrdini filtro) throws ParseException {
        if(StatoOrdineEnum.TUTTI.getDescrizione().equals(filtro.getFiltroStatus())){
            List<String> stati = new ArrayList<>();
            stati.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
            stati.add(StatoOrdineEnum.COMPLETO.getDescrizione());
            stati.add(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
            stati.add(StatoOrdineEnum.DA_ORDINARE.getDescrizione());
            filtro.setStati(stati);
            filtro.setFiltroStatus(null);
        }
        filtro.setSize(0); // oppure null
        return Response.ok(ordineService.findAllByStati(filtro).getList()).build();
    }


    @Operation(summary = "Returns all the ordini from the database")
    @POST
    @RolesAllowed({ADMIN, VENDITORE, LOGISTICA, AMMINISTRATIVO, MAGAZZINIERE})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    @Path("/consegne-settimanali")
    public Response getConsegneSettimanali(FiltroOrdini filtro) throws ParseException {
        return Response.ok(ordineService.consegneSettimanali(filtro)).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @POST
    @RolesAllowed({ADMIN, VENDITORE, LOGISTICA, AMMINISTRATIVO, MAGAZZINIERE})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    @Path("/pregressi")
    public Response getAllPregressi(FiltroOrdini filtro) throws ParseException {
        return Response.ok(ordineService.getAllPregressi(filtro)).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @POST
    @RolesAllowed({ADMIN, VENDITORE, LOGISTICA, AMMINISTRATIVO, MAGAZZINIERE})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    @Path("/riservati")
    public Response getAllOrdiniRiservati(FiltroOrdini filtro) throws ParseException {
        if(StatoOrdineEnum.TUTTI.getDescrizione().equals(filtro.getFiltroStatus())){
            List<String> stati = new ArrayList<>();
            stati.add(StatoOrdineEnum.INCOMPLETO.getDescrizione());
            stati.add(StatoOrdineEnum.COMPLETO.getDescrizione());
            stati.add(StatoOrdineEnum.DA_ORDINARE.getDescrizione());
            stati.add(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
            filtro.setStati(stati);
            filtro.setFiltroStatus(null);
        }
        PageOrdineDto pageOrdineDto = ordineService.findAllRiservati(filtro);
        List<OrdineDTO> allRiservati = pageOrdineDto.getList();
        allRiservati.forEach(ordineDTO -> {
            if(ordineDTO.getImportoRiservati() == null) {
                ordineDTO.setImportoRiservati(0D);
            }
        });
        OrdineResponseDto dto = new OrdineResponseDto();
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
        FiltroOrdini f = new FiltroOrdini();
        f.setSize(10);
        f.setPage(0);
        return Response.ok(ordineService.findAllByStatus(f)).build();
    }

    @GET
    @Path("/apriOrdine/{anno}/{serie}/{progressivo}/{status}")
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
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
    @RolesAllowed({ADMIN, VENDITORE, LOGISTICA, AMMINISTRATIVO, MAGAZZINIERE})
    public Uni<Response> streamDataFromFile(String sottoConto, Integer anno, String serie, Integer progressivo) {
        final OpenOptions openOptions = new OpenOptions().setCreate(false).setWrite(false);
        String ordineId = sottoConto + "_" + anno + "_" + serie + "_" + progressivo + ".pdf";
        String fullPath = pathReport + anno + "/" + serie + "/" + ordineId;

        return vertx.fileSystem()
                .open(fullPath, openOptions)
                .onItem().transform(asyncFile ->
                        Response.ok(asyncFile)
                                .header("Content-Disposition", "attachment;filename=" + ordineId)
                                .build()
                )
                .onFailure().recoverWithItem(err -> Response.status(Response.Status.NOT_FOUND)
                        .entity("File non trovato: " + ordineId)
                        .build());
    }

    @Transactional
    @Operation(summary = "salva testata ordini")
    @PUT
    @Produces(APPLICATION_JSON)
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
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
   @Operation(summary = "Returns all the ordini from the database")
    @POST
   @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    @Path("/ordine-fattura-acconto/{sottoConto}")
    public Response findOrdiniPerFatturaAcconto(String sottoConto) throws ParseException {
        return Response.ok(ordineService.findOrdiniPerFatturaAcconto(sottoConto)).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @POST
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    @Path("/crea-fattura-acconto")
    public Response creaFatturaAcconto(List<FatturaAccontoDto> fatturaAccontoDtoList) {
        String result = ordineService.creaFatturaAcconto(fatturaAccontoDtoList, user);
        return Response.ok(new ResponseDto(result, StringUtils.isBlank(result))).build();
    }

    @Operation(summary = "Acconti NON validati legati ad un ordine cliente (lista)")
    @GET
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @Path("/accontiNonValidati/{anno}/{serie}/{progressivo}")
    public Response getAccontiNonValidatiByOrdine(Integer anno, String serie, Integer progressivo) {
        return Response.ok(fatturaService.findAccontiNonValidatiByOrdine(anno, serie, progressivo)).build();
    }

    @Operation(summary = "Conteggio acconti NON validati legati ad un ordine cliente")
    @GET
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @Path("/accontiNonValidati/{anno}/{serie}/{progressivo}/count")
    public Response countAccontiNonValidatiByOrdine(Integer anno, String serie, Integer progressivo) {
        long count = fatturaService.countAccontiNonValidatiByOrdine(anno, serie, progressivo);
        return Response.ok(count).build(); // ritorna un numero
    }

    /**
     * Esempio di guard per storno: rifiuta se non validata
     * (se hai già una rotta di storno altrove, riutilizza il check service.isValidata)
     */
    @Operation(summary = "Storno acconto (consentito solo se validata)")
    @POST
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @Path("/storno-acconto")
    public Response stornoAcconto(@QueryParam("anno") Integer anno,
                                  @QueryParam("serie") String serie,
                                  @QueryParam("progressivo") Integer progressivo) {
        if (!fatturaService.isValidata(anno, serie, progressivo)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseDto("Impossibile stornare: l'acconto non è validato (mancano numero e/o data fattura).", true))
                    .build();
        }
        // TODO: logica di storno reale
        return Response.ok(new ResponseDto("Storno eseguito", false)).build();
    }

    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @PUT
    @Path("/updateVeicolo")
    @Consumes(APPLICATION_JSON)
    public Response updateVeicolo(ProgrammaConsegnaDto dto) {
        if(ordineService.programmaConsegna(dto, codVenditore)){
            return Response.ok(new ResponseDto("Consegna inserita con successo", false)).build();
        } else {
            return Response.notModified().build();
        }
    }

    @DELETE
    @Path("/elimina-programmazione/{anno}/{serie}/{progressivo}")
    @Transactional
    public Response eliminaProgrammazione(@PathParam("anno") Integer anno,
                                      @PathParam("serie") String serie,
                                      @PathParam("progressivo") Integer progressivo) {

        ordineService.eliminaProgrammazione(anno, serie, progressivo);
        return Response.ok(new ResponseDto("Consegna rimossa con successo", false)).build();
    }

    @PUT
    @Path("/riordina-consegne")
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @Transactional
    public Response riordinaConsegne(List<RiordinoDto> lista) {
        service.riordinaConsegne(lista);
        return Response.ok(new ResponseDto("Consegne riordinate con successo", false)).build();
    }

    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @POST
    @Path("/salvaPregressi")
    @Consumes(APPLICATION_JSON)
    public Response salvaPregressi(List<OrdineDTO> dto) {
        if(ordineService.salvaPregressi(dto, codVenditore)){
            return Response.ok(new ResponseDto("Veicoli aggiornati con successo", false)).build();
        } else {
            return Response.notModified().build();
        }
    }

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @Path("/getOrdiniClienteNonOrdinati")
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    public Response getOrdiniClienteNonOrdinati() throws ParseException {
        return Response.ok(ordineService.getOrdiniClienteNonOrdinati()).build();
    }

    @GET
    @Path("/run-bolle")
    @RolesAllowed({ADMIN})
    @Produces(MediaType.TEXT_PLAIN)
    public String runBolle() throws Exception {
        fetchScheduler.update();
        return "Scheduler eseguito manualmente.";
    }

    @Path("/test-residui")
    @GET
    @PermitAll
    public List<ResiduoDto> testResidui() {

        List<OrdineDettaglio> list = OrdineDettaglio.list("anno= ?1 and serie = ?2 and progressivo = ?3", 2024, "13", 246);

        List<OrdineDettaglioDto> dto = list.stream()
                .map(o -> {
                    OrdineDettaglioDto d = new OrdineDettaglioDto();
                    d.setProgrGenerale(o.getProgrGenerale());
                    d.setQuantita(o.getQuantita());
                    d.setAnno(o.getAnno());
                    d.setSerie(o.getSerie());
                    d.setProgressivo(o.getProgressivo());
                    d.setRigo(o.getRigo());
                    d.setFArticolo(o.getFArticolo());
                    d.setFDescrArticolo(o.getFDescrArticolo());
                    return d;
                }).toList();

        return residuoService.calcolaResidui(dto);
    }
}
