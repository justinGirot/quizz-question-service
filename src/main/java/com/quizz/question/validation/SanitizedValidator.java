package com.quizz.question.validation;

import com.quizz.question.common.util.ValidationUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validator to check if input contains potentially dangerous content
 */
@Component
@RequiredArgsConstructor
public class SanitizedValidator implements ConstraintValidator<Sanitized, String> {

    private final ValidationUtil validationUtil;
    private Sanitized.SanitizationType sanitizationType;

    @Override
    public void initialize(Sanitized constraintAnnotation) {
        this.sanitizationType = constraintAnnotation.type();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Let @NotBlank handle null/empty validation
        }

        // Check for dangerous content
        if (validationUtil.containsDangerousContent(value)) {
            return false;
        }

        // Additional validation based on type
        if (sanitizationType == Sanitized.SanitizationType.URL && !value.isBlank()) {
            // Don't validate empty URLs, but validate non-empty ones
            return validationUtil.isValidUrl(value);
        }

        return true;
    }
}
