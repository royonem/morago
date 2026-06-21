package com.roy.morago.service.topic;

import com.roy.morago.dto.topic.TopicRequest;
import com.roy.morago.dto.topic.TopicResponse;
import com.roy.morago.entity.topic.Topic;
import com.roy.morago.repository.topic.TopicRepository;
import com.roy.morago.service.file.FileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TopicService {
    private final TopicRepository topicRepository;
    private final TopicHelper topicHelper;
    private final FileService fileService;

    @Transactional
    public TopicResponse createTopic(TopicRequest dto) {
        Topic topic = new Topic();
        topicHelper.checkDuplicateTopics(dto.name());
        topic.setName(dto.name());
        topic.setActive(dto.active());
        if (dto.categoryId() != null) {
            topic.setCategory(topicHelper.findCategoryById(dto.categoryId()));
        }
        topicRepository.save(topic);
        if (dto.iconId() != null) {
            fileService.saveTopicIcon(topic.getId(), dto.iconId());
        }

        return topicHelper.createTopicResponse(topic);
    }

    public List<TopicResponse> getAllTopics() {
        List<Topic> topics = topicRepository.findAll();
        List<TopicResponse> topicsList = new ArrayList<>();
        for (Topic topic : topics) {
            topicsList.add(topicHelper.createTopicResponse(topic));
        }
        return topicsList;
    }

    public TopicResponse getTopic(Long id) {
        Topic topic = topicHelper.findTopicById(id);
        return topicHelper.createTopicResponse(topic);
    }

    @Transactional
    public TopicResponse updateTopic(Long id, TopicRequest dto) {
        Topic topic = topicHelper.findTopicById(id);
        topicHelper.checkDuplicateTopicsForUpdate(topic, dto.name());
        topic.setName(dto.name());
        if (dto.categoryId() != null) {
            topic.setCategory(topicHelper.findCategoryById(dto.categoryId()));
        }        topic.setActive(dto.active());
        if (dto.iconId() != null) {
            fileService.saveTopicIcon(topic.getId(), dto.iconId());
        }
        return topicHelper.createTopicResponse(topic);
    }

    @Transactional
    public void deleteTopic(Long id) {
        Topic topic  = topicHelper.findTopicById(id);
        topicRepository.delete(topic);
    }
}
