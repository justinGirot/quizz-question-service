package com.quizz.question.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for DifficultyLevel responses
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DifficultyLevelDTO {

    private Long id;
    private String name;
    private String description;
    private Integer displayOrder;
    private Double pointsMultiplier;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
