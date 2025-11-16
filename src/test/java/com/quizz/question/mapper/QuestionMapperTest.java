package com.quizz.question.mapper;

import com.quizz.question.dto.AnswerDTO;
import com.quizz.question.dto.CreateQuestionRequest;
import com.quizz.question.dto.QuestionDTO;
import com.quizz.question.dto.UpdateQuestionRequest;
import com.quizz.question.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("QuestionMapper Unit Tests")
class QuestionMapperTest {

    private QuestionMapper questionMapper;

    private Question question;
    private Category category;
    private DifficultyLevel difficultyLevel;
    private Answer answer1;
    private Answer answer2;

    @BeforeEach
    void setUp() {
        questionMapper = new QuestionMapper();

        category = new Category();
        category.setId(1L);
        category.setName("Java");
        category.setDescription("Java programming questions");
        category.setStatus(CategoryStatus.ACTIVE);
        category.setCreatedBy(1L);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        difficultyLevel = new DifficultyLevel();
        difficultyLevel.setId(1L);
        difficultyLevel.setName("Easy");
        difficultyLevel.setDescription("Easy questions");
        difficultyLevel.setDisplayOrder(1);
        difficultyLevel.setPointsMultiplier(1.0);
        difficultyLevel.setCreatedAt(LocalDateTime.now());
        difficultyLevel.setUpdatedAt(LocalDateTime.now());

        answer1 = Answer.builder()
                .id(1L)
                .text("Answer 1")
                .isCorrect(true)
                .imageUrl("https://example.com/image1.png")
                .build();

        answer2 = Answer.builder()
                .id(2L)
                .text("Answer 2")
                .isCorrect(false)
                .imageUrl(null)
                .build();

        question = Question.builder()
                .id(1L)
                .text("What is Java?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .status(QuestionStatus.DRAFT)
                .points(10)
                .category(category)
                .difficultyLevel(difficultyLevel)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should convert Question to QuestionDTO")
    void shouldConvertQuestionToQuestionDTO() {
        // When
        QuestionDTO result = questionMapper.toDTO(question);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(question.getId());
        assertThat(result.getText()).isEqualTo(question.getText());
        assertThat(result.getType()).isEqualTo(question.getType());
        assertThat(result.getStatus()).isEqualTo(question.getStatus());
        assertThat(result.getPoints()).isEqualTo(question.getPoints());
        assertThat(result.getCategory()).isEqualTo("Java");
        assertThat(result.getCategoryRef()).isNotNull();
        assertThat(result.getCategoryRef().getId()).isEqualTo(1L);
        assertThat(result.getCategoryRef().getName()).isEqualTo("Java");
        assertThat(result.getDifficultyLevel()).isNotNull();
        assertThat(result.getDifficultyLevel().getId()).isEqualTo(1L);
        assertThat(result.getDifficultyLevel().getName()).isEqualTo("Easy");
        assertThat(result.getCreatedBy()).isEqualTo(1L);
        assertThat(result.getCreatedAt()).isEqualTo(question.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(question.getUpdatedAt());
    }

    @Test
    @DisplayName("Should return null when converting null Question")
    void shouldReturnNullWhenConvertingNullQuestion() {
        // When
        QuestionDTO result = questionMapper.toDTO(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle Question with null category")
    void shouldHandleQuestionWithNullCategory() {
        // Given
        question.setCategory(null);

        // When
        QuestionDTO result = questionMapper.toDTO(question);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCategory()).isNull();
        assertThat(result.getCategoryRef()).isNull();
    }

    @Test
    @DisplayName("Should handle Question with null difficulty level")
    void shouldHandleQuestionWithNullDifficultyLevel() {
        // Given
        question.setDifficultyLevel(null);

        // When
        QuestionDTO result = questionMapper.toDTO(question);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDifficultyLevel()).isNull();
    }

    @Test
    @DisplayName("Should convert Question with answers to QuestionDTO")
    void shouldConvertQuestionWithAnswersToQuestionDTO() {
        // Given
        question.addAnswer(answer1);
        question.addAnswer(answer2);

        // When
        QuestionDTO result = questionMapper.toDTO(question);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAnswers()).hasSize(2);
        assertThat(result.getAnswers().get(0).getText()).isEqualTo("Answer 1");
        assertThat(result.getAnswers().get(0).getIsCorrect()).isTrue();
        assertThat(result.getAnswers().get(0).getImageUrl()).isEqualTo("https://example.com/image1.png");
        assertThat(result.getAnswers().get(1).getText()).isEqualTo("Answer 2");
        assertThat(result.getAnswers().get(1).getIsCorrect()).isFalse();
        assertThat(result.getAnswers().get(1).getImageUrl()).isNull();
    }

    @Test
    @DisplayName("Should convert list of Questions to list of QuestionDTOs")
    void shouldConvertListOfQuestionsToListOfQuestionDTOs() {
        // Given
        Question question2 = Question.builder()
                .id(2L)
                .text("What is Python?")
                .type(QuestionType.TEXT_INPUT)
                .status(QuestionStatus.PENDING)
                .points(5)
                .build();

        List<Question> questions = Arrays.asList(question, question2);

        // When
        List<QuestionDTO> result = questionMapper.toDTOList(questions);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getText()).isEqualTo("What is Java?");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getText()).isEqualTo("What is Python?");
    }

