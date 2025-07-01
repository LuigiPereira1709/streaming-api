package com.pitanguinha.streaming.exceptions.internal;

import java.time.Instant;

import com.pitanguinha.streaming.enums.exceptions.SeverityLevel;

/**
 * Exception class for handling internal errors within the application.
 * 
 * <p>
 * This exception is thrown when there are issues that do not fall under
 * specific categories but are critical to the application's operation.
 * </p>
 * 
 * @since 1.0
 */
public class InternalException extends RuntimeException {
    private final InternalDetails details;

    public InternalException(String message, Class<?> instanceClass, SeverityLevel severityLevel) {
        super(message);
        this.details = new InternalDetails(message, instanceClass, severityLevel, Instant.now());
    }

    public InternalException(String message, Class<?> instanceClass, SeverityLevel severityLevel, Throwable cause) {
        super(message, cause);
        this.details = new InternalDetails(message, instanceClass, severityLevel, Instant.now());
    }

    public InternalDetails getDetails() {
        return details;
    }

}
