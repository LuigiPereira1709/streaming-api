package com.pitanguinha.streaming.util.test.creator.media.music;

import java.util.*;

import com.pitanguinha.streaming.enums.media.music.*;
import com.pitanguinha.streaming.domain.media.Music;

import static com.pitanguinha.streaming.enums.media.ConversionStatus.*;
import static com.pitanguinha.streaming.util.test.creator.media.MediaAttributes.*;
import static com.pitanguinha.streaming.util.test.creator.media.music.MusicAttributes.*;

/**
 * Creates a Music object for testing purposes.
 * 
 * @since 1.0
 */
public class MusicEntityCreator {
    public static Music createEntity() {
        return Music.builder()
                .id((String) ID.getValue())
                .title((String) TITLE.getValue())
                .thumbnailSuffix((String) THUMBNAIL_SUFFIX.getValue())
                .contentKey((String) CONTENT_KEY.getValue())
                .conversionStatus(SUCCESS)
                .artist((String) ARTIST.getValue())
                .feats(transformToStringList(FEAT.getValues()))
                .album((String) ALBUM.getValue())
                .genre((Genre) GENRE.getValue())
                .year((int) YEAR.getValue())
                .moods(transformToMoods(MOOD.getValues()))
                .explicit((boolean) EXPLICIT.getValue())
                .build();
    }

    public static Music createEntityToSave() {
        var music = createEntity();
        music.setId(null);
        music.setConversionStatus(null);
        return music;
    }

    public static Music createEntityToUpdate(String id) {
        return Music.builder()
                .id(id)
                .title("UPDATED")
                .thumbnailSuffix("UPDATED")
                .contentKey("UPDATED")
                .conversionStatus(PENDING)
                .artist("UPDATED")
                .feats(List.of("UPDATED"))
                .album("UPDATED")
                .genre(Genre.JAZZ)
                .year(0)
                .moods(List.of(Mood.MYSTERIOUS))
                .explicit(false)
                .build();
    }

    private static List<String> transformToStringList(List<?> list) {
        List<String> stringList = new ArrayList<>();
        for (Object obj : list)
            stringList.add(obj.toString());
        return stringList;
    }

    private static List<Mood> transformToMoods(List<?> list) {
        List<Mood> moods = new ArrayList<>();
        for (Object obj : list)
            moods.add((Mood) obj);
        return moods;
    }
}
