package com.quizz.question.dto;

import com.quizz.question.common.constants.ValidationConstants;
import com.quizz.question.model.QuestionType;
import com.quizz.question.model.QuestionVisibility;
import com.quizz.question.validation.AtLeastOneCorrectAnswer;
import com.quizz.question.validation.Sanitized;
import com.quizz.question.validation.ValidCategoryReference;
import com.quizz.question.validation.ValidDifficultyReference;
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
@ValidCategoryReference
@ValidDifficultyReference
public class CreateQuestionRequest {

    @NotBlank(message = ValidationConstants.QUESTION_TEXT_REQUIRED)
    @Size(min = ValidationConstants.QUESTION_TEXT_MIN_LENGTH,
          max = ValidationConstants.QUESTION_TEXT_MAX_LENGTH,
          message = ValidationConstants.QUESTION_TEXT_SIZE)
    @Sanitized(type = Sanitized.SanitizationType.TEXT)
    private String text;

    @NotNull(message = ValidationConstants.TYPE_REQUIRED)
    private QuestionType type;

    // Category ID reference (for existing categories)
    private Long categoryId;

    // New approach: custom category name (for new categories not in referential)
    @Size(min = ValidationConstants.CATEGORY_MIN_LENGTH,
          max = ValidationConstants.CATEGORY_MAX_LENGTH,
          message = ValidationConstants.CATEGORY_SIZE)
    @Sanitized(type = Sanitized.SanitizationType.CATEGORY)
    private String categoryName;

    // Difficulty level ID reference
    private Long difficultyLevelId;

    @NotNull(message = ValidationConstants.POINTS_REQUIRED)
    @Min(value = ValidationConstants.POINTS_MIN_VALUE,
         message = ValidationConstants.POINTS_RANGE)
    @Max(value = ValidationConstants.POINTS_MAX_VALUE,
         message = ValidationConstants.POINTS_RANGE)
    private Integer points;

    // Group ID reference (null = public question)
    private Long groupId;

    // Question visibility (PUBLIC or PRIVATE)
    @NotNull(message = "Visibility is required")
    @Builder.Default
    private QuestionVisibility visibility = QuestionVisibility.PUBLIC;

    @NotNull(message = ValidationConstants.ANSWERS_REQUIRED)
    @Size(min = ValidationConstants.MIN_ANSWERS_REQUIRED,
          message = ValidationConstants.MIN_ANSWERS)
    @Valid
    private List<AnswerDTO> answers;
}
