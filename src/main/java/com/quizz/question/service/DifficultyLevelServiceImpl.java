package com.quizz.question.service;

import com.quizz.question.dto.DifficultyLevelDTO;
import com.quizz.question.exception.QuestionNotFoundException;
import com.quizz.question.model.DifficultyLevel;
import com.quizz.question.repository.DifficultyLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for DifficultyLevel management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DifficultyLevelServiceImpl implements DifficultyLevelService {

    private final DifficultyLevelRepository difficultyLevelRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DifficultyLevelDTO> getAllDifficultyLevels() {
        log.info("Fetching all difficulty levels");
        return difficultyLevelRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DifficultyLevelDTO getDifficultyLevelById(Long id) {
        log.info("Fetching difficulty level by ID: {}", id);
        DifficultyLevel difficultyLevel = difficultyLevelRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException("Difficulty level not found with ID: " + id));
        return toDTO(difficultyLevel);
    }

    /**
     * Convert entity to DTO
     */
    private DifficultyLevelDTO toDTO(DifficultyLevel entity) {
        return DifficultyLevelDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .displayOrder(entity.getDisplayOrder())
                .pointsMultiplier(entity.getPointsMultiplier())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
