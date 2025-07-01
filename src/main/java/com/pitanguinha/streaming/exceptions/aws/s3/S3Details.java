package com.pitanguinha.streaming.exceptions.aws.s3;

import java.time.Instant;

import com.pitanguinha.streaming.enums.exceptions.*;

/**
 * Record class representing details of an S3 operation exception.
 *
 * @param message         The error message associated with the exception.
 * @param objectKey       The S3 object key involved in the operation.
 * @param operation       The S3 operation that caused the exception.
 * @param severityLevel   The severity level of the exception.
 * @param propagationTime The time when the exception was propagated.
 *
 * @since 1.0
 */
public record S3Details(
        String message,
        String objectKey,
        S3OperationException operation,
        SeverityLevel severityLevel,
        Instant propagationTime) {
}
