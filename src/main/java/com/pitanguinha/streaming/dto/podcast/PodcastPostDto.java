package com.pitanguinha.streaming.dto.podcast;

import java.util.List;

import com.pitanguinha.streaming.annotation.ValidEnum;
import com.pitanguinha.streaming.dto.media.MediaPostDto;
import com.pitanguinha.streaming.enums.media.podcast.Category;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;

/**
 * Represents a Post Request for {@link Podcast}.
 * 
 * @see MediaPostDto The parent class of this class.
 * @since 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Represents a Post Request for Podcast.")
public class PodcastPostDto extends MediaPostDto {
    @NotNull(message = "Presenter cannot be null")
    @NotEmpty(message = "Presenter cannot be empty")
    @Schema(description = "The name of the presenter of the podcast.", example = "Pitanguinha Marvada")
    private String presenter;

    @Nullable
    @Schema(description = "The list of guests featured in the podcast.", example = "[\"Pitanguinha\", \"Marvada\"]")
    private List<String> guests;

    @NotNull(message = "Description cannot be null")
    @NotEmpty(message = "Description cannot be empty")
    @Schema(description = "A brief description of the podcast.", example = "This is a podcast about the adventures of Pitanguinha Marvada.")
    private String description;

    @NotNull(message = "Language cannot be null")
    @Size(min = 1, max = 3, message = "The category list must contain between 1 and 3 elements")
    @ValidEnum(enumClass = Category.class, message = "All categories must be valid")
    @Schema(description = "The list of categories associated with the podcast.", example = "[\"Comedy\", \"Technology\"]")
    private List<String> categories;

    @Schema(description = "The number of the episode.", example = "1")
    private Integer episodeNumber;

    @Schema(description = "The number of the season.", example = "1")
    private Integer seasonNumber;
}
