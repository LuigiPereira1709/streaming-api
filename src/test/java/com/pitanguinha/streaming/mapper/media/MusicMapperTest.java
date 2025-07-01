package com.pitanguinha.streaming.mapper.media;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;

import com.pitanguinha.streaming.enums.media.music.*;

import static com.pitanguinha.streaming.util.test.creator.media.music.MusicDtoCreator.*;
import static com.pitanguinha.streaming.util.test.creator.media.music.MusicEntityCreator.*;

import static com.pitanguinha.streaming.util.test.creator.media.MediaAttributes.ID;

/**
 * Unit tests for {@link MusicMapper}.
 * <p>
 * This class tests the behavior of the {@link MusicMapper} class.
 * </p>
 * 
 * @see MusicMapper
 * @since 1.0
 */
public class MusicMapperTest {
    final MusicMapper mapper = new MusicMapperImpl();

    private void assertGenre(String genreStr, Genre genreEnum) {
        assertEquals(genreStr.toLowerCase(), genreEnum.genreName.toLowerCase(), "The genre should be the same");
    }

    private void assertMoods(List<String> moodsStr, List<Mood> moodsEnum) {
        assertEquals(moodsStr.size(), moodsEnum.size(), "The moods should be the same");

        for (int i = 0; i < moodsStr.size(); i++) {
            assertEquals(moodsStr.get(i).toLowerCase(), moodsEnum.get(i).name().toLowerCase(),
                    "The moods should be the same");
        }
    }

    @Test
    @DisplayName("Should map music entity to music success dto")
    void toDto_ReturnSuccessDto() {
        var entity = createEntity();

        var dto = mapper.toDto(entity);
        assertNotNull(dto, "The dto should not be null");

        assertAll("Attributes", () -> {
            assertEquals(entity.getId(), dto.getId(), "The id should be the same");
            assertEquals(entity.getTitle(), dto.getTitle(), "The title should be the same");
            assertEquals(entity.getDuration(), dto.getDuration(), "The duration should be the same");
            assertEquals(entity.getArtist(), dto.getArtist(), "The artist should be the same");
            assertEquals(entity.getFeats(), dto.getFeats(), "The feat should be the same");
            assertEquals(entity.getAlbum(), dto.getAlbum(), "The album should be the same");
            assertEquals(entity.isExplicit(), dto.isExplicit(), "The explicit should be the same");
        });

        assertAll("Enums", () -> {
            assertGenre(dto.getGenre(), entity.getGenre());
            assertMoods(dto.getMoods(), entity.getMoods());
        });
    }

    @Test
    @DisplayName("Should map post dto to entity")
    void toEntity_PostDto_ReturnEntity() {
        var postDto = createPostDto();

        var entity = mapper.toEntity(postDto);
        assertNotNull(entity, "The entity should not be null");

        assertAll("Attributes", () -> {
            assertNull(entity.getId(), "The id should be null");
            assertEquals(postDto.getTitle(), entity.getTitle(), "The title should be the same");
            assertEquals(postDto.getArtist(), entity.getArtist(), "The artist should be the same");
            assertEquals(postDto.getFeats(), entity.getFeats(), "The feat should be the same");
            assertEquals(postDto.getAlbum(), entity.getAlbum(), "The album should be the same");
            assertEquals(postDto.isExplicit(), entity.isExplicit(), "The explicit should be the same");
        });

        assertAll("Enums", () -> {
            assertGenre(postDto.getGenre(), entity.getGenre());
            assertMoods(postDto.getMoods(), entity.getMoods());

        });
    }

    @Test
    @DisplayName("Should update entity from put dto")
    void updateFromPutDto_EntityAndPutDto_ReturnEntity() {
        var entity = createEntity();
        var putDto = createPutDto((String) ID.getValue());

        // When
        mapper.updateFromPutDto(entity, putDto);

        // Then
        assertAll(() -> {
            assertEquals(entity.getId(), putDto.getId(), "The id should be the same");
            assertEquals(entity.getTitle(), putDto.getTitle(), "The title should be the same");
            assertEquals(entity.getArtist(), putDto.getArtist(), "The artist should be the same");
            assertEquals(entity.getFeats(), putDto.getFeats(), "The feat should be the same");
            assertEquals(entity.getAlbum(), putDto.getAlbum(), "The album should be the same");
            assertEquals(entity.isExplicit(), putDto.isExplicit(), "The explicit should be the same");
        });

        assertAll("Enums", () -> {
            assertGenre(putDto.getGenre(), entity.getGenre());
            assertMoods(putDto.getMoods(), entity.getMoods());
        });
    }
}
