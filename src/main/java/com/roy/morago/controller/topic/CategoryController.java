package com.roy.morago.controller.topic;

import com.roy.morago.dto.topic.CategoryDTO;
import com.roy.morago.service.topic.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDTO createCategory(@Valid @RequestBody CategoryDTO dto) {
        return categoryService.createCategory(dto);
    }

    @GetMapping("/{id}")
    public CategoryDTO getCategory(@PathVariable Long id) {
        return categoryService.getCategory(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public void deactivateCategory(@PathVariable Long id) {
        categoryService.deactivateCategory(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activate")
    public void activateCategory(@PathVariable Long id) {
        categoryService.activateCategory(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/rename")
    public void renameCategory(@PathVariable Long id, @RequestParam String newName) {
        categoryService.renameCategory(id, newName);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }
}
