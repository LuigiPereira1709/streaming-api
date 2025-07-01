package com.pitanguinha.streaming.enums.aws;

/**
 * Enum representing various content types used in HTTP requests and responses.
 * This enum is primarily used to specify the content type of media files.
 *
 * @since 1.0
 */
public enum ContentType {
    JSON("application/json");

    public String value;

    ContentType(String value) {
        this.value = value;
    }
}
