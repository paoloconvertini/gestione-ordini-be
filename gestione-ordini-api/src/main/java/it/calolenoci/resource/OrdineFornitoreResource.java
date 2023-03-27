package it.calolenoci.resource;

import it.calolenoci.dto.ResponseDto;
import it.calolenoci.service.OrdineFornitoreService;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("api/v1/oaf")
public class OrdineFornitoreResource {

    @Inject
    OrdineFornitoreService service;

    @Claim(standard = Claims.iss)
    String username;

    @Inject
    JsonWebToken jsonWebToken;

    @Operation(summary = "Crea ordine a fornitore")
    @GET
    @RolesAllowed({"Admin", "Amministrativo"})
    @Path("/{anno}/{serie}/{progressivo}")
    public Response createOAF(Integer anno, String serie, Integer progressivo) {

        try {
            System.out.println(jsonWebToken.getName());
            service.save(anno, serie, progressivo);
            return Response.status(Response.Status.CREATED).entity(new ResponseDto("Salvataggio con successo!", false)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseDto(e.getMessage(), true)).build();
        }
    }


}
