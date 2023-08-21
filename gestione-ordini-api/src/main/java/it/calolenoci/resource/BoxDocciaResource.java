package it.calolenoci.resource;

import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.BoxDocciaDto;
import it.calolenoci.dto.ResponseDto;
import it.calolenoci.entity.BoxDoccia;
import it.calolenoci.entity.Ordine;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static it.calolenoci.enums.Ruolo.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Produces(APPLICATION_JSON)
@Path("api/box-doccia")
@RequestScoped
public class BoxDocciaResource {


    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    public Response getAll() {
        return Response.ok(
                BoxDoccia.find("SELECT id,codice,descrizione FROM BoxDoccia WHERE venduto = 0")
                        .project(BoxDocciaDto.class).list()).build();
    }

    @Operation(summary = "Returns all the ordini from the database")
    @GET
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO, LOGISTICA})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Ordine.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Ordini")
    @Consumes(APPLICATION_JSON)
    @Path("/{id}")
    public Response getDettaglio(String id) {
        Optional<BoxDocciaDto> opt = BoxDoccia.find("SELECT profilo, estensibilita, versione, qta, prezzo, extra, foto, posa FROM BoxDoccia " +
                "WHERE id =:id", Parameters.with("id", id)).project(BoxDocciaDto.class).firstResultOptional();
        if(opt.isEmpty()) {
            return Response.noContent().build();
        } else {
            return Response.ok(opt.get()).build();
        }
    }



    @Transactional
    @Operation(summary = "salva testata ordini")
    @PUT
    @Produces(APPLICATION_JSON)
    @RolesAllowed({ADMIN, VENDITORE, MAGAZZINIERE, AMMINISTRATIVO})
    @Consumes(APPLICATION_JSON)
    public Response salva(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Response.status(Response.Status.NOT_MODIFIED).entity(new ResponseDto("lista vuota", true)).build();
        }
        BoxDoccia.update("venduto = '1' WHERE id IN (:ids)",
                Parameters.with("ids", ids));
        return Response.status(Response.Status.CREATED).entity(new ResponseDto("Record salvati", false)).build();
    }

}
