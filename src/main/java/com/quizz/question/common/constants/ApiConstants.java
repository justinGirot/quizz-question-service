package com.quizz.question.common.constants;

/**
 * API-related constants for endpoints, parameters, and headers
 */
public final class ApiConstants {

    private ApiConstants() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    // Base paths
    public static final String API_BASE_PATH = "/api";
    public static final String QUESTIONS_PATH = API_BASE_PATH + "/questions";

    // Path parameters
    public static final String ID_PATH_PARAM = "/{id}";
    public static final String CATEGORIES_PATH = "/categories";

    // Query parameters
    public static final String STATUSES_PARAM = "statuses[]";
    public static final String CATEGORIES_PARAM = "categories[]";

    // Cookie names
    public static final String AUTH_TOKEN_COOKIE = "auth_token";

    // Headers
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";

    // Response messages
    public static final String QUESTION_DELETED_MESSAGE = "Question deleted successfully";
}
