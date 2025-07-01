package com.pitanguinha.streaming.enums.media.contenttypes;

/**
 * Enum representing supported audio content types.
 * This enum implements the SupportedType interface and provides
 * specific content types for audio files.
 * 
 * @since 1.0
 */
public enum AudioSupportedTypes implements SupportedType {
    OPUS("audio/opus"),
    OGG("audio/ogg"),
    FLAC("audio/flac"),
    MP3("audio/mpeg"),
    WAV("audio/wav"),
    WAV_X("audio/x-wav"),
    AAC("audio/aac");

    private final String contentType;

    AudioSupportedTypes(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}
