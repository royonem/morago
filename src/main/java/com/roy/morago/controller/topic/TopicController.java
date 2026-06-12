package com.roy.morago.controller.topic;

import com.roy.morago.dto.file.FileResponse;
import com.roy.morago.dto.topic.TopicRequest;
import com.roy.morago.dto.topic.TopicResponse;
import com.roy.morago.service.file.FileService;
import com.roy.morago.service.topic.TopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/topics")
public class TopicController {
    private final TopicService topicService;
    private final FileService fileService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public TopicResponse createTopic(@Valid @RequestBody TopicRequest dto) {
        return topicService.createTopic(dto);
    }

    @GetMapping("/")
    public List<TopicResponse> topicList() {
        return topicService.getAllTopics();
    }

    @GetMapping("/{id}")
    public TopicResponse getTopic(@PathVariable Long id) {
        return topicService.getTopic(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public TopicResponse updateTopic(@PathVariable Long id, @Valid @RequestBody TopicRequest dto) {
        return topicService.updateTopic(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTopic(@PathVariable Long id) {
        topicService.deleteTopic(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/icon")
    @ResponseStatus(HttpStatus.CREATED)
    public FileResponse uploadIcon(@RequestParam MultipartFile icon) {
        return fileService.uploadTopicIcon(icon);
    }

    @GetMapping("/icon/{id}")
    public FileResponse viewIcon(@PathVariable Long id) {
        return fileService.viewFile(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/icon")
    public void saveIcon(@PathVariable Long id, @RequestParam Long iconId) {
        fileService.saveTopicIcon(id, iconId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/icon")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIcon(@PathVariable Long id) {
        fileService.deleteTopicIcon(id);
    }
}
