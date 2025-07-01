package com.pitanguinha.streaming.service.media;

import java.util.*;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.pitanguinha.streaming.dto.music.*;
import com.pitanguinha.streaming.dto.media.*;
import com.pitanguinha.streaming.dto.media.response.MediaResponseDto;
import com.pitanguinha.streaming.domain.media.*;
import com.pitanguinha.streaming.enums.media.music.*;
import com.pitanguinha.streaming.exceptions.search.*;
import com.pitanguinha.streaming.enums.media.SearchType;
import com.pitanguinha.streaming.mapper.media.MusicMapper;
import com.pitanguinha.streaming.repository.media.MusicRepository;
import com.pitanguinha.streaming.service.TempDirService;
import com.pitanguinha.streaming.service.aws.*;
import com.pitanguinha.streaming.service.media.operation.MediaOperator;

import reactor.core.publisher.*;

/**
 * Service for managing music media.
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
@ConditionalOnProperty(name = "spring.application.services.music.enabled", havingValue = "true", matchIfMissing = false)
public class MusicService extends AbstractMediaService<Music, MusicSuccessDto> {
    private final MusicRepository repository;
    private final MusicMapper mapper;

    @Autowired
    public MusicService(MusicRepository repository, AwsS3Service s3Service, AwsCloudFrontService cloudFrontService,
            TempDirService tempDirService, MediaOperator<Music> mediaOperator, MusicMapper mapper) {
        super(s3Service, cloudFrontService, tempDirService, repository, mediaOperator);
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Saves a new music to the repository and S3 storage.
     * 
     * @param postDto the DTO containing the music post data
     * @return a Mono containing the saved music success DTO
     * @throws S3Exception If the upload to S3 fails.
     * @since 1.0
     */
    public Mono<MusicSuccessDto> save(MediaPostDto postDto) {
        return this.saveInS3AndRepository(mapper.toEntity((MusicPostDto) postDto), postDto.getThumbnailFile(),
                postDto.getContentFile());
    }

    /**
     * Updates an existing music in the repository and S3 storage.
     * 
     * @param putDto the DTO containing the music update data
     * @return a Mono containing the updated music success DTO
     * @throws DomainStateException If the conversion status is pending or failed.
     * @throws NotFoundException    If the music to update is not found.
     * @throws S3Exception          If the upload to S3 fails.
     * 
     * @since 1.0
     */
    public Mono<MusicSuccessDto> update(MediaPutDto putDto) {
        return this.updateInS3AndRepository((MusicPutDto) putDto, mapper::updateFromPutDto);
    }

    /**
     * Finds all music media for a given owner ID.
     * 
     * @param ownerId the ID of the owner
     * @return a Flux containing all music media for the owner
     * @throws IllegalStateException if the owner is not found.
     * @since 1.0
     */
    // NOTE: This method is not used in the current implementation, but in the
    // future will be used
    public Flux<MediaResponseDto> findAllForOwner(String ownerId) {
        return this.findAllForOwnerWithErrorWrap(repository.findByArtistRegexCaseInsensitive(ownerId));
    }

    /**
     * Finds music media by any string, such as title, artist, album, etc.
     * 
     * @param searchType the type of search to perform
     * @param anyString  the strings to search for
     * @return a Flux containing the found music success DTOs
     * @throws InvalidSearchTypeException   if the search type is not valid for the
     *                                      method scope.
     * @throws SearchTypeArgumentsException if no strings are provided or if the
     *                                      number of arguments is invalid.
     * @since 1.0
     */
    public Flux<MusicSuccessDto> findByAnyString(SearchType searchType, String... anyString) {
        if (anyString.length == 0)
            return Flux.error(new SearchTypeArgumentsException("Invalid number of arguments, expected at least 1"));

        Flux<Music> entities;
        switch ((SearchType) searchType) {
            case SearchType.TITLE -> entities = repository.findByTitleRegexCaseInsensitive(anyString[0]);
            case SearchType.ARTIST -> entities = repository.findByArtistRegexCaseInsensitive(anyString[0]);
            case SearchType.ALBUM -> entities = repository.findByAlbumRegexCaseInsensitive(anyString[0]);
            case SearchType.FEAT_IN -> entities = repository.findByFeatsIn(Arrays.asList(anyString));
            case SearchType.FEAT_CONTAINS -> {
                if (anyString.length != 1)
                    throw new SearchTypeArgumentsException(
                            "Invalid number of arguments, expected 1, got: " + anyString.length);

                entities = repository.findByFeatsContains(anyString[0]);
            }

            default -> throw new InvalidSearchTypeException(
                    "The valid search types is: [TITLE, ARTIST, ALBUM, FEAT_IN, FEAT_CONTAINS]",
                    "The provided search type is not valid for searching by any string.",
                    searchType);
        }

        return this.mapOnlySuccessfulConversions(entities);
    }

    /**
     * Finds music media by any enum, such as genre or moods.
     * 
     * @param searchType the type of search to perform
     * @param anyEnum    the enums to search for
     * @return a Flux containing the found music success DTOs
     * @throws SearchTypeArgumentsException if no enums are provided.
     * @throws InvalidSearchTypeException   if the search type is not valid for the
     *                                      method scope.
     * @since 1.0
     */
    public Flux<MusicSuccessDto> findByAnyEnum(SearchType searchType, Enum<?>... anyEnum) {
        if (anyEnum.length == 0)
            throw new SearchTypeArgumentsException("Invalid number of arguments, expected at least 1");

        Flux<Music> entities;

        switch (searchType) {
            case SearchType.GENRE ->
                entities = repository.findByGenre((Genre) anyEnum[0]);
            case SearchType.MOODS_IN -> {
                entities = repository.findByMoodsIn(Stream.of(anyEnum)
                        .map(e -> (Mood) e)
                        .toList());
            }
            default -> throw new InvalidSearchTypeException(
                    "The valid search types is: [GENRE, MOODS_IN]",
                    "The provided search type is not valid for searching by any enum.",
                    searchType);
        }

        return this.mapOnlySuccessfulConversions(entities);
    }

    /**
     * Finds music media by any integer, such as year or year range.
     * 
     * @param searchType the type of search to perform
     * @param anyInt     the integers to search for
     * @return a Flux containing the found music success DTOs
     * @throws InvalidSearchTypeException   if the search type is not valid for the
     *                                      method scope.
     * @throws SearchTypeArgumentsException if no integers are provided or if the
     *                                      number of arguments is invalid for the
     *                                      given search type.
     * @since 1.0
     */
    public Flux<MusicSuccessDto> findByAnyInt(SearchType searchType, int... anyInt) {
        if (anyInt.length == 0)
            throw new SearchTypeArgumentsException(
                    "Invalid number of arguments, expected at least 1");

        Flux<Music> entities;

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
                    "The valid search types is: [YEAR, YEAR_BETWEEN]",
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
    protected MusicSuccessDto toDto(Music entity) {
        return mapper.toDto(entity);
    }
}
