package com.roy.morago.service.topic;

import com.roy.morago.dto.topic.TopicRequest;
import com.roy.morago.dto.topic.TopicResponse;
import com.roy.morago.entity.topic.Topic;
import com.roy.morago.mapper.TopicMapper;
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
    private final TopicMapper topicMapper;

    @Transactional
    public TopicResponse createTopic(TopicRequest request) {
        topicHelper.checkDuplicateTopics(request.name());

        Topic topic = topicMapper.toEntity(request);
        if (request.categoryId() != null) {
            topic.setCategory(topicHelper.findCategoryById(request.categoryId()));
        }
        topicRepository.save(topic);
        if (request.iconId() != null) {
            fileService.saveTopicIcon(topic.getId(), request.iconId());
        }
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
        return topicMapper.toResponse(topic);
    }

    @Transactional
    public void deleteTopic(Long id) {
        Topic topic  = topicHelper.findTopicById(id);
        topicRepository.delete(topic);
    }
}
