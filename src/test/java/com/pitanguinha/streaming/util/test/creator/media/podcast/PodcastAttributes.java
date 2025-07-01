package com.pitanguinha.streaming.util.test.creator.media.podcast;

import java.util.List;

import com.pitanguinha.streaming.enums.media.podcast.Category;

public enum PodcastAttributes {
    TITLE("PODCAST_TITLE"),
    PRESENTER("PRESENTER"),
    GUESTS(List.of("GUEST_1", "GUEST_2", "GUEST_3")),
    DESCRIPTION("DESCRIPTION"),
    CATEGORIES(List.of(Category.EDUCATION, Category.TECHNOLOGY)),
    EPISODE_NUMBER(1),
    SEASON_NUMBER(1);

    private Object value;
    private List<Object> values;

    PodcastAttributes(Object value) {
        this.value = value;
        this.values = null;
    }

    PodcastAttributes(List<Object> values) {
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
