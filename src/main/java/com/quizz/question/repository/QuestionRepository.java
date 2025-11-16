package com.quizz.question.repository;

import com.quizz.question.model.Question;
import com.quizz.question.model.QuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Find all questions with optional filtering by statuses and categories
     * Uses JOIN FETCH to avoid N+1 queries
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.answers " +
           "LEFT JOIN FETCH q.category " +
           "LEFT JOIN FETCH q.difficultyLevel " +
           "WHERE (:statuses IS NULL OR q.status IN :statuses) " +
           "AND (:categoryIds IS NULL OR q.category.id IN :categoryIds) " +
           "ORDER BY q.createdAt DESC")
    List<Question> findByFilters(
        @Param("statuses") List<QuestionStatus> statuses,
        @Param("categoryIds") List<Long> categoryIds
    );

    /**
     * Find questions with pagination and optional filtering
     * Note: Cannot use JOIN FETCH with pagination, so this may cause N+1 queries
     * Consider using @EntityGraph as an alternative
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "WHERE (:statuses IS NULL OR q.status IN :statuses) " +
           "AND (:categoryIds IS NULL OR q.category.id IN :categoryIds)")
    Page<Question> findByFiltersPageable(
        @Param("statuses") List<QuestionStatus> statuses,
        @Param("categoryIds") List<Long> categoryIds,
        Pageable pageable
    );

    /**
     * Find all questions by creator (user ID)
     * Uses JOIN FETCH to avoid N+1 queries
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.answers " +
           "LEFT JOIN FETCH q.category " +
           "LEFT JOIN FETCH q.difficultyLevel " +
           "WHERE q.createdBy = :createdBy " +
           "ORDER BY q.createdAt DESC")
    List<Question> findByCreatedByOrderByCreatedAtDesc(@Param("createdBy") Long createdBy);

    /**
     * Check if a question exists and belongs to a specific user
     */
    boolean existsByIdAndCreatedBy(Long id, Long createdBy);

    /**
     * Check if any questions reference a specific category
     */
    boolean existsByCategory_Id(Long categoryId);
}
