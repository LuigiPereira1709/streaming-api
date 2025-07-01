package com.pitanguinha.streaming.enums.media;

/**
 * Enum representing various types of search criteria for media content.
 * This enum is used to categorize different search types for music and
 * podcasts.
 *
 * @since 1.0
 */
public enum SearchType {
    // Common
    TITLE,

    // Music
    ARTIST,
    ALBUM,
    FEAT_CONTAINS,
    FEAT_IN,
    GENRE,
    MOODS_IN,
    YEAR,
    YEAR_BETWEEN,

    // Podcast
    PRESENTER,
    GUEST_CONTAINS,
    GUESTS_IN,
    CATEGORIES_IN
}
