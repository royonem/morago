package com.roy.morago.controller;

import com.roy.morago.dto.file.FileResponse;
import com.roy.morago.dto.notification.NotificationRequest;
import com.roy.morago.dto.notification.NotificationResponse;
import com.roy.morago.dto.user.*;
import com.roy.morago.service.notification.NotificationService;
import com.roy.morago.service.file.FileService;
import com.roy.morago.service.user.LanguageService;
import com.roy.morago.service.user.UserService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService userService;
    private final FileService fileService;
    private final LanguageService languageService;
    private final NotificationService notificationService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> getAllUsers(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
            return userService.getAllUsers(pageable);
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> searchUsers(@RequestBody UserSearchRequest request) {
        return userService.searchUsers(request);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PatchMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest dto) {
        userService.updateUser(id, dto);
    }

    @PatchMapping("/translators/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void verifyTranslator(@PathVariable Long id) {
        userService.verifyTranslator(id);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PostMapping("/topics/icon")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public FileResponse uploadIcon(@RequestParam MultipartFile icon) {
        return fileService.uploadTopicIcon(icon);
    }

    @PutMapping("/topics/{id}/icon")
    @PreAuthorize("hasRole('ADMIN')")
    public void saveIcon(@PathVariable Long id, @RequestParam Long iconId) {
        fileService.saveTopicIcon(id, iconId);
    }

    @DeleteMapping("/topics/{id}/icon")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIcon(@PathVariable Long id) {
        fileService.deleteTopicIcon(id);
    }

    @PostMapping("/languages")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public LanguageResponse createLanguage(@Valid @RequestBody LanguageRequest request) {
        return languageService.createLanguage(request);
    }

    @DeleteMapping("/languages/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLanguage(@PathVariable Long id) {
        languageService.deleteLanguage(id);
    }

    @PostMapping("/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse createNotification(@Valid @RequestBody NotificationRequest dto) {
        return notificationService.createNotification(dto);
    }

    @GetMapping("/notifications/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public NotificationResponse getNotification(@PathVariable Long id) {
        return notificationService.getNotification(id);
    }

    @PatchMapping("/notifications/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void sendNotification(@RequestParam Long userId, @PathVariable Long id) {
        notificationService.sendNotification(userId, id);
    }

    @DeleteMapping("/notifications/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUnsentNotification(@PathVariable Long id) {
        notificationService.deleteUnsentNotification(id);
    }

    @DeleteMapping("/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUnsentNotifications(@RequestParam List<Long> ids) {
        notificationService.deleteUnsentNotifications(ids);
    }
}

