package it.calolenoci.resource;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.security.ForbiddenException;
import it.calolenoci.dto.PermissionDTO;
import it.calolenoci.dto.RolePermissionUpdateDTO;
import it.calolenoci.entity.Permission;
import it.calolenoci.entity.Role;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestSecurity(user = "test-admin", roles = "Admin")
class RolePermissionResourceTest {

    @Inject
    PermissionResource permissionResource;

    @Inject
    RoleResource roleResource;

    @Test
    @TestTransaction
    void creaUnPermessoEBloccaIlNomeDuplicato() {
        PermissionDTO dto = new PermissionDTO();
        dto.name = "test.permission." + UUID.randomUUID();
        dto.description = "Permesso di test";

        PermissionDTO created = permissionResource.create(dto);

        assertTrue(created.id > 0);
        try {
            permissionResource.create(dto);
        } catch (jakarta.ws.rs.WebApplicationException exception) {
            assertEquals(409, exception.getResponse().getStatus());
            return;
        }
        throw new AssertionError("Il permesso duplicato doveva essere rifiutato");
    }

    @Test
    @TestSecurity(user = "test-venditore", roles = "Venditore")
    void vietaLaGestionePermessiAChiNonEAdmin() {
        assertThrows(ForbiddenException.class, permissionResource::getAll);
    }

    @Test
    @TestTransaction
    void sostituisceIPermessiDelRuoloIgnorandoGliIdInesistenti() {
        Role role = Role.findByName("Admin");
        assertNotNull(role);
        Permission oldPermission = persistPermission("old");
        Permission newPermission = persistPermission("new");
        role.permissions.clear();
        role.permissions.add(oldPermission);

        RolePermissionUpdateDTO request = new RolePermissionUpdateDTO();
        request.permissionIds = Set.of(newPermission.id, Long.MAX_VALUE);

        try (Response response = roleResource.updateRolePermissions(role.id, request)) {
            assertEquals(200, response.getStatus());
        }
        assertEquals(Set.of(newPermission), role.permissions);
    }

    private Permission persistPermission(String prefix) {
        Permission permission = new Permission("test." + prefix + "." + UUID.randomUUID(), "Permesso di test");
        permission.persist();
        return permission;
    }
}
