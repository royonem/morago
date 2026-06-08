package com.roy.morago.controller;

import com.roy.morago.dto.file.FileDTO;
import com.roy.morago.dto.user.UpdateUserRequest;
import com.roy.morago.dto.user.UserResponse;
import com.roy.morago.service.file.FileService;
import com.roy.morago.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<UserResponse> userList() {
        return userService.getAllUsers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUserById(id);
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
    @PostMapping("/topics/icon")
    @ResponseStatus(HttpStatus.CREATED)
    public FileDTO uploadIcon(@RequestParam MultipartFile icon) {
        return fileService.uploadTopicIcon(icon);
    }

    @GetMapping("/topics/icon/{id}")
    public FileDTO viewIcon(@PathVariable Long id) {
        return fileService.viewFile(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/topics/{id}/icon")
    public void saveIcon(@PathVariable Long id, @RequestParam Long iconId) {
        fileService.saveTopicIcon(id, iconId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/topics/{id}/icon")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIcon(@PathVariable Long id) {
        fileService.deleteTopicIcon(id);
    }
}

