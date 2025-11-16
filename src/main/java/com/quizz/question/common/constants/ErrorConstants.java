package com.quizz.question.common.constants;

/**
 * Error messages and exception-related constants
 */
public final class ErrorConstants {

    private ErrorConstants() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    // Question errors
    public static final String QUESTION_NOT_FOUND = "Question not found with ID: %d";
    public static final String QUESTION_NOT_FOUND_GENERIC = "Question not found";

    // Authorization errors
    public static final String FORBIDDEN_UPDATE = "You don't have permission to update this question";
    public static final String FORBIDDEN_DELETE = "You don't have permission to delete this question";
    public static final String UNAUTHORIZED_ACCESS = "Authentication required to access this resource";

    // Validation errors
    public static final String ONLY_DRAFT_EDITABLE = "Only DRAFT questions can be fully edited. For other statuses, only status changes are allowed.";
    public static final String ONLY_DRAFT_DELETABLE = "Only DRAFT questions can be deleted";
    public static final String INVALID_STATUS_TRANSITION = "Invalid status transition from %s to %s";
    public static final String DRAFT_TO_PENDING_ONLY = "DRAFT questions can only be moved to PENDING by non-admin users";
    public static final String ADMIN_ONLY_VALIDATION = "Only admins can validate, reject, or archive questions";
    public static final String ADMIN_ONLY_ARCHIVE_MODIFY = "Only admins can modify archived questions";

    // Generic errors
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred";
    public static final String INVALID_INPUT = "Invalid input data provided";
}
