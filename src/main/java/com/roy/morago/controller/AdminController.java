package com.roy.morago.controller;

import com.roy.morago.dto.notification.NotificationRequest;
import com.roy.morago.dto.notification.NotificationResponse;
import com.roy.morago.dto.user.UpdateUserRequest;
import com.roy.morago.dto.user.UserResponse;
import com.roy.morago.service.notification.NotificationService;
import com.roy.morago.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService userService;
    private final NotificationService notificationService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<UserResponse> userList() {
        return userService.getAllUsers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/{id}")
    public void updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest dto) {
        userService.updateUser(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notifications")
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse createNotification(@Valid @RequestBody NotificationRequest dto) {
        return notificationService.createNotification(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/notifications/{id}")
    public NotificationResponse getNotification(@PathVariable Long id) {
        return notificationService.getNotification(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/notifications/{id}")
    public void sendNotification(@RequestParam Long userId, @PathVariable Long id) {
        notificationService.sendNotification(userId, id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/notifications/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUnsentNotification(@PathVariable Long id) {
        notificationService.deleteUnsentNotification(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/notifications")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUnsentNotifications(@RequestParam List<Long> ids) {
        notificationService.deleteUnsentNotifications(ids);
    }
}

