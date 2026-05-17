package com.roy.morago.service.topic;

import com.roy.morago.dto.topic.CategoryDTO;
import com.roy.morago.entity.topic.Category;
import com.roy.morago.exception.CategoryNotFoundException;
import com.roy.morago.exception.DuplicateCategoryNameException;
import com.roy.morago.repository.topic.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        checkDuplicateCategories(dto.getName());
        Category category = new Category();
        category.setName(dto.getName());
        category.setActive(true);
        categoryRepository.save(category);
        return createCategoryDTO(category);
    }

    public CategoryDTO getCategory(Long id) {
        Category category = findCategoryById(id);
        return createCategoryDTO(category);
    }

    @Transactional
    public void deactivateCategory(Long id) {
        Category category = findCategoryById(id);
        category.setActive(false);
    }

    @Transactional
    public void activateCategory(Long id) {
        Category category = findCategoryById(id);
        category.setActive(true);
    }

    @Transactional
    public void renameCategory(Long id, String newName) {
        checkDuplicateCategories(newName);
        Category category = findCategoryById(id);
        category.setName(newName);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategoryById(id);
        categoryRepository.delete(category);
    }

    private void checkDuplicateCategories(String name) {
        if (categoryRepository.existByName(name)) {
            throw new DuplicateCategoryNameException("Category with name " + name + " already exists");
        }
    }

    private CategoryDTO createCategoryDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(()
                -> new CategoryNotFoundException("Category not found."));
    }
}
