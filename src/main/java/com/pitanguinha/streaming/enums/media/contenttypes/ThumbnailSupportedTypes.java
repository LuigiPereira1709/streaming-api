package com.pitanguinha.streaming.enums.media.contenttypes;

/**
 * Enum representing supported thumbnail content types.
 * This enum implements the SupportedType interface and provides
 * specific content types for thumbnails.
 * 
 * @since 1.0
 */
public enum ThumbnailSupportedTypes implements SupportedType {
    PNG("image/png"),
    JPEG("image/jpeg"),
    WEBP("image/webp");

    private final String contentType;

    ThumbnailSupportedTypes(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}
