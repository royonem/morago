package com.roy.morago.service.topic;

import com.roy.morago.dto.topic.TopicRequest;
import com.roy.morago.dto.topic.TopicResponse;
import com.roy.morago.entity.topic.Topic;
import com.roy.morago.mapper.TopicMapper;
import com.roy.morago.repository.topic.TopicRepository;
import com.roy.morago.service.file.FileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TopicService {
    private final TopicRepository topicRepository;
    private final TopicHelper topicHelper;
    private final FileService fileService;
    private final TopicMapper topicMapper;

    @Transactional
    public TopicResponse createTopic(TopicRequest request) {
        log.info("Creating topic: name={}", request.name());
        topicHelper.checkDuplicateTopics(request.name());

        Topic topic = topicMapper.toEntity(request);
        if (request.categoryId() != null) {
            topic.setCategory(topicHelper.findCategoryById(request.categoryId()));
        }
        topicRepository.save(topic);
        if (request.iconId() != null) {
            fileService.saveTopicIcon(topic.getId(), request.iconId());
        }
        log.info("Topic created: topicId={}, name={}", topic.getId(), topic.getName());
        return topicMapper.toResponse(topic);
    }

    public List<TopicResponse> getAllTopics() {
        List<Topic> topics = topicRepository.findAll();
        List<TopicResponse> topicsList = new ArrayList<>();
        for (Topic topic : topics) {
            topicsList.add(topicMapper.toResponse(topic));
        }
        return topicsList;
    }

    public TopicResponse getTopic(Long id) {
        Topic topic = topicHelper.findTopicById(id);
        return topicMapper.toResponse(topic);
    }

    @Transactional
    public TopicResponse updateTopic(Long id, TopicRequest request) {
        log.info("Updating topic: topicId={}", id);
        Topic topic = topicHelper.findTopicById(id);
        topicHelper.checkDuplicateTopicsForUpdate(topic, request.name());
        topic.setName(request.name());
        if (request.categoryId() != null) {
            topic.setCategory(topicHelper.findCategoryById(request.categoryId()));
        }        topic.setActive(request.active());
        if (request.iconId() != null) {
            fileService.saveTopicIcon(topic.getId(), request.iconId());
        }
        log.info("Topic updated: topicId={}, name={}", topic.getId(), topic.getName());
        return topicMapper.toResponse(topic);
    }

    @Transactional
    public void deleteTopic(Long id) {
        log.info("Deleting topic: topicId={}", id);
        Topic topic  = topicHelper.findTopicById(id);
        topicRepository.delete(topic);
        log.info("Topic deleted: topicId={}, name={}", topic.getId(), topic.getName());
    }
}
