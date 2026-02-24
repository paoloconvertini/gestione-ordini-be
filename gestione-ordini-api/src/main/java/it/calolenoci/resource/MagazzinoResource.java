package it.calolenoci.resource;

import io.quarkus.logging.Log;
import it.calolenoci.dto.*;
import it.calolenoci.entity.Causale;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.entity.Vettore;
import it.calolenoci.enums.Ruolo;
import it.calolenoci.service.JasperService;
import it.calolenoci.service.OrdineFornitoreService;
import it.calolenoci.service.SaldiMagazzinoService;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.text.ParseException;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("api/saldi-magazzino")
@RequestScoped
public class MagazzinoResource {

    @Inject
    SaldiMagazzinoService service;

    @Inject
    @Claim(standard = Claims.upn)
    String user;



    @Operation(summary = "Carica articoli in magazzino")
    @POST
    @RolesAllowed({Ruolo.ADMIN, Ruolo.AMMINISTRATIVO})
    public Response caricaMagazzino(CaricoMagazzinoDto dto) {
        try {
            if (dto != null && dto.getArticoli().isEmpty()) {
                return Response.status(Response.Status.CREATED).entity(new ResponseDto("Nessun articolo da caricare in magazzino!", true)).build();
            }
            String save = service.save(dto, user);
            return Response.ok().entity(new ResponseDto(save, StringUtils.isBlank(save))).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseDto(e.getMessage(), true)).build();
        }
    }

    @Operation(summary = "Carica articoli in magazzino")
    @Path("/vettori")
    @RolesAllowed({Ruolo.ADMIN, Ruolo.AMMINISTRATIVO})
    @GET
    public Response caricaVettori() {
        try {
            List<DizionarioStringDto> list = Vettore.find("SELECT v.vettore, v.intestvettore FROM Vettore v").project(DizionarioStringDto.class).list();
            return Response.ok().entity(list).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseDto(e.getMessage(), true)).build();
        }
    }

    @Operation(summary = "Carica articoli in magazzino")
    @Path("/causali")
    @GET
    @RolesAllowed({Ruolo.ADMIN, Ruolo.AMMINISTRATIVO})
    public Response caricaCausali() {
        try {
            List<DizionarioStringDto> list = Causale.find("SELECT c.mcausale, c.descrcausalemag FROM Causale c").project(DizionarioStringDto.class).list();
            return Response.ok().entity(list).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseDto(e.getMessage(), true)).build();
        }
    }

}
