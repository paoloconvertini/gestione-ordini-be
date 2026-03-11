package it.calolenoci.resource;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.NotaConsegnaDto;
import it.calolenoci.dto.ResponseDto;
import it.calolenoci.entity.NotaConsegna;
import it.calolenoci.entity.Veicolo;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("api/nota-consegna")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
public class NotaConsegnaResource {

    @Operation(summary = "Returns all the articoli from the database")
    @GET
    @PermitAll
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation =  NotaConsegna.class, type = SchemaType.OBJECT)))
    @APIResponse(responseCode = "204", description = "No Note")
    @Transactional
    @Path(value = "/{data}")
    public Response getNota(String data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        LocalDate localDate = LocalDate.parse(data, formatter);
        return Response.ok(NotaConsegna.find("dataNota =:d", Parameters.with("d", localDate)).firstResult()).build();
    }

    @Operation(summary = "Returns all the articoli from the database")
    @POST
    @PermitAll
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation =  NotaConsegna.class, type = SchemaType.OBJECT)))
    @APIResponse(responseCode = "204", description = "No Note")
    @Transactional
    public Response saveNota(NotaConsegnaDto dto) {
        NotaConsegna notaConsegna;
        if(StringUtils.isNotBlank(dto.getId())) {
            notaConsegna = NotaConsegna.findById(dto.getId());
            notaConsegna.setNota(dto.getNota());
        } else {
            notaConsegna = new NotaConsegna();
            notaConsegna.setNota(dto.getNota());
            notaConsegna.setDataNota(dto.getDataNota());
            notaConsegna.persist();
        }
        return Response.ok().status(Response.Status.CREATED).entity(new ResponseDto("Nota salvata con successo", false)).build();
    }

}
