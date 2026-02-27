package it.calolenoci.service;

import it.calolenoci.config.AuthHeaderFactory;
import it.calolenoci.dto.UserResponseDTO;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/users")
@RegisterRestClient
@RegisterClientHeaders(AuthHeaderFactory.class)
public interface UserService {

    @GET
    @Path("/getVenditori")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns all venditori")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = UserResponseDTO.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Users")
    @Timeout(5000)
    List<UserResponseDTO> getVenditori();
}
