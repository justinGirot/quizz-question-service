package com.quizz.question.security;

import com.quizz.question.common.constants.ApiConstants;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        log.debug("Processing request: {} {}", request.getMethod(), requestPath);

        try {
            String token = extractTokenFromCookie(request);

            if (token != null) {
                log.debug("JWT token found in cookie for request: {}", requestPath);
                Claims claims = jwtUtil.parseToken(token);

                if (claims != null) {
                    Long userId = jwtUtil.getUserId(claims);
                    String email = jwtUtil.getEmail(claims);
                    List<String> roles = jwtUtil.getRoles(claims);

                    if (userId != null && email != null) {
                        JwtAuthentication authentication = new JwtAuthentication(userId, email, roles);
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.info("✓ Authenticated user: {} (ID: {}, roles: {}) for {}", email, userId, roles, requestPath);
                    } else {
                        log.warn("✗ Invalid JWT claims: userId or email is null for {}", requestPath);
                    }
                } else {
                    log.warn("✗ Failed to parse JWT token for {}", requestPath);
                }
            } else {
                log.warn("✗ No JWT token found in cookies for {}", requestPath);
                logReceivedCookies(request);
            }
        } catch (Exception e) {
            log.error("✗ Error processing JWT authentication for {}: {}", requestPath, e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ApiConstants.AUTH_TOKEN_COOKIE.equals(cookie.getName())) {
                    log.debug("Found {} cookie", ApiConstants.AUTH_TOKEN_COOKIE);
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void logReceivedCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            log.warn("  → No cookies received in request");
        } else {
            String cookieNames = Arrays.stream(cookies)
                    .map(Cookie::getName)
                    .collect(Collectors.joining(", "));
            log.warn("  → Received cookies: [{}]", cookieNames);
            log.warn("  → Expected cookie: '{}'", ApiConstants.AUTH_TOKEN_COOKIE);
        }
    }
}
