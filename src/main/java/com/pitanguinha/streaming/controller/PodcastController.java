package com.pitanguinha.streaming.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.pitanguinha.streaming.annotation.ValidEnum;
import com.pitanguinha.streaming.dto.podcast.*;
import com.pitanguinha.streaming.enums.media.SearchType;
import com.pitanguinha.streaming.enums.media.podcast.Category;
import com.pitanguinha.streaming.exceptions.search.SearchTypeArgumentsException;
import com.pitanguinha.streaming.utils.MapperUtils;
import com.pitanguinha.streaming.service.media.PodcastService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.*;

/**
 * Controller for consuming and exposing podcast operations.
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
@RequestMapping("/podcast")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.application.services.podcast.enabled", havingValue = "true", matchIfMissing = false)
@Tag(name = "Podcast", description = "Provides endpoints for managing and consuming podcast media.")
public class PodcastController {
    private final PodcastService service;

    @GetMapping("/content")
    @Operation(summary = "Generates a signed URL for accessing the content of a podcast media by its ID.")
    public Mono<String> getContentSignedUrl(@RequestParam @NotBlank String id) {
        return service.getContentSignedUrl(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Saves a new podcast media.")
    public Mono<PodcastSuccessDto> save(@Valid @ModelAttribute PodcastPostDto postDto) {
        return service.save(postDto);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Updates an existing podcast media.")
    public Mono<PodcastSuccessDto> update(@Valid @ModelAttribute PodcastPutDto putDto) {
        return service.update(putDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes a podcast media by its ID.")
    public Mono<ResponseEntity<Void>> delete(@PathVariable @NotBlank String id) {
        return service.delete(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieves a podcast media by its ID.")
    public Mono<PodcastSuccessDto> findById(@PathVariable @NotBlank String id) {
        return service.findById(id);
    }

    @PostMapping("/search/by-guests")
    @Operation(summary = "Finds podcasts by guests.")
    public Flux<PodcastSuccessDto> findByGuests(@RequestBody @NotEmpty String... guests) {
        return service.findByAnyString(SearchType.GUESTS_IN, guests);
    }

    @PostMapping("/search/by-categories")
    @Operation(summary = "Finds podcasts by categories.")
    public Flux<PodcastSuccessDto> findByCategoriesIn(
            @RequestBody @ValidEnum(enumClass = Category.class, message = "All categories must be valid") String... categories) {
        Category[] categoriesArray = new Category[categories.length];
        for (int i = 0; i < categories.length; i++)
            categoriesArray[i] = (Category) MapperUtils.mapStringToEnum(Category.class, categories[i], "[\\s&-]|AND");

        return service.findByAnyEnum(SearchType.CATEGORIES_IN, categoriesArray);
    }

    @GetMapping("/search")
    @Operation(summary = "Performs a search for various parameters such as title, presenter, guest, etc.", description = "At this point, it is a dumb search, meaning it will return result based on the first non-null parameter provided. In the future, it will be improved to allow more sophisticated searches.")
    public Flux<PodcastSuccessDto> smartSearch(
            String title,
            String presenter,
            String guest,
            Integer year,
            Integer startYear,
            Integer endYear) {
        return dumbSearch(title, presenter, guest, year, startYear, endYear);
    }

    /**
     * Performs a dumb search for podcast based on various parameters.
     * 
     * @throws SearchTypeArgumentsException if no valid search parameters are
     *                                      provided.
     * 
     * @since 1.0
     */
    private Flux<PodcastSuccessDto> dumbSearch(
            String title,
            String presenter,
            String guest,
            Integer year,
            Integer startYear,
            Integer endYear) {
        // NOTE: Yeah, I know this is ugly, but in the future I will write a more
        // sophisticated strategy
        if (title != null && !title.isBlank())
            return service.findByAnyString(SearchType.TITLE, title);

        if (presenter != null && !presenter.isBlank())
            return service.findByAnyString(SearchType.PRESENTER, presenter);

        if (guest != null && !guest.isBlank())
            return service.findByAnyString(SearchType.GUEST_CONTAINS, guest);

        if (year != null && year > 0)
            return service.findByAnyInt(SearchType.YEAR, year);

        if ((startYear != null && startYear > 0) && (endYear != null && endYear > 0))
            return service.findByAnyInt(SearchType.YEAR_BETWEEN, startYear, endYear);

        throw new SearchTypeArgumentsException(
                "At least one search parameter must be provided.t"
                        + "Available parameters: title, presenter, guest, year, startYear, endYear");
    }
}
