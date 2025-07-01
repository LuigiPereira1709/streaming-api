package com.pitanguinha.streaming.util.test.creator.media;

import java.time.*;

import com.pitanguinha.streaming.util.test.FilePartMock;

/**
 * Represents the attributes of a media file.
 * 
 * <p>
 * This enum is used to create mock objects for testing purposes.
 * </p>
 * 
 * @since 1.0
 */
public enum MediaAttributes {
    ID("ID"),
    THUMBNAIL_URL("THUMB_URL"),
    THUMBNAIL_SUFFIX("THUMB_SUFFIX"),
    CONTENT_KEY("CONTENT_KEY"),
    PUBLISHED_AT(Instant.now()),
    YEAR(2003),
    EXPLICIT(true),
    THUMBNAIL_FILE(new FilePartMock()),
    CONTENT_FILE(new FilePartMock());

    private final Object value;

    MediaAttributes(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
