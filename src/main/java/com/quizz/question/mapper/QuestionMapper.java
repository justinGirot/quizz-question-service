package com.quizz.question.mapper;

import com.quizz.question.dto.AnswerDTO;
import com.quizz.question.dto.CreateQuestionRequest;
import com.quizz.question.dto.QuestionDTO;
import com.quizz.question.dto.UpdateQuestionRequest;
import com.quizz.question.model.Answer;
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

        return QuestionDTO.builder()
                .id(question.getId())
                .text(question.getText())
                .type(question.getType())
                .status(question.getStatus())
                .category(question.getCategory())
                .difficulty(question.getDifficulty())
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
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
                .points(request.getPoints())
                .createdBy(userId)
                .build();

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
        question.setCategory(request.getCategory());
        question.setDifficulty(request.getDifficulty());
        question.setPoints(request.getPoints());

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
}
