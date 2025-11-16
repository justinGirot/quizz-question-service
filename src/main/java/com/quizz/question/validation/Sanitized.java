package com.quizz.question.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Annotation to mark fields that should be sanitized for XSS prevention
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SanitizedValidator.class)
@Documented
public @interface Sanitized {

    String message() default "Input contains potentially dangerous content";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Type of sanitization to apply
     */
    SanitizationType type() default SanitizationType.TEXT;

    enum SanitizationType {
        TEXT,
        URL,
        CATEGORY
    }
}
