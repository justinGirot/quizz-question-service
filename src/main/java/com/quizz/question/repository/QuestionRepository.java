package com.quizz.question.repository;

import com.quizz.question.model.Question;
import com.quizz.question.model.QuestionStatus;
import com.quizz.question.model.QuestionVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
     * Uses @EntityGraph to eagerly fetch associations and avoid N+1 queries
     */
    @EntityGraph(attributePaths = {"answers", "category", "difficultyLevel"})
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

    /**
     * Find all questions by group ID and status
     */
    List<Question> findByGroupIdAndStatus(Long groupId, QuestionStatus status);

    /**
     * Find all public questions (groupId is null) with given status
     */
    List<Question> findByGroupIdIsNullAndStatus(QuestionStatus status);

    /**
     * Find questions by group ID, visibility, and status
     */
    List<Question> findByGroupIdAndVisibilityAndStatus(
        Long groupId,
        QuestionVisibility visibility,
        QuestionStatus status
    );

    /**
     * Find questions accessible by user (public questions + user's group questions)
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "WHERE q.status = :status " +
           "AND (q.groupId IS NULL " +
           "OR q.groupId IN :groupIds " +
           "OR q.visibility = 'PUBLIC') " +
           "ORDER BY q.createdAt DESC")
    List<Question> findAccessibleQuestions(
        @Param("status") QuestionStatus status,
        @Param("groupIds") List<Long> groupIds
    );

    /**
     * Count questions by group
     */
    Long countByGroupId(Long groupId);

    /**
     * Check if question belongs to group
     */
    boolean existsByIdAndGroupId(Long questionId, Long groupId);
}
