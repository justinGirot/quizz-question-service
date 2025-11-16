package com.quizz.question.service;

import com.quizz.question.dto.CategoryDTO;
import com.quizz.question.dto.CreateCategoryRequest;
import com.quizz.question.dto.UpdateCategoryRequest;

import java.util.List;

/**
 * Service for Category management (admin only)
 */
public interface CategoryService {

    /**
     * Create a new category
     */
    CategoryDTO createCategory(CreateCategoryRequest request, Long adminId);

    /**
     * Get all categories (for admin)
     */
    List<CategoryDTO> getAllCategories();

    /**
     * Get all active categories (for users selecting category)
     */
    List<CategoryDTO> getActiveCategories();

    /**
     * Get categories accessible for a specific group
     * Returns: public categories + group-specific categories
     */
    List<CategoryDTO> getAccessibleCategories(Long groupId);

    /**
     * Get only public categories (groupId is null)
     */
    List<CategoryDTO> getPublicCategories();

    /**
     * Get categories for a specific group
     */
    List<CategoryDTO> getGroupCategories(Long groupId);

    /**
     * Get category by ID
     */
    CategoryDTO getCategoryById(Long id);

    /**
     * Update a category
     */
    CategoryDTO updateCategory(Long id, UpdateCategoryRequest request);

    /**
     * Delete a category (only if not used in any questions)
     */
    void deleteCategory(Long id);
}
