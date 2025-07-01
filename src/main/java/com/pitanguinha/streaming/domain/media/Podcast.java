package com.pitanguinha.streaming.domain.media;

import java.util.*;

import org.springframework.data.mongodb.core.mapping.*;

import com.mongodb.lang.Nullable;
import com.pitanguinha.streaming.enums.media.podcast.Category;

import lombok.*;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.*;

@Data
@SuperBuilder
@NoArgsConstructor
@Document(collection = "podcast")
@EqualsAndHashCode(callSuper = true)
public class Podcast extends Media {
    @NotNull(message = "Title cannot be null")
    @NotBlank(message = "Title cannot be blank")
    private String presenter;

    @Nullable
    @Size(min = 1)
    private List<String> guests;

    private String description;

    @Size(min = 1, max = 3)
    private List<Category> categories;

    @Field("episode_number")
    @Default
    private Integer episodeNumber = 1;

    @Field("season_number")
    @Default
    private Integer seasonNumber = 1;

    public Map<String, String> getMetadata() {
        Map<String, String> metadata = super.getMetadata();
        metadata.put("presenter", this.presenter);
        metadata.put("description", this.description);
        metadata.put("type", "podcast");
        metadata.put("collection_name", "podcast");
        return metadata;
    }
}
