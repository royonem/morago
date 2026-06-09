package com.roy.morago.dto.notification;

public record NotificationRequest(
        String title,
        String content,
        String actionUrl
) {}
