package it.calolenoci.resource;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import io.vertx.core.file.OpenOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.file.AsyncFile;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import it.calolenoci.enums.Ruolo;
import it.calolenoci.service.ListaCarichiService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static it.calolenoci.enums.Ruolo.*;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Consumes(APPLICATION_JSON)
@Path("api/lista-carichi")
@RequestScoped
public class ListaCarichiResource {

    @Inject
    ListaCarichiService service;

    @Inject
    Vertx vertx;

    @ConfigProperty(name = "lista.carico.path")
    String pathReport;

    @Operation(summary = "Returns all the carichi from the database")
    @POST
    @RolesAllowed({ADMIN, VENDITORE, AMMINISTRATIVO})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ListaCarichi.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Carichi")
    @Consumes(APPLICATION_JSON)
    public Response carichi(FiltroCarichi filtroCarichi) {
        return Response.ok(service.findCarichi(filtroCarichi)).build();
    }

    @Operation(summary = "Returns all the carichi from the database")
    @POST
    @RolesAllowed({ADMIN, VENDITORE, AMMINISTRATIVO})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ListaCarichi.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Carichi")
    @Consumes(APPLICATION_JSON)
    @Path("/inviati")
    public Response carichiInviati(FiltroCarichi filtroCarichi) {
        return Response.ok(service.findCarichiInviati(filtroCarichi)).build();
    }

    @GET
    @Path("/download/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    @PermitAll
    public Uni<Response> streamDataFromFile(String id) {
        final OpenOptions openOptions = (new OpenOptions()).setCreate(false).setWrite(false);
        Uni<AsyncFile> uni1 = vertx.fileSystem()
                .open(pathReport + "/" + id + ".pdf", openOptions);

        return uni1.onItem()
                .transform(asyncFile -> Response.ok(asyncFile)
                        .header("Content-Disposition", "attachment;filename=" + id)
                        .build());
    }

    @Operation(summary = "Returns all the carichi from the database")
    @POST
    @RolesAllowed({ADMIN, VENDITORE, AMMINISTRATIVO})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ListaCarichi.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Carichi")
    @Consumes(APPLICATION_JSON)
    @Path("/convalide")
    public Response convalide(FiltroCarichi filtroCarichi) {
        return Response.ok(service.findConvalide(filtroCarichi)).build();
    }

    @Operation(summary = "Returns the carico by id")
    @GET
    @RolesAllowed({ADMIN, VENDITORE, AMMINISTRATIVO})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ListaCarichi.class, type = SchemaType.OBJECT)))
    @APIResponse(responseCode = "204", description = "No Carico")
    @Consumes(APPLICATION_JSON)
    @Path(("/{id}"))
    public Response getCarico(Long id) {
        return Response.ok(service.getCarico(id)).build();
    }

    @Transactional
    @Operation(summary = "salva carichi")
    @POST
    @Produces(APPLICATION_JSON)
    @RolesAllowed({ADMIN, VENDITORE, AMMINISTRATIVO})
    @Path("/salva")
    public Response salva(ListaCarichiDto dto) {
        if(service.salvaCarico(dto)) {
            return Response.status(Response.Status.CREATED).entity(new ResponseDto("Record salvati", false)).build();
        } else {
            return  Response.ok().entity(new ResponseDto("Numero ordine, " + dto.getNumeroOrdine() + ", già presente", true)).build();
        }
    }

    @POST
    @Path("/convalida")
    @Produces(APPLICATION_JSON)
    @RolesAllowed({ADMIN, VENDITORE, AMMINISTRATIVO})
    public Response convalida(List<ListaCarichiDto> list) {
        String nomeFile = service.creaReport(list);
        return  Response.status(Response.Status.CREATED).entity(new ResponseDto(nomeFile, Boolean.FALSE)).build();
    }

    @GET
    @Path("/generaPdf/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    @PermitAll
    public Uni<Response> generaPdf(String id) {
        final OpenOptions openOptions = (new OpenOptions()).setCreate(false).setWrite(false);
        Uni<AsyncFile> uni1 = vertx.fileSystem()
                .open(pathReport + "/" + id, openOptions);
        return uni1.onItem()
                .transform(asyncFile -> Response.ok(asyncFile)
                        .header("Content-Disposition", "attachment;filename=" + id)
                        .build());
    }

    @Operation(summary = "Returns all fornitori")
    @GET
    @Path("/cercaAzienda/{search}")
    @RolesAllowed({ADMIN, VENDITORE, AMMINISTRATIVO})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PianoConti.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Fornitori")
    @Consumes(APPLICATION_JSON)
    public Response cercaAzienda(String search) {
        return Response.ok(PianoConti.find("select p.intestazione FROM PianoConti p where p.gruppoConto = 2351 and p.intestazione like :s",
                        Parameters.with("s", "%"+search+"%"))
                .project(String.class).list()).build();
    }

    @Operation(summary = "Returns all depositi")
    @GET
    @Path("/cercaDeposito/{search}")
    @RolesAllowed({ADMIN, VENDITORE, AMMINISTRATIVO})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Deposito.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Deposito")
    @Consumes(APPLICATION_JSON)
    public Response cercaDeposito(String search) {
        return Response.ok(Deposito.find("select id, nome FROM Deposito where nome like :s",
                        Parameters.with("s", "%"+search+"%")).project(DizionarioDto.class).list()).build();
    }

    @Operation(summary = "Returns all depositi")
    @GET
    @Path("/depositi")
    @RolesAllowed({ADMIN, VENDITORE, AMMINISTRATIVO})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Deposito.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Deposito")
    @Consumes(APPLICATION_JSON)
    public Response depositi() {
        return Response.ok(Deposito.find("select id, nome FROM Deposito order by nome")
                .project(DizionarioDto.class).list()).build();
    }

    @POST
    @Transactional
    @Path("/depositi")
    @APIResponse(responseCode = "200", description = "Role salvato con successo")
    public Response saveDeposito(DizionarioDto dto) {
        Deposito d = Deposito.findById(dto.getId());
        if (d != null) {
            d.setNome(dto.getNome());
        } else {
            Deposito entity = new Deposito();
            entity.setNome(dto.getNome());
            entity.persist();
        }
        return Response.status(Response.Status.CREATED)
                .entity(new ResponseDto("Deposito salvato", false))
                .build();
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    public Response delete(Long id) {
        this.findById(id).delete();
        return Response.ok().entity(new ResponseDto("Deposito eliminato", false)).build();
    }

    @Operation(summary = "Returns all trasportatori")
    @GET
    @Path("/cercaTrasportatore/{search}")
    @RolesAllowed({ADMIN, VENDITORE, AMMINISTRATIVO})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Trasportatore.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Trasportatore")
    @Consumes(APPLICATION_JSON)
    public Response cercaTrasportatore(String search) {
        return Response.ok(Trasportatore.find("select id, nome FROM Trasportatore where nome like :s",
                        Parameters.with("s", "%"+search+"%")).project(DizionarioDto.class).list()).build();
    }

    private Deposito findById(Long id) {
        Deposito entity = Deposito.findById(id);
        if(entity == null) {
            throw new NotFoundException();
        }
        return entity;
    }
}
