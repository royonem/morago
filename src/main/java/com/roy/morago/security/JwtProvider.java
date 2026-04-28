package com.roy.morago.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;

@Component
public class JwtProvider {
    private final SecretKey jwtSigningKey;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpiration;

    public JwtProvider(SecretKey jwtSigningKey) {
        this.jwtSigningKey = jwtSigningKey;
    }

    public String generateToken(String username, Set<String> roleNames) {
        return Jwts.builder()
                .subject(username)
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
