package com.quizz.question.validation;

import com.quizz.question.dto.CreateQuestionRequest;
import com.quizz.question.dto.UpdateQuestionRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidCategoryReferenceValidator implements ConstraintValidator<ValidCategoryReference, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }

        Long categoryId = null;
        String categoryName = null;

        if (value instanceof CreateQuestionRequest request) {
            categoryId = request.getCategoryId();
            categoryName = request.getCategoryName();
        } else if (value instanceof UpdateQuestionRequest request) {
            categoryId = request.getCategoryId();
            categoryName = request.getCategoryName();
        } else {
            return true; // Not a supported type
        }

        // Count how many category fields are provided
        int providedCount = 0;
        if (categoryId != null) {
            providedCount++;
        }
        if (categoryName != null && !categoryName.isBlank()) {
            providedCount++;
        }

        // Exactly one must be provided
        if (providedCount != 1) {
            context.disableDefaultConstraintViolation();
            if (providedCount == 0) {
                context.buildConstraintViolationWithTemplate(
                    "Either categoryId or categoryName must be provided"
                ).addConstraintViolation();
            } else {
                context.buildConstraintViolationWithTemplate(
                    "Only one of categoryId or categoryName can be provided"
                ).addConstraintViolation();
            }
            return false;
        }

        return true;
    }
}
