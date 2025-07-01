package com.pitanguinha.streaming.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;

import jakarta.validation.*;

/**
 * Custom annotation for validating that a year is within a specified range.
 * The year must be between 1900 and the current year.
 * 
 * @since 1.0
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, PARAMETER })
@Constraint(validatedBy = ValidYearRangeValidator.class)
public @interface ValidYearRange {
    String message() default "Year must be between 1900 and the current year";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class ValidYearRangeValidator implements ConstraintValidator<ValidYearRange, Integer> {
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value == null || value >= 1900 && value <= java.time.LocalDate.now().getYear();
    }
}
