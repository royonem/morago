package com.roy.morago.security;

import com.roy.morago.entity.user.Role;
import com.roy.morago.entity.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtProvider {
    private final SecretKey jwtSigningKey;

    @Value("${app.jwt.access-expiration-ms}")
    private long jwtExpiration;

    public JwtProvider(SecretKey jwtSigningKey) {
        this.jwtSigningKey = jwtSigningKey;
    }

    public String generateToken(User user) {
        String email = user.getEmail();
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        return Jwts.builder()
                .subject(email)
                .claim("roleNames", roleNames)
                .issuer("morago-backend")
                .audience().add("morago-app")
                .and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(jwtSigningKey)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtSigningKey)
                .requireIssuer("morago-backend")
                .requireAudience("morago-app")
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
