package com.quizz.question.config.converter;

import com.quizz.question.model.QuestionType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToQuestionTypeConverter implements Converter<String, QuestionType> {

    @Override
    public QuestionType convert(String source) {
        return QuestionType.fromValue(source);
    }
}
