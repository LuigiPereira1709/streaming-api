package com.pitanguinha.streaming.exceptions.aws.s3;

import java.time.Instant;

import com.pitanguinha.streaming.enums.exceptions.*;

/**
 * Exception class for handling errors related to AWS S3 operations.
 * 
 * <p>
 * This exception is thrown when there are issues with S3 operations such as
 * uploading, downloading, or deleting objects.
 * </p>
 * 
 * @since 1.0
 */
public class S3Exception extends RuntimeException {
    private final S3Details details;

    public S3Exception(String message, String objectKey, S3OperationException operation, SeverityLevel severityLevel) {
        super(message);
        this.details = new S3Details(message, objectKey, operation, severityLevel, Instant.now());
    }

    public S3Exception(String message, String objectKey, S3OperationException operation, SeverityLevel severityLevel,
            Throwable cause) {
        super(message, cause);
        this.details = new S3Details(message, objectKey, operation, severityLevel, Instant.now());
    }

    public S3Details getDetails() {
        return details;
    }

}
