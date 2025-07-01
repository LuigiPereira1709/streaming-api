package com.pitanguinha.streaming.dto.podcast;

import java.util.List;

import com.pitanguinha.streaming.annotation.ValidEnum;
import com.pitanguinha.streaming.dto.media.MediaPutDto;
import com.pitanguinha.streaming.enums.media.podcast.Category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;

/**
 * Represents a Put Request for {@link Podcast}.
 * 
 * @see MediaPutDto The parent class of this class.
 * @since 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Represents a Put Request for Podcast.")
public class PodcastPutDto extends MediaPutDto {
    @Nullable
    @Schema(description = "The name of the presenter of the podcast.", example = "Pitanguinha Marvada")
    private String presenter;

    @Nullable
    @Schema(description = "The list of guests featured in the podcast.", example = "[\"Pitanguinha\", \"Marvada\"]")
    private List<String> guests;

    @Nullable
    @Schema(description = "A brief description of the podcast.", example = "This is a podcast about the adventures of Pitanguinha Marvada.")
    private String description;

    @Nullable
    @Size(min = 1, max = 3, message = "The category list must contain between 1 and 3 elements")
    @ValidEnum(enumClass = Category.class, message = "All categories must be valid")
    @Schema(description = "The list of categories associated with the podcast.", example = "[\"Comedy\", \"Technology\"]")
    private List<String> categories;

    @Nullable
    @Schema(description = "The number of the episode.", example = "1")
    private Integer episodeNumber;

    @Nullable
    @Schema(description = "The number of the season.", example = "1")
    private Integer seasonNumber;
}
