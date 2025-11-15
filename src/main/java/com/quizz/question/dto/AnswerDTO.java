package com.quizz.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerDTO {

    private Long id;

    @NotBlank(message = "Answer text is required")
    private String text;

    @NotNull(message = "isCorrect is required")
    private Boolean isCorrect;

    private String imageUrl;
}
