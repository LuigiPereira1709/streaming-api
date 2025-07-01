package com.pitanguinha.streaming.exceptions.domain;

/**
 * Exception class representing an illegal state in a domain.
 * This exception is thrown when an operation is attempted that is not allowed
 * in the current state of the domain.
 *
 * @since 1.0
 */
public class DomainStateException extends IllegalStateException {
    private final DomainStateDetails details;

    public DomainStateException(String message, String stateName) {
        super(message);
        this.details = new DomainStateDetails(message, stateName);
    }

    public DomainStateException(String message, String stateName, Throwable cause) {
        super(message, cause);
        this.details = new DomainStateDetails(message, stateName);
    }

    public DomainStateDetails getDetails() {
        return details;
    }
}
