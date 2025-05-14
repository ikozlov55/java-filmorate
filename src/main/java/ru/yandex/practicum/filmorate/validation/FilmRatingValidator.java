package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class FilmRatingValidator implements ConstraintValidator<FilmRating, String> {
    public static final Set<String> RATING_NAMES = Set.of("G", "PG", "PG-13", "R", "NC-17");

    @Override
    public void initialize(FilmRating constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String string, ConstraintValidatorContext context) {
        if (string == null) {
            setError(context, "film rating is required");
            return false;
        }
        if (!RATING_NAMES.contains(string)) {
            setError(context, "film rating must be one of values: " + RATING_NAMES);
            return false;
        }
        return true;
    }

    private void setError(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

}
