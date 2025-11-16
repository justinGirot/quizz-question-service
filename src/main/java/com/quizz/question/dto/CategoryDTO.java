package com.quizz.question.dto;

import com.quizz.question.model.CategoryStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for Category responses
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {

    private Long id;
    private String name;
    private String description;
    private CategoryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
}
