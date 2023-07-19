package it.calolenoci.resource;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.ResponseDTO;
import it.calolenoci.dto.SuperUserDTO;
import it.calolenoci.dto.UserResponseDTO;
import it.calolenoci.entity.Role;
import it.calolenoci.entity.User;
import it.calolenoci.service.CryptoService;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static it.calolenoci.constant.Ruolo.ADMIN;
import static it.calolenoci.constant.Ruolo.USER;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("api/users")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RequestScoped
public class UserResource {

    @Inject
    CryptoService cryptoService;

    @Inject
    @Claim(standard = Claims.nickname)
    String codVenditore;

    @POST
    @Transactional
    //@RolesAllowed({ADMIN})
    @PermitAll
    @APIResponse(responseCode = "200", description = "User salvato con successo")
    public Response saveUser(UserResponseDTO user) {
        User entity = new User();
        int count = 1;
        boolean exists = true;
        while (exists || count == user.getName().length()) {
            entity.username = StringUtils.truncate(user.getName(), count) + user.getLastname();
            count++;
            exists = (User.findByUsername(entity.username) != null);
        }
        entity.name = user.getName();
        entity.lastname = user.getLastname();
        entity.password = cryptoService.encrypt(user.getPassword());
        if (user.getDataNascita() != null) {
            entity.dataNascita = user.getDataNascita();
        }
        if (StringUtils.isNotBlank(user.getEmail())) {
            entity.email = user.getEmail();
        }
        if (!user.getRoles().isEmpty()) {
            List<Role> collect = user.getRoles().stream().map(r -> Role.findByName(r.name)).toList();
            entity.roles.addAll(collect);
        }
        entity.persist();
        return Response.status(Response.Status.CREATED).entity(user).build();
    }

    @GET
    @Path("/{idUser}")
    //@RolesAllowed({ADMIN})
    @PermitAll
    public Response getUser(Long idUser) {
        User entity = findUserById(idUser);
        return Response.ok(entity).build();
    }

    @Operation(summary = "Returns all the roles from the database")
    @GET
    @RolesAllowed({ADMIN})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = User.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Users")
    public Response getAllUsers() {
        return Response.ok(User.find("SELECT u.id, u.username, u.name, u.lastname FROM User u", Sort.ascending("username")).project(SuperUserDTO.class).list()).build();
    }

    @Operation(summary = "Returns all the roles from the database")
    @POST
    @RolesAllowed({ADMIN, USER})
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = User.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Users")
    @Path("/byRole")
    public Response getAllUsersByRole(List<String> ruoli) {

        List<User> list = User.listAll();
        List<User> users = list.stream().filter(u ->
                        u.roles.stream().anyMatch(r ->
                                ruoli.stream().anyMatch(role -> r.name.equals(role))))
                .toList();
        List<UserResponseDTO> result = new ArrayList<>();
        users.forEach(u -> {
            UserResponseDTO dto = new UserResponseDTO();
            dto.setFullname(u.name);
            dto.setChecked(Boolean.FALSE);
            if (StringUtils.isNotBlank(u.codVenditore)) {
                dto.setCodVenditore(u.codVenditore);
            }
            if (StringUtils.isNotBlank(u.email)) {
                dto.setEmail(u.email);
            }
            result.add(dto);
        });
        result.add(new UserResponseDTO("tutti", "", "", Boolean.TRUE));
        return Response.ok(result).build();
    }

    @Operation(summary = "Returns all the roles from the database")
    @GET
    @PermitAll
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = User.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Users")
    @Path("/getVenditori")
    public Response getVenditori() {

        List<User> list = User.listAll();
        List<User> users = list.stream().filter(u ->
                        u.roles.stream().anyMatch(r -> r.name.equals("Venditore"))).toList();
        List<UserResponseDTO> result = new ArrayList<>();
        users.forEach(u -> {
            UserResponseDTO dto = new UserResponseDTO();
            dto.setFullname(u.name);
            dto.setCodVenditore(u.codVenditore);
            if (StringUtils.isNotBlank(u.email)) {
                dto.setEmail(u.email);
            }
            result.add(dto);
        });
        return Response.ok(result).build();
    }


    @DELETE
    @Path("/{idUser}")
    @Transactional
    @RolesAllowed(ADMIN)
    public Response delete(Long idUser) {
        findUserById(idUser).delete();
        return Response.ok().entity(new ResponseDTO("Dipendente eliminato!", false)).build();
    }

    @PUT
    @Path("/{username}")
    @Transactional
    @PermitAll
    @APIResponse(responseCode = "404", description = "User non trovato")
    @APIResponse(responseCode = "200", description = "User aggiornato con successo")
    public Response updatePassword(String username, UserResponseDTO dto) {
        User entity = findUserByName(username);
        entity.password = cryptoService.encrypt(dto.getPassword());
        return Response.ok().entity(new ResponseDTO("Password aggiornata!", false)).build();
    }

    @PUT
    @Path("/update/{id}")
    @Transactional
    //@RolesAllowed({ADMIN})
    @PermitAll
    @APIResponse(responseCode = "404", description = "User non trovato")
    @APIResponse(responseCode = "200", description = "User aggiornato con successo")
    public Response update(Long id, UserResponseDTO user) {
        User entity = findUserById(id);

        if (StringUtils.isNotBlank(user.getName())) {
            entity.name = user.getName();
        }
        if (StringUtils.isNotBlank(user.getLastname())) {
            entity.lastname = user.getLastname();
        }
        if (user.getDataNascita() != null) {
            entity.dataNascita = user.getDataNascita();
        }
        if (user.getEmail() != null) {
            entity.email = user.getEmail();
        }
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            entity.roles = new LinkedHashSet<>();
            List<Role> collect = user.getRoles().stream().map(r -> Role.findByName(r.name)).toList();
            entity.roles.addAll(collect);
        }
        if (StringUtils.isNotBlank(user.getPassword())) {
            entity.password = cryptoService.encrypt(user.getPassword());
        }

        return Response.ok().entity(new ResponseDTO("Utente aggiornato!", false)).build();
    }

    private User findUserById(Long id) {
        User entity = User.findById(id);
        if (entity == null) {
            throw new NotFoundException();
        }
        return entity;
    }

    private User findUserByName(String username) {
        User entity = User.findByUsername(username);
        if (entity == null) {
            throw new NotFoundException();
        }
        return entity;
    }
}
