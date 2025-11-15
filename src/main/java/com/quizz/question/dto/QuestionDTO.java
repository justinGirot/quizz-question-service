package com.quizz.question.dto;

import com.quizz.question.model.DifficultyLevel;
import com.quizz.question.model.QuestionStatus;
import com.quizz.question.model.QuestionType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDTO {

    private Long id;
    private String text;
    private QuestionType type;
    private QuestionStatus status;
    private String category;
    private DifficultyLevel difficulty;
    private Integer points;
    private List<AnswerDTO> answers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
}
