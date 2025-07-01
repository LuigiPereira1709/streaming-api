package com.pitanguinha.streaming.dto.media;

import org.springframework.http.codec.multipart.FilePart;

import com.pitanguinha.streaming.annotation.*;
import com.pitanguinha.streaming.enums.media.contenttypes.*;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.*;

/**
 * Represents a Post Request for {@link Media} .
 * 
 * @since 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MediaPostDto {
    @NotNull(message = "Title don't be null")
    @NotEmpty(message = "Title don't be empty")
    @Schema(description = "The title of the media.", example = "My Media Title")
    private String title;

    @ValidYearRange
    @Schema(description = "The release year of the media.", example = "2025")
    private Integer year;

    @Schema(description = "Indicates whether the media is explicit.", example = "true")
    private boolean explicit;

    @NotNull(message = "Thumb file don't be null")
    @Schema(description = "The thumbnail file associated with the media.", example = "thumbnail.jpg")
    @SupportedContentType(enumClass = ThumbnailSupportedTypes.class, message = "Invalid thumbnail content type. Supported types: png, jpeg, webp.")
    private FilePart thumbnailFile;

    @NotNull(message = "Content file don't be null")
    @Schema(description = "The content file associated with the media.", example = "content.mp3")
    @SupportedContentType(enumClass = AudioSupportedTypes.class, message = "Invalid content file content type. Supported types: opus, ogg, flac, mp3, wav, wav_x, aac.")
    private FilePart contentFile;
}
