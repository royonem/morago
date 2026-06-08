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
        topicHelper.checkDuplicateTopics(dto.getName());
        Topic topic = new Topic();
        topic.setName(dto.getName());
        topic.setActive(dto.getActive());
        if (dto.getCategoryId() != null) {
            topic.setCategory(topicHelper.findCategoryById(dto.getCategoryId()));
        }
        topicRepository.save(topic);
        if (dto.getIconId() != null) {
            fileService.saveTopicIcon(topic.getId(), dto.getIconId());
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
        topicHelper.checkDuplicateTopics(dto.getName());
        Topic topic = topicHelper.findTopicById(id);
        topic.setName(dto.getName());
        if (dto.getCategoryId() != null) {
            topic.setCategory(topicHelper.findCategoryById(dto.getCategoryId()));
        }        topic.setActive(dto.getActive());
        if (dto.getIconId() != null) {
            fileService.saveTopicIcon(topic.getId(), dto.getIconId());
        }
        return topicHelper.createTopicResponse(topic);
    }

    @Transactional
    public void deleteTopic(Long id) {
        Topic topic  = topicHelper.findTopicById(id);
        topicRepository.delete(topic);
    }
}
