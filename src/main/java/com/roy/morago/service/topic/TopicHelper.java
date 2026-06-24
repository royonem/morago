package com.roy.morago.service.topic;

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
        boolean exists = topicRepository.existsByNameIgnoreCase(name);
        if (exists) {
            throw new DuplicateTopicNameException("Topic with name " + name + " already exists");
        }
    }

    protected void checkDuplicateTopicsForUpdate(Topic topic, String name) {
        boolean exists = topicRepository.existsByNameIgnoreCase(name);
        boolean differentName = !(topic.getName().equalsIgnoreCase(name));
        if (exists && differentName) {
            throw new DuplicateTopicNameException("Topic with name " + name + " already exists");
        }
    }

    protected void checkDuplicateCategories(String name) {
        boolean exists = categoryRepository.existsByNameIgnoreCase(name);
        if (exists) {
            throw new DuplicateCategoryNameException("Category with name " + name + " already exists");
        }
    }

    protected void checkDuplicateCategoriesForUpdate(Category category, String name) {
        boolean exists = categoryRepository.existsByNameIgnoreCase(name);
        boolean differentName = !(category.getName().equalsIgnoreCase(name));
        if (exists && differentName) {
            throw new DuplicateCategoryNameException("Category with name " + name + " already exists");
        }
    }

    public Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(()
                -> new CategoryNotFoundException("Category not found."));
    }

    public Topic findTopicById(Long topicId) {
        return topicRepository.findById(topicId).orElseThrow(()
                -> new TopicNotFoundException("Topic not found."));
    }

}
