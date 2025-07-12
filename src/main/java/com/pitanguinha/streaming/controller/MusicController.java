package com.pitanguinha.streaming.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.pitanguinha.streaming.annotation.ValidEnum;
import com.pitanguinha.streaming.dto.music.*;
import com.pitanguinha.streaming.enums.media.SearchType;
import com.pitanguinha.streaming.enums.media.music.*;
import com.pitanguinha.streaming.utils.MapperUtils;
import com.pitanguinha.streaming.service.media.MusicService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.pitanguinha.streaming.exceptions.search.SearchTypeArgumentsException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;

import reactor.core.publisher.*;

/**
 * Controller for consuming and exposing music operations.
 * 
 * <p>
 * Ps: It uses conditional properties to enable or disable the configuration,
 * defaulting to falsae if not specified in the application properties or
 * dynamic properties.
 * </p>
 * 
 * @since 1.0
 */
@RestController
@RequestMapping("/music")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.application.services.music.enabled", havingValue = "true", matchIfMissing = false)
@Tag(name = "Music", description = "Provides endpoints for managing and consuming music media.")
public class MusicController {
    private final MusicService service;

    @GetMapping("/content")
    @Operation(summary = "Generates a signed URL for accessing the content of a music media by its ID.")
    public Mono<String> getContentSignedUrl(@RequestParam @NotBlank String id) {
        return service.getContentSignedUrl(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Saves a new music media.")
    public Mono<MusicSuccessDto> save(@Valid @ModelAttribute MusicPostDto postDto) {
        return service.save(postDto);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Updates an existing music media.")
    public Mono<MusicSuccessDto> update(@Valid @ModelAttribute MusicPutDto putDto) {
        return service.update(putDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes a music media by its ID.")
    public Mono<ResponseEntity<Void>> delete(@PathVariable @NotBlank String id) {
        return service.delete(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Finds a music media by its ID.")
    public Mono<MusicSuccessDto> findById(@PathVariable @NotBlank String id) {
        return service.findById(id);
    }

    // NOTE: Uncomment when jwt authentication is implemented
    // @GetMapping("/find/owner")
    // public Flux<MediaResponseDto> findByOwnerId(@PathVariable String ownerId) {
    // ValidationHelper.validadeString("Owner ID cannot be null or empty", ownerId);
    // return service.findAllForOwner(ownerId);
    // }

    @PostMapping("/search/by-feats")
    @Operation(summary = "Finds music media by feats (featuring artists).")
    public Flux<MusicSuccessDto> findByFeatsIn(@RequestBody @NotEmpty String... feats) {
        return service.findByAnyString(SearchType.FEAT_IN, feats);
    }

    @PostMapping("/search/by-moods")
    @Operation(summary = "Finds music media by moods.")
    public Flux<MusicSuccessDto> findByMoodsIn(
            @RequestBody @ValidEnum(enumClass = Mood.class, message = "All moods must be valid") String... moodsString) {
        Mood[] moodsEnum = new Mood[moodsString.length];
        for (int i = 0; i < moodsString.length; i++)
            moodsEnum[i] = (Mood) MapperUtils.mapStringToEnum(Mood.class, moodsString[i], "[\\s&-]|AND");

        return service.findByAnyEnum(SearchType.MOODS_IN, moodsEnum);
    }

    @GetMapping("/search")
    @Operation(summary = "Performs a search for various parameters such as title, artist, album, etc.", description = "At this point, it is a dumb search, meaning it will return result based on the first non-null parameter provided. In the future, it will be improved to allow more sophisticated searches.")
    public Flux<MusicSuccessDto> smartSearch(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) String album,
            @RequestParam(required = false) String feat,
            @RequestParam(required = false) @ValidEnum(enumClass = Genre.class) String genre,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer endYear) {
        return dumbSearch(title, artist, album, feat, genre, year, startYear, endYear);
    }

    /**
     * Performs a dumb search for music based on various parameters.
     * 
     * @throws SearchTypeArgumentsException if no search parameters are provided.
     * @since 1.0
     */
    private Flux<MusicSuccessDto> dumbSearch(
            String title,
            String artist,
            String album,
            String feat,
            String genre,
            Integer year,
            Integer startYear,
            Integer endYear) {
        // NOTE: Yeah, I know this is ugly, but in the future I will write a more
        // sophisticated strategy
        if (title != null && !title.isBlank())
            return service.findByAnyString(SearchType.TITLE, title);

        if (artist != null && !artist.isBlank())
            return service.findByAnyString(SearchType.ARTIST, artist);

        if (album != null && !album.isBlank())
            return service.findByAnyString(SearchType.ALBUM, album);

        if (feat != null && !feat.isBlank())
            return service.findByAnyString(SearchType.FEAT_CONTAINS, feat);

        if (genre != null && !genre.isBlank()) {
            Genre genreEnum = (Genre) MapperUtils.mapStringToEnum(Genre.class, genre, "[\\s&-]|AND");
            return service.findByAnyEnum(SearchType.GENRE, genreEnum);
        }

        if (year != null && year > 0)
            return service.findByAnyInt(SearchType.YEAR, year);

        if ((startYear != null && startYear > 0) && (endYear != null && endYear > 0))
            return service.findByAnyInt(SearchType.YEAR_BETWEEN, startYear, endYear);

        throw new SearchTypeArgumentsException(
                "At least one search parameter must be provided. "
                        + "Available parameters: title, artist, album, feat, genre, year, startYear, endYear");
    }
}
