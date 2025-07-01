package com.pitanguinha.streaming.repository.media;

import org.testcontainers.junit.jupiter.*;
import org.testcontainers.containers.MongoDBContainer;

import com.pitanguinha.streaming.enums.media.podcast.Category;

import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.springframework.test.context.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import static com.pitanguinha.streaming.util.test.creator.media.podcast.PodcastEntityCreator.*;

@DataMongoTest
@Testcontainers
public class PodcastRepositoryTest {
    @Container
    static MongoDBContainer container = new MongoDBContainer("mongo:latest");
    @Autowired
    PodcastRepository repository;

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
        // Given: Create a entity to save
        var entityToSave = createEntityToSave();

        // When: Save the entity
        StepVerifier.create(repository.save(entityToSave))
                .assertNext(entitySaved -> {
                    // Then: Verify the entity
                    String id = entitySaved.getId();
                    assertTrue(id != null && !id.isBlank(), "Id should not be null or blank");
                    assertEquals(entityToSave.getTitle(), entitySaved.getTitle(), "Title should be the same");
                    assertEquals(entityToSave.getPresenter(), entitySaved.getPresenter(),
                            "Presenter should be the same");
                    assertEquals(entityToSave.getGuests(), entitySaved.getGuests(), "Guests should be the same");
                    assertEquals(entityToSave.getCategories(), entitySaved.getCategories(),
                            "Categories should be the same");
                })
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should update a entity and return the updated entity")
    void update_Entity_ReturnMonoOfEntity() {
        // Given: Create a entity to save
        var entitySaved = repository.save(createEntityToSave())
                .doOnError(e -> fail("Error saving entity: " + e.getMessage()))
                .block();

        // When: Update the entity
        StepVerifier.create(repository.save(createEntityToUpdate(entitySaved.getId())))
                .assertNext(entityUpdated -> {
                    // Then: Verify the updated entity
                    assertEquals(entitySaved.getId(), entityUpdated.getId(), "Id should be the same");
                    assertNotEquals(entitySaved.getTitle(), entityUpdated.getTitle(), "Title should be different");
                    assertNotEquals(entitySaved.getPresenter(), entityUpdated.getPresenter(),
                            "Presenter should be different");
                    assertNotEquals(entitySaved.getGuests(), entityUpdated.getGuests(), "Guests should be different");
                    assertNotEquals(entitySaved.getCategories(), entityUpdated.getCategories(),
                            "Categories should be different");
                })
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should delete a entity and ensure it is no longer retrievable")
    void delete_Entity_ReturnEmptyMono() {
        // Given: Create and save an entity
        var entitySaved = repository.save(createEntityToSave())
                .doOnError(e -> fail("Error saving entity: " + e.getMessage()))
                .block();

        // When: Delete the entity
        repository.delete(entitySaved)
                .doOnError(e -> fail("Error deleting entity: " + e.getMessage()))
                .block();

        // Then: Verify the entity is no longer retrievable
        StepVerifier.create(repository.findById(entitySaved.getId()))
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should retrieve a entity by id")
    void findById_String_ReturnMonoOfEntity() {
        // Given: Create and save an entity
        var entitySaved = repository.save(createEntityToSave())
                .doOnError(e -> fail("Error saving entity: " + e.getMessage()))
                .block();

        // When: Retrieve the entity by id
        StepVerifier.create(repository.findById(entitySaved.getId()))
                .assertNext(entityFound -> {
                    // Then: Verify the retrieved entity
                    assertEquals(entitySaved.getId(), entityFound.getId(), "Id should be the same");
                    assertEquals(entitySaved.getTitle(), entityFound.getTitle(), "Title should be the same");
                    assertEquals(entitySaved.getPresenter(), entityFound.getPresenter(),
                            "Presenter should be the same");
                    assertEquals(entitySaved.getGuests(), entityFound.getGuests(), "Guests should be the same");
                    assertEquals(entitySaved.getCategories(), entityFound.getCategories(),
                            "Categories should be the same");
                })
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should retrieve entities with titles matching a given pattern (including partial matches)")
    void findByTitleRegexCaseInsensitive_String_ReturnFluxOfEntity() {
        // Given: Create and save entities, two with similar titles and one with a
        // different title
        var entity0 = createEntityToSave();
        var entity1 = createEntityToSave();
        entity1.setTitle(entity0.getTitle() + " - 2");
        var entity2 = createEntityToSave();
        entity2.setTitle("BLACK SHEEP");

        // Save all entities
        repository.saveAll(List.of(entity0, entity1, entity2))
                .doOnError(e -> fail("Error saving entities: " + e.getMessage()))
                .blockLast();

        // When: Retrieve entities with titles matching
        // Case 1: Find By title that will match with two entities
        StepVerifier.create(repository.findByTitleRegexCaseInsensitive(entity0.getTitle()))
                // Then: Also the entity0 and entity1 should be returned
                .expectNextMatches(entityFound -> entityFound.getTitle().equals(entity0.getTitle()))
                .expectNextMatches(entityFound -> entityFound.getTitle().equals(entity1.getTitle()))
                .expectComplete()
                .verify();

        // Case 2: Find by title that will match with one entity
        StepVerifier.create(repository.findByTitleRegexCaseInsensitive(entity2.getTitle()))
                // Then: Only the entity2 should be returned
                .expectNextMatches(entityFound -> entityFound.getTitle().equals(entity2.getTitle()))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should retrieve entities with presenters matching a given pattern (including partial matches)")
    void findByPresenterRegexCaseInsensitive_String_ReturnFluxOfEntity() {
        // Given: Create and save entities, two with similar presenters and one with a
        // different presenter
        var entity0 = createEntityToSave();
        var entity1 = createEntityToSave();
        entity1.setPresenter(entity0.getPresenter() + " - 2");
        var entity2 = createEntityToSave();
        entity2.setPresenter("BLACK SHEEP");

        // Save all entities
        repository.saveAll(List.of(entity0, entity1, entity2))
                .doOnError(e -> fail("Error saving entities: " + e.getMessage()))
                .blockLast();

        // When: Retrieve entities with presenters matching
        // Case 1: Find By presenter that will match with two entities
        StepVerifier.create(repository.findByPresenterRegexCaseInsensitive(entity0.getPresenter()))
                // Then: Also the entity0 and entity1 should be returned
                .expectNextMatches(entityFound -> entityFound.getPresenter().equals(entity0.getPresenter()))
                .expectNextMatches(entityFound -> entityFound.getPresenter().equals(entity1.getPresenter()))
                .expectComplete()
                .verify();

        // Case 2: Find by presenter that will match with one entity
        StepVerifier.create(repository.findByPresenterRegexCaseInsensitive(entity2.getPresenter()))
                // Then: Only the entity2 should be returned
                .expectNextMatches(entityFound -> entityFound.getPresenter().equals(entity2.getPresenter()))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should retrieve entities with containing a guest case insensitive")
    void findByGuestCaseInsensitive_String_ReturnFluxOfEntity() {
        // Given: Create and save entities, two with the same guest and one without the
        // search guest
        var entity0 = createEntityToSave();
        var entity1 = createEntityToSave();
        entity1.getGuests().add("GUEST ADDED");
        var entity2 = createEntityToSave();
        entity2.setGuests(List.of("BLACK SHEEP"));

        // Save all entities
        repository.saveAll(List.of(entity0, entity1, entity2))
                .doOnError(e -> fail("Error saving entities: " + e.getMessage()))
                .blockLast();

        // When
        // Case 1: Find By guest that will match with two entities
        StepVerifier.create(repository.findByGuestContainsCaseInsensitive(entity0.getGuests().get(0)))
                // Then: Also the entity0 and entity1 should be returned
                .expectNextMatches(entityFound -> entityFound.getGuests().contains(entity0.getGuests().get(0)))
                .expectNextMatches(entityFound -> entityFound.getGuests().contains(entity1.getGuests().get(0)))
                .expectComplete()
                .verify();

        // Case 2: Will not match with any entity
        StepVerifier.create(repository.findByGuestContainsCaseInsensitive("GUEST NOT FOUND"))
                // Then: No entity should be returned
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should retrieve entities with guests in the list")
    void findByGuestsIn_ListOfString_ReturnFluxOfEntity() {
        // Given: Create and save entities, two with similar list of guests and one with
        // a different list of guests
        var entity0 = createEntityToSave();
        var entity1 = createEntityToSave();
        entity1.getGuests().add("GUEST ADDED");
        var entity2 = createEntityToSave();
        entity2.setGuests(List.of("BLACK SHEEP"));

        // Save all entities
        repository.saveAll(List.of(entity0, entity1, entity2))
                .doOnError(e -> fail("Error saving entities: " + e.getMessage()))
                .blockLast();

        // When
        // Case 1: Find By guests that will match with two entities
        StepVerifier.create(repository.findByGuestsIn(entity0.getGuests()))
                // Then: Also the entity0 and entity1 should be returned
                .expectNextMatches(entityFound -> entityFound.getGuests().contains(entity0.getGuests().get(0)))
                .expectNextMatches(entityFound -> entityFound.getGuests().contains(entity1.getGuests().get(0)))
                .expectComplete()
                .verify();

        // Case 2: Find by guest that will match with one entity
        StepVerifier.create(repository.findByGuestsIn(entity2.getGuests()))
                // Then: Only the entity2 should be returned
                .expectNextMatches(entityFound -> entityFound.getGuests().contains(entity2.getGuests().get(0)))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should retrieve entities with categories in the list")
    void findByCategoriesIn_ListOfCategory_ReturnFluxOfEntity() {
        // Given: Create and save entities, two with similar list of categories and one
        // with a different list of categories
        var entity0 = createEntityToSave();
        var entity1 = createEntityToSave();
        entity1.getCategories().add(Category.SPORTS);
        var entity2 = createEntityToSave();
        entity2.setCategories(List.of(Category.NEWS));

        // Save all entities
        repository.saveAll(List.of(entity0, entity1, entity2))
                .doOnError(e -> fail("Error saving entities: " + e.getMessage()))
                .blockLast();

        // When
        // Case 1: Find By categories that will match with two entities
        StepVerifier.create(repository.findByCategoriesIn(entity0.getCategories()))
                // Then: Also the entity0 and entity1 should be returned
                .expectNextMatches(entityFound -> entityFound.getCategories().contains(entity0.getCategories().get(0)))
                .expectNextMatches(entityFound -> entityFound.getCategories().contains(entity1.getCategories().get(0)))
                .expectComplete()
                .verify();

        // Case 2: Find by category that will match with one entity
        StepVerifier.create(repository.findByCategoriesIn(entity2.getCategories()))
                // Then: Only the entity2 should be returned
                .expectNextMatches(entityFound -> entityFound.getCategories().contains(entity2.getCategories().get(0)))
                .expectComplete()
                .verify();
    }
}
