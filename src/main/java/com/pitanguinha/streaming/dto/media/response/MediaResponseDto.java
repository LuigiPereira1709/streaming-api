package com.pitanguinha.streaming.dto.media.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Represents a Response for {@link Media}.
 * 
 * @since 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public sealed class MediaResponseDto permits MediaSuccessDto, MediaErrorDto {
    @Schema(description = "The unique identifier of the media.", example = "12345")
    protected String id;
}
