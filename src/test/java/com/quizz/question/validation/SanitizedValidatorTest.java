package com.quizz.question.validation;

import com.quizz.question.common.util.ValidationUtil;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SanitizedValidator Unit Tests")
class SanitizedValidatorTest {

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private ConstraintValidatorContext context;

    @InjectMocks
    private SanitizedValidator validator;

    private Sanitized annotation;

    @BeforeEach
    void setUp() {
        // Create mock annotation for TEXT type (default)
        annotation = mock(Sanitized.class);
        when(annotation.type()).thenReturn(Sanitized.SanitizationType.TEXT);
        validator.initialize(annotation);
    }

    @Test
    @DisplayName("Should return true when value is null (let @NotBlank handle)")
    void shouldReturnTrueWhenValueIsNull() {
        // When
        boolean result = validator.isValid(null, context);

        // Then
        assertThat(result).isTrue();
        verify(validationUtil, never()).containsDangerousContent(any());
    }

    @Test
    @DisplayName("Should return true when value is blank (let @NotBlank handle)")
    void shouldReturnTrueWhenValueIsBlank() {
        // When
        boolean result = validator.isValid("   ", context);

        // Then
        assertThat(result).isTrue();
        verify(validationUtil, never()).containsDangerousContent(any());
    }

    @Test
    @DisplayName("Should return true when value is empty (let @NotBlank handle)")
    void shouldReturnTrueWhenValueIsEmpty() {
        // When
        boolean result = validator.isValid("", context);

        // Then
        assertThat(result).isTrue();
        verify(validationUtil, never()).containsDangerousContent(any());
    }

    @Test
    @DisplayName("Should return true when value is safe text")
    void shouldReturnTrueWhenValueIsSafeText() {
        // Given
        String safeText = "This is a safe text with no dangerous content";
        when(validationUtil.containsDangerousContent(safeText)).thenReturn(false);

        // When
        boolean result = validator.isValid(safeText, context);

        // Then
        assertThat(result).isTrue();
        verify(validationUtil).containsDangerousContent(safeText);
    }

    @Test
    @DisplayName("Should return false when value contains script tag")
    void shouldReturnFalseWhenValueContainsScriptTag() {
        // Given
        String dangerousText = "Hello <script>alert('XSS')</script>";
        when(validationUtil.containsDangerousContent(dangerousText)).thenReturn(true);

        // When
        boolean result = validator.isValid(dangerousText, context);

        // Then
        assertThat(result).isFalse();
        verify(validationUtil).containsDangerousContent(dangerousText);
    }

    @Test
    @DisplayName("Should return false when value contains javascript protocol")
    void shouldReturnFalseWhenValueContainsJavascriptProtocol() {
        // Given
        String dangerousText = "javascript:alert('XSS')";
        when(validationUtil.containsDangerousContent(dangerousText)).thenReturn(true);

        // When
        boolean result = validator.isValid(dangerousText, context);

        // Then
        assertThat(result).isFalse();
        verify(validationUtil).containsDangerousContent(dangerousText);
    }

    @Test
    @DisplayName("Should return false when value contains onerror attribute")
    void shouldReturnFalseWhenValueContainsOnerrorAttribute() {
        // Given
        String dangerousText = "<img src='x' onerror='alert(1)'>";
        when(validationUtil.containsDangerousContent(dangerousText)).thenReturn(true);

        // When
        boolean result = validator.isValid(dangerousText, context);

        // Then
        assertThat(result).isFalse();
        verify(validationUtil).containsDangerousContent(dangerousText);
    }

    @Test
    @DisplayName("Should return true for valid URL when type is URL")
    void shouldReturnTrueForValidUrlWhenTypeIsUrl() {
        // Given
        Sanitized urlAnnotation = mock(Sanitized.class);
        when(urlAnnotation.type()).thenReturn(Sanitized.SanitizationType.URL);
        validator.initialize(urlAnnotation);

        String validUrl = "https://example.com";
        when(validationUtil.containsDangerousContent(validUrl)).thenReturn(false);
        when(validationUtil.isValidUrl(validUrl)).thenReturn(true);

        // When
        boolean result = validator.isValid(validUrl, context);

        // Then
        assertThat(result).isTrue();
        verify(validationUtil).containsDangerousContent(validUrl);
        verify(validationUtil).isValidUrl(validUrl);
    }

