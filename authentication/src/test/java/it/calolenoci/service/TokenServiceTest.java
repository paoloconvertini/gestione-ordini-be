package it.calolenoci.service;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.auth.principal.JWTParser;
import it.calolenoci.dto.LoginDTO;
import it.calolenoci.entity.Permission;
import it.calolenoci.entity.Role;
import it.calolenoci.entity.User;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonString;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class TokenServiceTest {

    @Inject
    TokenService tokenService;

    @Inject
    JWTParser jwtParser;

    @Test
    void generaIClaimUsatiDalFrontendEDeduplicaIPermessi() throws Exception {
        User user = newUser("Mario", "Rossi");
        user.roles.add(role(1L, "Admin", permission(1L, "users.view"), permission(2L, "roles.view")));
        user.roles.add(role(2L, "User", permission(3L, "users.view")));

        long before = System.currentTimeMillis() / 1000;
        LoginDTO result = tokenService.generateToken(user);
        JsonWebToken token = jwtParser.parse(result.getIdToken());

        assertEquals("test-user", token.getName());
        assertEquals("authentication-service", token.getIssuer());
        assertEquals(Set.of("Admin", "User"), token.getGroups());
        assertEquals(Set.of("users.view", "roles.view"), stringSet(token.getClaim("permissions")));
        assertEquals("Mario Rossi", token.getClaim("full_name"));
        assertEquals(result.getExpireIn(), token.getExpirationTime());
        assertTrue(result.getExpireIn() >= before + 86399);
        assertTrue(result.getExpireIn() <= before + 86401);
        assertFalse(result.getError());
    }

    @Test
    void aggiungeIClaimSpecificiDelVenditore() throws Exception {
        User user = newUser("Luca", "Bianchi");
        user.codVenditore = "007";
        user.email = "venditore@example.test";
        user.roles.add(role(4L, "Venditore"));

        JsonWebToken token = jwtParser.parse(tokenService.generateToken(user).getIdToken());

        assertEquals("007", token.getClaim("nickname"));
        assertEquals("venditore@example.test", token.getClaim("email"));
    }

    @Test
    void nonAggiungeIClaimVenditoreAgliAltriRuoli() throws Exception {
        User user = newUser("Anna", "Verdi");
        user.roles.add(role(1L, "Admin"));

        JsonWebToken token = jwtParser.parse(tokenService.generateToken(user).getIdToken());

        assertFalse(token.getClaimNames().contains("nickname"));
        assertFalse(token.getClaimNames().contains("email"));
    }

    private User newUser(String name, String lastname) {
        User user = new User();
        user.username = "test-user";
        user.name = name;
        user.lastname = lastname;
        return user;
    }

    private Role role(Long id, String name, Permission... permissions) {
        Role role = new Role();
        role.id = id;
        role.name = name;
        role.permissions.addAll(Set.of(permissions));
        return role;
    }

    private Permission permission(Long id, String name) {
        Permission permission = new Permission(name, "Permesso di test");
        permission.id = id;
        return permission;
    }

    private Set<String> stringSet(Object claim) {
        return ((JsonArray) claim).getValuesAs(JsonString.class).stream()
                .map(JsonString::getString)
                .collect(Collectors.toSet());
    }
}
