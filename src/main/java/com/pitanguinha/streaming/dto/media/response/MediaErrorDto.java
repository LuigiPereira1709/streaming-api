package com.pitanguinha.streaming.dto.media.response;

import com.pitanguinha.streaming.enums.media.MediaErrorType;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class MediaErrorDto extends MediaResponseDto {
    private String message;
    private MediaErrorType errorType;
}
