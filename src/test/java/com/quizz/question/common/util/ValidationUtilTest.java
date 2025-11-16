package com.quizz.question.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ValidationUtil Tests")
class ValidationUtilTest {

    private ValidationUtil validationUtil;

    @BeforeEach
    void setUp() {
        validationUtil = new ValidationUtil();
    }

    // isValidUrl tests
    @Test
    @DisplayName("Should return false for null URL")
    void shouldReturnFalseForNullUrl() {
        // When
        boolean result = validationUtil.isValidUrl(null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for blank URL")
    void shouldReturnFalseForBlankUrl() {
        // When
        boolean result = validationUtil.isValidUrl("   ");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return true for valid HTTP URL")
    void shouldReturnTrueForValidHttpUrl() {
        // When
        boolean result = validationUtil.isValidUrl("http://example.com/image.jpg");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return true for valid HTTPS URL")
    void shouldReturnTrueForValidHttpsUrl() {
        // When
        boolean result = validationUtil.isValidUrl("https://example.com/image.jpg");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false for URL without protocol")
    void shouldReturnFalseForUrlWithoutProtocol() {
        // When
        boolean result = validationUtil.isValidUrl("example.com");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for invalid URL format")
    void shouldReturnFalseForInvalidUrlFormat() {
        // When
        boolean result = validationUtil.isValidUrl("not a valid url");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for malformed URL")
    void shouldReturnFalseForMalformedUrl() {
        // When
        boolean result = validationUtil.isValidUrl("http://");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should validate URL with path")
    void shouldValidateUrlWithPath() {
        // When
        boolean result = validationUtil.isValidUrl("https://example.com/path/to/resource");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should validate URL with simple path")
    void shouldValidateUrlWithSimplePath() {
        // When
        boolean result = validationUtil.isValidUrl("https://example.com/path");

        // Then
        assertThat(result).isTrue();
    }

    // isAlphanumericWithPunctuation tests
    @Test
    @DisplayName("Should return false for null input")
    void shouldReturnFalseForNullInput() {
        // When
        boolean result = validationUtil.isAlphanumericWithPunctuation(null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for blank input")
    void shouldReturnFalseForBlankInput() {
        // When
        boolean result = validationUtil.isAlphanumericWithPunctuation("   ");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return true for alphanumeric text")
    void shouldReturnTrueForAlphanumericText() {
        // When
        boolean result = validationUtil.isAlphanumericWithPunctuation("Java123");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return true for text with allowed punctuation")
    void shouldReturnTrueForTextWithAllowedPunctuation() {
        // When
        boolean result = validationUtil.isAlphanumericWithPunctuation("C & C-Programming_101");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false for text with special characters")
    void shouldReturnFalseForTextWithSpecialCharacters() {
        // When
        boolean result = validationUtil.isAlphanumericWithPunctuation("Java@Programming!");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return true for text with spaces")
    void shouldReturnTrueForTextWithSpaces() {
        // When
        boolean result = validationUtil.isAlphanumericWithPunctuation("Java Programming");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false for text with HTML tags")
    void shouldReturnFalseForTextWithHtmlTags() {
        // When
        boolean result = validationUtil.isAlphanumericWithPunctuation("Java<script>alert</script>");

        // Then
        assertThat(result).isFalse();
    }

    // containsDangerousContent tests
    @Test
    @DisplayName("Should return false for null input in dangerous content check")
    void shouldReturnFalseForNullInDangerousContentCheck() {
        // When
        boolean result = validationUtil.containsDangerousContent(null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should detect script tags")
    void shouldDetectScriptTags() {
        // When
        boolean result = validationUtil.containsDangerousContent("<script>alert('XSS')</script>");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should detect script tags case insensitively")
    void shouldDetectScriptTagsCaseInsensitively() {
        // When
        boolean result = validationUtil.containsDangerousContent("<SCRIPT>alert('XSS')</SCRIPT>");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should detect javascript: protocol")
    void shouldDetectJavascriptProtocol() {
        // When
        boolean result = validationUtil.containsDangerousContent("javascript:alert('XSS')");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should detect onerror event handler")
    void shouldDetectOnerrorEventHandler() {
        // When
        boolean result = validationUtil.containsDangerousContent("<img src=x onerror=alert('XSS')>");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should detect onclick event handler")
    void shouldDetectOnclickEventHandler() {
        // When
        boolean result = validationUtil.containsDangerousContent("<div onclick=alert('XSS')>Click</div>");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should detect onload event handler")
    void shouldDetectOnloadEventHandler() {
        // When
        boolean result = validationUtil.containsDangerousContent("<body onload=alert('XSS')>");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should detect eval function")
    void shouldDetectEvalFunction() {
        // When
        boolean result = validationUtil.containsDangerousContent("eval(maliciousCode)");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should detect expression function")
    void shouldDetectExpressionFunction() {
        // When
        boolean result = validationUtil.containsDangerousContent("expression(alert('XSS'))");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false for safe content")
    void shouldReturnFalseForSafeContent() {
        // When
        boolean result = validationUtil.containsDangerousContent("This is a safe question about Java programming");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for empty string")
    void shouldReturnFalseForEmptyString() {
        // When
        boolean result = validationUtil.containsDangerousContent("");

        // Then
        assertThat(result).isFalse();
    }
}
