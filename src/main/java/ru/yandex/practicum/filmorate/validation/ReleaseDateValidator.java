package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ReleaseDateValidator implements ConstraintValidator<FilmReleaseDate, LocalDate> {
    public static final LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);

    public ReleaseDateValidator() {
    }

    @Override
    public void initialize(FilmReleaseDate constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        return localDate != null && localDate.isAfter(MIN_DATE);
    }
}
