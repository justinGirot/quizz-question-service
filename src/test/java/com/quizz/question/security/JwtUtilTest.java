package com.quizz.question.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private SecretKey secretKey;
    private static final String SECRET = "dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQ=";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET);
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    @DisplayName("Should parse valid JWT token successfully")
    void shouldParseValidToken() {
        // Given
        String token = createToken("user@test.com", 1L, List.of("ROLE_USER"));

        // When
        Claims claims = jwtUtil.parseToken(token);

        // Then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("Should return null for invalid token")
    void shouldReturnNullForInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        Claims claims = jwtUtil.parseToken(invalidToken);

        // Then
        assertThat(claims).isNull();
    }

    @Test
    @DisplayName("Should return null for malformed token")
    void shouldReturnNullForMalformedToken() {
        // Given
        String malformedToken = "not-a-jwt-token";

        // When
        Claims claims = jwtUtil.parseToken(malformedToken);

        // Then
        assertThat(claims).isNull();
    }

    @Test
    @DisplayName("Should return null for expired token")
    void shouldReturnNullForExpiredToken() {
        // Given - token expired 1 hour ago
        String expiredToken = createExpiredToken("user@test.com", 1L, List.of("ROLE_USER"));

        // When
        Claims claims = jwtUtil.parseToken(expiredToken);

        // Then
        assertThat(claims).isNull();
    }

    @Test
    @DisplayName("Should extract Long userId from claims")
    void shouldExtractLongUserId() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 123L);
        Claims mockClaims = Jwts.claims().add(claims).build();

        // When
        Long userId = jwtUtil.getUserId(mockClaims);

        // Then
        assertThat(userId).isEqualTo(123L);
    }

    @Test
    @DisplayName("Should extract Integer userId from claims and convert to Long")
    void shouldExtractIntegerUserId() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 123);
        Claims mockClaims = Jwts.claims().add(claims).build();

        // When
        Long userId = jwtUtil.getUserId(mockClaims);

        // Then
        assertThat(userId).isEqualTo(123L);
    }

    @Test
    @DisplayName("Should return null when userId is missing")
    void shouldReturnNullWhenUserIdMissing() {
        // Given
        Claims mockClaims = Jwts.claims().build();

        // When
        Long userId = jwtUtil.getUserId(mockClaims);

        // Then
        assertThat(userId).isNull();
    }

    @Test
    @DisplayName("Should extract email from claims")
    void shouldExtractEmail() {
        // Given
        String token = createToken("test@example.com", 1L, List.of("ROLE_USER"));
        Claims claims = jwtUtil.parseToken(token);

        // When
        String email = jwtUtil.getEmail(claims);

        // Then
        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should extract roles as List from claims")
    void shouldExtractRolesAsList() {
        // Given
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("roles", List.of("ROLE_USER", "ROLE_ADMIN"));
        Claims mockClaims = Jwts.claims().add(claimsMap).build();

        // When
        List<String> roles = jwtUtil.getRoles(mockClaims);

        // Then
        assertThat(roles).containsExactly("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should extract single role string and convert to List")
    void shouldExtractSingleRoleAsString() {
        // Given
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("roles", "ROLE_USER");
        Claims mockClaims = Jwts.claims().add(claimsMap).build();

        // When
        List<String> roles = jwtUtil.getRoles(mockClaims);

        // Then
        assertThat(roles).containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("Should return empty list when roles are missing")
    void shouldReturnEmptyListWhenRolesMissing() {
        // Given
        Claims mockClaims = Jwts.claims().build();

        // When
        List<String> roles = jwtUtil.getRoles(mockClaims);

        // Then
        assertThat(roles).isEmpty();
    }

    @Test
    @DisplayName("Should return true for admin user with ADMIN role")
    void shouldReturnTrueForAdminWithAdminRole() {
        // Given
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("roles", List.of("ROLE_USER", "ADMIN"));
        Claims mockClaims = Jwts.claims().add(claimsMap).build();

        // When
        boolean isAdmin = jwtUtil.isAdmin(mockClaims);

        // Then
        assertThat(isAdmin).isTrue();
    }

    @Test
    @DisplayName("Should return true for admin user with ROLE_ADMIN role")
    void shouldReturnTrueForAdminWithRoleAdminRole() {
        // Given
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("roles", List.of("ROLE_USER", "ROLE_ADMIN"));
        Claims mockClaims = Jwts.claims().add(claimsMap).build();

        // When
        boolean isAdmin = jwtUtil.isAdmin(mockClaims);

        // Then
        assertThat(isAdmin).isTrue();
    }

    @Test
    @DisplayName("Should return false for non-admin user")
    void shouldReturnFalseForNonAdminUser() {
        // Given
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("roles", List.of("ROLE_USER"));
        Claims mockClaims = Jwts.claims().add(claimsMap).build();

        // When
        boolean isAdmin = jwtUtil.isAdmin(mockClaims);

        // Then
        assertThat(isAdmin).isFalse();
    }

    @Test
    @DisplayName("Should return false when roles are empty")
    void shouldReturnFalseWhenRolesEmpty() {
        // Given
        Claims mockClaims = Jwts.claims().build();

        // When
        boolean isAdmin = jwtUtil.isAdmin(mockClaims);

        // Then
        assertThat(isAdmin).isFalse();
    }

    // Helper methods
    private String createToken(String email, Long userId, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", roles);

        return Jwts.builder()
                .subject(email)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .signWith(secretKey)
                .compact();
    }

    private String createExpiredToken(String email, Long userId, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", roles);

        return Jwts.builder()
                .subject(email)
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .expiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .signWith(secretKey)
                .compact();
    }
}
