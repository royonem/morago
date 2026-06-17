package com.roy.morago.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank
        String refreshToken
) {
}
