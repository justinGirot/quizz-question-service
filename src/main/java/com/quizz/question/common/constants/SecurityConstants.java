package com.quizz.question.common.constants;

/**
 * Security and authentication-related constants
 */
public final class SecurityConstants {

    private SecurityConstants() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    // JWT Claims
    public static final String JWT_CLAIM_USER_ID = "userId";
    public static final String JWT_CLAIM_EMAIL = "sub";
    public static final String JWT_CLAIM_ROLES = "roles";
    public static final String JWT_CLAIM_EXPIRATION = "exp";
    public static final String JWT_CLAIM_ISSUED_AT = "iat";

    // Roles
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String ROLE_ADMIN_PREFIXED = ROLE_PREFIX + ROLE_ADMIN;

    // Public endpoints
    public static final String[] PUBLIC_ENDPOINTS = {
        "/actuator/**",
        "/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };

    // CORS
    public static final String[] ALLOWED_ORIGINS = {
        "http://localhost:5173",
        "http://localhost:3000"
    };

    public static final String[] ALLOWED_METHODS = {
        "GET", "POST", "PUT", "DELETE", "OPTIONS"
    };

    public static final long CORS_MAX_AGE = 3600L;
}
