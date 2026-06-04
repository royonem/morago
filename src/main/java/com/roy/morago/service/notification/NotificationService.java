package com.roy.morago.service.notification;

import com.roy.morago.dto.notification.NotificationDTO;
import com.roy.morago.dto.notification.NotificationResponse;
import com.roy.morago.entity.notification.Notification;
import com.roy.morago.exception.notification.NotificationNotFoundException;
import com.roy.morago.repository.notification.NotificationRepository;
import com.roy.morago.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserService userService;

    @Transactional
    public void createNotification(NotificationDTO dto) {
        Notification notification = new Notification();
        notification.setTitle(dto.getTitle());
        notification.setContent(dto.getContent());
        notification.setActionUrl(dto.getActionUrl());
        notification.setScheduledFor(dto.getScheduledFor());
        notificationRepository.save(notification);
    }

    @Transactional
    public void sendNotification(Long notificationId, Long userId) {
        Notification notification = findNotificationById(notificationId);
        notification.setUser(userService.findUserById(userId));
        notification.setSentAt(LocalDateTime.now());
    }

    @Transactional
    public void scheduleNotification(Long notificationId, LocalDateTime scheduledDate) {
        Notification notification = findNotificationById(notificationId);
        notification.setScheduledFor(scheduledDate);
    }

    @Transactional
    public void cancelScheduledNotification(Long notificationId) {
        Notification notification = findNotificationById(notificationId);
        notification.setScheduledFor(null);
    }

    public NotificationResponse getNotification(Long notificationId) {
        Notification notification = findNotificationById(notificationId);
        return createNotificationResponse(notification);
    }

    public List<NotificationResponse> getUserNotifications(Long userId) {
        List<Notification> notificationList = findByUserId(userId);
        List<NotificationResponse> responses = new ArrayList<>();
        for (Notification notification : notificationList) {
            responses.add(createNotificationResponse(notification));
        }
        return responses;
    }

    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        List<Notification> notificationList = findUnreadByUserId(userId);
        List<NotificationResponse> unreadResponses = new ArrayList<>();
        for (Notification notification : notificationList) {
            unreadResponses.add(createNotificationResponse(notification));
        }
        return unreadResponses;
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = findNotificationById(notificationId);
        notification.setIsRead(true);
    }

    @Transactional
    public void markAsUnread(Long notificationId) {
        Notification notification = findNotificationById(notificationId);
        notification.setIsRead(false);
    }

    @Transactional
    public void markAllAsRead(List<Long> notificationIds) {
        for (Long notificationId : notificationIds) {
            Notification notification = findNotificationById(notificationId);
            notification.setIsRead(true);
        }
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        Notification notification = findNotificationById(notificationId);
        notificationRepository.delete(notification);
    }

    // Helper Methods
    private Notification findNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));
    }

    private List<Notification> findByUserId(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    private List<Notification> findUnreadByUserId(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    private NotificationResponse createNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getTitle(),
                notification.getContent(),
                notification.getActionUrl(),
                notification.getIsRead(),
                notification.getSentAt()
        );
    }
}
