package com.roy.morago.service.notification;

import com.roy.morago.dto.notification.NotificationRequest;
import com.roy.morago.dto.notification.NotificationResponse;
import com.roy.morago.entity.notification.Notification;
import com.roy.morago.entity.user.User;
import com.roy.morago.repository.notification.NotificationRepository;
import com.roy.morago.service.user.UserHelper;
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

    public List<NotificationResponse> getUserNotifications(Long userId) {
        List<Notification> notificationList = helper.findByUserId(userId);
        List<NotificationResponse> responses = new ArrayList<>();
        for (Notification notification : notificationList) {
            responses.add(helper.createNotificationResponse(notification));
        }
        return responses;
    }

    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        List<Notification> notificationList = helper.findUnreadByUserId(userId);
        List<NotificationResponse> unreadResponses = new ArrayList<>();
        for (Notification notification : notificationList) {
            unreadResponses.add(helper.createNotificationResponse(notification));
        }
        return unreadResponses;
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
        notification.setIsRead(!notification.getIsRead());
        notification.setReadAt(LocalDateTime.now());
    }

    @Transactional
    public void toggleNotificationsRead(Long userId, List<Long> notificationIds) {
        for (Long notificationId : notificationIds) {
            toggleNotificationRead(userId, notificationId);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> allUnread = helper.findUnreadByUserId(userId);
        for (Notification unread : allUnread) {
            unread.setIsRead(true);
            unread.setReadAt(LocalDateTime.now());
        }
    }

    @Transactional
    public void deleteNotification(Long id, Long notificationId) {
        Notification notification = helper.findNotificationById(notificationId);
        helper.verifyOwnNotification(id, notification);
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteNotifications(Long id, List<Long> notificationIds) {
        for (Long notificationId : notificationIds) {
            deleteNotification(id, notificationId);
        }
    }

    @Transactional
    public void deleteUnsentNotification(Long notificationId) {
        Notification notification = helper.findNotificationById(notificationId);
        helper.verifyNotSent(notification);
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteUnsentNotifications(List<Long> notificationIds) {
        for (Long notificationId : notificationIds) {
            deleteUnsentNotification(notificationId);
        }
    }
}
