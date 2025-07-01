package com.pitanguinha.streaming.dto.media;

import org.springframework.http.codec.multipart.FilePart;

import com.pitanguinha.streaming.enums.media.contenttypes.*;
import com.pitanguinha.streaming.annotation.SupportedContentType;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;

/**
 * Represents a Put Request for {@link Media}.
 * 
 * @since 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MediaPutDto {
    @NotNull(message = "Id don't be null")
    @NotEmpty(message = "Id can't be empty")
    @Schema(description = "The unique identifier of the media.", example = "12345")
    private String id;

    @Nullable
    @Schema(description = "The title of the media.", example = "My Media Title")
    private String title;

    @Nullable
    @Schema(description = "Indicates whether the media is explicit.", example = "true")
    private boolean explicit;

    @Nullable
    @Schema(description = "The thumbnail file associated with the media.", example = "thumbnail.jpg")
    @SupportedContentType(enumClass = ThumbnailSupportedTypes.class, message = "Invalid thumbnail content type. Supported types: png, jpeg, webp.")
    private FilePart thumbnailFile;

    @Nullable
    @Schema(description = "The content file associated with the media.", example = "content.mp3")
    @SupportedContentType(enumClass = AudioSupportedTypes.class, message = "Invalid content file content type. Supported types: opus, ogg, flac, mp3, wav, wav_x, aac.")
    private FilePart contentFile;
}
