package com.pitanguinha.streaming.dto.podcast;

import java.util.List;

import com.pitanguinha.streaming.dto.media.response.MediaSuccessDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Represents a Response for {@link Podcast}.
 * 
 * @see MediaDto The parent class of this class.
 * @since 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Represents a successful response for Podcast.")
public class PodcastSuccessDto extends MediaSuccessDto {
    @Schema(description = "The name of the presenter of the podcast.", example = "Pitanguinha Marvada")
    private String presenter;

    @Schema(description = "The list of guests featured in the podcast.", example = "[\"Pitanguinha\", \"Marvada\"]")
    private List<String> guests;

    @Schema(description = "A brief description of the podcast.", example = "This is a podcast about the adventures of Pitanguinha Marvada.")
    private String description;

    @Schema(description = "The list of categories associated with the podcast.", example = "[\"Comedy\", \"Technology\"]")
    private List<String> categories;

    @Schema(description = "The number of the episode.", example = "1")
    private int episodeNumber;

    @Schema(description = "The number of the season.", example = "1")
    private int seasonNumber;
}
