package com.quizz.question.dto;

import com.quizz.question.model.DifficultyLevel;
import com.quizz.question.model.QuestionStatus;
import com.quizz.question.model.QuestionType;
import com.quizz.question.model.QuestionVisibility;
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

    // Legacy category field (kept for backward compatibility)
    private String category;

    // New category object reference
    private CategoryDTO categoryRef;

    // Legacy difficulty field (kept for backward compatibility)
    private DifficultyLevel difficulty;

    // New difficulty level object reference
    private DifficultyLevelDTO difficultyLevel;

    private Integer points;

    // Group support
    private Long groupId;
    private String groupName;  // Denormalized for display
    private QuestionVisibility visibility;

    private List<AnswerDTO> answers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
}
