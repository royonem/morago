package com.roy.morago.service.topic;

import com.roy.morago.dto.topic.CategoryRequest;
import com.roy.morago.dto.topic.CategoryResponse;
import com.roy.morago.entity.topic.Category;
import com.roy.morago.mapper.CategoryMapper;
import com.roy.morago.repository.topic.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final TopicHelper topicHelper;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        topicHelper.checkDuplicateCategories(request.name());
        Category category = new Category();
        category.setName(request.name());
        category.setActive(true);
        categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponse> categoriesList = new ArrayList<>();
        for (Category category : categories) {
            categoriesList.add(categoryMapper.toResponse(category));
        }
        return categoriesList;
    }

    public CategoryResponse getCategory(Long id) {
        Category category = topicHelper.findCategoryById(id);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = topicHelper.findCategoryById(id);
        topicHelper.checkDuplicateCategoriesForUpdate(category, request.name());
        category.setName(request.name());
        category.setActive(request.active());
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = topicHelper.findCategoryById(id);
        categoryRepository.delete(category);
    }
}
