package com.quizz.question.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SanitizationUtil Tests")
class SanitizationUtilTest {

    private SanitizationUtil sanitizationUtil;

    @BeforeEach
    void setUp() {
        sanitizationUtil = new SanitizationUtil();
    }

    // sanitizeText tests
    @Test
    @DisplayName("Should return null when sanitizing null text")
    void shouldReturnNullWhenSanitizingNullText() {
        // When
        String result = sanitizationUtil.sanitizeText(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should sanitize text with HTML tags")
    void shouldSanitizeTextWithHtmlTags() {
        // Given
        String input = "Hello <script>alert('XSS')</script> World";

        // When
        String result = sanitizationUtil.sanitizeText(input);

        // Then
        assertThat(result).doesNotContain("<script>");
        assertThat(result).doesNotContain("alert");
    }

    @Test
    @DisplayName("Should escape special HTML characters")
    void shouldEscapeSpecialHtmlCharacters() {
        // Given
        String input = "Test & <text> with \"quotes\"";

        // When
        String result = sanitizationUtil.sanitizeText(input);

        // Then
        assertThat(result).contains("&amp;");
        // Double escaping occurs due to OWASP sanitizer + StringEscapeUtils
    }

    @Test
    @DisplayName("Should trim whitespace from text")
    void shouldTrimWhitespaceFromText() {
        // Given
        String input = "  Text with spaces  ";

        // When
        String result = sanitizationUtil.sanitizeText(input);

        // Then
        assertThat(result).isEqualTo("Text with spaces");
    }

    @Test
    @DisplayName("Should remove control characters except newlines and tabs")
    void shouldRemoveControlCharacters() {
        // Given
        String input = "Text\u0000with\u0001control\u0002chars";

        // When
        String result = sanitizationUtil.sanitizeText(input);

        // Then
        assertThat(result).isEqualTo("Textwithcontrolchars");
    }

    @Test
    @DisplayName("Should preserve newlines and tabs")
    void shouldPreserveNewlinesAndTabs() {
        // Given
        String input = "Text\nwith\tnewline\tand\ttabs";

        // When
        String result = sanitizationUtil.sanitizeText(input);

        // Then
        assertThat(result).contains("\n");
        assertThat(result).contains("\t");
    }

    // sanitizeUrl tests
    @Test
    @DisplayName("Should return null for null URL")
    void shouldReturnNullForNullUrl() {
        // When
        String result = sanitizationUtil.sanitizeUrl(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null for blank URL")
    void shouldReturnNullForBlankUrl() {
        // When
        String result = sanitizationUtil.sanitizeUrl("   ");

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should block javascript: URLs")
    void shouldBlockJavascriptUrls() {
        // Given
        String input = "javascript:alert('XSS')";

        // When
        String result = sanitizationUtil.sanitizeUrl(input);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should block data: URLs")
    void shouldBlockDataUrls() {
        // Given
        String input = "data:text/html,<script>alert('XSS')</script>";

        // When
        String result = sanitizationUtil.sanitizeUrl(input);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should block vbscript: URLs")
    void shouldBlockVbscriptUrls() {
        // Given
        String input = "vbscript:msgbox('XSS')";

        // When
        String result = sanitizationUtil.sanitizeUrl(input);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should block javascript: URLs regardless of case")
    void shouldBlockJavascriptUrlsCaseInsensitive() {
        // Given
        String input = "JaVaScRiPt:alert('XSS')";

        // When
        String result = sanitizationUtil.sanitizeUrl(input);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should sanitize valid HTTP URL")
    void shouldSanitizeValidHttpUrl() {
        // Given
        String input = "https://example.com/image.jpg";

        // When
        String result = sanitizationUtil.sanitizeUrl(input);

        // Then
        assertThat(result).isEqualTo("https://example.com/image.jpg");
    }

    @Test
    @DisplayName("Should escape HTML entities in URL")
    void shouldEscapeHtmlEntitiesInUrl() {
        // Given
        String input = "https://example.com?param=<test>&other=value";

        // When
        String result = sanitizationUtil.sanitizeUrl(input);

        // Then
        assertThat(result).contains("&lt;");
        assertThat(result).contains("&amp;");
    }

    // sanitizeCategory tests
    @Test
    @DisplayName("Should return null for null category")
    void shouldReturnNullForNullCategory() {
        // When
        String result = sanitizationUtil.sanitizeCategory(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should sanitize category with HTML tags")
    void shouldSanitizeCategoryWithHtmlTags() {
        // Given
        String input = "Java<script>alert('XSS')</script>";

        // When
        String result = sanitizationUtil.sanitizeCategory(input);

        // Then
        assertThat(result).isEqualTo("Java");
    }

    @Test
    @DisplayName("Should allow alphanumeric and allowed punctuation in category")
    void shouldAllowAlphanumericAndPunctuation() {
        // Given
        String input = "C & C Programming-101";

        // When
        String result = sanitizationUtil.sanitizeCategory(input);

        // Then
        assertThat(result).contains("C");
        assertThat(result).contains("Programming");
    }

    @Test
    @DisplayName("Should remove special characters from category")
    void shouldRemoveSpecialCharactersFromCategory() {
        // Given
        String input = "Java Programming";

        // When
        String result = sanitizationUtil.sanitizeCategory(input);

        // Then
        assertThat(result).isEqualTo("Java Programming");
    }

    @Test
    @DisplayName("Should trim whitespace from category")
    void shouldTrimWhitespaceFromCategory() {
        // Given
        String input = "  Java Programming  ";

        // When
        String result = sanitizationUtil.sanitizeCategory(input);

        // Then
        assertThat(result).isEqualTo("Java Programming");
    }

    // normalizeWhitespace tests
    @Test
    @DisplayName("Should return null for null input in normalizeWhitespace")
    void shouldReturnNullForNullInputInNormalizeWhitespace() {
        // When
        String result = sanitizationUtil.normalizeWhitespace(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should replace multiple spaces with single space")
    void shouldReplaceMultipleSpacesWithSingleSpace() {
        // Given
        String input = "Text    with     multiple   spaces";

        // When
        String result = sanitizationUtil.normalizeWhitespace(input);

        // Then
        assertThat(result).isEqualTo("Text with multiple spaces");
    }

    @Test
    @DisplayName("Should trim leading and trailing whitespace")
    void shouldTrimLeadingAndTrailingWhitespace() {
        // Given
        String input = "   Text with spaces   ";

        // When
        String result = sanitizationUtil.normalizeWhitespace(input);

        // Then
        assertThat(result).isEqualTo("Text with spaces");
    }

    @Test
    @DisplayName("Should replace tabs and newlines with single space")
    void shouldReplaceTabsAndNewlinesWithSingleSpace() {
        // Given
        String input = "Text\twith\ttabs\nand\nnewlines";

        // When
        String result = sanitizationUtil.normalizeWhitespace(input);

        // Then
        assertThat(result).isEqualTo("Text with tabs and newlines");
    }

    // sanitizeQuestionText tests
    @Test
    @DisplayName("Should sanitize and normalize question text")
    void shouldSanitizeAndNormalizeQuestionText() {
        // Given
        String input = "What is  <b>Java</b>  programming?";

        // When
        String result = sanitizationUtil.sanitizeQuestionText(input);

        // Then
        assertThat(result).doesNotContain("<b>");
        assertThat(result).doesNotContain("  "); // Multiple spaces should be normalized
    }

    @Test
    @DisplayName("Should handle null question text")
    void shouldHandleNullQuestionText() {
        // When
        String result = sanitizationUtil.sanitizeQuestionText(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should remove XSS attempts from question text")
    void shouldRemoveXssAttemptsFromQuestionText() {
        // Given
        String input = "What is <script>alert('XSS')</script> Java?";

        // When
        String result = sanitizationUtil.sanitizeQuestionText(input);

        // Then
        assertThat(result).doesNotContain("<script>");
        assertThat(result).doesNotContain("alert");
    }

    // sanitizeAnswerText tests
    @Test
    @DisplayName("Should sanitize and normalize answer text")
    void shouldSanitizeAndNormalizeAnswerText() {
        // Given
        String input = "Java is a  <i>programming</i>  language";

        // When
        String result = sanitizationUtil.sanitizeAnswerText(input);

        // Then
        assertThat(result).doesNotContain("<i>");
        assertThat(result).doesNotContain("  "); // Multiple spaces should be normalized
    }

    @Test
    @DisplayName("Should handle null answer text")
    void shouldHandleNullAnswerText() {
        // When
        String result = sanitizationUtil.sanitizeAnswerText(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should remove XSS attempts from answer text")
    void shouldRemoveXssAttemptsFromAnswerText() {
        // Given
        String input = "True<img src=x onerror=alert('XSS')>";

        // When
        String result = sanitizationUtil.sanitizeAnswerText(input);

        // Then
        assertThat(result).doesNotContain("<img");
        assertThat(result).doesNotContain("onerror");
    }

    @Test
    @DisplayName("Should handle empty string in all methods")
    void shouldHandleEmptyString() {
        // When & Then
        assertThat(sanitizationUtil.sanitizeText("")).isEmpty();
        assertThat(sanitizationUtil.sanitizeCategory("")).isEmpty();
        assertThat(sanitizationUtil.normalizeWhitespace("")).isEmpty();
        assertThat(sanitizationUtil.sanitizeQuestionText("")).isEmpty();
        assertThat(sanitizationUtil.sanitizeAnswerText("")).isEmpty();
    }
}
