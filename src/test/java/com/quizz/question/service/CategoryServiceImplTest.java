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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CreateCategoryRequest createRequest;
    private UpdateCategoryRequest updateRequest;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Java");
        category.setDescription("Java programming questions");
        category.setStatus(CategoryStatus.ACTIVE);
        category.setCreatedBy(1L);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        createRequest = new CreateCategoryRequest();
        createRequest.setName("Python");
        createRequest.setDescription("Python programming questions");

        updateRequest = new UpdateCategoryRequest();
        updateRequest.setName("Java Advanced");
        updateRequest.setDescription("Advanced Java questions");
        updateRequest.setStatus(CategoryStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should create category successfully")
    void shouldCreateCategorySuccessfully() {
        // Given
        when(categoryRepository.existsByNameIgnoreCase("Python")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // When
        CategoryDTO result = categoryService.createCategory(createRequest, 1L);

        // Then
        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate category")
    void shouldThrowExceptionWhenCreatingDuplicateCategory() {
        // Given
        when(categoryRepository.existsByNameIgnoreCase("Python")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> categoryService.createCategory(createRequest, 1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should get all categories")
    void shouldGetAllCategories() {
        // Given
        List<Category> categories = Arrays.asList(category);
        when(categoryRepository.findAll()).thenReturn(categories);

        // When
        List<CategoryDTO> result = categoryService.getAllCategories();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("Should get active categories only")
    void shouldGetActiveCategoriesOnly() {
        // Given
        List<Category> activeCategories = Arrays.asList(category);
        when(categoryRepository.findByStatusOrderByNameAsc(CategoryStatus.ACTIVE))
                .thenReturn(activeCategories);

        // When
        List<CategoryDTO> result = categoryService.getActiveCategories();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        verify(categoryRepository).findByStatusOrderByNameAsc(CategoryStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should get category by ID")
    void shouldGetCategoryById() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // When
        CategoryDTO result = categoryService.getCategoryById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(QuestionNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should update category successfully")
    void shouldUpdateCategorySuccessfully() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCase("Java Advanced")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // When
        CategoryDTO result = categoryService.updateCategory(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when updating to duplicate name")
    void shouldThrowExceptionWhenUpdatingToDuplicateName() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCase("Java Advanced")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> categoryService.updateCategory(1L, updateRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should delete category when not used")
    void shouldDeleteCategoryWhenNotUsed() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(questionRepository.existsByCategory_Id(1L)).thenReturn(false);

        // When
        categoryService.deleteCategory(1L);

        // Then
        verify(categoryRepository).delete(category);
    }

    @Test
    @DisplayName("Should throw exception when deleting category in use")
    void shouldThrowExceptionWhenDeletingCategoryInUse() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(questionRepository.existsByCategory_Id(1L)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("currently used");
    }

    @Test
    @DisplayName("Should allow same name when updating same category")
    void shouldAllowSameNameWhenUpdatingSameCategory() {
        // Given
        updateRequest.setName("Java"); // Same name as current
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // When
        CategoryDTO result = categoryService.updateCategory(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
        verify(categoryRepository, never()).existsByNameIgnoreCase(anyString());
    }
}
