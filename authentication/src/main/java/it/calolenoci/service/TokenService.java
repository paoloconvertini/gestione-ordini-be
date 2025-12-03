package it.calolenoci.service;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import it.calolenoci.dto.LoginDTO;
import it.calolenoci.entity.Permission;
import it.calolenoci.entity.Role;
import it.calolenoci.entity.User;
import org.eclipse.microprofile.jwt.Claims;

import javax.inject.Singleton;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class TokenService {

    public LoginDTO generateToken(User user) {

        LoginDTO dto = new LoginDTO();

        // -----------------------------
        // 1. RACCOLTA RUOLI
        // -----------------------------
        Set<String> roles = user.roles
                .stream()
                .map(role -> role.name)
                .collect(Collectors.toSet());

        // -----------------------------
        // 2. RACCOLTA PERMESSI
        // -----------------------------
        Set<String> permissions = user.roles
                .stream()
                .flatMap((Role role) -> role.permissions.stream())
                .map((Permission p) -> p.name)
                .collect(Collectors.toSet());

        // -----------------------------
        // 3. COSTRUZIONE TOKEN
        // -----------------------------
        long expiresAt = (System.currentTimeMillis() + 86400000) / 1000; // 24 ore

        JwtClaimsBuilder builder = Jwt.upn(user.username);

        // Claim extra per Venditore (manteniamo la tua logica)
        if (roles.contains("Venditore")) {
            builder.claim(Claims.nickname, user.codVenditore);
            builder.claim(Claims.email, user.email);
        }

        builder
                .claim(Claims.full_name, user.getFullName())
                .subject("gp-auth-service")
                .expiresAt(expiresAt)
                .groups(roles)
                .claim("permissions", permissions);

        // -----------------------------
        // 4. SIGN + RESPONSE
        // -----------------------------
        dto.setIdToken(builder.sign());
        dto.setExpireIn(expiresAt);
        dto.setError(Boolean.FALSE);

        return dto;
    }
}
