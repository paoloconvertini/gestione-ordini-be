package it.calolenoci.resource;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
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

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static it.calolenoci.constant.Ruolo.*;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

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
    @RolesAllowed({ADMIN})
    @APIResponse(responseCode = "201", description = "User salvato con successo")
    public Response saveUser(UserResponseDTO user) {

        if (StringUtils.isBlank(user.getUsername())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO("INVALID", "VALIDATION", "Username obbligatorio"))
                    .build();
        }

        User existing = User.findByUsername(user.getUsername());
        if (existing != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponseDTO("CONFLICT", "VALIDATION", "Username già presente"))
                    .build();
        }

        User entity = new User();
        entity.username = user.getUsername();
        entity.name = user.getName();
        entity.lastname = user.getLastname();
        entity.password = cryptoService.encrypt(user.getPassword());
        entity.dataNascita = user.getDataNascita();
        entity.email = user.getEmail();
        entity.codVenditore = user.getCodVenditore();
        if (user.getRoles() != null) {
            entity.roles.clear();
            for (SimpleRoleDTO r : user.getRoles()) {
                Role role = Role.findById(r.id);
                if (role != null) {
                    entity.roles.add(role);
                }
            }
        }
        entity.persist();
        return Response.status(Response.Status.CREATED).entity(new ResponseDTO("utente salvato con successo", false)).build();
    }

    @GET
    @Path("/{idUser}")
    @RolesAllowed({ADMIN})
    public Response getUser(Long idUser) {

        User entity = findUserById(idUser);

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(entity.id);
        dto.setUsername(entity.username);
        dto.setName(entity.name);
        dto.setLastname(entity.lastname);
        dto.setDataNascita(entity.dataNascita);
        dto.setEmail(entity.email);
        dto.setCodVenditore(entity.codVenditore);

        dto.setRoles(
                entity.roles.stream()
                        .map(r -> new SimpleRoleDTO(r.id, r.name))
                        .toList()
        );

        return Response.ok(dto).build();
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
    @RolesAllowed({ADMIN, LOGISTICA, AMMINISTRATIVO, VENDITORE, MAGAZZINIERE, RECEPTION_CEGLIE, RECEPTION_OSTUNI, RECEPTION_CEGLIE})
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
    @RolesAllowed({ADMIN, LOGISTICA, AMMINISTRATIVO, VENDITORE, MAGAZZINIERE, RECEPTION_CEGLIE, RECEPTION_OSTUNI})
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
    @RolesAllowed({ADMIN})
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
    @RolesAllowed({ADMIN})
    @APIResponse(responseCode = "404", description = "User non trovato")
    @APIResponse(responseCode = "200", description = "User aggiornato con successo")
    public Response update(@PathParam("id") Long id, UserResponseDTO user) {

        User entity = User.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponseDTO("NOT_FOUND", "USER", "Utente non trovato"))
                    .build();
        }

        // Username obbligatorio
        if (StringUtils.isBlank(user.getUsername())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO("INVALID", "VALIDATION", "Username obbligatorio"))
                    .build();
        }

        // Controllo unicità username
        User existing = User.findByUsername(user.getUsername());
        if (existing != null && !existing.id.equals(id)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponseDTO("CONFLICT", "VALIDATION", "Username già usato da un altro utente"))
                    .build();
        }

        // Aggiorno campi base
        entity.username = user.getUsername();
        entity.name = user.getName();
        entity.lastname = user.getLastname();
        entity.dataNascita = user.getDataNascita();
        entity.email = user.getEmail();
        entity.codVenditore = user.getCodVenditore();

        // Aggiorno ruoli — VERSIONE CORRETTA
        entity.roles.clear();
        if (user.getRoles() != null) {
            for (SimpleRoleDTO r : user.getRoles()) {
                Role role = Role.findById(r.id);
                if (role != null) {
                    entity.roles.add(role);
                }
            }
        }

        // Aggiorno password solo se presente
        if (StringUtils.isNotBlank(user.getPassword())) {
            entity.password = cryptoService.encrypt(user.getPassword());
        }

        return Response.ok(new ResponseDTO("Utente aggiornato!", false)).build();
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
