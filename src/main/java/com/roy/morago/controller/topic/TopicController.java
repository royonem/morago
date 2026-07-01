package com.roy.morago.controller.topic;

import com.roy.morago.dto.file.FileResponse;
import com.roy.morago.dto.topic.TopicRequest;
import com.roy.morago.dto.topic.TopicResponse;
import com.roy.morago.service.file.FileService;
import com.roy.morago.service.topic.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "10 - Topics", description = "Topic management endpoints")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/topics")
public class TopicController {
    private final TopicService topicService;
    private final FileService fileService;

    @Operation(
            summary = "Create topic",
            description = "Creates a new topic. **Role: ADMIN only.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Topic created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "409", description = "Topic with same name already exists")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public TopicResponse createTopic(@Valid @RequestBody TopicRequest dto) {
        return topicService.createTopic(dto);
    }

    @Operation(
            summary = "List all topics",
            description = "Returns a list of all topics. Public endpoint – no authentication required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topics retrieved successfully")
    })
    @GetMapping("/")
    public List<TopicResponse> topicList() {
        return topicService.getAllTopics();
    }

    @Operation(
            summary = "Get topic by ID",
            description = "Returns topic details. Public endpoint – no authentication required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topic found successfully"),
            @ApiResponse(responseCode = "404", description = "Topic not found")
    })
    @GetMapping("/{id}")
    public TopicResponse getTopic(@PathVariable Long id) {
        return topicService.getTopic(id);
    }

    @Operation(
            summary = "Update topic",
            description = "Updates an existing topic. **Role: ADMIN only.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topic updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Topic not found"),
            @ApiResponse(responseCode = "409", description = "Topic with same name already exists")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public TopicResponse updateTopic(@PathVariable Long id, @Valid @RequestBody TopicRequest dto) {
        return topicService.updateTopic(id, dto);
    }

    @Operation(
            summary = "Delete topic",
            description = "Deletes a topic. **Role: ADMIN only.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Topic deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Topic not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTopic(@PathVariable Long id) {
        topicService.deleteTopic(id);
    }

    @Operation(
            summary = "Upload topic icon",
            description = "Uploads a temporary topic icon. **Role: ADMIN only.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Icon uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file type or size"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/icon")
    @ResponseStatus(HttpStatus.CREATED)
    public FileResponse uploadIcon(@Parameter(
            content = @Content(mediaType = "multipart/form-data"),
            schema = @Schema(type = "string", format = "binary")) @RequestPart MultipartFile icon) {
        return fileService.uploadTopicIcon(icon);
    }

    @Operation(
            summary = "View topic icon",
            description = "Retrieves a topic icon by file ID. Public endpoint – no authentication required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Icon retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/icon/{id}")
    public FileResponse viewIcon(@PathVariable Long id) {
        return fileService.viewFile(id);
    }

    @Operation(
            summary = "Save topic icon",
            description = "Saves a previously uploaded image as the topic's icon. **Role: ADMIN only.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Icon saved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "File or topic not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/icon")
    public void saveIcon(@PathVariable Long id, @RequestParam Long iconId) {
        fileService.saveTopicIcon(id, iconId);
    }

    @Operation(
            summary = "Delete topic icon",
            description = "Deletes the topic's icon. **Role: ADMIN only.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Icon deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Topic or icon not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/icon")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIcon(@PathVariable Long id) {
        fileService.deleteTopicIcon(id);
    }
}