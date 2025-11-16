package com.quizz.question.config.converter;

import com.quizz.question.model.QuestionVisibility;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToQuestionVisibilityConverter implements Converter<String, QuestionVisibility> {

    @Override
    public QuestionVisibility convert(String source) {
        return QuestionVisibility.fromValue(source);
    }
}
