package com.pitanguinha.streaming.util.test.creator.media.podcast;

import static com.pitanguinha.streaming.util.test.creator.media.podcast.PodcastAttributes.*;

import java.util.*;

import org.springframework.http.codec.multipart.FilePart;

import com.pitanguinha.streaming.dto.podcast.*;
import com.pitanguinha.streaming.enums.media.podcast.Category;
import com.pitanguinha.streaming.util.test.FilePartMock;

import static com.pitanguinha.streaming.util.test.creator.media.MediaAttributes.*;

public class PodcastDtoCreator {
    public static PodcastSuccessDto createSuccessDto() {
        return PodcastSuccessDto.builder()
                .id((String) ID.getValue())
                .title((String) TITLE.getValue())
                .thumbnailUrl((String) THUMBNAIL_URL.getValue())
                .presenter((String) PRESENTER.getValue())
                .guests(transformToStringList(GUESTS.getValues()))
                .description((String) DESCRIPTION.getValue())
                .categories(transformToStringList(CATEGORIES.getValues()))
                .episodeNumber((int) EPISODE_NUMBER.getValue())
                .seasonNumber((int) SEASON_NUMBER.getValue())
                .explicit((boolean) EXPLICIT.getValue())
                .build();
    }

    public static PodcastSuccessDto createUpdatedSuccessDto(String id) {
        return PodcastSuccessDto.builder()
                .id(id)
                .title("UPDATED")
                .thumbnailUrl("UPDATED")
                .presenter("UPDATED")
                .guests(List.of("UPDATED"))
                .description("UPDATED")
                .categories(List.of(Category.COMEDY.categoryName))
                .episodeNumber(42)
                .seasonNumber(42)
                .explicit(!(boolean) EXPLICIT.getValue())
                .build();
    }

    public static PodcastPostDto createPostDto() {
        return PodcastPostDto.builder()
                .title((String) TITLE.getValue())
                .contentFile((FilePart) CONTENT_FILE.getValue())
                .thumbnailFile((FilePart) THUMBNAIL_FILE.getValue())
                .presenter((String) PRESENTER.getValue())
                .guests(transformToStringList(GUESTS.getValues()))
                .description((String) DESCRIPTION.getValue())
                .categories(transformToStringList(CATEGORIES.getValues()))
                .episodeNumber((int) EPISODE_NUMBER.getValue())
                .seasonNumber((int) SEASON_NUMBER.getValue())
                .explicit((boolean) EXPLICIT.getValue())
                .build();
    }

    public static PodcastPutDto createPutDto(String id) {
        return PodcastPutDto.builder()
                .id(id)
                .title("UPDATED")
                .contentFile(new FilePartMock())
                .thumbnailFile(new FilePartMock())
                .presenter("UPDATED")
                .guests(List.of("UPDATED"))
                .description("UPDATED")
                .categories(List.of(Category.COMEDY.categoryName))
                .episodeNumber(42)
                .seasonNumber(42)
                .explicit(false)
                .build();
    }

    private static List<String> transformToStringList(List<?> list) {
        List<String> stringList = new ArrayList<>();
        for (Object obj : list) {
            stringList.add(convertToString(obj));
        }
        return stringList;
    }

    private static String convertToString(Object obj) {
        return obj instanceof Category
                ? ((Category) obj).categoryName
                : obj.toString();
    }
}
