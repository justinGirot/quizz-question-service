package com.quizz.question.validation;

import com.quizz.question.dto.CreateQuestionRequest;
import com.quizz.question.dto.UpdateQuestionRequest;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidDifficultyReferenceValidator Unit Tests")
class ValidDifficultyReferenceValidatorTest {

    private ValidDifficultyReferenceValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        validator = new ValidDifficultyReferenceValidator();
        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        lenient().when(violationBuilder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    @DisplayName("Should return true when null (let @NotNull handle)")
    void shouldReturnTrueWhenNull() {
        // When
        boolean result = validator.isValid(null, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return true when difficultyLevelId is provided")
    void shouldReturnTrueWhenDifficultyLevelIdIsProvided() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setDifficultyLevelId(1L);

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isTrue();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("Should return false when difficultyLevelId is null")
    void shouldReturnFalseWhenDifficultyLevelIdIsNull() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setDifficultyLevelId(null);

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("Difficulty level ID must be provided");
    }

    @Test
    @DisplayName("Should work with UpdateQuestionRequest when difficultyLevelId is provided")
    void shouldWorkWithUpdateQuestionRequestWhenDifficultyLevelIdIsProvided() {
        // Given
        UpdateQuestionRequest request = new UpdateQuestionRequest();
        request.setDifficultyLevelId(2L);

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isTrue();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("Should return false when UpdateQuestionRequest has null difficultyLevelId")
    void shouldReturnFalseWhenUpdateQuestionRequestHasNullDifficultyLevelId() {
        // Given
        UpdateQuestionRequest request = new UpdateQuestionRequest();
        request.setDifficultyLevelId(null);

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("Difficulty level ID must be provided");
    }

    @Test
    @DisplayName("Should return true when validating unsupported type")
    void shouldReturnTrueWhenValidatingUnsupportedType() {
        // Given
        String unsupportedType = "Not a CreateQuestionRequest or UpdateQuestionRequest";

        // When
        boolean result = validator.isValid(unsupportedType, context);

        // Then
        assertThat(result).isTrue();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("Should accept difficultyLevelId with value zero")
    void shouldAcceptDifficultyLevelIdWithValueZero() {
        // Given - testing edge case with 0 as valid ID
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setDifficultyLevelId(0L);

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isTrue();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("Should accept negative difficultyLevelId")
    void shouldAcceptNegativeDifficultyLevelId() {
        // Given - validator only checks if ID is present, not if it's valid
        // Database/service layer should validate if ID exists
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setDifficultyLevelId(-1L);

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isTrue();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("Should accept very large difficultyLevelId")
    void shouldAcceptVeryLargeDifficultyLevelId() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setDifficultyLevelId(Long.MAX_VALUE);

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isTrue();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }
}
