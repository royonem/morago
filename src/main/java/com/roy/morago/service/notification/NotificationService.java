package com.roy.morago.service.notification;

import com.roy.morago.dto.notification.NotificationRequest;
import com.roy.morago.dto.notification.NotificationResponse;
import com.roy.morago.entity.notification.Notification;
import com.roy.morago.entity.user.User;
import com.roy.morago.repository.notification.NotificationRepository;
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
    private final NotificationRepository notificationRepository;
    private final NotificationHelper helper;
    private final UserHelper userHelper;

    @Transactional
    public NotificationResponse createNotification(NotificationRequest dto) {
        Notification notification = new Notification();
        notification.setTitle(dto.title());
        notification.setContent(dto.content());
        notification.setActionUrl(dto.actionUrl());
        notificationRepository.save(notification);
        return helper.createNotificationResponse(notification);
    }

    @Transactional
    public void sendNotification(Long userId, Long notificationId) {
        User user = userHelper.findUserById(userId);
        Notification notification = helper.findNotificationById(notificationId);
        helper.verifyNotSent(notification);
        notification.setUser(user);
        notification.setSentAt(LocalDateTime.now());
        user.getNotifications().add(notification);
    }

    public NotificationResponse getNotification(Long notificationId) {
        Notification notification = helper.findNotificationById(notificationId);
        return helper.createNotificationResponse(notification);
    }

    public Page<NotificationResponse> getNotificationsByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable).map(helper::createNotificationResponse);
    }

    public Page<NotificationResponse> getUnreadNotificationsByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId, pageable).map(helper::createNotificationResponse);
    }

    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public NotificationResponse readNotification(Long userId, Long notificationId) {
        Notification notification = helper.findNotificationById(notificationId);
        helper.verifyOwnNotification(userId, notification);
        notification.setReadAt(LocalDateTime.now());
        notification.setIsRead(true);
        return helper.createNotificationResponse(notification);
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
        notificationRepository.toggleReadByIdInAndUserId(notificationIds, userId);
    }

    @Transactional
    public void markAsRead(Long userId, List<Long> notificationIds) {
        notificationRepository.markAsReadByIdInAndUserId(notificationIds, userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAsReadByUserId(userId);
    }

    @Transactional
    public void deleteNotificationByUserId(Long id, Long notificationId) {
        Notification notification = helper.findNotificationById(notificationId);
        helper.verifyOwnNotification(id, notification);
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteNotificationsByUserId(Long userId, List<Long> notificationIds) {
        notificationRepository.deleteByIdInAndUserId(notificationIds, userId);
    }

    @Transactional
    public void deleteAllReadNotificationsByUserId(Long userId) {
        notificationRepository.deleteByUserIdAndIsReadTrue(userId);
    }

    @Transactional
    public void deleteUnsentNotification(Long notificationId) {
        Notification notification = helper.findNotificationById(notificationId);
        helper.verifyNotSent(notification);
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteUnsentNotifications(List<Long> notificationIds) {
        notificationRepository.deleteByIdInAndSentAtIsNull(notificationIds);
    }
}
