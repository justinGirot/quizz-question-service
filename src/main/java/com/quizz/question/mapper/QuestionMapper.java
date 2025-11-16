package com.quizz.question.mapper;

import com.quizz.question.dto.AnswerDTO;
import com.quizz.question.dto.CategoryDTO;
import com.quizz.question.dto.CreateQuestionRequest;
import com.quizz.question.dto.DifficultyLevelDTO;
import com.quizz.question.dto.QuestionDTO;
import com.quizz.question.dto.UpdateQuestionRequest;
import com.quizz.question.model.Answer;
import com.quizz.question.model.Category;
import com.quizz.question.model.DifficultyLevel;
import com.quizz.question.model.Question;
import com.quizz.question.model.QuestionStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuestionMapper {

    public QuestionDTO toDTO(Question question) {
        if (question == null) {
            return null;
        }

        CategoryDTO categoryDTO = toCategoryDTO(question.getCategory());
        DifficultyLevelDTO difficultyLevelDTO = toDifficultyLevelDTO(question.getDifficultyLevel());

        return QuestionDTO.builder()
                .id(question.getId())
                .text(question.getText())
                .type(question.getType())
                .status(question.getStatus())
                .category(question.getCategory() != null ? question.getCategory().getName() : null)
                .categoryRef(categoryDTO)
                .difficulty(null) // Removed legacy field
                .difficultyLevel(difficultyLevelDTO)
                .points(question.getPoints())
                .answers(toAnswerDTOList(question.getAnswers()))
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .createdBy(question.getCreatedBy())
                .build();
    }

    public List<QuestionDTO> toDTOList(List<Question> questions) {
        return questions.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AnswerDTO toAnswerDTO(Answer answer) {
        if (answer == null) {
            return null;
        }

        return AnswerDTO.builder()
                .id(answer.getId())
                .text(answer.getText())
                .isCorrect(answer.isCorrect())
                .imageUrl(answer.getImageUrl())
                .build();
    }

    public List<AnswerDTO> toAnswerDTOList(List<Answer> answers) {
        if (answers == null) {
            return List.of();
        }
        return answers.stream()
                .map(this::toAnswerDTO)
                .collect(Collectors.toList());
    }

    public Question toEntity(CreateQuestionRequest request, Long userId) {
        Question question = Question.builder()
                .text(request.getText())
                .type(request.getType())
                .status(QuestionStatus.DRAFT)
                .points(request.getPoints())
                .createdBy(userId)
                .build();

        // Category and difficulty level will be set by service layer
        // Create and add answers
        if (request.getAnswers() != null) {
            request.getAnswers().forEach(answerDTO -> {
                Answer answer = Answer.builder()
                        .text(answerDTO.getText())
                        .isCorrect(answerDTO.getIsCorrect())
                        .imageUrl(answerDTO.getImageUrl())
                        .build();
                question.addAnswer(answer);
            });
        }

        return question;
    }

    public void updateEntityFromDTO(Question question, UpdateQuestionRequest request) {
        question.setText(request.getText());
        question.setType(request.getType());
        question.setStatus(request.getStatus());
        question.setPoints(request.getPoints());

        // Category and difficulty level are set by service layer via FK references
        // Update answers - clear existing and add new ones
        question.clearAnswers();

        if (request.getAnswers() != null) {
            request.getAnswers().forEach(answerDTO -> {
                Answer answer = Answer.builder()
                        .text(answerDTO.getText())
                        .isCorrect(answerDTO.getIsCorrect())
                        .imageUrl(answerDTO.getImageUrl())
                        .build();
                question.addAnswer(answer);
            });
        }
    }

    /**
     * Convert Category entity to DTO
     */
    private CategoryDTO toCategoryDTO(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .status(category.getStatus())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .createdBy(category.getCreatedBy())
                .build();
    }

    /**
     * Convert DifficultyLevel to DTO
     */
    private DifficultyLevelDTO toDifficultyLevelDTO(DifficultyLevel difficultyLevel) {
        if (difficultyLevel == null) {
            return null;
        }

        return DifficultyLevelDTO.builder()
                .id(difficultyLevel.getId())
                .name(difficultyLevel.getName())
                .description(difficultyLevel.getDescription())
                .displayOrder(difficultyLevel.getDisplayOrder())
                .pointsMultiplier(difficultyLevel.getPointsMultiplier())
                .createdAt(difficultyLevel.getCreatedAt())
                .updatedAt(difficultyLevel.getUpdatedAt())
                .build();
    }
}
