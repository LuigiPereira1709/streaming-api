package com.pitanguinha.streaming.exceptions.domain;

/**
 * Represents details of a "Not Found" error.
 * This record encapsulates the message and error message related to the not
 * found error.
 *
 * @since 1.0
 */
public record NotFoundDetails(
        String message,
        String errorMessage) {
}
