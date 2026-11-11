package it.calolenoci.resource;

import it.calolenoci.dto.AggiornamentoPrezziDto;
import it.calolenoci.dto.ResponseDto;
import it.calolenoci.dto.PrezziArticoliDto;
import it.calolenoci.dto.SogliaRicaricoDto;
import it.calolenoci.service.PrezziArticoliService;
import it.calolenoci.service.ArticoloClasseFornitoreService;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonString;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.math.BigDecimal;

@Path("api/prezzi-articoli")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@RequestScoped
public class PrezziArticoliResource {
    @Inject
    PrezziArticoliService service;

    @Inject
    @Claim(standard = Claims.upn)
    String user;

    @Inject
    JsonWebToken jwt;

    @Inject
    ArticoloClasseFornitoreService classiService;

    @GET
    public Response cerca(@QueryParam("classeA1") String classeA1,
                          @QueryParam("percentuale") BigDecimal percentuale,
                          @QueryParam("includiBloccati") @DefaultValue("false") boolean includiBloccati) {
        try {
            verificaPermesso();
            return Response.ok(new PrezziArticoliDto(service.cerca(classeA1, percentuale, includiBloccati),
                    service.ricaricoImpostato(classeA1))).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus()).entity(new ResponseDto(e.getMessage(), true)).build();
        }
    }

    @GET
    @Path("classi-fornitore")
    public Response classiFornitore() {
        try {
            verificaPermesso();
            return Response.ok(classiService.getClassi()).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus()).entity(new ResponseDto(e.getMessage(), true)).build();
        }
    }

    @GET
    @Path("storico")
    public Response storico(@QueryParam("articolo") String articolo) {
        try {
            verificaPermesso();
            return Response.ok(service.storico(articolo)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus()).entity(new ResponseDto(e.getMessage(), true)).build();
        }
    }

    @PUT
    public Response aggiorna(AggiornamentoPrezziDto richiesta) {
        try {
            verificaPermesso();
            int aggiornati = service.aggiorna(richiesta, user);
            return Response.ok(new ResponseDto("Prezzo base aggiornato per " + aggiornati + " articoli", false)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus()).entity(new ResponseDto(e.getMessage(), true)).build();
        }
    }

    @PUT
    @Path("soglia-fornitore")
    public Response salvaSoglia(SogliaRicaricoDto richiesta) {
        try {
            verificaPermesso();
            service.salvaSoglia(richiesta, user);
            return Response.ok(new ResponseDto("Soglia del fornitore salvata", false)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus()).entity(new ResponseDto(e.getMessage(), true)).build();
        }
    }

    private void verificaPermesso() {
        Object permissions = jwt.getClaim("permissions");
        if (!(permissions instanceof JsonArray array) || array.stream()
                .filter(JsonString.class::isInstance)
                .map(JsonString.class::cast)
                .noneMatch(p -> "prezzi_articoli.view".equals(p.getString()))) {
            throw new ForbiddenException("Non hai il permesso per gestire i prezzi articoli");
        }
    }
}
