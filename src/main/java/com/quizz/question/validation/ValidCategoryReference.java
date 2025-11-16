package com.quizz.question.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidCategoryReferenceValidator.class)
@Documented
public @interface ValidCategoryReference {

    String message() default "Exactly one of category, categoryId, or categoryName must be provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
