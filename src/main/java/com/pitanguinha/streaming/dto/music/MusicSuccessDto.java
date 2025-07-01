package com.pitanguinha.streaming.dto.music;

import java.util.List;

import com.pitanguinha.streaming.domain.media.Music;
import com.pitanguinha.streaming.dto.media.response.MediaSuccessDto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * This class represents a successful response for a music entity.
 * 
 * @see MediaDto The parent class of this class.
 * @see Music The entity class that this DTO represents.
 * @since 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Represents a successful response for Music.")
public class MusicSuccessDto extends MediaSuccessDto {
    @Schema(description = "The name of the artist.", example = "Pitanguinha Marvada")
    private String artist;

    @Schema(description = "The list of featured artists.", example = "[\"Pitanguinha\", \"Marvada\"]")
    private List<String> feats;

    @Schema(description = "The album name.", example = "Best of Pitanguinha")
    private String album;

    @Schema(description = "The genre of the music.", example = "Blues")
    private String genre;

    @Schema(description = "The list of moods associated with the music.", example = "[\"Happy\", \"Sad\"]")
    private List<String> moods;
}
