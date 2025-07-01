package com.pitanguinha.streaming.mapper.media;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;

import com.pitanguinha.streaming.enums.media.podcast.Category;

import static com.pitanguinha.streaming.util.test.creator.media.MediaAttributes.ID;
import static com.pitanguinha.streaming.util.test.creator.media.podcast.PodcastDtoCreator.*;
import static com.pitanguinha.streaming.util.test.creator.media.podcast.PodcastEntityCreator.*;

public class PodcastMapperTest {
    final PodcastMapper mapper = new PodcastMapperImpl();

    private void assertCategories(List<String> categoriesStr, List<Category> categoriesEnum) {
        assertEquals(categoriesStr.size(), categoriesEnum.size(), "The categories should be the same");

        for (int i = 0; i < categoriesStr.size(); i++) {
            assertEquals(categoriesStr.get(i).toLowerCase(), categoriesEnum.get(i).name().toLowerCase(),
                    "The categories should be the same");
        }
    }

    @Test
    @DisplayName("Should map entity to success dto")
    void toDto_ReturnSuccessDto() {
        // Given
        var expected = createEntity();

        // When
        var result = mapper.toDto(expected);
        assertNotNull(result, "The result should not be null");

        // Then
        assertAll("Attributes", () -> {
            assertEquals(expected.getId(), result.getId(), "The id should be the same");
            assertEquals(expected.getTitle(), result.getTitle(), "The title should be the same");
            assertEquals(expected.getPresenter(), result.getPresenter(), "The presenter should be the same");
            assertEquals(expected.getGuests(), result.getGuests(), "The guests should be the same");
            assertEquals(expected.getDescription(), result.getDescription(), "The description should be the same");
            assertEquals(expected.getEpisodeNumber(), result.getEpisodeNumber(),
                    "The episode number should be the same");
            assertEquals(expected.getSeasonNumber(), result.getSeasonNumber(), "The season number should be the same");
            assertEquals(expected.isExplicit(), result.isExplicit(), "The explicit should be the same");
        });

        assertCategories(result.getCategories(), expected.getCategories());
    }

    @Test
    @DisplayName("Should map post dto to entity")
    void toEntity_PostDto_ReturnEntity() {
        var postDto = createPostDto();

        var entity = mapper.toEntity(postDto);
        assertNotNull(entity, "The entity should not be null");

        assertAll("Attributes", () -> {
            assertEquals(postDto.getTitle(), entity.getTitle(), "The title should be the same");
            assertEquals(postDto.getPresenter(), entity.getPresenter(), "The presenter should be the same");
            assertEquals(postDto.getGuests(), entity.getGuests(), "The guests should be the same");
            assertEquals(postDto.getDescription(), entity.getDescription(), "The description should be the same");
            assertEquals(postDto.getEpisodeNumber(), entity.getEpisodeNumber(),
                    "The episode number should be the same");
            assertEquals(postDto.getSeasonNumber(), entity.getSeasonNumber(), "The season number should be the same");
            assertEquals(postDto.isExplicit(), entity.isExplicit(), "The explicit should be the same");
        });

        assertCategories(postDto.getCategories(), entity.getCategories());
    }

    @Test
    @DisplayName("Should update entity from put dto")
    void updateFromPutDto_Entity_PutDto_ReturnEntity() {
        var putDto = createPutDto((String) ID.getValue());
        var entity = createEntity();

        mapper.updateFromPutDto(entity, putDto);
        assertNotNull(entity, "The entity should not be null");

        assertAll("Attributes", () -> {
            assertEquals(putDto.getId(), entity.getId(), "The id should be the same");
            assertEquals(putDto.getTitle(), entity.getTitle(), "The title should be the same");
            assertEquals(putDto.getPresenter(), entity.getPresenter(), "The presenter should be the same");
            assertEquals(putDto.getGuests(), entity.getGuests(), "The guests should be the same");
            assertEquals(putDto.getDescription(), entity.getDescription(), "The description should be the same");
            assertEquals(putDto.getEpisodeNumber(), entity.getEpisodeNumber(), "The episode number should be the same");
            assertEquals(putDto.getSeasonNumber(), entity.getSeasonNumber(), "The season number should be the same");
            assertEquals(putDto.isExplicit(), entity.isExplicit(), "The explicit should be the same");
        });

        assertCategories(putDto.getCategories(), entity.getCategories());
    }
}
