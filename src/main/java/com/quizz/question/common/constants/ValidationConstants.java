package com.quizz.question.common.constants;

/**
 * Validation-related constants for constraints and error messages
 */
public final class ValidationConstants {

    private ValidationConstants() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    // Question validation
    public static final int QUESTION_TEXT_MIN_LENGTH = 10;
    public static final int QUESTION_TEXT_MAX_LENGTH = 1000;
    public static final int CATEGORY_MIN_LENGTH = 3;
    public static final int CATEGORY_MAX_LENGTH = 100;
    public static final int POINTS_MIN_VALUE = 1;
    public static final int POINTS_MAX_VALUE = 100;
    public static final int MIN_ANSWERS_REQUIRED = 1;

    // Answer validation
    public static final int ANSWER_TEXT_MAX_LENGTH = 500;
    public static final int IMAGE_URL_MAX_LENGTH = 500;

    // Error messages
    public static final String QUESTION_TEXT_REQUIRED = "Question text is required";
    public static final String QUESTION_TEXT_SIZE = "Question text must be between {min} and {max} characters";
    public static final String CATEGORY_REQUIRED = "Category is required";
    public static final String CATEGORY_SIZE = "Category must be between {min} and {max} characters";
    public static final String DIFFICULTY_REQUIRED = "Difficulty is required";
    public static final String POINTS_REQUIRED = "Points are required";
    public static final String POINTS_RANGE = "Points must be between {min} and {max}";
    public static final String TYPE_REQUIRED = "Question type is required";
    public static final String STATUS_REQUIRED = "Status is required";
    public static final String ANSWERS_REQUIRED = "Answers are required";
    public static final String MIN_ANSWERS = "At least {value} answer is required";
    public static final String ANSWER_TEXT_REQUIRED = "Answer text is required";
    public static final String ANSWER_IS_CORRECT_REQUIRED = "isCorrect is required";
    public static final String AT_LEAST_ONE_CORRECT = "At least one answer must be marked as correct";

    // Regex patterns
    public static final String URL_PATTERN = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$";
    public static final String ALPHANUMERIC_PATTERN = "^[a-zA-Z0-9\\s]+$";
}
