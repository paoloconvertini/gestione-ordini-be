package it.calolenoci.resource;

import io.quarkus.panache.common.Sort;
import it.calolenoci.entity.Role;
import it.calolenoci.entity.User;
import it.calolenoci.service.CryptoService;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("api/v1/users")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class UserResource {

    @Inject
    CryptoService cryptoService;

    @POST
    @Transactional
    @RolesAllowed({"Admin", "User"})
    @APIResponse(responseCode = "200", description = "User salvato con successo")
    public Response saveUser(User user) {
        User entity = new User();
        entity.username = user.username;
        entity.name = user.name;
        entity.lastname = user.lastname;
        entity.password = cryptoService.encrypt(user.password);
        entity.dataNascita = user.dataNascita;
        List<Role> collect = user.roles.stream().map(r -> Role.findByName(r.name)).toList();
        entity.roles.addAll(collect);
        entity.persist();
        return Response.status(Response.Status.CREATED).entity(user).build();
    }

    @GET
    @Path("/{idUser}")
    @RolesAllowed({"Admin", "User"})
    public Response getUser(Long idUser) {
        User entity = findUserById(idUser);
        return Response.ok(entity).build();
    }

    @Operation(summary = "Returns all the roles from the database")
    @GET
    @RolesAllowed("Admin")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = User.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Users")
    public Response getAllUsers() {
        return Response.ok(User.findAll(Sort.ascending("name", "lastname"))).build();
    }

    @DELETE
    @Path("/{idUser}")
    @Transactional
    @RolesAllowed("Admin")
    public Response delete(Long idUser) {
        findUserById(idUser).delete();
        return Response.ok().build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed({"Admin", "User"})
    @APIResponse(responseCode = "404", description = "User non trovato")
    @APIResponse(responseCode = "200", description = "User aggiornato con successo")
    public Response update(Long id, User user) {
        User entity = findUserById(id);

        if (StringUtils.isNotBlank(user.name)) {
            entity.name = user.name;
        }
        if (StringUtils.isNotBlank(user.lastname)) {
            entity.lastname = user.lastname;
        }
        if (user.dataNascita != null) {
            entity.dataNascita = user.dataNascita;
        }
        if (user.roles != null && !user.roles.isEmpty()) {
            entity.roles = new ArrayList<>();
            List<Role> collect = user.roles.stream().map(r -> Role.findByName(r.name)).collect(Collectors.toList());
            entity.roles.addAll(collect);
        }
        if (StringUtils.isNotBlank(user.password)) {
            entity.password = cryptoService.encrypt(user.password);
        }

        return Response.ok().build();
    }

    private User findUserById(Long id) {
        User entity = User.findById(id);
        if (entity == null) {
            throw new NotFoundException();
        }
        return entity;
    }
}
