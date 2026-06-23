package com.roy.morago.service.notification;

import com.roy.morago.dto.notification.NotificationResponse;
import com.roy.morago.entity.notification.Notification;
import com.roy.morago.exception.notification.AlreadySentNotificationException;
import com.roy.morago.exception.notification.NotificationNotFoundException;
import com.roy.morago.exception.notification.UnauthorizedNotificationException;
import com.roy.morago.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationHelper {
    private final NotificationRepository notificationRepository;

    protected Notification findNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));
    }

    protected NotificationResponse createNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getActionUrl(),
                notification.getIsRead(),
                notification.getSentAt()
        );
    }

    protected void verifyOwnNotification(Long userId, Notification notification) {
        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedNotificationException("Cannot delete another user's notification");
        }
    }

    protected void verifyNotSent(Notification notification) {
        if (notification.getSentAt() != null) {
            throw new AlreadySentNotificationException("Notification already sent");
        }
    }
}
