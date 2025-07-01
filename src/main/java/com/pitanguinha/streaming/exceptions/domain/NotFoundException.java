package com.pitanguinha.streaming.exceptions.domain;

/**
 * Custom exception class for handling not found errors in the application.
 * This exception is thrown when a requested resource is not found.
 *
 * @since 1.0
 */
public class NotFoundException extends IllegalArgumentException {
    private final NotFoundDetails details;

    public NotFoundException(String message, String errorMessage) {
        super(message);
        this.details = new NotFoundDetails(message, errorMessage);
    }

    public NotFoundException(String message, String errorMessage, Throwable cause) {
        super(message, cause);
        this.details = new NotFoundDetails(message, errorMessage);
    }

    public NotFoundDetails getDetails() {
        return details;
    }
}
