package com.roy.morago.controller;

import com.roy.morago.dto.file.FileResponse;
import com.roy.morago.dto.notification.NotificationResponse;
import com.roy.morago.dto.user.UserResponse;
import com.roy.morago.service.file.FileService;
import com.roy.morago.service.notification.NotificationService;
import com.roy.morago.service.user.LanguageService;
import com.roy.morago.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final FileService fileService;
    private final NotificationService notificationService;
    private final LanguageService languageService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id, authentication)")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PostMapping("/{id}/profile-picture")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    @ResponseStatus(HttpStatus.CREATED)
    public FileResponse uploadProfilePicture(@PathVariable Long id, @RequestParam MultipartFile picture) {
        return fileService.uploadProfilePicture(picture);
    }

    @PutMapping("/{id}/profile-picture")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    public void saveProfilePicture(@PathVariable Long id, @RequestParam Long pictureId) {
        fileService.saveProfilePicture(id, pictureId);
    }

    @DeleteMapping("/{id}/profile-picture")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id, authentication)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfilePicture(@PathVariable Long id) {
        fileService.deleteProfilePicture(id);
    }

    @GetMapping("/{id}/notifications/unread-count")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id, authentication)")
    public long getUnreadCount(@PathVariable Long id) {
        return notificationService.getUnreadCount(id);
    }

    @GetMapping("/{id}/notifications")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id, authentication)")
    public Page<NotificationResponse> getAllNotifications(@PathVariable Long id, @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return notificationService.getNotificationsByUserId(id, pageable);
    }

    @GetMapping("/{id}/notifications/unread")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id, authentication)")
    public Page<NotificationResponse> getUnreadNotifications(@PathVariable Long id, @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return notificationService.getUnreadNotificationsByUserId(id, pageable);
    }

    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    @PatchMapping("/{id}/notifications/{notificationId}/read")
    public NotificationResponse readNotification(@PathVariable Long id, @PathVariable Long notificationId) {
        return notificationService.readNotification(id, notificationId);
    }

    @PatchMapping("/{id}/notifications/{notificationId}/toggle")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    public void toggleNotificationRead(@PathVariable Long id, @PathVariable Long notificationId) {
        notificationService.toggleNotificationRead(id, notificationId);
    }

    @PatchMapping("/{id}/notifications/toggle")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    public void toggleNotificationsRead(@PathVariable Long id, @RequestParam List<Long> notificationIds) {
        notificationService.toggleNotificationsRead(id, notificationIds);
    }

    @PatchMapping("/{id}/notifications")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    public void markNotificationsAsRead(@PathVariable Long id, @RequestParam List<Long> notificationIds) {
        notificationService.markAsRead(id, notificationIds);
    }

    @PatchMapping("/{id}/notifications/all")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    public void markAllNotificationsAsRead(@PathVariable Long id) {
        notificationService.markAllAsRead(id);
    }

    @DeleteMapping("/{id}/notifications/{notificationId}")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(@PathVariable Long id, @PathVariable Long notificationId) {
        notificationService.deleteNotificationByUserId(id, notificationId);
    }

    @DeleteMapping("/{id}/notifications")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotifications(@PathVariable Long id, @RequestParam List<Long> notificationIds) {
        notificationService.deleteNotificationsByUserId(id, notificationIds);
    }

    @DeleteMapping("/{id}/notifications/read")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllReadNotifications(@PathVariable Long id) {
        notificationService.deleteAllReadNotificationsByUserId(id);
    }

    @PatchMapping("/{id}/languages")
    @PreAuthorize("@securityService.isCurrentTranslator(#id, authentication)")
    public void addLanguage(@PathVariable Long id, @RequestBody List<Long> languageIds) {
        languageService.addLanguages(id, languageIds);
    }
}
