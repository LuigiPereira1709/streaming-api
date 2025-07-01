package com.pitanguinha.streaming.service.media;

import java.util.Arrays;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.pitanguinha.streaming.domain.media.Podcast;
import com.pitanguinha.streaming.dto.media.*;
import com.pitanguinha.streaming.dto.media.response.MediaResponseDto;
import com.pitanguinha.streaming.dto.podcast.*;
import com.pitanguinha.streaming.enums.media.SearchType;
import com.pitanguinha.streaming.enums.media.podcast.Category;
import com.pitanguinha.streaming.exceptions.search.InvalidSearchTypeException;
import com.pitanguinha.streaming.exceptions.search.SearchTypeArgumentsException;
import com.pitanguinha.streaming.mapper.media.PodcastMapper;
import com.pitanguinha.streaming.repository.media.PodcastRepository;
import com.pitanguinha.streaming.service.TempDirService;
import com.pitanguinha.streaming.service.aws.*;
import com.pitanguinha.streaming.service.media.operation.MediaOperator;

import reactor.core.publisher.*;

/**
 * Service for managing podcast media.
 * 
 * <p>
 * Ps: It uses conditional properties to enable or disable the configuration,
 * defaulting to falsae if not specified in the application properties or
 * dynamic properties.
 * </p>
 * 
 * @since 1.0
 */
@Service
@ConditionalOnProperty(name = "spring.application.services.podcast.enabled", havingValue = "true", matchIfMissing = false)
public class PodcastService extends AbstractMediaService<Podcast, PodcastSuccessDto> {
    private final PodcastRepository repository;
    private final PodcastMapper mapper;

