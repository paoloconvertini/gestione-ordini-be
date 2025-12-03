package it.calolenoci.resource;

import it.calolenoci.dto.PermissionDTO;
import it.calolenoci.dto.ResponseDTO;
import it.calolenoci.dto.RoleDto;
import it.calolenoci.entity.Permission;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("api/permissions")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PermissionResource {

    @GET
    @Path("/with-roles")
    @RolesAllowed("Admin")
    public List<PermissionDTO> getPermissionsWithRoles() {

        return Permission.listAll().stream().map(p0 -> {
            Permission p = (Permission) p0;
            PermissionDTO dto = new PermissionDTO();
            dto.id = p.id;
            dto.name = p.name;
            dto.description = p.description;

            // aggiungo i ruoli associati
            dto.roles = p.roles.stream().map(r -> {
                RoleDto rr = new RoleDto();
                rr.setId(r.id);
                rr.setName(r.name);
                return rr;
            }).collect(Collectors.toList());

            return dto;
        }).collect(Collectors.toList());
    }

    @GET
    @RolesAllowed("Admin")
    public List<PermissionDTO> getAll() {
        return Permission.listAll().stream().map(p0 -> {
            Permission p = (Permission) p0;
            PermissionDTO dto = new PermissionDTO();
            dto.id = p.id;
            dto.name = p.name;
            dto.description = p.description;
            return dto;
        }).collect(Collectors.toList());
    }


    @POST
    @Transactional
    @RolesAllowed("Admin")
    public PermissionDTO create(PermissionDTO dto) {

        Permission existing = Permission.findByName(dto.name);
        if (existing != null) {
            throw new WebApplicationException("Permission already exists", 409);
        }

        Permission p = new Permission(dto.name, dto.description);
        p.persist();

        dto.id = p.id;
        return dto;
    }

    @GET
    @Path("/{id}/roles")
    @RolesAllowed("Admin")
    public List<RoleDto> getRolesForPermission(@PathParam("id") Long id) {

        Permission p = Permission.findById(id);
        if (p == null) {
            throw new NotFoundException("Permission not found");
        }

        return p.roles.stream().map(r -> {
            RoleDto dto = new RoleDto();
            dto.setId(r.id);
            dto.setName(r.name);
            return dto;
        }).collect(Collectors.toList());
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed("Admin")
    public PermissionDTO update(@PathParam("id") Long id, PermissionDTO dto) {

        Permission p = Permission.findById(id);

        if (p == null) {
            throw new NotFoundException("Permission not found");
        }

        // controlla duplicati nome
        if (!p.name.equals(dto.name)) {
            Permission existing = Permission.findByName(dto.name);
            if (existing != null) {
                throw new WebApplicationException("Permission name already exists", 409);
            }
        }

        p.name = dto.name;
        p.description = dto.description;

        // risposta
        PermissionDTO result = new PermissionDTO();
        result.id = p.id;
        result.name = p.name;
        result.description = p.description;

        // aggiungo ruoli in risposta
        result.roles = p.roles.stream().map(r -> {
            RoleDto rDto = new RoleDto();
            rDto.setId(r.id);
            rDto.setName(r.name);
            return rDto;
        }).collect(Collectors.toList());

        return result;
    }
    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed("Admin")
    public Response delete(@PathParam("id") Long id) {

        Permission p = Permission.findById(id);
        if (p == null) {
            throw new NotFoundException("Permission non found");
        }

        // Panache ManyToMany:
        // p.roles.clear(); <-- NON SERVE ma è safe
        p.roles.clear();

        p.delete();

        return Response.ok(
                new ResponseDTO("Permesso eliminato", false)
        ).build();
    }


}
