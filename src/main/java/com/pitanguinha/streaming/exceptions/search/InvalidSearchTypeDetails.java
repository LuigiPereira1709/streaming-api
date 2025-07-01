package com.pitanguinha.streaming.exceptions.search;

import com.pitanguinha.streaming.enums.media.SearchType;

/**
 * Represents details of an invalid search type error.
 * This record holds information about the error message, a more detailed error
 * message,
 * and the invalid search type that was attempted.
 *
 * @since 1.0
 */
public record InvalidSearchTypeDetails(
        String message,
        String errorMessage,
        String searchType) {

    public InvalidSearchTypeDetails(String message, String errorMessage, SearchType searchType) {
        this(message, errorMessage, searchType.name());
    }
}
