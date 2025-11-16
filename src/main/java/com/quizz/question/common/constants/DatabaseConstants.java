package com.quizz.question.common.constants;

/**
 * Database-related constants for table and column names
 */
public final class DatabaseConstants {

    private DatabaseConstants() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    // Table names
    public static final String TABLE_QUESTIONS = "questions";
    public static final String TABLE_ANSWERS = "answers";

    // Column names - Questions
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DIFFICULTY = "difficulty";
    public static final String COLUMN_POINTS = "points";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";
    public static final String COLUMN_CREATED_BY = "created_by";

    // Column names - Answers
    public static final String COLUMN_IS_CORRECT = "is_correct";
    public static final String COLUMN_IMAGE_URL = "image_url";
    public static final String COLUMN_QUESTION_ID = "question_id";

    // Index names
    public static final String INDEX_QUESTION_STATUS = "idx_question_status";
    public static final String INDEX_QUESTION_CATEGORY = "idx_question_category";
    public static final String INDEX_QUESTION_CREATED_BY = "idx_question_created_by";
    public static final String INDEX_ANSWER_QUESTION_ID = "idx_answer_question_id";

    // Foreign key names
    public static final String FK_ANSWER_QUESTION = "fk_answer_question";

    // Column lengths
    public static final int LENGTH_TEXT = 1000;
    public static final int LENGTH_TYPE = 50;
    public static final int LENGTH_STATUS = 50;
    public static final int LENGTH_CATEGORY = 100;
    public static final int LENGTH_DIFFICULTY = 20;
    public static final int LENGTH_ANSWER_TEXT = 500;
    public static final int LENGTH_IMAGE_URL = 500;
}
