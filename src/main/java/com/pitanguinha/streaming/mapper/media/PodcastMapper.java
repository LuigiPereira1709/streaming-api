package com.pitanguinha.streaming.mapper.media;

import java.util.List;

import org.mapstruct.*;
import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

import com.pitanguinha.streaming.dto.podcast.*;
import com.pitanguinha.streaming.domain.media.Podcast;
import com.pitanguinha.streaming.enums.media.podcast.Category;

import static com.pitanguinha.streaming.mapper.helper.MapperHelper.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE, nullValueCheckStrategy = ALWAYS, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PodcastMapper {
    @Mapping(target = "categories", expression = "java(mapCategoriesToString(entity.getCategories()))")
    PodcastSuccessDto toDto(Podcast entity);

    @Mapping(target = "categories", expression = "java(mapStringToCategories(dto.getCategories(), null))")
    Podcast toEntity(PodcastPostDto dto);

    @Mapping(target = "categories", expression = "java(mapStringToCategories(dto.getCategories(), entity.getCategories()))")
    Podcast updateFromPutDto(@MappingTarget Podcast entity, PodcastPutDto dto);

    default List<String> mapCategoriesToString(List<Category> categories) {
        return categories == null || categories.isEmpty() ? null
                : categories.stream()
                        .map(c -> c.categoryName)
                        .toList();
    }

    default List<Category> mapStringToCategories(List<String> categoriesStr, List<Category> categoriesFallback) {
        if (categoriesStr == null || categoriesStr.isEmpty()) {
            if (categoriesFallback == null) {
                return null;
            }
            return categoriesFallback;
        }

        return categoriesStr.stream()
                    .map(c -> mapStringToEnum(Category.class, c, "[\\s\\_\\&]"))
                    .map(e -> (Category) e)
                    .toList();
    }
}
