package com.roy.morago.controller;

import com.roy.morago.dto.file.FileDTO;
import com.roy.morago.enums.FilePurpose;
import com.roy.morago.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/file")
public class FileController {

    private final FileService fileService;

    @PostMapping("/uploadPicture")
    public FileDTO uploadProfilePicture(@RequestParam MultipartFile picture) {
        FilePurpose filePurpose = FilePurpose.PICTURE;
        return fileService.uploadFile(picture, filePurpose);
    }

    @PostMapping("/uploadIcon")
    public FileDTO uploadTopicIcon(@RequestParam MultipartFile icon) {
        FilePurpose filePurpose = FilePurpose.ICON;
        return fileService.uploadFile(icon, filePurpose);
    }

    @PostMapping("/savePicture")
    public void saveProfilePicture(@RequestParam Long pictureId, Authentication authentication) {
        fileService.saveProfilePicture(pictureId, authentication);
    }

    @PostMapping("/saveIcon")
    public void saveTopicIcon(@RequestParam Long iconId, @RequestParam Long topicId) {
        fileService.saveTopicIcon(iconId, topicId);
    }

    @DeleteMapping("/delete")
    public void deleteFile(@RequestParam Long id) {
        fileService.deleteFile(id);
    }

    @GetMapping("/view")
    public FileDTO viewFile(@RequestParam Long id) {
        return fileService.viewFile(id);
    }
}
