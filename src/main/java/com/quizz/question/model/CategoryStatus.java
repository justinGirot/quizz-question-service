package com.quizz.question.model;

/**
 * Status of a category
 */
public enum CategoryStatus {
    /**
     * Active category - can be used in questions
     */
    ACTIVE,

    /**
     * Inactive category - cannot be used in new questions
     */
    INACTIVE
}
