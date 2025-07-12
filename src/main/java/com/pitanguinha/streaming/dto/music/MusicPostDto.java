package com.pitanguinha.streaming.dto.music;

import java.util.List;

import com.pitanguinha.streaming.enums.media.music.*;

import io.swagger.v3.oas.annotations.media.Schema;

import com.pitanguinha.streaming.domain.media.Music;
import com.pitanguinha.streaming.dto.media.MediaPostDto;
import com.pitanguinha.streaming.annotation.ValidEnum;

import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;

/**
 * Represents a Post Request for {@link Music}.
 * 
 * @see MediaPostDto The parent class of this class.
 * @since 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Represents a Post Request for Music.")
public class MusicPostDto extends MediaPostDto {
    @NotNull(message = "The artist name cannot be null")
    @NotEmpty(message = "The artist name cannot be empty")
    @Schema(description = "The name of the artist.", example = "Pitanguinha Marvada")
    private String artist;

    @Nullable
    @Schema(description = "The list of featured artists.", example = "[\"Pitanguinha\", \"Marvada\"]")
    private List<String> feats;

    @NotNull(message = "The album name cannot be null")
    @Schema(description = "The album name.", example = "Best of Pitanguinha")
    private String album;

    @NotNull(message = "The genre cannot be null")
    @NotEmpty(message = "The genre cannot be empty")
    @ValidEnum(enumClass = Genre.class, message = "The genre must be a valid genre")
    @Schema(description = "The genre of the music.", example = "Blues")
    private String genre;

    @Size(min = 1, max = 6, message = "The mood list must contain between 1 and 6 elements")
    @ValidEnum(enumClass = Mood.class, message = "All moods must be valid")
    @Schema(description = "The list of moods associated with the music.", example = "[\"Happy\", \"Sad\"]")
    private List<String> moods;
}
