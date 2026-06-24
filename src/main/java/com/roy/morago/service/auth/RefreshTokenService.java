package com.roy.morago.service.auth;

import com.roy.morago.entity.auth.RefreshToken;
import com.roy.morago.entity.user.User;
import com.roy.morago.exception.auth.InvalidRefreshTokenException;
import com.roy.morago.repository.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpiration;

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        String token = UUID.randomUUID().toString();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshExpiration));
        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token created for user ID: {}", user.getId());
        return saved;
    }

    public RefreshToken findByToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token);
        if (refreshToken == null) {
            log.warn("Refresh token not found: {}", token);
            throw new InvalidRefreshTokenException("Refresh Token not found.");
        }
        return refreshToken;
    }

    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Refresh token expired for user ID: {}", refreshToken.getUser().getId());
            throw new InvalidRefreshTokenException("Refresh Token is expired.");
        }
        if (refreshToken.isRevoked()) {
            log.warn("Refresh token revoked for user ID: {}", refreshToken.getUser().getId());
            throw new InvalidRefreshTokenException("Refresh Token is revoked.");
        }
        return refreshToken;
    }

    public void validateRefreshTokenOwner(RefreshToken refreshToken, User currentUser) {
        if (!refreshToken.getUser().getId().equals(currentUser.getId())) {
            log.warn("User {} tried to use another user's refresh token (owner: {})",
                    currentUser.getId(), refreshToken.getUser().getId());
            throw new InvalidRefreshTokenException("Cannot revoke another user's token");
        }
    }

    public void revokeRefreshToken(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        log.info("Refresh token revoked for user ID: {}", refreshToken.getUser().getId());
    }
}
