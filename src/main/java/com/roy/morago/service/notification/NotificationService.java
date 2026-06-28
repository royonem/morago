package com.roy.morago.service.notification;

import com.roy.morago.dto.notification.NotificationRequest;
import com.roy.morago.dto.notification.NotificationResponse;
import com.roy.morago.entity.notification.Notification;
import com.roy.morago.mapper.NotificationMapper;
import com.roy.morago.repository.notification.NotificationRepository;
import com.roy.morago.service.user.UserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {
    private final NotificationRepository repository;
    private final NotificationHelper helper;
    private final NotificationMapper mapper;
    private final UserHelper userHelper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("Creating notification: userId={}", request.userId());
        Notification notification = mapper.toEntity(request);
        notification.setUser(userHelper.findUserById(request.userId()));
        notification.setSentAt(LocalDateTime.now());
        repository.save(notification);
        log.info("Notification created: notificationId={}, userId={}", notification.getId(), request.userId());

        NotificationResponse response = mapper.toResponse(notification);
        eventPublisher.publishEvent(response);
        return response;
    }

    public NotificationResponse getNotification(Long notificationId) {
        Notification notification = helper.findNotificationById(notificationId);
        return mapper.toResponse(notification);
    }

    public Page<NotificationResponse> getNotificationsByUserId(Long userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable).map(mapper::toResponse);
    }

    public Page<NotificationResponse> getUnreadNotificationsByUserId(Long userId, Pageable pageable) {
        return repository.findByUserIdAndIsReadFalse(userId, pageable).map(mapper::toResponse);
    }

    public Long getUnreadCount(Long userId) {
        return repository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public NotificationResponse readNotification(Long userId, Long notificationId) {
        log.info("Reading notification: notificationId={}, userId={}", notificationId, userId);
        Notification notification = helper.findNotificationById(notificationId);
        helper.verifyOwnNotification(userId, notification);
        notification.setReadAt(LocalDateTime.now());
        notification.setIsRead(true);
        log.info("Notification was read: notificationId={}, userId={}", notificationId, userId);
        return mapper.toResponse(notification);
    }

    @Transactional
    public void toggleNotificationRead(Long userId, Long notificationId) {
        Notification notification = helper.findNotificationById(notificationId);
        log.info("Toggling notification read status: userId={}, notificationId={}, isRead={}", userId, notificationId, notification.getIsRead());
        helper.verifyOwnNotification(userId, notification);
        if (!notification.getIsRead()) {
            notification.setReadAt(LocalDateTime.now());
        } else {
            notification.setReadAt(null);
        }
        notification.setIsRead(!notification.getIsRead());
        log.info("Notification read status toggled: userId={}, notificationId={}, isRead={}", userId, notificationId, notification.getIsRead());
    }

    @Transactional
    public void toggleNotificationsRead(Long userId, List<Long> notificationIds) {
        log.info("Toggling notifications read status: userId={}, listSize={}", userId, notificationIds.size());
        repository.toggleReadByIdInAndUserId(notificationIds, userId);
        log.info("Notifications read status was toggled: userId={}, toggled={}", userId, notificationIds.size());
    }

    @Transactional
    public void markAsRead(Long userId, List<Long> notificationIds) {
        log.info("Marking notifications as read: userId={}, listSize={}", userId, notificationIds.size());
        repository.markAsReadByIdInAndUserId(notificationIds, userId);
        log.info("Notifications marked as read: userId={}, markedAsRead={}", userId, notificationIds.size());
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read: userId={}", userId);
        repository.markAsReadByUserId(userId);
        log.info("All notifications marked as read: userId={}", userId);
    }

    @Transactional
    public void deleteNotificationByUserId(Long id, Long notificationId) {
        log.info("Deleting notification: notificationId={}, userId={}", notificationId, id);
        Notification notification = helper.findNotificationById(notificationId);
        helper.verifyOwnNotification(id, notification);
        repository.delete(notification);
        log.info("Notification deleted: notificationId={}, userId={}", notificationId, id);
    }

    @Transactional
    public void deleteNotificationsByUserId(Long userId, List<Long> notificationIds) {
        log.info("Deleting notifications: userId={}, listSize={}", userId, notificationIds.size());
        repository.deleteByIdInAndUserId(notificationIds, userId);
        log.info("Notifications deleted: userId={}, deleted={}", userId, notificationIds.size());
    }

    @Transactional
    public void deleteAllReadNotificationsByUserId(Long userId) {
        log.info("Deleting all read notifications: userId={}", userId);
        repository.deleteByUserIdAndIsReadTrue(userId);
        log.info("All read notifications deleted: userId={}", userId);
    }
}
