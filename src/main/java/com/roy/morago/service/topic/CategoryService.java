package com.roy.morago.service.topic;

import com.roy.morago.dto.topic.CategoryRequest;
import com.roy.morago.dto.topic.CategoryResponse;
import com.roy.morago.entity.topic.Category;
import com.roy.morago.mapper.CategoryMapper;
import com.roy.morago.repository.topic.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final TopicHelper topicHelper;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Creating category: name={}", request.name());
        topicHelper.checkDuplicateCategories(request.name());
        Category category = new Category();
        category.setName(request.name());
        category.setActive(true);
        categoryRepository.save(category);
        log.info("Category created: categoryId={}, name={}", category.getId(), category.getName());
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
        log.info("Updating category: categoryId={}", id);
        Category category = topicHelper.findCategoryById(id);
        topicHelper.checkDuplicateCategoriesForUpdate(category, request.name());
        category.setName(request.name());
        category.setActive(request.active());
        log.info("Category updated: categoryId={}, new name={}, active={}", category.getId(), category.getName(), category.getActive());
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category: categoryId={}", id);
        Category category = topicHelper.findCategoryById(id);
        categoryRepository.delete(category);
        log.info("Category deleted: categoryId={}, name={}", id, category.getName());
    }
}
