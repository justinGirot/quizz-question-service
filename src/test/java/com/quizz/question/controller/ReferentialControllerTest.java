package com.quizz.question.controller;

import com.quizz.question.common.constants.ApiConstants;
import com.quizz.question.dto.CategoryDTO;
import com.quizz.question.dto.DifficultyLevelDTO;
import com.quizz.question.model.CategoryStatus;
import com.quizz.question.security.JwtAuthentication;
import com.quizz.question.service.CategoryService;
import com.quizz.question.service.DifficultyLevelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReferentialController.class)
@DisplayName("ReferentialController Unit Tests")
class ReferentialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private DifficultyLevelService difficultyLevelService;

    @MockBean
    private com.quizz.question.security.JwtUtil jwtUtil;

    private JwtAuthentication userAuth;
    private CategoryDTO categoryDTO;
    private DifficultyLevelDTO difficultyLevelDTO;

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

        difficultyLevelDTO = DifficultyLevelDTO.builder()
                .id(1L)
                .name("Easy")
                .description("Easy questions for beginners")
                .displayOrder(1)
                .pointsMultiplier(1.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userAuth = new JwtAuthentication(1L, "user@test.com",
                List.of("ROLE_USER"));
    }

    @Test
    @DisplayName("Should get categories from referentiel successfully")
    void shouldGetCategoriesFromReferentielSuccessfully() throws Exception {
        // Given
        List<CategoryDTO> categories = Arrays.asList(categoryDTO);
        when(categoryService.getActiveCategories()).thenReturn(categories);

        // When & Then
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/question/referentiel/categories")
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
    @DisplayName("Should return 401 when getting categories without authentication")
    void shouldReturn401WhenGettingCategoriesWithoutAuthentication() throws Exception {
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/question/referentiel/categories"))
                .andExpect(status().isUnauthorized());

        verify(categoryService, never()).getActiveCategories();
    }

    @Test
    @DisplayName("Should return empty list when no categories exist")
    void shouldReturnEmptyListWhenNoCategoriesExist() throws Exception {
        // Given
        when(categoryService.getActiveCategories()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/question/referentiel/categories")
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(categoryService).getActiveCategories();
    }

    @Test
    @DisplayName("Should get difficulty levels from referentiel successfully")
    void shouldGetDifficultyLevelsFromReferentielSuccessfully() throws Exception {
        // Given
        List<DifficultyLevelDTO> levels = Arrays.asList(difficultyLevelDTO);
        when(difficultyLevelService.getAllDifficultyLevels()).thenReturn(levels);

        // When & Then
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/question/referentiel/difficulty-levels")
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Easy"))
                .andExpect(jsonPath("$[0].displayOrder").value(1))
                .andExpect(jsonPath("$[0].pointsMultiplier").value(1.0));

        verify(difficultyLevelService).getAllDifficultyLevels();
    }

    @Test
    @DisplayName("Should return 401 when getting difficulty levels without authentication")
    void shouldReturn401WhenGettingDifficultyLevelsWithoutAuthentication() throws Exception {
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/question/referentiel/difficulty-levels"))
                .andExpect(status().isUnauthorized());

        verify(difficultyLevelService, never()).getAllDifficultyLevels();
    }

    @Test
    @DisplayName("Should return empty list when no difficulty levels exist")
    void shouldReturnEmptyListWhenNoDifficultyLevelsExist() throws Exception {
        // Given
        when(difficultyLevelService.getAllDifficultyLevels()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/question/referentiel/difficulty-levels")
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(difficultyLevelService).getAllDifficultyLevels();
    }

    @Test
    @DisplayName("Should get multiple categories ordered by name")
    void shouldGetMultipleCategoriesOrderedByName() throws Exception {
        // Given
        CategoryDTO category1 = CategoryDTO.builder().id(1L).name("Java").status(CategoryStatus.ACTIVE).build();
        CategoryDTO category2 = CategoryDTO.builder().id(2L).name("Python").status(CategoryStatus.ACTIVE).build();
        CategoryDTO category3 = CategoryDTO.builder().id(3L).name("JavaScript").status(CategoryStatus.ACTIVE).build();
        List<CategoryDTO> categories = Arrays.asList(category1, category2, category3);
        when(categoryService.getActiveCategories()).thenReturn(categories);

        // When & Then
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/question/referentiel/categories")
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[1].name").value("Python"))
                .andExpect(jsonPath("$[2].name").value("JavaScript"));

        verify(categoryService).getActiveCategories();
    }

    @Test
    @DisplayName("Should get multiple difficulty levels ordered by display order")
    void shouldGetMultipleDifficultyLevelsOrderedByDisplayOrder() throws Exception {
        // Given
        DifficultyLevelDTO easy = DifficultyLevelDTO.builder()
                .id(1L).name("Easy").displayOrder(1).pointsMultiplier(1.0).build();
        DifficultyLevelDTO medium = DifficultyLevelDTO.builder()
                .id(2L).name("Medium").displayOrder(2).pointsMultiplier(1.5).build();
        DifficultyLevelDTO hard = DifficultyLevelDTO.builder()
                .id(3L).name("Hard").displayOrder(3).pointsMultiplier(2.0).build();
        List<DifficultyLevelDTO> levels = Arrays.asList(easy, medium, hard);
        when(difficultyLevelService.getAllDifficultyLevels()).thenReturn(levels);

        // When & Then
        mockMvc.perform(get(ApiConstants.API_BASE_PATH + "/question/referentiel/difficulty-levels")
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name").value("Easy"))
                .andExpect(jsonPath("$[0].displayOrder").value(1))
                .andExpect(jsonPath("$[1].name").value("Medium"))
                .andExpect(jsonPath("$[1].displayOrder").value(2))
                .andExpect(jsonPath("$[2].name").value("Hard"))
                .andExpect(jsonPath("$[2].displayOrder").value(3));

        verify(difficultyLevelService).getAllDifficultyLevels();
    }
}
