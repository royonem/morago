package com.roy.morago.dto.notification;

import jakarta.validation.constraints.NotBlank;

public record NotificationRequest(
        @NotBlank(message = "Title is required")
        String title,
        @NotBlank(message = "Content is required")
        String content,
        String actionUrl
) {}
