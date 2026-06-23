package com.roy.morago.service.notification;

import com.roy.morago.constants.SocketEvents;
import com.roy.morago.dto.notification.NotificationRequest;
import com.roy.morago.dto.notification.NotificationResponse;
import com.roy.morago.entity.notification.Notification;
import com.roy.morago.mapper.NotificationMapper;
import com.roy.morago.repository.notification.NotificationRepository;
import com.roy.morago.service.SocketService;
import com.roy.morago.service.user.UserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class NotificationService {
    private final NotificationRepository repository;
    private final NotificationHelper helper;
    private final NotificationMapper mapper;
    private final UserHelper userHelper;
    private final SocketService socketService;


    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        Notification notification = mapper.toEntity(request);
        notification.setUser(userHelper.findUserById(request.userId()));
        repository.save(notification);

        socketService.sendToUser(
                request.userId(),
                SocketEvents.NOTIFICATION,
                mapper.toResponse(notification)
        );

        return mapper.toResponse(notification);
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
        Notification notification = helper.findNotificationById(notificationId);
        helper.verifyOwnNotification(userId, notification);
        notification.setReadAt(LocalDateTime.now());
        notification.setIsRead(true);
        return mapper.toResponse(notification);
    }

    @Transactional
    public void toggleNotificationRead(Long userId, Long notificationId) {
        Notification notification = helper.findNotificationById(notificationId);
        helper.verifyOwnNotification(userId, notification);
        if (!notification.getIsRead()) {
            notification.setReadAt(LocalDateTime.now());
        } else {
            notification.setReadAt(null);
        }
        notification.setIsRead(!notification.getIsRead());
    }

    @Transactional
    public void toggleNotificationsRead(Long userId, List<Long> notificationIds) {
        repository.toggleReadByIdInAndUserId(notificationIds, userId);
    }

    @Transactional
    public void markAsRead(Long userId, List<Long> notificationIds) {
        repository.markAsReadByIdInAndUserId(notificationIds, userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        repository.markAsReadByUserId(userId);
    }

    @Transactional
    public void deleteNotificationByUserId(Long id, Long notificationId) {
        Notification notification = helper.findNotificationById(notificationId);
        helper.verifyOwnNotification(id, notification);
        repository.delete(notification);
    }

    @Transactional
    public void deleteNotificationsByUserId(Long userId, List<Long> notificationIds) {
        repository.deleteByIdInAndUserId(notificationIds, userId);
    }

    @Transactional
    public void deleteAllReadNotificationsByUserId(Long userId) {
        repository.deleteByUserIdAndIsReadTrue(userId);
    }
}
