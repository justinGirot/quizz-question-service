package com.quizz.question.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizz.question.common.constants.ApiConstants;
import com.quizz.question.dto.CategoryDTO;
import com.quizz.question.dto.CreateCategoryRequest;
import com.quizz.question.dto.UpdateCategoryRequest;
import com.quizz.question.exception.QuestionNotFoundException;
import com.quizz.question.exception.ValidationException;
import com.quizz.question.model.CategoryStatus;
import com.quizz.question.security.JwtAuthentication;
import com.quizz.question.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@DisplayName("CategoryController Unit Tests")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private com.quizz.question.security.JwtUtil jwtUtil;

    private CategoryDTO categoryDTO;
    private CreateCategoryRequest createRequest;
    private UpdateCategoryRequest updateRequest;
    private JwtAuthentication userAuth;
    private JwtAuthentication adminAuth;

    @BeforeEach
    void setUp() {
        categoryDTO = CategoryDTO.builder()
                .id(1L)
                .name("Java")
                .description("Java programming questions")
                .status(CategoryStatus.ACTIVE)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = new CreateCategoryRequest();
        createRequest.setName("Python");
        createRequest.setDescription("Python programming questions");

        updateRequest = new UpdateCategoryRequest();
        updateRequest.setName("Java Advanced");
        updateRequest.setDescription("Advanced Java questions");
        updateRequest.setStatus(CategoryStatus.ACTIVE);

        userAuth = new JwtAuthentication(1L, "user@test.com",
                List.of("ROLE_USER"));

        adminAuth = new JwtAuthentication(2L, "admin@test.com",
                List.of("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Should get active categories successfully for all authenticated users")
    void shouldGetActiveCategoriesSuccessfully() throws Exception {
        // Given
        List<CategoryDTO> categories = Arrays.asList(categoryDTO);
        when(categoryService.getActiveCategories()).thenReturn(categories);

        // When & Then
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/categories/active")
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(categoryService).getActiveCategories();
    }

    @Test
    @DisplayName("Should return 401 when getting active categories without authentication")
    void shouldReturn401WhenGettingActiveCategoriesWithoutAuthentication() throws Exception {
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/categories/active"))
                .andExpect(status().isUnauthorized());

        verify(categoryService, never()).getActiveCategories();
    }

    @Test
    @DisplayName("Should get all categories successfully for admin")
    void shouldGetAllCategoriesSuccessfullyForAdmin() throws Exception {
        // Given
        List<CategoryDTO> categories = Arrays.asList(categoryDTO);
        when(categoryService.getAllCategories()).thenReturn(categories);

        // When & Then
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/categories")
                        .with(authentication(adminAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(categoryService).getAllCategories();
    }

    @Test
    @DisplayName("Should return 403 when non-admin gets all categories")
    void shouldReturn403WhenNonAdminGetsAllCategories() throws Exception {
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/categories")
                        .with(authentication(userAuth)))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).getAllCategories();
    }

    @Test
    @DisplayName("Should get category by ID successfully for admin")
    void shouldGetCategoryByIdSuccessfullyForAdmin() throws Exception {
        // Given
        when(categoryService.getCategoryById(1L)).thenReturn(categoryDTO);

        // When & Then
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/categories/1")
                        .with(authentication(adminAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Java"));

        verify(categoryService).getCategoryById(1L);
    }

    @Test
    @DisplayName("Should return 404 when category not found")
    void shouldReturn404WhenCategoryNotFound() throws Exception {
        // Given
        when(categoryService.getCategoryById(999L))
                .thenThrow(new QuestionNotFoundException("Category not found with ID: 999"));

        // When & Then
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/categories/999")
                        .with(authentication(adminAuth)))
                .andExpect(status().isNotFound());

        verify(categoryService).getCategoryById(999L);
    }

    @Test
    @DisplayName("Should return 403 when non-admin gets category by ID")
    void shouldReturn403WhenNonAdminGetsCategoryById() throws Exception {
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/categories/1")
                        .with(authentication(userAuth)))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).getCategoryById(any());
    }

    @Test
    @DisplayName("Should create category successfully for admin")
    void shouldCreateCategorySuccessfullyForAdmin() throws Exception {
        // Given
        when(categoryService.createCategory(any(CreateCategoryRequest.class), eq(2L)))
                .thenReturn(categoryDTO);

        // When & Then
        mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/categories")
                        .with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Java"));

        verify(categoryService).createCategory(any(CreateCategoryRequest.class), eq(2L));
    }

    @Test
    @DisplayName("Should return 403 when non-admin creates category")
    void shouldReturn403WhenNonAdminCreatesCategory() throws Exception {
        mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/categories")
                        .with(authentication(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).createCategory(any(), any());
    }

    @Test
    @DisplayName("Should return 400 when creating duplicate category")
    void shouldReturn400WhenCreatingDuplicateCategory() throws Exception {
        // Given
        when(categoryService.createCategory(any(CreateCategoryRequest.class), eq(2L)))
                .thenThrow(new ValidationException("Category with name 'Python' already exists"));

        // When & Then
        mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/categories")
                        .with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());

        verify(categoryService).createCategory(any(CreateCategoryRequest.class), eq(2L));
    }

    @Test
    @DisplayName("Should return 400 when creating category with invalid data")
    void shouldReturn400WhenCreatingCategoryWithInvalidData() throws Exception {
        // Given
        CreateCategoryRequest invalidRequest = new CreateCategoryRequest();
        // Missing required fields

        // When & Then
        mockMvc.perform(post(ApiConstants.API_BASE_PATH + "/categories")
                        .with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any(), any());
    }

    @Test
    @DisplayName("Should update category successfully for admin")
    void shouldUpdateCategorySuccessfullyForAdmin() throws Exception {
        // Given
        CategoryDTO updatedCategory = CategoryDTO.builder()
                .id(1L)
                .name("Java Advanced")
                .description("Advanced Java questions")
                .status(CategoryStatus.ACTIVE)
                .build();
        when(categoryService.updateCategory(eq(1L), any(UpdateCategoryRequest.class)))
                .thenReturn(updatedCategory);

        // When & Then
        mockMvc.perform(put(ApiConstants.API_BASE_PATH + "/categories/1")
                        .with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Java Advanced"));

        verify(categoryService).updateCategory(eq(1L), any(UpdateCategoryRequest.class));
    }

    @Test
    @DisplayName("Should return 403 when non-admin updates category")
    void shouldReturn403WhenNonAdminUpdatesCategory() throws Exception {
        mockMvc.perform(put(ApiConstants.API_BASE_PATH + "/categories/1")
                        .with(authentication(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).updateCategory(any(), any());
    }

    @Test
    @DisplayName("Should return 400 when updating to duplicate name")
    void shouldReturn400WhenUpdatingToDuplicateName() throws Exception {
        // Given
        when(categoryService.updateCategory(eq(1L), any(UpdateCategoryRequest.class)))
                .thenThrow(new ValidationException("Category with name 'Java Advanced' already exists"));

        // When & Then
        mockMvc.perform(put(ApiConstants.API_BASE_PATH + "/categories/1")
                        .with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(categoryService).updateCategory(eq(1L), any(UpdateCategoryRequest.class));
    }

    @Test
    @DisplayName("Should delete category successfully for admin")
    void shouldDeleteCategorySuccessfullyForAdmin() throws Exception {
        // Given
        doNothing().when(categoryService).deleteCategory(1L);

        // When & Then
        mockMvc.perform(delete(ApiConstants.API_BASE_PATH + "/categories/1")
                        .with(authentication(adminAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));

        verify(categoryService).deleteCategory(1L);
    }

    @Test
    @DisplayName("Should return 403 when non-admin deletes category")
    void shouldReturn403WhenNonAdminDeletesCategory() throws Exception {
        mockMvc.perform(delete(ApiConstants.API_BASE_PATH + "/categories/1")
                        .with(authentication(userAuth)))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).deleteCategory(any());
    }

    @Test
    @DisplayName("Should return 400 when deleting category in use")
    void shouldReturn400WhenDeletingCategoryInUse() throws Exception {
        // Given
        doThrow(new ValidationException("Cannot delete category. It is currently used by one or more questions."))
                .when(categoryService).deleteCategory(1L);

        // When & Then
        mockMvc.perform(delete(ApiConstants.API_BASE_PATH + "/categories/1")
                        .with(authentication(adminAuth)))
                .andExpect(status().isBadRequest());

        verify(categoryService).deleteCategory(1L);
    }

    @Test
    @DisplayName("Should return empty list when no active categories exist")
    void shouldReturnEmptyListWhenNoActiveCategoriesExist() throws Exception {
        // Given
        when(categoryService.getActiveCategories()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/categories/active")
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(categoryService).getActiveCategories();
    }
}
