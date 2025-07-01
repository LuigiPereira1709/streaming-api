package com.pitanguinha.streaming.domain.media;

import java.time.*;
import java.util.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.mongodb.lang.NonNull;
import com.pitanguinha.streaming.enums.media.ConversionStatus;

import lombok.*;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

import jakarta.annotation.Nullable;

/**
 * Represents common data for media files.
 * 
 * <p>
 * This class is a superclass for all content types, such music and podcasts.
 * </p>
 * 
 * @since 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class Media {
    @Id
    @Nullable
    private String id;

    @NonNull
    private String title;

    @Default
    @Field("published_at")
    private Instant publishedAt = Instant.now(); // Default to the current time

    @Default
    private int year = LocalDate.now().getYear(); // Default to the current year

    @Default
    private boolean explicit = false;

    @Default
    private String duration = "00:00:00"; // Default to zero duration

    @Default
    @Field("thumbnail_suffix")
    private String thumbnailSuffix = "thumbnail";

    @Field("content_key")
    private String contentKey;

    @Default
    @Field("conversion_status")
    private ConversionStatus conversionStatus = ConversionStatus.PENDING;

    public Map<String, String> getMetadata() {
        var metadata = new HashMap<String, String>();
        metadata.put("id", this.id);
        metadata.put("title", this.title);
        metadata.put("year", String.valueOf(this.year));
        return metadata;
    }
}