    @Autowired
    public PodcastService(PodcastRepository repository, AwsS3Service s3Service, AwsCloudFrontService cloudFrontService,
            TempDirService tempDirService, MediaOperator<Podcast> mediaOperator, PodcastMapper mapper) {
        super(s3Service, cloudFrontService, tempDirService, repository, mediaOperator);
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Saves a new podcast to the repository and S3 storage.
     *
     * @param postDto the DTO containing the podcast post data
     * @return a Mono containing the saved podcast success DTO
     * @throws S3Exception If the upload to S3 fails.
     *
     * @since 1.0
     */
    public Mono<PodcastSuccessDto> save(MediaPostDto postDto) {
        return this.saveInS3AndRepository(mapper.toEntity((PodcastPostDto) postDto), postDto.getThumbnailFile(),
                postDto.getContentFile());
    }

    /**
     * Updates an existing podcast in the repository and S3 storage.
     *
     * @param putDto the DTO containing the podcast update data
     * @return a Mono containing the updated podcast success DTO
     * @throws S3Exception If the upload to S3 fails.
     *
     * @since 1.0
     */
    public Mono<PodcastSuccessDto> update(MediaPutDto putDto) {
        return this.updateInS3AndRepository((PodcastPutDto) putDto, mapper::updateFromPutDto);
    }

    /**
     * Finds all podcasts for a specific owner.
     *
     * @param ownerId the ID of the owner
     * @return a Flux containing all podcasts for the owner
     * @throws NotFoundException if no podcasts are found for the owner.
     *
     * 
     * @since 1.0
     */
    // NOTE: This method is not used in the current implementation, but in the
    // future will be used
    public Flux<MediaResponseDto> findAllForOwner(String ownerId) {
        return this.findAllForOwnerWithErrorWrap(repository.findByPresenterRegexCaseInsensitive(ownerId));
    }

    /**
     * Finds podcasts by any string attribute (title, presenter, guests, or guest
     * contains).
     *
     * @param searchType the type of search to perform
     * @param anyString  the search terms
     * @return a Flux containing the found podcast success DTOs
     * @throws InvalidSearchTypeException   if the search type is not valid for the
     *                                      method scope.
     * @throws SearchTypeArgumentsException if the number of arguments is invalid
     *                                      for the given search type.
     * 
     * @see SearchType Enum for valid search types.
     *
     * @since 1.0
     */
    public Flux<PodcastSuccessDto> findByAnyString(SearchType searchType, String... anyString) {
        if (anyString.length == 0)
            throw new SearchTypeArgumentsException(
                    "Invalid number of arguments, expected at least 1");

        Flux<Podcast> entities;
        switch (searchType) {
            case TITLE -> entities = repository.findByTitleRegexCaseInsensitive(anyString[0]);
            case PRESENTER -> entities = repository.findByPresenterRegexCaseInsensitive(anyString[0]);
            case GUESTS_IN -> entities = repository.findByGuestsIn(Arrays.asList(anyString));
            case GUEST_CONTAINS -> {
                if (anyString.length != 1)
                    throw new SearchTypeArgumentsException(
                            "Invalid number of arguments, expected 1, got: " + anyString.length);

                entities = repository.findByGuestContainsCaseInsensitive(anyString[0]);
            }
            default -> throw new InvalidSearchTypeException(
                    "The valid search types are: [TITLE, PRESENTER, GUESTS_IN, GUEST_CONTAINS]",
                    "The provided search type is not valid for searching by any string.",
                    searchType);
        }

        return this.mapOnlySuccessfulConversions(entities);
    }

    /**
     * Finds podcasts by any enum attribute (categories).
     *
     * @param searchType the type of search to perform
     * @param anyEnum    the enums to search for
     * @return a Flux containing the found podcast success DTOs
     * @throws InvalidSearchTypeException   if the search type is not valid for the
     *                                      method scope.
     * @throws SearchTypeArgumentsException if no enums are provided.
     *
     * @see SearchType Enum for valid search types.
     * 
     * @since 1.0
     */
    public Flux<PodcastSuccessDto> findByAnyEnum(SearchType searchType, Enum<?>... anyEnum) {
        if (anyEnum.length == 0)
            throw new SearchTypeArgumentsException(
                    "Invalid number of arguments, expected at least 1");

        Flux<Podcast> entities;

        switch (searchType) {
            case SearchType.CATEGORIES_IN -> {
                entities = repository.findByCategoriesIn(Stream.of(anyEnum)
                        .map(e -> (Category) e)
                        .toList());
            }
            default -> throw new InvalidSearchTypeException(
                    "The valid search types is: [CATEGORIES_IN]",
                    "The provided search type is not valid for searching by any enum.",
                    searchType);
        }

        return this.mapOnlySuccessfulConversions(entities);
    }

    /**
     * Finds podcasts by any integer attribute (year or year range).
     *
     * @param searchType the type of search to perform
     * @param anyInt     the integers to search for
     * @return a Flux containing the found podcast success DTOs
     * @throws InvalidSearchTypeException   if the search type is not valid for the
     *                                      method scope.
     * @throws SearchTypeArgumentsException if the number of arguments is invalid or
     *                                      if no integers are provided.
     *
     * @see SearchType Enum for valid search types.
     * 
     * @since 1.0
     */
    public Flux<PodcastSuccessDto> findByAnyInt(SearchType searchType, int... anyInt) {
        if (anyInt.length == 0)
            throw new SearchTypeArgumentsException(
                    "Invalid number of arguments, expected at least 1");

        Flux<Podcast> entities;

        switch (searchType) {
            case SearchType.YEAR -> {
                entities = repository.findByYear(anyInt[0]);
            }
            case SearchType.YEAR_BETWEEN -> {
                if (anyInt.length != 2)
                    throw new SearchTypeArgumentsException(
                            "Invalid number of arguments, expected 2, got: " + anyInt.length);

                entities = repository.findByYearBetween(anyInt[0], anyInt[1]);
            }
            default -> throw new InvalidSearchTypeException(
                    "The valid search types are: [YEAR, YEAR_BETWEEN]",
                    "The provided search type is not valid for searching by any integer.",
                    searchType);
        }

        return this.mapOnlySuccessfulConversions(entities);
    }

    // NOTE: This method is not implemented yet, but in the future will be used
    public Mono<Void> report(String id) {
        throw new UnsupportedOperationException("Unimplemented method 'report'");
    }

    @Override
    protected PodcastSuccessDto toDto(Podcast entity) {
        return mapper.toDto(entity);
    }
}
