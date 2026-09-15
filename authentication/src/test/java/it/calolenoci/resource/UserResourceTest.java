package it.calolenoci.resource;

import io.quarkus.security.ForbiddenException;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import it.calolenoci.dto.SimpleRoleDTO;
import it.calolenoci.dto.UserResponseDTO;
import it.calolenoci.entity.Role;
import it.calolenoci.entity.User;
import it.calolenoci.service.CryptoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@TestSecurity(user = "test-admin", roles = "Admin")
@JwtSecurity(claims = @Claim(key = "nickname", value = "001"))
class UserResourceTest {

    @Inject
    UserResource userResource;

    @Inject
    CryptoService cryptoService;

    @Test
    @TestTransaction
    void creaUtenteConPasswordCifrataERuoloAssociato() {
        Role venditore = existingRole("Venditore");
        String username = newUsername();
        UserResponseDTO request = newUserRequest(username, "Password test", venditore);

        try (Response response = userResource.saveUser(request)) {
            assertEquals(201, response.getStatus());
        }

        User saved = User.findByUsername(username);
        assertNotNull(saved);
        assertNotEquals(request.getPassword(), saved.password);
        assertEquals(request.getPassword(), cryptoService.decrypt(saved.password));
        assertEquals(List.of("Venditore"), saved.roles.stream().map(role -> role.name).toList());
    }

    @Test
    @TestTransaction
    void validaUsernameObbligatorioEUnivoco() {
        UserResponseDTO blankUsername = newUserRequest(" ", "Password test", null);
        try (Response response = userResource.saveUser(blankUsername)) {
            assertEquals(400, response.getStatus());
        }

        String username = newUsername();
        persistUser(username, "Password originale");
        UserResponseDTO duplicate = newUserRequest(username, "Password diversa", null);
        try (Response response = userResource.saveUser(duplicate)) {
            assertEquals(409, response.getStatus());
        }
    }

    @Test
    @TestTransaction
    void aggiornaDatiERuoliSenzaSostituireUnaPasswordVuota() {
        User user = persistUser(newUsername(), "Password originale");
        user.roles.add(existingRole("User"));
        String encryptedPassword = user.password;

        UserResponseDTO request = newUserRequest(user.username, " ", existingRole("Magazziniere"));
        request.setName("Nome aggiornato");
        request.setLastname("Cognome aggiornato");

        try (Response response = userResource.update(user.id, request)) {
            assertEquals(200, response.getStatus());
        }

        assertEquals("Nome aggiornato", user.name);
        assertEquals("Cognome aggiornato", user.lastname);
        assertEquals(encryptedPassword, user.password);
        assertEquals(List.of("Magazziniere"), user.roles.stream().map(role -> role.name).toList());
    }

    @Test
    @TestSecurity(user = "test-venditore", roles = "Venditore")
    @JwtSecurity(claims = @Claim(key = "nickname", value = "001"))
    void vietaLaCreazioneUtentiAChiNonEAdmin() {
        assertThrows(ForbiddenException.class,
                () -> userResource.saveUser(newUserRequest(newUsername(), "Password test", null)));
    }

    private UserResponseDTO newUserRequest(String username, String password, Role role) {
        UserResponseDTO request = new UserResponseDTO();
        request.setUsername(username);
        request.setName("Nome test");
        request.setLastname("Cognome test");
        request.setPassword(password);
        if (role != null) {
            request.setRoles(List.of(new SimpleRoleDTO(role.id, role.name)));
        }
        return request;
    }

    private User persistUser(String username, String password) {
        User user = new User();
        user.username = username;
        user.name = "Nome test";
        user.lastname = "Cognome test";
        user.password = cryptoService.encrypt(password);
        user.persist();
        return user;
    }

    private Role existingRole(String name) {
        Role role = Role.findByName(name);
        assertNotNull(role);
        return role;
    }

    private String newUsername() {
        return "test-" + UUID.randomUUID();
    }
}
