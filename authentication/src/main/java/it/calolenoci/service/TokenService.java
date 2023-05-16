package it.calolenoci.service;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import it.calolenoci.dto.LoginDTO;
import it.calolenoci.entity.User;
import org.eclipse.microprofile.jwt.Claims;

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
        long l = (System.currentTimeMillis() / 1000) + 3600;
        JwtClaimsBuilder builder = Jwt.upn(user.username);
        if(roles.contains("Venditore")){
            builder.claim(Claims.nickname, user.codVenditore);
            builder.claim(Claims.email, user.email);
        }
        dto.setIdToken(builder.claim(Claims.full_name, user.getFullName())
                .subject("gp-api-service").expiresAt(l).groups(roles).sign());
        dto.setExpireIn(l);
        dto.setError(Boolean.FALSE);
        return dto;
    }
}
