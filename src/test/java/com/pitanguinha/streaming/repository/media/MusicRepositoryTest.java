package com.pitanguinha.streaming.repository.media;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;

import org.springframework.test.context.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import org.testcontainers.junit.jupiter.*;
import org.testcontainers.containers.MongoDBContainer;

import com.pitanguinha.streaming.enums.media.music.*;

import static com.pitanguinha.streaming.util.test.creator.media.music.MusicEntityCreator.*;

import reactor.test.StepVerifier;

@DataMongoTest
@Testcontainers
public class MusicRepositoryTest {
    @Container
    static MongoDBContainer container = new MongoDBContainer("mongo:latest");
    @Autowired
    MusicRepository repository;

    @BeforeAll
    static void setUp() {
        container.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", container::getReplicaSetUrl);
    }

    @AfterEach
    void cleanRepository() {
        repository.deleteAll().block();
    }

    @AfterAll
    static void tearDown() {
        container.stop();
    }

    @Test
    @DisplayName("Should save a entity and return it with the correct properties")
    void save_Entity_ReturnMonoOfEntity() {
        // Given: Create an entity to save
        var entityToSave = createEntityToSave();

        // When: Save the entity
        StepVerifier.create(repository.save(entityToSave))
                .assertNext(entitySaved -> {
                    // Then: The entity should be saved
                    String id = entitySaved.getId();
                    assertTrue(id != null && !id.isBlank(), "Id should not be null or blank");
                    assertEquals(entityToSave.getTitle(), entitySaved.getTitle(), "Title should be the same");
                    assertEquals(entityToSave.getFeats(), entitySaved.getFeats(), "Feat should be the same");
                    assertEquals(entityToSave.getGenre(), entitySaved.getGenre(), "Genre should be the same");
                    assertEquals(entityToSave.getMoods(), entitySaved.getMoods(), "Mood should be the same");
                    assertEquals(entityToSave.isExplicit(), entitySaved.isExplicit(), "Explicit should be the same");
                })
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should update a entity and return the updated entity")
    void update_Entity_ReturnMono() {
        // Given: Create and save an entity
        var entitySaved = repository.save(createEntityToSave())
                .doOnError(e -> fail("Save operation failed")) // Guarantee that the save operation was successful
                .block();

        // When: Update the entity
        StepVerifier.create(repository.save(createEntityToUpdate(entitySaved.getId())))
                .assertNext(entityUpdated -> {
                    // Then: The entity should be updated
                    assertEquals(entitySaved.getId(), entityUpdated.getId(), "Id should be the same");
                    assertNotEquals(entitySaved.getTitle(), entityUpdated.getTitle(), "Title should be different");
                    assertNotEquals(entitySaved.getFeats(), entityUpdated.getFeats(), "Feat should be different");
                    assertNotEquals(entitySaved.getGenre(), entityUpdated.getGenre(), "Genre should be different");
                    assertNotEquals(entitySaved.getMoods(), entityUpdated.getMoods(), "Mood should be different");
                    assertNotEquals(entitySaved.isExplicit(), entityUpdated.isExplicit(),
                            "Explicit should be different");
                })
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should delete a entity and ensure it is no longer retrievable")
    void delete_Entity_ReturnEmptyMono() {
        // Given: Create and save an entity
        var entitySaved = repository.save(createEntityToSave())
                .doOnError(e -> fail("Save operation failed")) // Guarantee that the save operation was successful
                .block();

        // When: Delete the entity
        repository.delete(entitySaved)
                .doOnError(e -> fail("Delete operation failed")) // Guarantee that the delete operation was successful
                .block();

        // Then: Verify the entity is no longer retrievable
        StepVerifier.create(repository.findById(entitySaved.getId()))
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should retrieve a entity by its ID")
    void findById_String_ReturnMonoOfEntity() {
        // Given: Create and save an entity
        var entitySaved = repository.save(createEntityToSave())
                .doOnError(e -> fail("Save operation failed")) // Guarantee that the save operation was successful
                .block();

        // When: Found by id
        StepVerifier.create(repository.findById(entitySaved.getId()))
                .assertNext(entityFound -> {
                    // Then the entity should be returned
                    assertEquals(entitySaved.getId(), entityFound.getId(), "Id should be the same");
                    assertEquals(entitySaved.getTitle(), entityFound.getTitle(), "Title should be the same");
                    assertEquals(entitySaved.getFeats(), entityFound.getFeats(), "Feat should be the same");
                    assertEquals(entitySaved.getGenre(), entityFound.getGenre(), "Genre should be the same");
                    assertEquals(entitySaved.getMoods(), entityFound.getMoods(), "Mood should be the same");
                    assertEquals(entitySaved.isExplicit(), entityFound.isExplicit(), "Explicit should be the same");
                })
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should return entities with titles matching the given pattern (including partial matches)")
    void findByTitleRegexCaseInsensitive_String_ReturnFluxOfEntity() {
        // Given: Create and save entities, two with similar titles, and one with a
        // different title
        var entity0 = createEntityToSave();
        var entity1 = createEntityToSave();
        entity1.setTitle(entity1.getTitle() + " 1");
        var entity2 = createEntityToSave();
        entity2.setTitle("BLACK SHEEP"); // set a completely different title

        // Save all entities
        repository.saveAll(List.of(entity0, entity1, entity2))
                .doOnError(e -> fail("Save operation failed")) // Guarantee that the save operation was successful
                .collectList()
                .block();

        // When
        // Case 1: Find by title that will match with two entities
        StepVerifier.create(repository.findByTitleRegexCaseInsensitive(entity0.getTitle()))
                // Then: Also the entity0 and entity1 should be returned
                .expectNextMatches(entityFound -> entityFound.getTitle().equals(entity0.getTitle()))
                .expectNextMatches(entityFound -> entityFound.getTitle().equals(entity1.getTitle()))
                .expectComplete()
                .verify();

        // Case 2: Find by title that will match with one entity
        StepVerifier.create(repository.findByTitleRegexCaseInsensitive(entity2.getTitle()))
                // Then: The entity2 should be returned
                .expectNextMatches(entityFound -> entityFound.getTitle().equals(entity2.getTitle()))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should return entities with artists matching the given pattern (including partial matches)")
    void findByArtistRegexCaseInsensitive_String_ReturnFluxOfEntity() {
        // Given: Create and save entities, two with similar artists and one with a
        // different artist
        var entity0 = createEntityToSave();
        var entity1 = createEntityToSave();
        entity1.setArtist(entity1.getArtist() + " 1");
        var entity2 = createEntityToSave();
        entity2.setArtist("BLACK SHEEP");

        // Save all entities
        repository.saveAll(List.of(entity0, entity1, entity2))
                .doOnError(e -> fail("Save operation failed"))
                .collectList()
                .block();

        // When
        // Case 1: Find by artist that will match with two entities
        StepVerifier.create(repository.findByArtistRegexCaseInsensitive(entity0.getArtist()))
                // Then: Also the entity0 and entity1 should be returned
                .expectNextMatches(result -> result.getArtist().equals(entity0.getArtist()))
                .expectNextMatches(result -> result.getArtist().equals(entity1.getArtist()))
                .expectComplete()
                .verify();

        // Case 2: Find by artist that will match with one entity
        StepVerifier.create(repository.findByArtistRegexCaseInsensitive(entity2.getArtist()))
                // Then: The entity2 should be returned
                .expectNextMatches(result -> result.getArtist().equals(entity2.getArtist()))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should return entities with albuns matching the given pattern (including partial matches)")
    void findByAlbumRegexCaseInsensitive_String_ReturnFluxOfEntity() {
        // Given: Create and save entities, two with similar album and one with a
        // different album
        var entity0 = createEntityToSave();
        var entity1 = createEntityToSave();
        entity1.setAlbum(entity1.getAlbum() + " 1");
        var entity2 = createEntityToSave();
        entity2.setAlbum("BLACK SHEEP");

        // Save all entities
        repository.saveAll(List.of(entity0, entity1, entity2))
                .doOnError(e -> fail("Save operation failed"))
                .collectList()
                .block();

        // When
        // Case 1: Find by album that will match with two entities
        StepVerifier.create(repository.findByAlbumRegexCaseInsensitive(entity0.getAlbum()))
                // Then: Also the entity0 and entity1 should be returned
                .expectNextMatches(entityFound -> entityFound.getAlbum().equals(entity0.getAlbum()))
                .expectNextMatches(entityFound -> entityFound.getAlbum().equals(entity1.getAlbum()))
                .expectComplete()
                .verify();

        // Case 2: Find by album that will match with one entity
        StepVerifier.create(repository.findByAlbumRegexCaseInsensitive(entity2.getAlbum()))
                // Then: The entity2 should be returned
                .expectNextMatches(entityFound -> entityFound.getAlbum().equals(entity2.getAlbum()))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should return entities with genre matching")
    void findByGenre_Genre_ReturnFluxOfEntity() {
        // Given: Create and save entities,two with the same genre and one with a
        // different genre
        var entity0 = createEntityToSave();
        var entity1 = createEntityToSave();
        var entity2 = createEntityToSave();
        entity2.setGenre(Genre.R_AND_B);

        // Save all entities
        repository.saveAll(List.of(entity0, entity1, entity2))
                .doOnError(e -> fail("Save operation failed"))
                .collectList()
                .block();

        // When
        // Case 1: Find by genre that will match with two entities
        StepVerifier.create(repository.findByGenre(entity0.getGenre()))
                // Then: Also the entity0 and entity1 should be returned
                .expectNextMatches(entityFound -> entityFound.getGenre().equals(entity0.getGenre()))
                .expectNextMatches(entityFound -> entityFound.getGenre().equals(entity1.getGenre()))
                .expectComplete()
                .verify();

        // Case 2: Find by genre that will match with one entity
        StepVerifier.create(repository.findByGenre(entity2.getGenre()))
                // Then: The entity2 should be returned
                .expectNextMatches(entityFound -> entityFound.getGenre().equals(entity2.getGenre()))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should return entities with the specified year")
    void findByYear_Int_ReturnFluxOfMusic() {
        // Given: Create entities, two with the same year and one with a
        // different year
        var entity0 = createEntityToSave();
        var entity1 = createEntityToSave();
        entity1.setTitle("BLACK SHEEP");
        var entity2 = createEntityToSave();
        entity2.setYear(0);

        // Save all entities
        repository.saveAll(List.of(entity0, entity1, entity2))
                .doOnError(e -> fail("Save operation failed"))
                .collectList()
                .block();

        // When
        // Case 1: Find by year that will match with two entities
        StepVerifier.create(repository.findByYear(entity0.getYear()))
                // Then: Also the entity0 and entity1 should be returned
                .expectNextMatches(entityFound -> entityFound.getYear() == entity0.getYear())
                .expectNextMatches(entityFound -> entityFound.getYear() == entity1.getYear())
                .expectComplete()
                .verify();

        // Case 2: Find by year that will match with one entity
        StepVerifier.create(repository.findByYear(entity2.getYear()))
                // Then: The entity2 should be returned
                .expectNextMatches(entityFound -> entityFound.getYear() == entity2.getYear())
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should return entities with moods matching the given list")
    void findByMoodsIn_ListOfStrings_ReturnFluxOfMusic() {
        // Given: Create entities, two with similar moods and one with a
        // different moods
        var entity0 = createEntityToSave();
        var entity1 = createEntityToSave();
        entity1.getMoods().add(Mood.NOSTALGIC);
        var entity2 = createEntityToSave();
        entity2.setMoods(List.of(Mood.SOULFUL));

        // Save all entities
        repository.saveAll(List.of(entity0, entity1, entity2))
                .doOnError(e -> fail("Save operation failed"))
                .collectList()
                .block();

        // When: When mood is matched with mood of entity0 and entity1
        // Case 1: Find by mood that will match with two entities
        StepVerifier.create(repository.findByMoodsIn(entity0.getMoods()))
                // Then: Also the entity0 and entity1 should be returned
                .expectNextMatches(entityFound -> entityFound.getMoods().equals(entity0.getMoods()))
                .expectNextMatches(entityFound -> entityFound.getMoods().equals(entity1.getMoods()))
                .expectComplete()
                .verify();

        // Case 2: Find by mood that will match with one entity
        StepVerifier.create(repository.findByMoodsIn(entity2.getMoods()))
                // Then: The entity2 should be returned
                .expectNextMatches(entityFound -> entityFound.getMoods().equals(entity2.getMoods()))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should return entities with years between the specified range")
    void findByYearBetween_IntInt_ReturnFluxOfMusic() {
        // Given: Create entities, two with close years and one with a
        // year outside the range
        var entity0 = createEntityToSave();

        var entity1 = createEntityToSave();
        entity1.setTitle("ENTITY 1");
        entity1.setYear(entity1.getYear() + 1);

        var entity2 = createEntityToSave();
        entity2.setTitle("ENTITY 2");
        entity2.setYear(entity2.getYear() + 2);

        var entity3 = createEntityToSave();
        entity3.setTitle("ENTITY 3");
        entity3.setYear(7);

        // Save all entities
        repository.saveAll(List.of(entity0, entity1, entity2, entity3))
                .doOnError(e -> fail("Save operation failed")) // Guarantee that the save operation was successful
                .collectList()
                .block();

        // When: When year is matched with year of entity0 and entity1
        // Case 1: Find by year that will match with two entities
        StepVerifier.create(repository.findByYearBetween(entity0.getYear(), entity2.getYear()))
                // Then: Also the entity0 and entity1 should be returned
                .expectNextCount(3)
                .expectComplete()
                .verify();

        // Case 2: Find by year that will match with one entity
        StepVerifier.create(repository.findByYearBetween(0, entity3.getYear()))
                // Then: The entity2 should be returned
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }
}
