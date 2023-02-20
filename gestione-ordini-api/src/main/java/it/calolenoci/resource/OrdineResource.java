package it.calolenoci.resource;

import it.calolenoci.dto.MultipartBody;
import it.calolenoci.dto.OrdineReportDto;
import it.calolenoci.entity.*;
import it.calolenoci.repository.FirmaOrdineClienteRepository;
import it.calolenoci.repository.OrdineClienteRepository;
import it.calolenoci.repository.OrdineRepository;
import it.calolenoci.repository.OrdineTestataRepository;
import it.calolenoci.service.JasperService;
import it.calolenoci.service.OrdineService;
import net.sf.jasperreports.engine.JRException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Produces(APPLICATION_JSON)
@Path("api/v1/ordini-clienti")
public class OrdineResource {

    @Inject
    OrdineRepository rep;

    @Inject
    JasperService service;

    @Autowired
    OrdineTestataRepository ordineTestataRepository;

    @Autowired
    FirmaOrdineClienteRepository firmaOrdineClienteRepository;

    @Autowired
    OrdineClienteRepository ordineClienteRepository;

    @Inject
    OrdineService ordineService;


    @ConfigProperty(name = "firma.store.path")
    String pathFirma;

   @Path("/test")
   @GET
   @PermitAll
   @Consumes(APPLICATION_JSON)
   @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
   public Response test(){
       return Response.ok(rep.findAll()).build();
   }

    @Operation(summary = "Returns all the roles from the database")
    @POST
    @PermitAll
    @APIResponse(responseCode = "200", description = "Pdf generato con successo")
    @Consumes(MULTIPART_FORM_DATA)
    @Path("/upload")
    public Response upload(MultipartBody data) throws IOException, JRException {

        String filename = "firma_" + data.orderId + ".png";
        FirmaOrdineCliente firmaOrdineCliente = new FirmaOrdineCliente();
        OrdineId ordineId = new OrdineId(Integer.valueOf(data.orderId.split("_")[0]),
                data.orderId.split("_")[1], Integer.valueOf(data.orderId.split("_")[2]));
        firmaOrdineCliente.setOrdineId(ordineId);
        firmaOrdineCliente.setFileName(filename);
        firmaOrdineClienteRepository.save(firmaOrdineCliente);
        firmaOrdineCliente.setOrdineId(ordineId);
        firmaOrdineCliente.setFileName(filename);
        firmaOrdineClienteRepository.save(firmaOrdineCliente);

        RegistroAzioni r = new RegistroAzioni();
        r.setAnno(Integer.valueOf(data.orderId.split("_")[0]));
        r.setProgressivo(Integer.valueOf(data.orderId.split("_")[2]));
        r.setSerie(data.orderId.split("_")[1]);
        r.setData(new Date());
        r.setAzione("FIRMA");
        r.persist();

        String encodedImage = data.file.split(",")[1];
        byte[] decodedImage = Base64.getDecoder().decode(encodedImage);
        FileOutputStream fos = new FileOutputStream(pathFirma + filename);
        fos.write(decodedImage);
        fos.close();


        Optional<OrdineClienteTestata> ordineClienteTestata = ordineTestataRepository.findById(ordineId);

        List<OrdineReportDto> dtoList = new ArrayList<>();
        if(ordineClienteTestata.isPresent()) {
            OrdineClienteTestata ordine = ordineClienteTestata.get();
            List<OrdineCliente> articoli = ordineClienteRepository.findByOrdineDettaglioIdAnnoAndOrdineDettaglioIdSerieAndOrdineDettaglioIdProgressivoOrderByOrdineDettaglioId(ordineId.getAnno(),
                    ordineId.getSerie(), ordineId.getProgressivo());
            articoli.forEach(a -> {
                OrdineReportDto dto = new OrdineReportDto();
                dto.setANNO(ordineId.getAnno());
                dto.setSERIE(ordineId.getSerie());
                dto.setPROGRESSIVO(ordineId.getProgressivo());
                dto.setRIGO(a.getOrdineDettaglioId().getRigo());
                dto.setFARTICOLO("pippo");//FIXME aggiungere FARTICOLO
                dto.setCODARTFORNITORE(a.getCodArtFornitore());
                dto.setFDESCRARTICOLO(a.getFDescrArticolo());
                dto.setQUANTITA(a.getQuantita());
                dto.setPREZZO(a.getPrezzo());
                dto.setFUNITAMISURA(a.getFUnitaMisura());
                dto.setSCONTOARTICOLO(5f);
                dto.setINTESTAZIONE("Pippo PLuto");//FIXME aggiungere intestazione
                dto.setINDIRIZZO("Via le mani dal naso");//FIXME aggiungere indirizzo
                dto.setLOCALITA("Metropoli");//FIXME aggiungere localita
                dto.setCAP("00000");//FIXME aggiungere cap
                dto.setPROVINCIA("AZ");//FIXME aggiungere provincia
                dto.setTELEFONO("080 00 1100");//FIXME aggiungere telefono
                dto.setFCOLLI(1); //FIXME aggiungere colli
                dto.setFILENAME(pathFirma + filename);
                dto.setDATAORDINE(ordine.getDataOrdine());
                dto.setDATACONFERMA(ordine.getDataConferma());
                dto.setNUMEROCONFERMA(ordine.getNumeroConferma());
                dtoList.add(dto);
            });
        }

        if(!dtoList.isEmpty()){
            service.createReport(dtoList);
        }

        return Response.ok().build();
    }


    @Operation(summary = "Returns all the roles from the database")
    @GET
    @PermitAll
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = OrdineClienteTestata.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Users")
    @Consumes(APPLICATION_JSON)
    public Response getAllOrdini() {
        return Response.ok(ordineService.findAll()).build();
    }


}
