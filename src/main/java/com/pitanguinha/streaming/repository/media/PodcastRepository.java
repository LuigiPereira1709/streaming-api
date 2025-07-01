package com.pitanguinha.streaming.repository.media;

import java.util.List;

import org.springframework.data.mongodb.repository.*;

import com.pitanguinha.streaming.domain.media.Podcast;
import com.pitanguinha.streaming.enums.media.podcast.Category;

import reactor.core.publisher.*;
import jakarta.validation.constraints.*;

/**
 * Repository for {@link Podcast} entities.
 * 
 * @since 1.0
 */
public interface PodcastRepository extends ReactiveMongoRepository<Podcast, String> {
    /**
     * Find a {@link Podcast} by its id.
     * 
     * @param id {@link String} must not be blank.
     * @return a {@link Mono} of {@link Podcast} if found.
     * 
     * @since 1.0
     */
    Mono<Podcast> findById(@NotBlank(message = "Id cannot be blank") String id);

    /**
     * Find a {@link Podcast} by its title case insensitive.
     * 
     * @param title {@link String} must not be blank.
     * @return a {@link Flux} of {@link Podcast} if found.
     * 
     * @since 1.0
     */
    @Query("{ 'title' : { $regex: ?0, $options: 'i' } }")
    Flux<Podcast> findByTitleRegexCaseInsensitive(@NotBlank(message = "Title cannot be blank") String title);

    /**
     * Find a {@link Podcast} by its presenter case insensitive.
     * 
     * @param presenter {@link String} must not be blank.
     * @return a {@link Flux} of {@link Podcast} if found.
     * 
     * @since 1.0
     */
    @Query("{ 'presenter' : { $regex: ?0, $options: 'i' } }")
    Flux<Podcast> findByPresenterRegexCaseInsensitive(
            @NotBlank(message = "Presenter cannot be blank") String presenter);

    /**
     * Find a {@link Podcast} by its guest case insensitive.
     * 
     * @param guest {@link String} must not be blank.
     * @return a {@link Flux} of {@link Podcast} if found.
     * 
     * @since 1.0
     */
    @Query("{ 'guests' : { $regex: ?0, $options: 'i' } }")
    Flux<Podcast> findByGuestContainsCaseInsensitive(@NotBlank(message = "Guest cannot be blank") String guest);

    /**
     * Find a {@link Podcast} by guests in the list.
     * 
     * @param guests {@link List<String>} must not be empty.
     * @return a {@link Flux} of {@link Podcast} if found.
     * 
     * @since 1.0
     */
    Flux<Podcast> findByGuestsIn(@NotEmpty(message = "Guests list cannot be empty") List<String> guests);

    /**
     * Find a {@link Podcast} by categories in the list.
     * 
     * @param categories {@link List<Category>} must not be empty.
     * @return a {@link Flux} of {@link Podcast} if found.
     * 
     * @since 1.0
     */
    Flux<Podcast> findByCategoriesIn(@NotEmpty(message = "Category list cannot be empty") List<Category> categories);

    /**
     * Find a {@link Podcast} by its year.
     * 
     * @param year {@link Integer}.
     * @return a {@link Flux} of {@link Podcast} if found.
     * 
     * @since 1.0
     */
    @Query("{ 'year' : ?0 }")
    Flux<Podcast> findByYear(int year);

    /**
     * Find a {@link Podcast} by its year between two values.
     * 
     * @param start {@link Integer}.
     * @param end   {@link Integer}.
     * @return a {@link Flux} of {@link Podcast} if found.
     * 
     * @since 1.0
     */
    @Query("{ 'year' : { $gte: ?0, $lte: ?1 } }")
    Flux<Podcast> findByYearBetween(int start, int end);
}
