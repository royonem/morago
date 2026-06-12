package com.roy.morago.controller;

import com.roy.morago.dto.file.FileDTO;
import com.roy.morago.dto.notification.NotificationResponse;
import com.roy.morago.service.file.FileService;
import com.roy.morago.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final FileService fileService;
    private final NotificationService notificationService;

    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    @PostMapping("/{id}/profile-picture")
    @ResponseStatus(HttpStatus.CREATED)
    public FileDTO uploadProfilePicture(@PathVariable Long id, @RequestParam MultipartFile picture) {
        return fileService.uploadProfilePicture(picture);
    }

    @GetMapping("/profile-picture/{id}")
    public FileDTO viewIcon(@PathVariable Long id) {
        return fileService.viewFile(id);
    }

    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    @PutMapping("/{id}/profile-picture")
    public void saveProfilePicture(@PathVariable Long id, @RequestParam Long pictureId) {
        fileService.saveProfilePicture(id, pictureId);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id, authentication)")
    @DeleteMapping("/{id}/profile-picture")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfilePicture(@PathVariable Long id) {
        fileService.deleteProfilePicture(id);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id, authentication)")
    @GetMapping("/{id}/notifications/unread-count")
    public long getUnreadCount(@PathVariable Long id) {
        return notificationService.getUnreadCount(id);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id, authentication)")
    @GetMapping("/{id}/notifications")
    public List<NotificationResponse> getNotifications(@PathVariable Long id) {
        return notificationService.getUserNotifications(id);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id, authentication)")
    @GetMapping("/{id}/notifications/unread")
    public List<NotificationResponse> getUnreadNotifications(@PathVariable Long id) {
        return notificationService.getUnreadNotifications(id);
    }

    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    @PatchMapping("/{id}/notifications/{notificationId}/read")
    public NotificationResponse readNotification(@PathVariable Long id, @PathVariable Long notificationId) {
        return notificationService.readNotification(id, notificationId);
    }

    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    @PatchMapping("/{id}/notifications/{notificationId}/toggle")
    public void toggleNotificationRead(@PathVariable Long id, @PathVariable Long notificationId) {
        notificationService.toggleNotificationRead(id, notificationId);
    }

    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    @PatchMapping("/{id}/notifications/toggle")
    public void toggleNotificationsRead(@PathVariable Long id, @RequestParam List<Long> notificationIds) {
        notificationService.toggleNotificationsRead(id, notificationIds);
    }

    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    @PatchMapping("/{id}/notifications")
    public void markAllNotificationsAsRead(@PathVariable Long id) {
        notificationService.markAllAsRead(id);
    }

    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    @DeleteMapping("/{id}/notifications/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(@PathVariable Long id, @PathVariable Long notificationId) {
        notificationService.deleteNotification(id, notificationId);
    }

    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    @DeleteMapping("/{id}/notifications")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotifications(@PathVariable Long id, @RequestParam List<Long> notificationIds) {
        notificationService.deleteNotifications(id, notificationIds);
    }
}
