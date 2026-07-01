package com.roy.morago.controller;

import com.roy.morago.dto.file.FileResponse;
import com.roy.morago.dto.notification.NotificationResponse;
import com.roy.morago.dto.user.UserResponse;
import com.roy.morago.service.file.FileService;
import com.roy.morago.service.notification.NotificationService;
import com.roy.morago.service.user.LanguageService;
import com.roy.morago.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "02 - Users", description = "User management, profile, notification, and language endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;
    private final FileService fileService;
    private final NotificationService notificationService;
    private final LanguageService languageService;

    @Operation(
            summary = "Get user by ID",
            description = "Returns user details. **Role: ADMIN or the user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot access this user's data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id, authentication)")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @Operation(
            summary = "Upload profile picture",
            description = "Uploads a temporary profile picture for the user. **Role: The user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Picture uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file type or size"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/{id}/profile-picture")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    @ResponseStatus(HttpStatus.CREATED)
    public FileResponse uploadProfilePicture(@PathVariable Long id, @RequestPart MultipartFile picture) {
        return fileService.uploadProfilePicture(picture);
    }

    @Operation(
            summary = "Save profile picture",
            description = "Saves a previously uploaded picture as the user's profile picture. **Role: The user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile picture saved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @PutMapping("/{id}/profile-picture")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    public void saveProfilePicture(@PathVariable Long id, @RequestParam Long pictureId) {
        fileService.saveProfilePicture(id, pictureId);
    }

    @Operation(
            summary = "Delete profile picture",
            description = "Deletes the user's profile picture. **Role: ADMIN or the user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Profile picture deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Profile picture not found")
    })
    @DeleteMapping("/{id}/profile-picture")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id, authentication)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfilePicture(@PathVariable Long id) {
        fileService.deleteProfilePicture(id);
    }

    @Operation(
            summary = "Get unread notification count",
            description = "Returns the number of unread notifications for the user. **Role: ADMIN or the user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Count retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}/notifications/unread-count")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id, authentication)")
    public long getUnreadCount(@PathVariable Long id) {
        return notificationService.getUnreadCount(id);
    }

    @Operation(
            summary = "Get all notifications",
            description = "Returns paginated notifications for the user. **Role: ADMIN or the user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}/notifications")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id, authentication)")
    public Page<NotificationResponse> getAllNotifications(@PathVariable Long id, @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return notificationService.getNotificationsByUserId(id, pageable);
    }

    @Operation(
            summary = "Get unread notifications",
            description = "Returns paginated unread notifications for the user. **Role: ADMIN or the user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unread notifications retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}/notifications/unread")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id, authentication)")
    public Page<NotificationResponse> getUnreadNotifications(@PathVariable Long id, @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return notificationService.getUnreadNotificationsByUserId(id, pageable);
    }

    @Operation(
            summary = "Mark notification as read",
            description = "Marks a single notification as read. **Role: The user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification marked as read"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PatchMapping("/{id}/notifications/{notificationId}/read")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    public NotificationResponse readNotification(@PathVariable Long id, @PathVariable Long notificationId) {
        return notificationService.readNotification(id, notificationId);
    }

    @Operation(
            summary = "Toggle notification read status",
            description = "Toggles read/unread for a single notification. **Role: The user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification toggled"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PatchMapping("/{id}/notifications/{notificationId}/toggle")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    public void toggleNotificationRead(@PathVariable Long id, @PathVariable Long notificationId) {
        notificationService.toggleNotificationRead(id, notificationId);
    }

    @Operation(
            summary = "Toggle multiple notifications",
            description = "Toggles read/unread for multiple notifications. **Role: The user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications toggled"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/{id}/notifications/toggle")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    public void toggleNotificationsRead(@PathVariable Long id, @RequestParam List<Long> notificationIds) {
        notificationService.toggleNotificationsRead(id, notificationIds);
    }

    @Operation(
            summary = "Mark notifications as read",
            description = "Marks multiple notifications as read. **Role: The user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications marked as read"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/{id}/notifications")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    public void markNotificationsAsRead(@PathVariable Long id, @RequestParam List<Long> notificationIds) {
        notificationService.markAsRead(id, notificationIds);
    }

    @Operation(
            summary = "Mark all notifications as read",
            description = "Marks all notifications as read for the user. **Role: The user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All notifications marked as read"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/{id}/notifications/all")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    public void markAllNotificationsAsRead(@PathVariable Long id) {
        notificationService.markAllAsRead(id);
    }

    @Operation(
            summary = "Delete notification",
            description = "Deletes a single notification. **Role: The user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notification deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @DeleteMapping("/{id}/notifications/{notificationId}")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(@PathVariable Long id, @PathVariable Long notificationId) {
        notificationService.deleteNotificationByUserId(id, notificationId);
    }

    @Operation(
            summary = "Delete multiple notifications",
            description = "Deletes multiple notifications. **Role: The user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notifications deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{id}/notifications")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotifications(@PathVariable Long id, @RequestParam List<Long> notificationIds) {
        notificationService.deleteNotificationsByUserId(id, notificationIds);
    }

    @Operation(
            summary = "Delete all read notifications",
            description = "Deletes all read notifications for the user. **Role: The user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Read notifications deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{id}/notifications/read")
    @PreAuthorize("@securityService.isCurrentUser(#id, authentication)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllReadNotifications(@PathVariable Long id) {
        notificationService.deleteAllReadNotificationsByUserId(id);
    }

    @Operation(
            summary = "Add languages to translator",
            description = "Adds languages to a translator's profile. **Role: The translator themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Languages added successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User or language not found")
    })
    @PatchMapping("/{id}/languages")
    @PreAuthorize("@securityService.isCurrentTranslator(#id, authentication)")
    public void addLanguage(@PathVariable Long id, @RequestBody List<Long> languageIds) {
        languageService.addLanguages(id, languageIds);
    }
}