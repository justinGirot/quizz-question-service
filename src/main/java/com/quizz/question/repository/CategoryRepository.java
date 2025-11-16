package com.quizz.question.repository;

import com.quizz.question.model.Category;
import com.quizz.question.model.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find all active categories
     */
    List<Category> findByStatusOrderByNameAsc(CategoryStatus status);

    /**
     * Find category by name (case-insensitive)
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Check if category name exists (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find all public categories (groupId is null) with given status
     */
    List<Category> findByGroupIdIsNullAndStatusOrderByNameAsc(CategoryStatus status);

    /**
     * Find all categories for a specific group with given status
     */
    List<Category> findByGroupIdAndStatusOrderByNameAsc(Long groupId, CategoryStatus status);

    /**
     * Find accessible categories for a user creating a question in a group
     * Returns: public categories + group-specific categories
     */
    @Query("SELECT c FROM Category c " +
           "WHERE c.status = :status " +
           "AND (c.groupId IS NULL OR c.groupId = :groupId) " +
           "ORDER BY c.name ASC")
    List<Category> findAccessibleCategoriesForGroup(
        @Param("groupId") Long groupId,
        @Param("status") CategoryStatus status
    );

    /**
     * Find all accessible categories for a user (public categories only if no groupId)
     */
    @Query("SELECT c FROM Category c " +
           "WHERE c.status = :status " +
           "AND c.groupId IS NULL " +
           "ORDER BY c.name ASC")
    List<Category> findPublicCategories(@Param("status") CategoryStatus status);
}
