package com.pitanguinha.streaming.annotation;

import java.util.*;
import java.util.stream.Collectors;

import com.pitanguinha.streaming.utils.StringUtils;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.*;

/**
 * Custom annotation for validating that a value is a valid enum constant.
 * This annotation can be applied to fields or parameters.
 * It supports case-insensitive validation and can handle both single values and
 * lists of values.
 * 
 * @since 1.0
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, PARAMETER })
@Constraint(validatedBy = EnumValidator.class)
public @interface ValidEnum {
    String message() default "Invalid value. Must be a valid value of the specified enum.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends Enum<?>> enumClass();

    boolean ignoreCase() default true;
}

/**
 * Validator for the {@link ValidEnum} annotation.
 * This class implements the validation logic to check if a given value is a
 * valid
 * enum constant of the specified enum class.
 * It supports both single string values and lists of strings.
 * 
 * @since 1.0
 */
class EnumValidator implements ConstraintValidator<ValidEnum, Object> {
    private static final String REGEX = "[-_\\s&]|AND";
    private static final String REPLACEMENT = "";

    private boolean ignoreCase;
    private Set<String> enumConstants;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        this.ignoreCase = constraintAnnotation.ignoreCase();
        Class<? extends Enum<?>> enumClass = constraintAnnotation.enumClass();
        this.enumConstants = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .map(name -> StringUtils.normalize(name, REGEX, REPLACEMENT))
                .map(name -> ignoreCase ? name.toLowerCase() : name)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null)
            return true; // let @NotNull handle null values

        return switch (value) {
            case String str -> isValidValue(str);
            case String[] strArray -> isValidArray(strArray);
            case List<?> list -> isValidCollection(list.stream().map(Object::toString).toList());
            default -> throw new IllegalArgumentException(
                    "Unsupported type for ValidEnum validation: " + value.getClass().getName());
        };
    }

    /**
     * Handles a single string value, checking if it is a valid enum constant.
     * Returns true if the string is a valid enum constant, false otherwise.
     *
     * @param value         the string value to validate
     * @param enumConstants the list of valid enum constants
     * @return true if the string is a valid enum constant, false otherwise
     * @since 1.0
     */
    private boolean isValidValue(String value) {
        String normalizedValue = StringUtils.normalize(value.toString(), REGEX, REPLACEMENT);
        normalizedValue = ignoreCase ? normalizedValue.toLowerCase() : normalizedValue;
        return enumConstants.contains(normalizedValue);
    }

    /**
     * Handles a list of strings, checking if each string is a valid enum constant.
     * Returns true if all strings are valid or the list is empty, false otherwise.
     *
     * @param values        the list of string values to validate
     * @param enumConstants the list of valid enum constants
     * @return true if all strings are valid or the list is empty, false otherwise
     * @since 1.0
     */
    private boolean isValidCollection(List<String> values) {
        if (values.isEmpty()) {
            return true; // Empty collection is valid
        }

        return values.stream()
                .filter(Objects::nonNull)
                .map(valueName -> StringUtils.normalize(valueName, REGEX, REPLACEMENT))
                .allMatch(value -> isValidValue(value));
    }

    private boolean isValidArray(String[] values) {
        if (values.length == 0)
            return true; // Empty array is valid
        return isValidCollection(Arrays.stream(values).toList());
    }
}
