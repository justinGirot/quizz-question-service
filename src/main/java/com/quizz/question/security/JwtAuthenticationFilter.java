package com.quizz.question.security;

import com.quizz.question.exception.UnauthorizedException;
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
import java.util.List;

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

        try {
            String token = extractTokenFromCookie(request);

            if (token != null) {
                Claims claims = jwtUtil.parseToken(token);

                if (claims != null) {
                    Long userId = jwtUtil.getUserId(claims);
                    String email = jwtUtil.getEmail(claims);
                    List<String> roles = jwtUtil.getRoles(claims);

                    if (userId != null && email != null) {
                        JwtAuthentication authentication = new JwtAuthentication(userId, email, roles);
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("Authenticated user: {} (ID: {}, roles: {})", email, userId, roles);
                    } else {
                        log.warn("Invalid JWT claims: userId or email is null");
                    }
                } else {
                    log.warn("Failed to parse JWT token");
                }
            }
        } catch (Exception e) {
            log.error("Error processing JWT authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
