package com.roy.morago.dto.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull
        Long userId,
        @NotBlank(message = "Title is required")
        String title,
        @NotBlank(message = "Content is required")
        String content,
        String actionUrl
) {}
