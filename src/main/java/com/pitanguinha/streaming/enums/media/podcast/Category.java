package com.pitanguinha.streaming.enums.media.podcast;

/**
 * Enum representing various categories of podcasts.
 *
 * @since 1.0
 */
public enum Category {
    COMEDY("Comedy"),
    EDUCATION("Education"),
    BUSINESS("Business"),
    TECHNOLOGY("Technology"),
    HEALTH("Health"),
    NEWS("News"),
    SPORTS("Sports"),
    ARTS("Arts"),
    SCIENCE("Science"),
    SOCIETY_CULTURE("Society & Culture"),;

    public final String categoryName;

    Category(String categoryName) {
        this.categoryName = categoryName;
    }
}
