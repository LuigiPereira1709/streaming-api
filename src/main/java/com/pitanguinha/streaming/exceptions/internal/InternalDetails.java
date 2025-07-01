package com.pitanguinha.streaming.exceptions.internal;

import java.time.Instant;

import com.pitanguinha.streaming.enums.exceptions.SeverityLevel;

/**
 * Record class representing details of an internal exception.
 *
 * @param message The error message associated with the exception.

 * @param instanceClass The class where the exception occurred.
 * @param severityLevel The severity level of the exception.
 * @param propagationTime The time when the exception was propagated.
 *
 * @since 1.0
 */
public record InternalDetails(
        String message,
        Class<?> instanceClass,
        SeverityLevel severityLevel,
        Instant propagationTime) {
}
