package com.quizz.question.config;

import com.quizz.question.config.converter.StringToQuestionStatusConverter;
import com.quizz.question.config.converter.StringToQuestionTypeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RequestSanitizationInterceptor requestSanitizationInterceptor;
    private final StringToQuestionStatusConverter questionStatusConverter;
    private final StringToQuestionTypeConverter questionTypeConverter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestSanitizationInterceptor)
                .addPathPatterns("/api/**");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(questionStatusConverter);
        registry.addConverter(questionTypeConverter);
    }
}
