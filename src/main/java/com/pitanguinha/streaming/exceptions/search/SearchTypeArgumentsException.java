package com.pitanguinha.streaming.exceptions.search;

/**
 * Custom exception class for handling errors related to search type
 * arguments.
 * This exception is thrown when there are issues with the arguments provided
 * for a search type operation.
 *
 * @since 1.0
 */
public class SearchTypeArgumentsException extends IllegalArgumentException {
    private final SearchTypeArgumentsDetails details;

    public SearchTypeArgumentsException(String errorMessage) {
        super(errorMessage);
        this.details = new SearchTypeArgumentsDetails(errorMessage);
    }

    public SearchTypeArgumentsException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.details = new SearchTypeArgumentsDetails(errorMessage);
    }

    public SearchTypeArgumentsDetails getDetails() {
        return details;
    }
}