    @Test
    @DisplayName("Should return false for invalid URL when type is URL")
    void shouldReturnFalseForInvalidUrlWhenTypeIsUrl() {
        // Given
        Sanitized urlAnnotation = mock(Sanitized.class);
        when(urlAnnotation.type()).thenReturn(Sanitized.SanitizationType.URL);
        validator.initialize(urlAnnotation);

        String invalidUrl = "not-a-valid-url";
        when(validationUtil.containsDangerousContent(invalidUrl)).thenReturn(false);
        when(validationUtil.isValidUrl(invalidUrl)).thenReturn(false);

        // When
        boolean result = validator.isValid(invalidUrl, context);

        // Then
        assertThat(result).isFalse();
        verify(validationUtil).containsDangerousContent(invalidUrl);
        verify(validationUtil).isValidUrl(invalidUrl);
    }

    @Test
    @DisplayName("Should return false when URL contains dangerous content")
    void shouldReturnFalseWhenUrlContainsDangerousContent() {
        // Given
        Sanitized urlAnnotation = mock(Sanitized.class);
        when(urlAnnotation.type()).thenReturn(Sanitized.SanitizationType.URL);
        validator.initialize(urlAnnotation);

        String dangerousUrl = "javascript:alert('XSS')";
        when(validationUtil.containsDangerousContent(dangerousUrl)).thenReturn(true);

        // When
        boolean result = validator.isValid(dangerousUrl, context);

        // Then
        assertThat(result).isFalse();
        verify(validationUtil).containsDangerousContent(dangerousUrl);
        verify(validationUtil, never()).isValidUrl(any());
    }

    @Test
    @DisplayName("Should handle CATEGORY sanitization type")
    void shouldHandleCategorySanitizationType() {
        // Given
        Sanitized categoryAnnotation = mock(Sanitized.class);
        when(categoryAnnotation.type()).thenReturn(Sanitized.SanitizationType.CATEGORY);
        validator.initialize(categoryAnnotation);

        String safeCategory = "Java Programming";
        when(validationUtil.containsDangerousContent(safeCategory)).thenReturn(false);

        // When
        boolean result = validator.isValid(safeCategory, context);

        // Then
        assertThat(result).isTrue();
        verify(validationUtil).containsDangerousContent(safeCategory);
    }

    @Test
    @DisplayName("Should return false when category contains dangerous content")
    void shouldReturnFalseWhenCategoryContainsDangerousContent() {
        // Given
        Sanitized categoryAnnotation = mock(Sanitized.class);
        when(categoryAnnotation.type()).thenReturn(Sanitized.SanitizationType.CATEGORY);
        validator.initialize(categoryAnnotation);

        String dangerousCategory = "Java<script>alert(1)</script>";
        when(validationUtil.containsDangerousContent(dangerousCategory)).thenReturn(true);

        // When
        boolean result = validator.isValid(dangerousCategory, context);

        // Then
        assertThat(result).isFalse();
        verify(validationUtil).containsDangerousContent(dangerousCategory);
    }

    @Test
    @DisplayName("Should validate text with special characters but no dangerous content")
    void shouldValidateTextWithSpecialCharactersButNoDangerousContent() {
        // Given
        String textWithSpecialChars = "What is the difference between & and &&?";
        when(validationUtil.containsDangerousContent(textWithSpecialChars)).thenReturn(false);

        // When
        boolean result = validator.isValid(textWithSpecialChars, context);

        // Then
        assertThat(result).isTrue();
        verify(validationUtil).containsDangerousContent(textWithSpecialChars);
    }

    @Test
    @DisplayName("Should validate text with line breaks")
    void shouldValidateTextWithLineBreaks() {
        // Given
        String textWithLineBreaks = "Line 1\nLine 2\nLine 3";
        when(validationUtil.containsDangerousContent(textWithLineBreaks)).thenReturn(false);

        // When
        boolean result = validator.isValid(textWithLineBreaks, context);

        // Then
        assertThat(result).isTrue();
        verify(validationUtil).containsDangerousContent(textWithLineBreaks);
    }

    @Test
    @DisplayName("Should not validate URL format for TEXT type")
    void shouldNotValidateUrlFormatForTextType() {
        // Given
        Sanitized textAnnotation = mock(Sanitized.class);
        when(textAnnotation.type()).thenReturn(Sanitized.SanitizationType.TEXT);
        validator.initialize(textAnnotation);

        String text = "not-a-url-but-safe";
        when(validationUtil.containsDangerousContent(text)).thenReturn(false);

        // When
        boolean result = validator.isValid(text, context);

        // Then
        assertThat(result).isTrue();
        verify(validationUtil).containsDangerousContent(text);
        verify(validationUtil, never()).isValidUrl(any());
    }
}
