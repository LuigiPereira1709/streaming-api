package com.pitanguinha.streaming.exceptions.search;

import com.pitanguinha.streaming.enums.media.SearchType;

public class InvalidSearchTypeException extends IllegalArgumentException {
    private final InvalidSearchTypeDetails details;

    public InvalidSearchTypeException(String message, String errorMessage, SearchType searchType) {
        super(message);
        this.details = new InvalidSearchTypeDetails(message, errorMessage, searchType);
    }

    public InvalidSearchTypeException(String message, String errorMessage, SearchType searchType, Throwable cause) {
        super(message, cause);
        this.details = new InvalidSearchTypeDetails(message, errorMessage, searchType);
    }

    public InvalidSearchTypeDetails getDetails() {
        return details;
    }

}
