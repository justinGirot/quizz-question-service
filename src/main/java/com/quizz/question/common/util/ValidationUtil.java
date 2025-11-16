package com.quizz.question.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Utility class for additional validation logic
 */
@Component
@Slf4j
public class ValidationUtil {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9\\s\\-_&]+$"
    );

    /**
     * Validate if a string is a valid URL
     *
     * @param url the URL string to validate
     * @return true if valid URL
     */
    public boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        try {
            new URL(url);
            return URL_PATTERN.matcher(url).matches();
        } catch (MalformedURLException e) {
            log.debug("Invalid URL: {}", url);
            return false;
        }
    }

    /**
     * Validate if a string contains only alphanumeric characters and allowed punctuation
     *
     * @param input the string to validate
     * @return true if valid
     */
    public boolean isAlphanumericWithPunctuation(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }

        return ALPHANUMERIC_PATTERN.matcher(input).matches();
    }

    /**
     * Check if a string contains potentially dangerous content
     *
     * @param input the string to check
     * @return true if potentially dangerous
     */
    public boolean containsDangerousContent(String input) {
        if (input == null) {
            return false;
        }

        String lower = input.toLowerCase();
        return lower.contains("<script") ||
               lower.contains("javascript:") ||
               lower.contains("onerror=") ||
               lower.contains("onclick=") ||
               lower.contains("onload=") ||
               lower.contains("eval(") ||
               lower.contains("expression(");
    }
}
