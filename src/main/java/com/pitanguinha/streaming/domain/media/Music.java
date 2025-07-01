package com.pitanguinha.streaming.domain.media;

import java.util.*;

import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;
import com.pitanguinha.streaming.enums.media.music.*;

import lombok.*;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.Size;

/**
 * Represents music data.
 * 
 * <p>
 * This class is a subclass of {@link Media} and represents music data.
 * </p>
 * 
 * @see Media
 * @since 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@Document(collection = "music")
@EqualsAndHashCode(callSuper = true)
public class Music extends Media {
    @NonNull
    private String artist;

    @Nullable
    @Size(min = 1)
    private List<String> feats;

    @Nullable
    private String album;

    @NonNull
    private Genre genre;

    @NonNull
    @Size(min = 1, max = 6)
    private List<Mood> moods;

    public Map<String, String> getMetadata() {
        Map<String, String> metadata = super.getMetadata();
        metadata.put("artist", this.artist);
        metadata.put("album", this.album);
        metadata.put("genre", this.genre.toString());
        metadata.put("type", "music");
        metadata.put("collection_name", "music");
        return metadata;
    }
}
