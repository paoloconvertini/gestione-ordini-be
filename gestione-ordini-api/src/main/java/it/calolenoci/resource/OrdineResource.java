package it.calolenoci.resource;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.MultipartBody;
import it.calolenoci.entity.OrdineClienteTestata;
import it.calolenoci.repository.OrdineTestatarepository;
import it.calolenoci.service.JasperService;
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
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Produces(APPLICATION_JSON)
@Path("api/v1/ordini-clienti")
public class OrdineResource {

    @Inject
    JasperService service;

    @Inject
    OrdineTestatarepository ordineTestatarepository;

    @Operation(summary = "Returns all the roles from the database")
    @POST
    @PermitAll
    @APIResponse(responseCode = "200", description = "Pdf generato con successo")
    @Consumes(MULTIPART_FORM_DATA)
    @Path("/upload/{id}")
    public Response upload(Long id, MultipartBody data) throws IOException {
        File targetFile = new File("src/main/resources/firma_" + id + ".png");
        FileUtils.copyInputStreamToFile(data.file, targetFile);
        return Response.ok().build();
    }

    @Operation(summary = "Returns all the roles from the database")
    @GET
    @PermitAll
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = OrdineClienteTestata.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Users")
    @Consumes(APPLICATION_JSON)
    public Response getAllOrdini() {
        return Response.ok(ordineTestatarepository.findAll()).build();
    }


}
