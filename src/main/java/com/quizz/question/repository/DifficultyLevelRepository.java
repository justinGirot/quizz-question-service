package com.quizz.question.repository;

import com.quizz.question.model.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for DifficultyLevel entity
 */
@Repository
public interface DifficultyLevelRepository extends JpaRepository<DifficultyLevel, Long> {

    /**
     * Find all difficulty levels ordered by display order
     */
    List<DifficultyLevel> findAllByOrderByDisplayOrderAsc();

    /**
     * Find difficulty level by name (case-insensitive)
     */
    Optional<DifficultyLevel> findByNameIgnoreCase(String name);

    /**
     * Check if difficulty level name exists (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);
}
