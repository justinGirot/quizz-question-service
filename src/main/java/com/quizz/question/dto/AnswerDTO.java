package com.quizz.question.dto;

import com.quizz.question.common.constants.ValidationConstants;
import com.quizz.question.validation.Sanitized;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerDTO {

    private Long id;

    @NotBlank(message = ValidationConstants.ANSWER_TEXT_REQUIRED)
    @Size(max = ValidationConstants.ANSWER_TEXT_MAX_LENGTH,
          message = "Answer text must not exceed {max} characters")
    @Sanitized(type = Sanitized.SanitizationType.TEXT)
    private String text;

    @NotNull(message = ValidationConstants.ANSWER_IS_CORRECT_REQUIRED)
    private Boolean isCorrect;

    @Size(max = ValidationConstants.IMAGE_URL_MAX_LENGTH,
          message = "Image URL must not exceed {max} characters")
    @Sanitized(type = Sanitized.SanitizationType.URL)
    private String imageUrl;
}
