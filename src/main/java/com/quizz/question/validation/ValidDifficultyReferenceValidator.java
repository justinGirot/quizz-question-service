package com.quizz.question.validation;

import com.quizz.question.dto.CreateQuestionRequest;
import com.quizz.question.dto.UpdateQuestionRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDifficultyReferenceValidator implements ConstraintValidator<ValidDifficultyReference, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }

        Long difficultyLevelId = null;

        if (value instanceof CreateQuestionRequest request) {
            difficultyLevelId = request.getDifficultyLevelId();
        } else if (value instanceof UpdateQuestionRequest request) {
            difficultyLevelId = request.getDifficultyLevelId();
        } else {
            return true; // Not a supported type
        }

        // Difficulty level ID is required
        if (difficultyLevelId == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Difficulty level ID must be provided"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
