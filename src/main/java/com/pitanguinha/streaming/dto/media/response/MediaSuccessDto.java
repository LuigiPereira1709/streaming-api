package com.pitanguinha.streaming.dto.media.response;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public non-sealed class MediaSuccessDto extends MediaResponseDto {
    @Schema(description = "The title of the media.", example = "My Media Title")
    private String title;

    @Schema(description = "The URL of the thumbnail image.", example = "http://example.com/thumbnail")
    private String thumbnailUrl;

    @Schema(description = "The duration of the media content.", example = "00:00:42")
    private String duration;

    @Schema(description = "The release year of the media.", example = "2025")
    private int year;

    @Schema(description = "Indicates whether the media is explicit.", example = "true")
    private boolean explicit;
}
