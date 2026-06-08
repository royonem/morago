package com.roy.morago.service.topic;

import com.roy.morago.dto.topic.CategoryDTO;
import com.roy.morago.dto.topic.TopicResponse;
import com.roy.morago.entity.topic.Category;
import com.roy.morago.entity.topic.Topic;
import com.roy.morago.exception.topic.CategoryNotFoundException;
import com.roy.morago.exception.topic.DuplicateCategoryNameException;
import com.roy.morago.exception.topic.DuplicateTopicNameException;
import com.roy.morago.exception.topic.TopicNotFoundException;
import com.roy.morago.repository.topic.CategoryRepository;
import com.roy.morago.repository.topic.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopicHelper {
    private final CategoryRepository categoryRepository;
    private final TopicRepository topicRepository;

    protected void checkDuplicateTopics(String name) {
        if (topicRepository.existByNameIgnoreCase(name)) {
            throw new DuplicateTopicNameException("Topic with name " + name + " already exists");
        }
    }

    protected void checkDuplicateCategories(String name) {
        if (categoryRepository.existByNameIgnoreCase(name)) {
            throw new DuplicateCategoryNameException("Category with name " + name + " already exists");
        }
    }

    protected TopicResponse createTopicResponse(Topic topic) {
        TopicResponse response = new TopicResponse();
        response.setId(topic.getId());
        response.setName(topic.getName());
        return response;
    }

    protected CategoryDTO createCategoryDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }

    protected Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(()
                -> new CategoryNotFoundException("Category not found."));
    }

    public Topic findTopicById(Long topicId) {
        return topicRepository.findById(topicId).orElseThrow(()
                -> new TopicNotFoundException("Topic not found."));
    }

}
