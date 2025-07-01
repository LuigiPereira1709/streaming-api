package com.pitanguinha.streaming.enums.media.music;

/**
 * Enum representing various music genres.
 * 
 * @since 1.0
 */
public enum Genre {
    ROCK("Rock"),
    POP("Pop"),
    JAZZ("Jazz"),
    CLASSICAL("Classical"),
    HIP_HOP("Hip-Hop"),
    COUNTRY("Country"),
    REGGAE("Reggae"),
    BLUES("Blues"),
    ELECTRONIC("Electronic"),
    FOLK("Folk"),
    METAL("Metal"),
    PUNK("Punk"),
    R_AND_B("R&B"),
    SOUL("Soul"),
    FUNK("Funk"),
    GOSPEL("Gospel"),
    INDIE("Indie"),
    ALTERNATIVE_ROCK("Alternative Rock");

    public final String genreName;

    Genre(String genreName) {
        this.genreName = genreName;
    }
}
