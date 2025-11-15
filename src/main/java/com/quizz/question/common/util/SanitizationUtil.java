package com.quizz.question.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

/**
 * Utility class for sanitizing user input to prevent XSS attacks
 */
@Component
@Slf4j
public class SanitizationUtil {

    private static final PolicyFactory HTML_SANITIZER = new HtmlPolicyBuilder()
            .toFactory();

    /**
     * Sanitize text input by removing all HTML tags and escaping special characters
     *
     * @param input the input string to sanitize
     * @return sanitized string
     */
    public String sanitizeText(String input) {
        if (input == null) {
            return null;
        }

        // Trim whitespace
        String trimmed = input.trim();

        // Remove HTML tags using OWASP sanitizer
        String noHtml = HTML_SANITIZER.sanitize(trimmed);

        // Escape HTML entities
        String escaped = StringEscapeUtils.escapeHtml4(noHtml);

        // Remove control characters except newlines and tabs
        String cleaned = escaped.replaceAll("[\\p{Cntrl}&&[^\n\t]]", "");

        log.debug("Sanitized input: '{}' -> '{}'", input, cleaned);
        return cleaned;
    }

    /**
     * Sanitize URL input
     *
     * @param url the URL to sanitize
     * @return sanitized URL
     */
    public String sanitizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        // Trim and remove potential XSS vectors
        String trimmed = url.trim();

        // Block javascript: and data: URLs
        if (trimmed.toLowerCase().startsWith("javascript:") ||
            trimmed.toLowerCase().startsWith("data:") ||
            trimmed.toLowerCase().startsWith("vbscript:")) {
            log.warn("Blocked potentially malicious URL: {}", trimmed);
            return null;
        }

        // Escape HTML entities
        return StringEscapeUtils.escapeHtml4(trimmed);
    }

    /**
     * Sanitize category name (alphanumeric and spaces only)
     *
     * @param category the category to sanitize
     * @return sanitized category
     */
    public String sanitizeCategory(String category) {
        if (category == null) {
            return null;
        }

        String trimmed = category.trim();

        // Remove HTML tags
        String noHtml = HTML_SANITIZER.sanitize(trimmed);

        // Allow only alphanumeric characters, spaces, and common punctuation
        String cleaned = noHtml.replaceAll("[^a-zA-Z0-9\\s\\-_&]", "");

        return cleaned.trim();
    }

    /**
     * Normalize whitespace in text
     *
     * @param input the input string
     * @return normalized string
     */
    public String normalizeWhitespace(String input) {
        if (input == null) {
            return null;
        }

        // Replace multiple spaces with single space
        return input.trim().replaceAll("\\s+", " ");
    }

    /**
     * Validate and sanitize a complete question text
     *
     * @param questionText the question text
     * @return sanitized question text
     */
    public String sanitizeQuestionText(String questionText) {
        String sanitized = sanitizeText(questionText);
        return normalizeWhitespace(sanitized);
    }

    /**
     * Validate and sanitize answer text
     *
     * @param answerText the answer text
     * @return sanitized answer text
     */
    public String sanitizeAnswerText(String answerText) {
        String sanitized = sanitizeText(answerText);
        return normalizeWhitespace(sanitized);
    }
}
