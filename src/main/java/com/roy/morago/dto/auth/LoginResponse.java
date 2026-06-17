package com.roy.morago.dto.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
