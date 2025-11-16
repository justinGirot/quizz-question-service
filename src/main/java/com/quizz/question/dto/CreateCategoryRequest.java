package com.quizz.question.dto;

import com.quizz.question.common.constants.ValidationConstants;
import com.quizz.question.validation.Sanitized;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request DTO for creating a new category
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCategoryRequest {

    @NotBlank(message = ValidationConstants.CATEGORY_REQUIRED)
    @Size(min = ValidationConstants.CATEGORY_MIN_LENGTH,
          max = ValidationConstants.CATEGORY_MAX_LENGTH,
          message = ValidationConstants.CATEGORY_SIZE)
    @Sanitized(type = Sanitized.SanitizationType.CATEGORY)
    private String name;

    @Size(max = 500, message = "Description must not exceed {max} characters")
    @Sanitized(type = Sanitized.SanitizationType.TEXT)
    private String description;

    // Group ID (null = public category for all, otherwise group-specific)
    private Long groupId;
}
