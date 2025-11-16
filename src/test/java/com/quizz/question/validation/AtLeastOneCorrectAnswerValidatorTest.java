package com.quizz.question.validation;

import com.quizz.question.dto.AnswerDTO;
import com.quizz.question.dto.CreateQuestionRequest;
import com.quizz.question.dto.UpdateQuestionRequest;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtLeastOneCorrectAnswerValidator Unit Tests")
class AtLeastOneCorrectAnswerValidatorTest {

    private AtLeastOneCorrectAnswerValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new AtLeastOneCorrectAnswerValidator();
    }

    @Test
    @DisplayName("Should return true when null (let @NotNull handle)")
    void shouldReturnTrueWhenNull() {
        // When
        boolean result = validator.isValid(null, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return true when answers list is null (let @Size handle)")
    void shouldReturnTrueWhenAnswersListIsNull() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setAnswers(null);

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return true when answers list is empty (let @Size handle)")
    void shouldReturnTrueWhenAnswersListIsEmpty() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setAnswers(Collections.emptyList());

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return true when at least one answer is correct")
    void shouldReturnTrueWhenAtLeastOneAnswerIsCorrect() {
        // Given
        AnswerDTO correctAnswer = new AnswerDTO();
        correctAnswer.setText("Correct answer");
        correctAnswer.setIsCorrect(true);

        AnswerDTO incorrectAnswer = new AnswerDTO();
        incorrectAnswer.setText("Incorrect answer");
        incorrectAnswer.setIsCorrect(false);

        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setAnswers(Arrays.asList(correctAnswer, incorrectAnswer));

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when no answer is marked as correct")
    void shouldReturnFalseWhenNoAnswerIsMarkedAsCorrect() {
        // Given
        AnswerDTO answer1 = new AnswerDTO();
        answer1.setText("Answer 1");
        answer1.setIsCorrect(false);

        AnswerDTO answer2 = new AnswerDTO();
        answer2.setText("Answer 2");
        answer2.setIsCorrect(false);

        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setAnswers(Arrays.asList(answer1, answer2));

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when all answers have null isCorrect")
    void shouldReturnFalseWhenAllAnswersHaveNullIsCorrect() {
        // Given
        AnswerDTO answer1 = new AnswerDTO();
        answer1.setText("Answer 1");
        answer1.setIsCorrect(null);

        AnswerDTO answer2 = new AnswerDTO();
        answer2.setText("Answer 2");
        answer2.setIsCorrect(null);

        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setAnswers(Arrays.asList(answer1, answer2));

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return true when multiple answers are correct")
    void shouldReturnTrueWhenMultipleAnswersAreCorrect() {
        // Given
        AnswerDTO correctAnswer1 = new AnswerDTO();
        correctAnswer1.setText("Correct answer 1");
        correctAnswer1.setIsCorrect(true);

        AnswerDTO correctAnswer2 = new AnswerDTO();
        correctAnswer2.setText("Correct answer 2");
        correctAnswer2.setIsCorrect(true);

        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setAnswers(Arrays.asList(correctAnswer1, correctAnswer2));

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should work with UpdateQuestionRequest")
    void shouldWorkWithUpdateQuestionRequest() {
        // Given
        AnswerDTO correctAnswer = new AnswerDTO();
        correctAnswer.setText("Correct answer");
        correctAnswer.setIsCorrect(true);

        UpdateQuestionRequest request = new UpdateQuestionRequest();
        request.setAnswers(Collections.singletonList(correctAnswer));

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when UpdateQuestionRequest has no correct answers")
    void shouldReturnFalseWhenUpdateQuestionRequestHasNoCorrectAnswers() {
        // Given
        AnswerDTO incorrectAnswer = new AnswerDTO();
        incorrectAnswer.setText("Incorrect answer");
        incorrectAnswer.setIsCorrect(false);

        UpdateQuestionRequest request = new UpdateQuestionRequest();
        request.setAnswers(Collections.singletonList(incorrectAnswer));

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return true when single correct answer exists")
    void shouldReturnTrueWhenSingleCorrectAnswerExists() {
        // Given
        AnswerDTO correctAnswer = new AnswerDTO();
        correctAnswer.setText("The only correct answer");
        correctAnswer.setIsCorrect(true);

        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setAnswers(Collections.singletonList(correctAnswer));

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should handle mixed null and boolean isCorrect values")
    void shouldHandleMixedNullAndBooleanIsCorrectValues() {
        // Given
        AnswerDTO answer1 = new AnswerDTO();
        answer1.setText("Answer 1");
        answer1.setIsCorrect(null);

        AnswerDTO answer2 = new AnswerDTO();
        answer2.setText("Answer 2");
        answer2.setIsCorrect(false);

        AnswerDTO answer3 = new AnswerDTO();
        answer3.setText("Answer 3");
        answer3.setIsCorrect(true);

        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setAnswers(Arrays.asList(answer1, answer2, answer3));

        // When
        boolean result = validator.isValid(request, context);

        // Then
        assertThat(result).isTrue();
    }
}
