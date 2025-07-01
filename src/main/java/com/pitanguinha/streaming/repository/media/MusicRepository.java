package com.pitanguinha.streaming.repository.media;

import java.util.List;

import org.springframework.data.mongodb.repository.*;

import com.pitanguinha.streaming.enums.media.music.*;
import com.pitanguinha.streaming.domain.media.Music;

import reactor.core.publisher.*;
import jakarta.validation.constraints.*;

/**
 * Repository for {@link Music} entities.
 * 
 * @since 1.0
 */
public interface MusicRepository extends ReactiveMongoRepository<Music, String> {
    /**
     * Find a {@link Music} by its id.
     * 
     * @param id {@link String} must not be blank.
     * @return a {@link Mono} of {@link Music} if found.
     * 
     * @since 1.0
     */
    Mono<Music> findById(@NotBlank(message = "Id cannot be blank") String id);

    /**
     * Find a {@link Music} by its title case insensitive.
     * 
     * @param title {@link String} must not be blank.
     * @return a {@link Flux} of {@link Music} if found.
     * 
     * @since 1.0
     */
    @Query("{ 'title' : { $regex: ?0, $options: 'i' } }")
    Flux<Music> findByTitleRegexCaseInsensitive(@NotBlank(message = "Title cannot be blank") String title);

    /**
     * Find a {@link Music} by its artist case insensitive.
     * 
     * @param artist {@link String} must not be blank.
     * @return a {@link Flux} of {@link Music} if found.
     * 
     * @since 1.0
     */
    @Query("{ 'artist' : { $regex: ?0, $options: 'i' } }")
    Flux<Music> findByArtistRegexCaseInsensitive(@NotBlank(message = "Artist cannot be blank") String artist);

    /**
     * Find a {@link Music} by its album case insensitive.
     * 
     * @param album {@link String} must not be blank.
     * @return a {@link Flux} of {@link Music} if found.
     * 
     * @since 1.0
     */
    @Query("{ 'album' : { $regex: ?0, $options: 'i' } }")
    Flux<Music> findByAlbumRegexCaseInsensitive(@NotBlank(message = "Album cannot be blank") String album);

    /**
     * Find a {@link Music} by its feats case insensitive.
     * 
     * @param feat {@link String} must not be blank.
     * @return a {@link Flux} of {@link Music} if found.
     * 
     * @since 1.0
     */
    @Query("{ 'feats' : { $regex: ?0, $options: 'i' } }")
    Flux<Music> findByFeatsContains(@NotBlank(message = "Feats cannot be blank") String feat);

    /**
     * Find a {@link Music} by feats in the list.
     * 
     * @param feats {@link List<String>} must not be empty.
     * @return a {@link Flux} of {@link Music} if found.
     * 
     * @since 1.0
     */
    Flux<Music> findByFeatsIn(@NotEmpty(message = "Feats cannot be empty") List<String> feats);

    /**
     * Find a {@link Music} by its genre.
     * 
     * @param genre The genre of the music.
     * @return a {@code Flux<Music>} if found.
     * 
     * @since 1.0
     */
    Flux<Music> findByGenre(@NotNull(message = "Genre cannot be null") Genre genre);

    /**
     * Find a {@link Music} by moods in the list.
     * 
     * @param mood {@code List<Mood>} must not be empty.
     * @return a {@code Flux<Music>} if found.
     * 
     * @see Mood Enum for mood values.
     * 
     * @since 1.0
     */
    Flux<Music> findByMoodsIn(@NotEmpty(message = "Mood cannot be empty") List<Mood> mood);

    /**
     * Find a {@link Music} by its year.
     * 
     * @param year {@link Integer}.
     * @return a {@link Flux} of {@link Music} if found.
     * 
     * @since 1.0
     */
    @Query("{ 'year' : ?0 }")
    Flux<Music> findByYear(int year);

    /**
     * Find a {@link Music} by its year between two values.
     * 
     * @param start {@link Integer}.
     * @param end   {@link Integer}.
     * @return a {@link Flux} of {@link Music} if found.
     * 
     * @since 1.0
     */
    @Query("{ 'year' : { $gte: ?0, $lte: ?1 } }")
    Flux<Music> findByYearBetween(int start, int end);
}
