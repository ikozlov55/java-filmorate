package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FilmRatingValidator.class)
public @interface FilmRating {
    String message() default "invalid film rating name";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}