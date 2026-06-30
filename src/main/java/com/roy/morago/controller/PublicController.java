package com.roy.morago.controller;

import com.roy.morago.dto.file.FileResponse;
import com.roy.morago.dto.user.LanguageResponse;
import com.roy.morago.service.file.FileService;
import com.roy.morago.service.user.LanguageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "04 - Public", description = "Publicly accessible endpoints (no authentication required)")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/public")
public class PublicController {
    private final LanguageService languageService;
    private final FileService fileService;

    @Operation(
            summary = "View profile picture",
            description = "Retrieves a user's profile picture by file ID. Public endpoint – no authentication required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile picture retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/profile-picture/{id}")
    public FileResponse viewProfilePicture(@PathVariable Long id) {
        return fileService.viewFile(id);
    }

    @Operation(
            summary = "View topic icon",
            description = "Retrieves a topic icon by file ID. Public endpoint – no authentication required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topic icon retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/topics/icon/{id}")
    public FileResponse viewIcon(@PathVariable Long id) {
        return fileService.viewFile(id);
    }

    @Operation(
            summary = "List languages",
            description = "Returns a list of all available languages. Public endpoint – no authentication required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Languages retrieved successfully")
    })
    @GetMapping("/languages")
    public List<LanguageResponse> languageList() {
        return languageService.getAllLanguages();
    }
}