package com.quizz.question.config;

import com.quizz.question.common.util.SanitizationUtil;
import com.quizz.question.dto.AnswerDTO;
import com.quizz.question.dto.CreateQuestionRequest;
import com.quizz.question.dto.UpdateQuestionRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor to log and sanitize incoming requests
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestSanitizationInterceptor implements HandlerInterceptor {

    private final SanitizationUtil sanitizationUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        log.debug("Incoming request: {} {}", method, uri);

        // Additional request validation can be added here
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                          ModelAndView modelAndView) {
        // Post-processing if needed
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                               Exception ex) {
        if (ex != null) {
            log.error("Request completed with error: {}", ex.getMessage());
        }
    }

    /**
     * Sanitize CreateQuestionRequest
     */
    public void sanitize(CreateQuestionRequest request) {
        if (request == null) {
            return;
        }

        request.setText(sanitizationUtil.sanitizeQuestionText(request.getText()));
        if (request.getCategoryName() != null) {
            request.setCategoryName(sanitizationUtil.sanitizeCategory(request.getCategoryName()));
        }

        if (request.getAnswers() != null) {
            request.getAnswers().forEach(this::sanitize);
        }
    }

    /**
     * Sanitize UpdateQuestionRequest
     */
    public void sanitize(UpdateQuestionRequest request) {
        if (request == null) {
            return;
        }

        request.setText(sanitizationUtil.sanitizeQuestionText(request.getText()));
        if (request.getCategoryName() != null) {
            request.setCategoryName(sanitizationUtil.sanitizeCategory(request.getCategoryName()));
        }

        if (request.getAnswers() != null) {
            request.getAnswers().forEach(this::sanitize);
        }
    }

    /**
     * Sanitize AnswerDTO
     */
    private void sanitize(AnswerDTO answer) {
        if (answer == null) {
            return;
        }

        answer.setText(sanitizationUtil.sanitizeAnswerText(answer.getText()));
        answer.setImageUrl(sanitizationUtil.sanitizeUrl(answer.getImageUrl()));
    }
}
