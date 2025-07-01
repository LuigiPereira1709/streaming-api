package com.pitanguinha.streaming.util.test.creator.media.music;

import java.util.List;

import com.pitanguinha.streaming.enums.media.music.*;

/**
 * Represents the attributes of a music file.
 * 
 * <p>
 * This enum is used to create mock objects for testing purposes.
 * </p>
 * 
 * @since 1.0
 */
public enum MusicAttributes {
    TITLE("MUSIC TITLE"),
    ARTIST("ARTIST"),
    FEAT(List.of("FEAT_1", "FEAT_2")),
    ALBUM("ALBUM"),
    GENRE(Genre.ROCK),
    MOOD(List.of(Mood.HAPPY, Mood.SAD));

    private Object value;
    private List<Object> values;

    MusicAttributes(Object value) {
        this.value = value;
        this.values = null;
    }

    MusicAttributes(List<Object> values) {
        this.value = null;
        this.values = values;
    }

    public Object getValue() {
        return this.value;
    }

    public List<Object> getValues() {
        return this.values;
    }
}
