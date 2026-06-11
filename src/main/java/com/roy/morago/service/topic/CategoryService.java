package com.roy.morago.service.topic;

import com.roy.morago.dto.topic.CategoryDTO;
import com.roy.morago.entity.topic.Category;
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
    private final TopicHelper topicHelper;

    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        topicHelper.checkDuplicateCategories(dto.getName());
        Category category = new Category();
        category.setName(dto.getName());
        category.setActive(true);
        categoryRepository.save(category);
        return topicHelper.createCategoryDTO(category);
    }

    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoriesList = new ArrayList<>();
        for (Category category : categories) {
            categoriesList.add(topicHelper.createCategoryDTO(category));
        }
        return categoriesList;
    }

    public CategoryDTO getCategory(Long id) {
        Category category = topicHelper.findCategoryById(id);
        return topicHelper.createCategoryDTO(category);
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category category = topicHelper.findCategoryById(id);
        topicHelper.checkDuplicateCategoriesForUpdate(category, dto.getName());
        category.setName(dto.getName());
        category.setActive(dto.getActive());
        return topicHelper.createCategoryDTO(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = topicHelper.findCategoryById(id);
        categoryRepository.delete(category);
    }
}
