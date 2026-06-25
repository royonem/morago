package com.roy.morago.dto.notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long userId,
        String title,
        String content,
        String actionUrl,
        Boolean isRead,
        LocalDateTime sentAt
) {}
