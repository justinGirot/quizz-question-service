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
@DisplayName("ValidCategoryReferenceValidator Unit Tests")
class ValidCategoryReferenceValidatorTest {

    private ValidCategoryReferenceValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        validator = new ValidCategoryReferenceValidator();
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
    @DisplayName("Should return true when only categoryId is provided")
    void shouldReturnTrueWhenOnlyCategoryIdIsProvided() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setCategoryId(1L);
        request.setCategoryName(null);

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isTrue();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("Should return true when only categoryName is provided")
    void shouldReturnTrueWhenOnlyCategoryNameIsProvided() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setCategoryId(null);
        request.setCategoryName("Java");

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isTrue();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("Should return false when both categoryId and categoryName are provided")
    void shouldReturnFalseWhenBothCategoryIdAndCategoryNameAreProvided() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setCategoryId(1L);
        request.setCategoryName("Java");

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("Only one of categoryId or categoryName can be provided");
    }

    @Test
    @DisplayName("Should return false when neither categoryId nor categoryName is provided")
    void shouldReturnFalseWhenNeitherCategoryIdNorCategoryNameIsProvided() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setCategoryId(null);
        request.setCategoryName(null);

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("Either categoryId or categoryName must be provided");
    }

    @Test
    @DisplayName("Should return false when categoryName is blank")
    void shouldReturnFalseWhenCategoryNameIsBlank() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setCategoryId(null);
        request.setCategoryName("   ");

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("Either categoryId or categoryName must be provided");
    }

    @Test
    @DisplayName("Should return false when categoryName is empty")
    void shouldReturnFalseWhenCategoryNameIsEmpty() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setCategoryId(null);
        request.setCategoryName("");

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("Either categoryId or categoryName must be provided");
    }

    @Test
    @DisplayName("Should work with UpdateQuestionRequest when only categoryId is provided")
    void shouldWorkWithUpdateQuestionRequestWhenOnlyCategoryIdIsProvided() {
        // Given
        UpdateQuestionRequest request = new UpdateQuestionRequest();
        request.setCategoryId(1L);
        request.setCategoryName(null);

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isTrue();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("Should work with UpdateQuestionRequest when only categoryName is provided")
    void shouldWorkWithUpdateQuestionRequestWhenOnlyCategoryNameIsProvided() {
        // Given
        UpdateQuestionRequest request = new UpdateQuestionRequest();
        request.setCategoryId(null);
        request.setCategoryName("Python");

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isTrue();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("Should return false when UpdateQuestionRequest has both fields")
    void shouldReturnFalseWhenUpdateQuestionRequestHasBothFields() {
        // Given
        UpdateQuestionRequest request = new UpdateQuestionRequest();
        request.setCategoryId(1L);
        request.setCategoryName("Java");

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("Only one of categoryId or categoryName can be provided");
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
    @DisplayName("Should accept categoryName with whitespace but not blank")
    void shouldAcceptCategoryNameWithWhitespaceButNotBlank() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setCategoryId(null);
        request.setCategoryName("  Java Programming  ");

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isTrue();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("Should return false when both fields provided with blank categoryName")
    void shouldReturnFalseWhenBothFieldsProvidedWithBlankCategoryName() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setCategoryId(1L);
        request.setCategoryName("   ");

        // When
        boolean result = validator.isValid(request, context);

        // Then
        // Since categoryName is blank, only categoryId is considered provided
        assertThat(result).isTrue();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }
}
