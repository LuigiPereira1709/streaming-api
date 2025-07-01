package com.pitanguinha.streaming.exceptions.aws.cloudfront;

import java.time.Instant;

import com.pitanguinha.streaming.enums.exceptions.SeverityLevel;

/**
 * Record class representing details of a CloudFront signing exception.
 * 
 * @param message         The error message associated with the exception.
 * @param url             The URL that caused the exception.
 * @param severityLevel   The severity level of the exception.
 * @param propagationTime The time when the exception was propagated.
 * 
 * @since 1.0
 */
public record CloudFrontSigningDetails(
        String message,
        String url,
        SeverityLevel severityLevel,
        Instant propagationTime) {
}
