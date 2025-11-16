package com.quizz.question.service;

import com.quizz.question.dto.DifficultyLevelDTO;

import java.util.List;

/**
 * Service for DifficultyLevel management
 * Difficulty levels are system-defined and read-only for users
 */
public interface DifficultyLevelService {

    /**
     * Get all difficulty levels ordered by display order
     */
    List<DifficultyLevelDTO> getAllDifficultyLevels();

    /**
     * Get difficulty level by ID
     */
    DifficultyLevelDTO getDifficultyLevelById(Long id);
}
