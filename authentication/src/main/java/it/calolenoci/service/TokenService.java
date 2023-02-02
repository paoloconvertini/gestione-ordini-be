package it.calolenoci.service;

import io.smallrye.jwt.build.Jwt;
import it.calolenoci.dto.LoginDTO;
import it.calolenoci.entity.User;

import javax.inject.Singleton;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class TokenService {

    public LoginDTO generateToken(User user) {
        LoginDTO dto = new LoginDTO();
        Set<String> roles = user.roles.stream()
                .map(role -> role.name)
                .collect(Collectors.toSet());
        long l = System.currentTimeMillis() + 3600;
        dto.setIdToken(Jwt.subject("gp-api-service").expiresAt(l).groups(roles).sign());
        dto.setExpireIn(l);
        dto.setError(Boolean.FALSE);
        return dto;
    }
}
