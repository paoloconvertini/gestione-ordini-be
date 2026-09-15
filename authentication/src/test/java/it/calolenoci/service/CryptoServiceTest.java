package it.calolenoci.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@QuarkusTest
class CryptoServiceTest {

    @Inject
    CryptoService cryptoService;

    @Test
    void cifraEDecifraLaPassword() {
        String password = "Password di test 21!";

        String encrypted = cryptoService.encrypt(password);

        assertNotEquals(password, encrypted);
        assertEquals(password, cryptoService.decrypt(encrypted));
    }

    @Test
    void laCifraturaRimaneDeterministicaPerIlLoginEsistente() {
        assertEquals(cryptoService.encrypt("password"), cryptoService.encrypt("password"));
    }
}
