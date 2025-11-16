package com.quizz.question.config.converter;

import com.quizz.question.model.QuestionStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToQuestionStatusConverter implements Converter<String, QuestionStatus> {

    @Override
    public QuestionStatus convert(String source) {
        return QuestionStatus.fromValue(source);
    }
}
