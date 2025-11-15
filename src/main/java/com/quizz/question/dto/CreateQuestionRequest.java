package com.quizz.question.dto;

import com.quizz.question.model.DifficultyLevel;
import com.quizz.question.model.QuestionType;
import com.quizz.question.validation.AtLeastOneCorrectAnswer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AtLeastOneCorrectAnswer
public class CreateQuestionRequest {

    @NotBlank(message = "Question text is required")
    @Size(min = 10, max = 1000, message = "Question text must be between 10 and 1000 characters")
    private String text;

    @NotNull(message = "Question type is required")
    private QuestionType type;

    @NotBlank(message = "Category is required")
    @Size(min = 3, max = 100, message = "Category must be between 3 and 100 characters")
    private String category;

    @NotNull(message = "Difficulty is required")
    private DifficultyLevel difficulty;

    @NotNull(message = "Points are required")
    @Min(value = 1, message = "Points must be at least 1")
    @Max(value = 100, message = "Points must not exceed 100")
    private Integer points;

    @NotNull(message = "Answers are required")
    @Size(min = 1, message = "At least one answer is required")
    @Valid
    private List<AnswerDTO> answers;
}
