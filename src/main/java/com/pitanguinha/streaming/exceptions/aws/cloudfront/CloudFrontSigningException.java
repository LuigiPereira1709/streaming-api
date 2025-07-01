package com.pitanguinha.streaming.exceptions.aws.cloudfront;

import java.time.Instant;

import com.pitanguinha.streaming.enums.exceptions.SeverityLevel;

/**
 * Exception class for handling errors related to AWS CloudFront signing
 * operations.
 * 
 * <p>
 * This exception is thrown when there are issues with signing URLs or cookies
 * for
 * CloudFront distributions.
 * </p>
 * 
 * @since 1.0
 */
public class CloudFrontSigningException extends RuntimeException {
    private final CloudFrontSigningDetails details;

    public CloudFrontSigningException(String message, String url,
            SeverityLevel severityLevel) {
        super(message);
        this.details = new CloudFrontSigningDetails(message, url, severityLevel, Instant.now());
    }

    public CloudFrontSigningException(String message, String url, SeverityLevel severityLevel, Throwable cause) {
        super(message, cause);
        this.details = new CloudFrontSigningDetails(message, url, severityLevel, Instant.now());
    }

    public CloudFrontSigningDetails getDetails() {
        return this.details;
    }
}
