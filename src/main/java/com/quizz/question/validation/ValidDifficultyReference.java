package com.quizz.question.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDifficultyReferenceValidator.class)
@Documented
public @interface ValidDifficultyReference {

    String message() default "Exactly one of difficulty or difficultyLevelId must be provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
