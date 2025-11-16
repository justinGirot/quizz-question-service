package com.quizz.question.service;

import com.quizz.question.dto.CategoryDTO;
import com.quizz.question.dto.CreateCategoryRequest;
import com.quizz.question.dto.UpdateCategoryRequest;
import com.quizz.question.exception.QuestionNotFoundException;
import com.quizz.question.exception.ValidationException;
import com.quizz.question.model.Category;
import com.quizz.question.model.CategoryStatus;
import com.quizz.question.repository.CategoryRepository;
import com.quizz.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Category management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;

    @Override
    @Transactional
    public CategoryDTO createCategory(CreateCategoryRequest request, Long adminId) {
        log.info("Creating category: {} (groupId: {})", request.getName(), request.getGroupId());

        // Check if category already exists (case-insensitive)
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ValidationException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(CategoryStatus.ACTIVE)
                .groupId(request.getGroupId())  // null = public, otherwise group-specific
                .createdBy(adminId)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Category created with ID: {} (public: {})", saved.getId(), saved.isPublicCategory());

        return toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        log.info("Fetching all categories");
        return categoryRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getActiveCategories() {
        log.info("Fetching all active categories");
        return categoryRepository.findByStatusOrderByNameAsc(CategoryStatus.ACTIVE).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get categories accessible for creating a question in a specific group
     * Returns: public categories + group-specific categories
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAccessibleCategories(Long groupId) {
        log.info("Fetching accessible categories for groupId: {}", groupId);

        if (groupId == null) {
            // No group - return only public categories
            return categoryRepository.findByGroupIdIsNullAndStatusOrderByNameAsc(CategoryStatus.ACTIVE).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } else {
            // Group specified - return public + group-specific categories
            return categoryRepository.findAccessibleCategoriesForGroup(groupId, CategoryStatus.ACTIVE).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Get only public categories (groupId is null)
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getPublicCategories() {
        log.info("Fetching public categories only");
        return categoryRepository.findByGroupIdIsNullAndStatusOrderByNameAsc(CategoryStatus.ACTIVE).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get categories for a specific group
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getGroupCategories(Long groupId) {
        log.info("Fetching categories for groupId: {}", groupId);
        return categoryRepository.findByGroupIdAndStatusOrderByNameAsc(groupId, CategoryStatus.ACTIVE).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        log.info("Fetching category with ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException("Category not found with ID: " + id));
        return toDTO(category);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long id, UpdateCategoryRequest request) {
        log.info("Updating category: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException("Category not found with ID: " + id));

        // Check if new name conflicts with existing category
        if (!category.getName().equalsIgnoreCase(request.getName()) &&
            categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ValidationException("Category with name '" + request.getName() + "' already exists");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setStatus(request.getStatus());

        Category updated = categoryRepository.save(category);
        log.info("Category updated: {}", id);

        return toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException("Category not found with ID: " + id));

        // Check if category is used in any questions
        if (questionRepository.existsByCategory_Id(id)) {
            throw new ValidationException("Cannot delete category. It is currently used by one or more questions.");
        }

        categoryRepository.delete(category);
        log.info("Category deleted: {}", id);
    }

    private CategoryDTO toDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .status(category.getStatus())
                .groupId(category.getGroupId())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .createdBy(category.getCreatedBy())
                .build();
    }
}
