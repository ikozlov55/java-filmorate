package ru.yandex.practicum.filmorate.testdata;

import org.springframework.test.util.AssertionErrors;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.bind.MethodArgumentNotValidException;

public class Matchers {
    public static ResultMatcher validationError(String fieldName, String message) {
        return result -> {
            MethodArgumentNotValidException exception = (MethodArgumentNotValidException) result.getResolvedException();
            AssertionErrors.assertNotNull("No validation exception raised", exception);
            int errorCount = exception.getFieldErrorCount();
            String errorField = exception.getFieldError().getField();
            String errorMessage = exception.getFieldError().getDefaultMessage();
            AssertionErrors.assertEquals("Errors count", 1, errorCount);
            AssertionErrors.assertEquals("Error field name", fieldName, errorField);
            AssertionErrors.assertEquals("Error field message", message, errorMessage);
        };
    }
}
