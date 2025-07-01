package com.pitanguinha.streaming.mapper.media;

import java.util.List;

import org.mapstruct.*;
import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

import com.pitanguinha.streaming.dto.music.*;
import com.pitanguinha.streaming.enums.media.music.*;
import com.pitanguinha.streaming.domain.media.Music;
import static com.pitanguinha.streaming.mapper.helper.MapperHelper.*;
import static com.pitanguinha.streaming.mapper.helper.StringHelper.*;

/**
 * Mapper for {@link Music} entity and its DTOs.
 * 
 * @see Music The entity class representing a music record.
 * @see MusicSuccessDto The DTO class for returning a music record.
 * @see MusicPostDto The DTO class for creating a new music record.
 * @see MusicPutDto The DTO class for updating an existing music record.
 * @since 1.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE, nullValueCheckStrategy = ALWAYS, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MusicMapper {
    @Mapping(target = "genre", expression = "java(entity.getGenre().genreName)")
    @Mapping(target = "moods", expression = "java(mapMoodsToString(entity))")
    MusicSuccessDto toDto(Music entity);

    @Mapping(target = "genre", expression = "java(mapStringToGenre(dto.getGenre(), null))")
    @Mapping(target = "moods", expression = "java(mapStringToMoods(dto.getMoods(), null))")
    Music toEntity(MusicPostDto dto);

    @Mapping(target = "genre", expression = "java(mapStringToGenre(dto.getGenre(), entity.getGenre()))")
    @Mapping(target = "moods", expression = "java(mapStringToMoods(dto.getMoods(), entity.getMoods()))")
    Music updateFromPutDto(@MappingTarget Music entity, MusicPutDto dto);

    default Genre mapStringToGenre(String genreStr, Genre genreFallback) {
        if (genreStr == null || genreStr.isEmpty()) {
            if (genreFallback == null) {
                throw new IllegalArgumentException("Genre cannot be null or empty");
            }
            return genreFallback;
        }

        return (Genre) mapStringToEnum(Genre.class, genreStr, "[\\s&-]|AND");
    }

    default List<String> mapMoodsToString(Music entity) {
        return entity.getMoods() == null || entity.getMoods().isEmpty() ? null
                : entity.getMoods().stream()
                        // Captalize the name
                        .map(m -> capitalize(m.name()))
                        .toList();
    }

    default List<Mood> mapStringToMoods(List<String> moodsStr, List<Mood> moodsFallback) {
        if (moodsStr == null || moodsStr.isEmpty()) {
            if (moodsFallback == null) {
                return null;
            }
            return moodsFallback;
        }

        return moodsStr.stream()
                        .map(m -> mapStringToEnum(Mood.class, m, ""))
                        .map(e -> (Mood) e)
                        .toList();
    }
}
