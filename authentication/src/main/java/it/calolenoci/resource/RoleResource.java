package it.calolenoci.resource;

import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.PermissionDTO;
import it.calolenoci.dto.ResponseDTO;
import it.calolenoci.dto.RolePermissionUpdateDTO;
import it.calolenoci.entity.Permission;
import it.calolenoci.entity.Role;
import it.calolenoci.dto.RoleDto;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

import static it.calolenoci.constant.Ruolo.ADMIN;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("api/roles")
@RolesAllowed(ADMIN)
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class RoleResource {


    @POST
    @Transactional
    @APIResponse(responseCode = "200", description = "Role salvato con successo")
    public Response saveRole(RoleDto ruolo) {
        Role entity = new Role();
        entity.name = ruolo.getName();
        entity.persist();
        return Response.status(Response.Status.CREATED).entity(new ResponseDTO("Ruolo salvato", false)).build();
    }

    @GET
    @Path("/{idRole}")
    public Response getRole(Long idRole){
        Role role = findById(idRole);
        return Response.ok(role).build();
    }

    @GET
    @APIResponse(responseCode = "200")
    public Response getAllRoles() {

        List<Role> entities = Role.listAll(Sort.ascending("name"));

        List<RoleDto> dtos = entities.stream().map(role -> {

            RoleDto dto = new RoleDto();
            dto.setId(role.id);
            dto.setName(role.name);

            List<PermissionDTO> perms = role.permissions.stream().map(p -> {
                PermissionDTO pd = new PermissionDTO();
                pd.id = p.id;
                pd.name = p.name;
                pd.description = p.description;
                return pd;
            }).collect(Collectors.toList());

            dto.setPermissions(perms);

            return dto;

        }).collect(Collectors.toList());

        return Response.ok(dtos).build();
    }



    @DELETE
    @Transactional
    @Path("/{idRole}")
    public Response delete(Long idRole) {
        this.findById(idRole).delete();
        return Response.ok().entity(new ResponseDTO("Ruolo eliminato", false)).build();
    }

    @PUT
    @Path("/{id}")
    @APIResponse(responseCode = "404", description = "Role non trovato")
    @APIResponse(responseCode = "200", description = "Role aggiornato con successo")
    @Transactional
    public Response update(Long id, RoleDto ruolo){
        Role role = findById(id);
        role.name = ruolo.getName();
        return Response.status(Response.Status.CREATED).entity(new ResponseDTO("Ruolo aggiornato", false)).build();
    }

    @GET
    @Path("/{id}/permissions")
    public Response getRolePermissions(@PathParam("id") Long id) {

        Role role = findById(id);

        List<PermissionDTO> list = role.permissions.stream()
                .map(p -> {
                    PermissionDTO dto = new PermissionDTO();
                    dto.id = p.id;
                    dto.name = p.name;
                    dto.description = p.description;
                    return dto;
                })
                .collect(Collectors.toList());

        return Response.ok(list).build();
    }

    @PUT
    @Path("/{id}/permissions")
    @Transactional
    public Response updateRolePermissions(@PathParam("id") Long id, RolePermissionUpdateDTO req) {

        Role role = findById(id);

        // svuota lista
        role.permissions.clear();

        // aggiunge i permessi inviati dal client
        if (req.permissionIds != null) {
            req.permissionIds.forEach(pid -> {
                Permission p = Permission.findById(pid);
                if (p != null) {
                    role.permissions.add(p);
                }
            });
        }

        return Response.ok(new ResponseDTO("Permessi aggiornati", false)).build();
    }

    @POST
    @Path("/{id}/permissions/{permId}")
    @Transactional
    public Response addPermission(
            @PathParam("id") Long id,
            @PathParam("permId") Long permId) {

        Role role = findById(id);
        Permission p = Permission.findById(permId);

        if (p == null) {
            throw new NotFoundException("Permission non trovato");
        }

        role.permissions.add(p);

        return Response.ok(new ResponseDTO("Permesso aggiunto", false)).build();
    }

    @DELETE
    @Path("/{id}/permissions/{permId}")
    @Transactional
    public Response removePermission(
            @PathParam("id") Long id,
            @PathParam("permId") Long permId) {

        Role role = findById(id);
        Permission p = Permission.findById(permId);

        if (p == null) {
            throw new NotFoundException("Permission non trovato");
        }

        role.permissions.remove(p);

        return Response.ok(new ResponseDTO("Permesso rimosso", false)).build();
    }

    private Role findById(Long id) {
        Role entity = Role.findById(id);
        if(entity == null) {
            throw new NotFoundException();
        }
        return entity;
    }
}