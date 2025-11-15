package com.quizz.question.repository;

import com.quizz.question.model.Question;
import com.quizz.question.model.QuestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Find all questions with optional filtering by statuses and categories
     */
    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.answers " +
           "WHERE (:statuses IS NULL OR q.status IN :statuses) " +
           "AND (:categories IS NULL OR q.category IN :categories) " +
           "ORDER BY q.createdAt DESC")
    List<Question> findByFilters(
        @Param("statuses") List<QuestionStatus> statuses,
        @Param("categories") List<String> categories
    );

    /**
     * Find all questions by creator (user ID)
     */
    List<Question> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    /**
     * Get all unique categories
     */
    @Query("SELECT DISTINCT q.category FROM Question q ORDER BY q.category")
    List<String> findAllCategories();

    /**
     * Check if a question exists and belongs to a specific user
     */
    boolean existsByIdAndCreatedBy(Long id, Long createdBy);
}
