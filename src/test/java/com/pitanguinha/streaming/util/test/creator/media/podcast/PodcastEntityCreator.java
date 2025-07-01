package com.pitanguinha.streaming.util.test.creator.media.podcast;

import static com.pitanguinha.streaming.util.test.creator.media.podcast.PodcastAttributes.*;

import java.util.*;

import com.pitanguinha.streaming.domain.media.Podcast;
import com.pitanguinha.streaming.enums.media.podcast.Category;

import static com.pitanguinha.streaming.enums.media.ConversionStatus.*;
import static com.pitanguinha.streaming.util.test.creator.media.MediaAttributes.*;

public class PodcastEntityCreator {
    public static Podcast createEntity() {
        return Podcast.builder()
                .id((String) ID.getValue())
                .title((String) TITLE.getValue())
                .thumbnailSuffix((String) THUMBNAIL_SUFFIX.getValue())
                .contentKey((String) CONTENT_KEY.getValue())
                .conversionStatus(SUCCESS)
                .presenter((String) PRESENTER.getValue())
                .guests(transformToStringList(GUESTS.getValues()))
                .description((String) DESCRIPTION.getValue())
                .categories(transformToCategories(CATEGORIES.getValues()))
                .episodeNumber((int) EPISODE_NUMBER.getValue())
                .seasonNumber((int) SEASON_NUMBER.getValue())
                .explicit((boolean) EXPLICIT.getValue())
                .build();
    }

    public static Podcast createEntityToSave() {
        var podcast = createEntity();
        podcast.setId(null);
        podcast.setConversionStatus(null);
        return podcast;
    }

    public static Podcast createEntityToUpdate(String id) {
        return Podcast.builder()
                .id(id)
                .title("UPDATED")
                .thumbnailSuffix("UPDATED")
                .contentKey("UPDATED")
                .conversionStatus(PENDING)
                .presenter("UPDATED")
                .guests(List.of("UPDATED"))
                .description("UPDATED")
                .categories(List.of(Category.COMEDY))
                .episodeNumber(42)
                .seasonNumber(42)
                .explicit(false)
                .build();
    }

    private static List<String> transformToStringList(List<?> list) {
        List<String> stringList = new ArrayList<>();
        for (Object obj : list) {
            stringList.add(obj.toString());
        }
        return stringList;
    }

    private static List<Category> transformToCategories(List<?> list) {
        List<Category> categories = new ArrayList<>();
        for (Object obj : list) {
            categories.add(Category.valueOf(obj.toString().toUpperCase()));
        }
        return categories;
    }
}
