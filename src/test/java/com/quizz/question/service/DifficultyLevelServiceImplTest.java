package com.quizz.question.service;

import com.quizz.question.dto.DifficultyLevelDTO;
import com.quizz.question.exception.QuestionNotFoundException;
import com.quizz.question.model.DifficultyLevel;
import com.quizz.question.repository.DifficultyLevelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DifficultyLevelService Unit Tests")
class DifficultyLevelServiceImplTest {

    @Mock
    private DifficultyLevelRepository difficultyLevelRepository;

    @InjectMocks
    private DifficultyLevelServiceImpl difficultyLevelService;

    private DifficultyLevel easyLevel;
    private DifficultyLevel mediumLevel;
    private DifficultyLevel hardLevel;

    @BeforeEach
    void setUp() {
        easyLevel = new DifficultyLevel();
        easyLevel.setId(1L);
        easyLevel.setName("Easy");
        easyLevel.setDescription("Easy questions for beginners");
        easyLevel.setDisplayOrder(1);
        easyLevel.setPointsMultiplier(1.0);
        easyLevel.setCreatedAt(LocalDateTime.now());
        easyLevel.setUpdatedAt(LocalDateTime.now());

        mediumLevel = new DifficultyLevel();
        mediumLevel.setId(2L);
        mediumLevel.setName("Medium");
        mediumLevel.setDescription("Medium difficulty questions");
        mediumLevel.setDisplayOrder(2);
        mediumLevel.setPointsMultiplier(1.5);
        mediumLevel.setCreatedAt(LocalDateTime.now());
        mediumLevel.setUpdatedAt(LocalDateTime.now());

        hardLevel = new DifficultyLevel();
        hardLevel.setId(3L);
        hardLevel.setName("Hard");
        hardLevel.setDescription("Hard questions for advanced users");
        hardLevel.setDisplayOrder(3);
        hardLevel.setPointsMultiplier(2.0);
        hardLevel.setCreatedAt(LocalDateTime.now());
        hardLevel.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should get all difficulty levels ordered by display order")
    void shouldGetAllDifficultyLevelsOrderedByDisplayOrder() {
        // Given
        List<DifficultyLevel> difficultyLevels = Arrays.asList(easyLevel, mediumLevel, hardLevel);
        when(difficultyLevelRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(difficultyLevels);

        // When
        List<DifficultyLevelDTO> result = difficultyLevelService.getAllDifficultyLevels();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getName()).isEqualTo("Easy");
        assertThat(result.get(0).getDisplayOrder()).isEqualTo(1);
        assertThat(result.get(0).getPointsMultiplier()).isEqualTo(1.0);
        assertThat(result.get(1).getName()).isEqualTo("Medium");
        assertThat(result.get(1).getDisplayOrder()).isEqualTo(2);
        assertThat(result.get(1).getPointsMultiplier()).isEqualTo(1.5);
        assertThat(result.get(2).getName()).isEqualTo("Hard");
        assertThat(result.get(2).getDisplayOrder()).isEqualTo(3);
        assertThat(result.get(2).getPointsMultiplier()).isEqualTo(2.0);
        verify(difficultyLevelRepository).findAllByOrderByDisplayOrderAsc();
    }

    @Test
    @DisplayName("Should return empty list when no difficulty levels exist")
    void shouldReturnEmptyListWhenNoDifficultyLevelsExist() {
        // Given
        when(difficultyLevelRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(Collections.emptyList());

        // When
        List<DifficultyLevelDTO> result = difficultyLevelService.getAllDifficultyLevels();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(difficultyLevelRepository).findAllByOrderByDisplayOrderAsc();
    }

    @Test
    @DisplayName("Should get difficulty level by ID successfully")
    void shouldGetDifficultyLevelByIdSuccessfully() {
        // Given
        when(difficultyLevelRepository.findById(1L)).thenReturn(Optional.of(easyLevel));

        // When
        DifficultyLevelDTO result = difficultyLevelService.getDifficultyLevelById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Easy");
        assertThat(result.getDescription()).isEqualTo("Easy questions for beginners");
        assertThat(result.getDisplayOrder()).isEqualTo(1);
        assertThat(result.getPointsMultiplier()).isEqualTo(1.0);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(difficultyLevelRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when difficulty level not found by ID")
    void shouldThrowExceptionWhenDifficultyLevelNotFoundById() {
        // Given
        when(difficultyLevelRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> difficultyLevelService.getDifficultyLevelById(999L))
                .isInstanceOf(QuestionNotFoundException.class)
                .hasMessageContaining("Difficulty level not found with ID: 999");
        verify(difficultyLevelRepository).findById(999L);
    }

    @Test
    @DisplayName("Should correctly map all entity fields to DTO")
    void shouldCorrectlyMapAllEntityFieldsToDTO() {
        // Given
        when(difficultyLevelRepository.findById(2L)).thenReturn(Optional.of(mediumLevel));

        // When
        DifficultyLevelDTO result = difficultyLevelService.getDifficultyLevelById(2L);

        // Then
        assertThat(result.getId()).isEqualTo(mediumLevel.getId());
        assertThat(result.getName()).isEqualTo(mediumLevel.getName());
        assertThat(result.getDescription()).isEqualTo(mediumLevel.getDescription());
        assertThat(result.getDisplayOrder()).isEqualTo(mediumLevel.getDisplayOrder());
        assertThat(result.getPointsMultiplier()).isEqualTo(mediumLevel.getPointsMultiplier());
        assertThat(result.getCreatedAt()).isEqualTo(mediumLevel.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(mediumLevel.getUpdatedAt());
    }

    @Test
    @DisplayName("Should preserve order when getting all difficulty levels")
    void shouldPreserveOrderWhenGettingAllDifficultyLevels() {
        // Given - levels in specific order based on displayOrder
        List<DifficultyLevel> orderedLevels = Arrays.asList(easyLevel, mediumLevel, hardLevel);
        when(difficultyLevelRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(orderedLevels);

        // When
        List<DifficultyLevelDTO> result = difficultyLevelService.getAllDifficultyLevels();

        // Then - verify the order is preserved
        assertThat(result).extracting(DifficultyLevelDTO::getDisplayOrder)
                .containsExactly(1, 2, 3);
        assertThat(result).extracting(DifficultyLevelDTO::getName)
                .containsExactly("Easy", "Medium", "Hard");
    }

    @Test
    @DisplayName("Should handle difficulty level with null description")
    void shouldHandleDifficultyLevelWithNullDescription() {
        // Given
        easyLevel.setDescription(null);
        when(difficultyLevelRepository.findById(1L)).thenReturn(Optional.of(easyLevel));

        // When
        DifficultyLevelDTO result = difficultyLevelService.getDifficultyLevelById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isNull();
        assertThat(result.getName()).isEqualTo("Easy");
    }

    @Test
    @DisplayName("Should correctly handle different points multipliers")
    void shouldCorrectlyHandleDifferentPointsMultipliers() {
        // Given
        List<DifficultyLevel> levels = Arrays.asList(easyLevel, mediumLevel, hardLevel);
        when(difficultyLevelRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(levels);

        // When
        List<DifficultyLevelDTO> result = difficultyLevelService.getAllDifficultyLevels();

        // Then
        assertThat(result).extracting(DifficultyLevelDTO::getPointsMultiplier)
                .containsExactly(1.0, 1.5, 2.0);
    }
}
