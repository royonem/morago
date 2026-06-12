package com.roy.morago.controller;

import com.roy.morago.dto.file.FileResponse;
import com.roy.morago.dto.user.LanguageResponse;
import com.roy.morago.service.file.FileService;
import com.roy.morago.service.user.LanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/public")
public class PublicController {
    private final LanguageService languageService;
    private final FileService fileService;

    @GetMapping("/profile-picture/{id}")
    public FileResponse viewProfilePicture(@PathVariable Long id) {
        return fileService.viewFile(id);
    }

    @GetMapping("/topics/icon/{id}")
    public FileResponse viewIcon(@PathVariable Long id) {
        return fileService.viewFile(id);
    }

    @GetMapping("/languages")
    public List<LanguageResponse> languageList() {
        return languageService.getAllLanguages();
    }
}