    @Test
    @DisplayName("Should convert empty list of Questions")
    void shouldConvertEmptyListOfQuestions() {
        // When
        List<QuestionDTO> result = questionMapper.toDTOList(Collections.emptyList());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should convert Answer to AnswerDTO")
    void shouldConvertAnswerToAnswerDTO() {
        // When
        AnswerDTO result = questionMapper.toAnswerDTO(answer1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Answer 1");
        assertThat(result.getIsCorrect()).isTrue();
        assertThat(result.getImageUrl()).isEqualTo("https://example.com/image1.png");
    }

    @Test
    @DisplayName("Should return null when converting null Answer")
    void shouldReturnNullWhenConvertingNullAnswer() {
        // When
        AnswerDTO result = questionMapper.toAnswerDTO(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should convert list of Answers to list of AnswerDTOs")
    void shouldConvertListOfAnswersToListOfAnswerDTOs() {
        // Given
        List<Answer> answers = Arrays.asList(answer1, answer2);

        // When
        List<AnswerDTO> result = questionMapper.toAnswerDTOList(answers);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getText()).isEqualTo("Answer 1");
        assertThat(result.get(1).getText()).isEqualTo("Answer 2");
    }

    @Test
    @DisplayName("Should return empty list when converting null answers list")
    void shouldReturnEmptyListWhenConvertingNullAnswersList() {
        // When
        List<AnswerDTO> result = questionMapper.toAnswerDTOList(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should create Question entity from CreateQuestionRequest")
    void shouldCreateQuestionEntityFromCreateQuestionRequest() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setText("What is Spring Boot?");
        request.setType(QuestionType.MULTIPLE_CHOICE);
        request.setPoints(15);
        request.setCategoryId(1L);
        request.setDifficultyLevelId(1L);

        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setText("A framework");
        answerDTO.setIsCorrect(true);
        answerDTO.setImageUrl("https://example.com/spring.png");

        request.setAnswers(Collections.singletonList(answerDTO));

        // When
        Question result = questionMapper.toEntity(request, 2L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull(); // ID not set yet
        assertThat(result.getText()).isEqualTo("What is Spring Boot?");
        assertThat(result.getType()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
        assertThat(result.getStatus()).isEqualTo(QuestionStatus.DRAFT);
        assertThat(result.getPoints()).isEqualTo(15);
        assertThat(result.getCreatedBy()).isEqualTo(2L);
        assertThat(result.getAnswers()).hasSize(1);
        assertThat(result.getAnswers().get(0).getText()).isEqualTo("A framework");
        assertThat(result.getAnswers().get(0).isCorrect()).isTrue();
        assertThat(result.getAnswers().get(0).getImageUrl()).isEqualTo("https://example.com/spring.png");
    }

    @Test
    @DisplayName("Should create Question entity with empty answers list")
    void shouldCreateQuestionEntityWithEmptyAnswersList() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setText("Question text");
        request.setType(QuestionType.TEXT_INPUT);
        request.setPoints(5);

        // When
        Question result = questionMapper.toEntity(request, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAnswers()).isEmpty();
    }

    @Test
    @DisplayName("Should create Question entity with null answers")
    void shouldCreateQuestionEntityWithNullAnswers() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setText("Question text");
        request.setType(QuestionType.TEXT_INPUT);
        request.setPoints(5);
        request.setAnswers(null);

        // When
        Question result = questionMapper.toEntity(request, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAnswers()).isEmpty();
    }

    @Test
    @DisplayName("Should update Question entity from UpdateQuestionRequest")
    void shouldUpdateQuestionEntityFromUpdateQuestionRequest() {
        // Given
        question.addAnswer(answer1);

        UpdateQuestionRequest request = new UpdateQuestionRequest();
        request.setText("Updated question text");
        request.setType(QuestionType.TEXT_INPUT);
        request.setStatus(QuestionStatus.PENDING);
        request.setPoints(20);
        request.setCategoryId(1L);
        request.setDifficultyLevelId(1L);

        AnswerDTO newAnswer = new AnswerDTO();
        newAnswer.setText("New answer");
        newAnswer.setIsCorrect(false);

        request.setAnswers(Collections.singletonList(newAnswer));

        // When
        questionMapper.updateEntityFromDTO(question, request);

        // Then
        assertThat(question.getText()).isEqualTo("Updated question text");
        assertThat(question.getType()).isEqualTo(QuestionType.TEXT_INPUT);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.PENDING);
        assertThat(question.getPoints()).isEqualTo(20);
        assertThat(question.getAnswers()).hasSize(1);
        assertThat(question.getAnswers().get(0).getText()).isEqualTo("New answer");
        assertThat(question.getAnswers().get(0).isCorrect()).isFalse();
    }

    @Test
    @DisplayName("Should clear all answers when updating with null answers")
    void shouldClearAllAnswersWhenUpdatingWithNullAnswers() {
        // Given
        question.addAnswer(answer1);
        question.addAnswer(answer2);

        UpdateQuestionRequest request = new UpdateQuestionRequest();
        request.setText("Updated text");
        request.setType(QuestionType.MULTIPLE_CHOICE);
        request.setStatus(QuestionStatus.DRAFT);
        request.setPoints(10);
        request.setAnswers(null);

        // When
        questionMapper.updateEntityFromDTO(question, request);

        // Then
        assertThat(question.getAnswers()).isEmpty();
    }

    @Test
    @DisplayName("Should replace existing answers when updating")
    void shouldReplaceExistingAnswersWhenUpdating() {
        // Given
        question.addAnswer(answer1);
        question.addAnswer(answer2);

        UpdateQuestionRequest request = new UpdateQuestionRequest();
        request.setText("Question text");
        request.setType(QuestionType.MULTIPLE_CHOICE);
        request.setStatus(QuestionStatus.DRAFT);
        request.setPoints(10);

        AnswerDTO newAnswer1 = new AnswerDTO();
        newAnswer1.setText("Completely new answer");
        newAnswer1.setIsCorrect(true);

        request.setAnswers(Collections.singletonList(newAnswer1));

        // When
        questionMapper.updateEntityFromDTO(question, request);

        // Then
        assertThat(question.getAnswers()).hasSize(1);
        assertThat(question.getAnswers().get(0).getText()).isEqualTo("Completely new answer");
    }

    @Test
    @DisplayName("Should handle Answer without imageUrl")
    void shouldHandleAnswerWithoutImageUrl() {
        // When
        AnswerDTO result = questionMapper.toAnswerDTO(answer2);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getImageUrl()).isNull();
    }

    @Test
    @DisplayName("Should set correct question status to DRAFT when creating from request")
    void shouldSetCorrectQuestionStatusToDraftWhenCreatingFromRequest() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setText("Test question");
        request.setType(QuestionType.MULTIPLE_CHOICE);
        request.setPoints(10);

        // When
        Question result = questionMapper.toEntity(request, 1L);

        // Then
        assertThat(result.getStatus()).isEqualTo(QuestionStatus.DRAFT);
    }

    @Test
    @DisplayName("Should maintain bidirectional relationship when adding answers")
    void shouldMaintainBidirectionalRelationshipWhenAddingAnswers() {
        // Given
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setText("Test question");
        request.setType(QuestionType.MULTIPLE_CHOICE);
        request.setPoints(10);

        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setText("Answer text");
        answerDTO.setIsCorrect(true);

        request.setAnswers(Collections.singletonList(answerDTO));

        // When
        Question result = questionMapper.toEntity(request, 1L);

        // Then
        assertThat(result.getAnswers()).hasSize(1);
        assertThat(result.getAnswers().get(0).getQuestion()).isEqualTo(result);
    }
}
