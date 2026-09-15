package it.calolenoci.resource;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.LoginDTO;
import it.calolenoci.dto.UserRequestDTO;
import it.calolenoci.entity.User;
import it.calolenoci.service.CryptoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class LoginResourceTest {

    @Inject
    LoginResource loginResource;

    @Inject
    CryptoService cryptoService;

    @Test
    @TestTransaction
    void restituisceUnTokenPerCredenzialiValide() {
        String username = "test-" + UUID.randomUUID();
        String password = "Password test";
        persistUser(username, password);

        try (Response response = loginResource.login(new UserRequestDTO(username, password))) {
            LoginDTO result = (LoginDTO) response.getEntity();

            assertNotNull(result.getIdToken());
            assertFalse(result.getIdToken().isBlank());
            assertFalse(result.getError());
        }
    }

    @Test
    @TestTransaction
    void rifiutaCredenzialiNonValide() {
        assertThrows(NotFoundException.class,
                () -> loginResource.login(new UserRequestDTO("inesistente-" + UUID.randomUUID(), "password")));
    }

    private void persistUser(String username, String password) {
        User user = new User();
        user.username = username;
        user.name = "Nome";
        user.lastname = "Test";
        user.password = cryptoService.encrypt(password);
        user.persist();
    }
}
