package com.quizz.question.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneCorrectAnswerValidator.class)
@Documented
public @interface AtLeastOneCorrectAnswer {

    String message() default "At least one answer must be marked as correct";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
