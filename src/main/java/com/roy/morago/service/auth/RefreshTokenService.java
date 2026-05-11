package com.roy.morago.service.auth;

import com.roy.morago.entity.auth.RefreshToken;
import com.roy.morago.entity.user.User;
import com.roy.morago.exception.InvalidRefreshTokenException;
import com.roy.morago.repository.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpiration;

    public String createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        String token = UUID.randomUUID().toString();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshExpiration));
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    public RefreshToken findByToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token);
        if (refreshToken == null) {
            throw new InvalidRefreshTokenException("Refresh Token not found.");
        }
        return refreshToken;
    }

    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("Refresh Token is expired.");
        }
        if (refreshToken.isRevoked()) {
            throw new InvalidRefreshTokenException("Refresh Token is revoked.");
        }
        return refreshToken;
    }

    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
}
