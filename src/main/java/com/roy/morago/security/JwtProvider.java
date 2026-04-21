package com.roy.morago.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;

@Component
public class JwtProvider {
    private final SecretKey jwtSigningKey;

    public JwtProvider(SecretKey jwtSigningKey) {
        this.jwtSigningKey = jwtSigningKey;
    }

    public String generateToken(String username, Set<String> roleNames) {
        return Jwts.builder()
                .subject(username)
                .claim("roleNames", roleNames)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(jwtSigningKey)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtSigningKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
