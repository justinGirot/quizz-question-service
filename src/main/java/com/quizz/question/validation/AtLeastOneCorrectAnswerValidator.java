package com.quizz.question.validation;

import com.quizz.question.dto.AnswerDTO;
import com.quizz.question.dto.CreateQuestionRequest;
import com.quizz.question.dto.UpdateQuestionRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class AtLeastOneCorrectAnswerValidator implements ConstraintValidator<AtLeastOneCorrectAnswer, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }

        List<AnswerDTO> answers = null;

        if (value instanceof CreateQuestionRequest request) {
            answers = request.getAnswers();
        } else if (value instanceof UpdateQuestionRequest request) {
            answers = request.getAnswers();
        }

        if (answers == null || answers.isEmpty()) {
            return true; // Let @Size handle empty list validation
        }

        return answers.stream()
                .anyMatch(answer -> answer.getIsCorrect() != null && answer.getIsCorrect());
    }
}
