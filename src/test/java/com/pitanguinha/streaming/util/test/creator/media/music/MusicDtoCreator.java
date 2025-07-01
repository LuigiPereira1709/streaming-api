package com.pitanguinha.streaming.util.test.creator.media.music;

import java.util.*;

import org.springframework.http.codec.multipart.FilePart;

import com.pitanguinha.streaming.dto.music.*;
import com.pitanguinha.streaming.enums.media.music.*;
import com.pitanguinha.streaming.util.test.FilePartMock;

import static com.pitanguinha.streaming.util.test.creator.media.MediaAttributes.*;
import static com.pitanguinha.streaming.util.test.creator.media.music.MusicAttributes.*;

/**
 * Creates a Music Transfer Object for testing purposes.
 * 
 * @since 1.0
 */
public class MusicDtoCreator {
    public static MusicSuccessDto createSuccessDto() {
        return MusicSuccessDto.builder()
                .id((String) ID.getValue())
                .title((String) TITLE.getValue())
                .thumbnailUrl((String) THUMBNAIL_URL.getValue())
                .artist((String) ARTIST.getValue())
                .feats(transformToStringList(FEAT.getValues()))
                .album((String) ALBUM.getValue())
                .genre(convertToString(GENRE.getValue()))
                .year((int) YEAR.getValue())
                .moods(transformToStringList(MOOD.getValues()))
                .explicit((boolean) EXPLICIT.getValue())
                .build();
    }

    public static MusicSuccessDto createUpdatedDto(String id) {
        return MusicSuccessDto.builder()
                .id(id)
                .title("UPDATED")
                .thumbnailUrl("UPDATED")
                .artist("UPDATED")
                .feats(List.of("UPDATED"))
                .album("UPDATED")
                .genre(Genre.JAZZ.genreName)
                .year((int) YEAR.getValue())
                .moods(List.of(Mood.MYSTERIOUS.name()))
                .explicit((boolean) EXPLICIT.getValue())
                .build();
    }

    public static MusicPostDto createPostDto() {
        return MusicPostDto.builder()
                .title((String) TITLE.getValue())
                .thumbnailFile((FilePart) THUMBNAIL_FILE.getValue())
                .contentFile((FilePart) CONTENT_FILE.getValue())
                .artist((String) ARTIST.getValue())
                .feats(transformToStringList(FEAT.getValues()))
                .album((String) ALBUM.getValue())
                .genre(convertToString(GENRE.getValue()))
                .year((int) YEAR.getValue())
                .moods(transformToStringList(MOOD.getValues()))
                .explicit((boolean) EXPLICIT.getValue())
                .build();
    }

    public static MusicPutDto createPutDto(String id) {
        return MusicPutDto.builder()
                .id(id)
                .title("UPDATED")
                .thumbnailFile(new FilePartMock())
                .contentFile(new FilePartMock())
                .artist("UPDATED")
                .feats(List.of("UPDATED"))
                .album("UPDATED")
                .genre(Genre.JAZZ.genreName)
                .moods(List.of(Mood.MYSTERIOUS.name()))
                .explicit(!(boolean) EXPLICIT.getValue())
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
        return obj instanceof Enum<?>
                ? obj instanceof Mood
                        ? ((Mood) obj).name()
                        : ((Genre) obj).genreName
                : obj.toString();
    }
}
