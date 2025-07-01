package com.pitanguinha.streaming.exceptions.domain;

/**
 * Record class representing details of a domain state.
 * This class is used to encapsulate information about the state of a domain
 * when an exception occurs.
 *
 * @param message The error message associated with the domain state.
 * @param stateName The name of the domain state.
 *
 * @since 1.0
 */
public record DomainStateDetails(
        String message,
        String stateName) {
}
