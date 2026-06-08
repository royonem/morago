package com.roy.morago.controller;

import com.roy.morago.dto.file.FileDTO;
import com.roy.morago.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final FileService fileService;

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
}
