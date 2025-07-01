package com.pitanguinha.streaming.util.test.creator.media;

import com.pitanguinha.streaming.enums.media.MediaErrorType;
import com.pitanguinha.streaming.dto.media.response.MediaErrorDto;

public class MediaDtoCreator {
    public static MediaErrorDto createErrorDto(MediaErrorType errorType) {
        String message = null;

        switch (errorType) {
            case CONVERSION_PENDING -> message = "Conversion is pending";
            case CONVERSION_FAILED -> message = "Conversion failed";
            default -> {
                throw new IllegalArgumentException("Invalid error type: " + errorType);
            }
        }

        return MediaErrorDto.builder()
                .message(message)
                .errorType(errorType)
                .build();
    }
}
