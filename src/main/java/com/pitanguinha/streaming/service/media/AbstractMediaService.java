package com.pitanguinha.streaming.service.media;

import java.util.function.*;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.pitanguinha.streaming.dto.media.*;
import com.pitanguinha.streaming.dto.media.response.*;
import com.pitanguinha.streaming.domain.media.Media;
import com.pitanguinha.streaming.enums.media.MediaErrorType;
import com.pitanguinha.streaming.exceptions.domain.*;
import com.pitanguinha.streaming.enums.media.ConversionStatus;
import com.pitanguinha.streaming.service.*;
import com.pitanguinha.streaming.service.aws.*;
import com.pitanguinha.streaming.service.media.operation.MediaOperator;

import reactor.core.publisher.*;

/**
 * * Abstract class for media services, providing common functionality for
 * handling media entities.
 *
 * @param <E> The type of media entity.
 * @param <D> The type of media success DTO.
 * 
 * @since 1.0
 */
public abstract class AbstractMediaService<E extends Media, D extends MediaSuccessDto>
        implements MediaServiceInterface<E, D> {
    protected final AwsS3Service s3Service;
    protected final AwsCloudFrontService cloudFrontService;
    protected final TempDirService tempDirService;
    private final MediaOperator<E> mediaOperator;
    private final ReactiveCrudRepository<E, String> repository;

    /**
     * Constructs an AbstractMediaService with the specified dependencies.
     *
     * @param s3Service         The AWS S3 service for handling media storage.
     * @param cloudFrontService The AWS CloudFront service for handling access
     *                          media.
     * @param tempDirService    The service for managing temporary directories.
     * @param repository        The reactive CRUD repository for media entities.
     * @param mediaOperator     The media operator for handling media operations.
     */
    public AbstractMediaService(AwsS3Service s3Service, AwsCloudFrontService cloudFrontService,
            TempDirService tempDirService, ReactiveCrudRepository<E, String> repository,
            MediaOperator<E> mediaOperator) {
        this.s3Service = s3Service;
        this.cloudFrontService = cloudFrontService;
        this.tempDirService = tempDirService;
        this.repository = repository;
        this.mediaOperator = mediaOperator;
    }

    /**
     * Gets a signed URL for the content of a media entity.
     * 
     * <p>
     * Retrieves the media entity by its ID and generates a signed URL for its
     * content.
     * </p>
     * 
     * @param id The ID of the media entity.
     * 
     * @return A Mono containing the signed URL for the media content.
     * 
     * @throws NotFoundException if the entity does not exist.
     * 
     * @see #findByIdErrorHandler(String) Finds the media entity by ID with error
     *      handling.
     * @see AwsCloudFrontService#getSignedUrl(String) Gets the signed URL for the
     *      media content.
     * 
     * @since 1.0
     */
    public Mono<String> getContentSignedUrl(String id) {
        return findByIdErrorHandler(id)
                .map(entity -> cloudFrontService.getSignedUrl(entity.getContentKey()).url());
    }

    /**
     * Saves a media entity in S3 and the repository.
     * 
     * <p>
     * First is uploaded to S3, then saved in the repository.
     * </p>
     * 
     * @param entity        The media entity to save.
     * @param thumbnailFile The thumbnail file to upload.
     * @param contentFile   The content file to upload.
     * 
     * @return A Mono containing the saved media DTO.
     * 
     * @see #saveInRepository(Media) Saves the media in the repository.
     * @see MediaOperator#uploadOrUpdateToS3(Media, FilePart, FilePart)
     *      Uploads the media to S3.
     * @see #toDtoInternal(Media) Converts the entity to a DTO with a signed URL.
     * 
     * @since 1.0
     */
    protected Mono<D> saveInS3AndRepository(E entity, FilePart thumbnailFile, FilePart contentFile) {
        return saveInRepository(entity)
                .flatMap(savedEntity -> mediaOperator.uploadOrUpdateToS3(savedEntity, thumbnailFile, contentFile))
                .flatMap(this::toDtoInternal);
    }

    /**
     * Updates a media entity in S3 and the repository.
     * 
     * <p>
     * First is updated in S3, then saved in the repository.
     * </p>
     * 
     * @param putDto         The DTO containing the update information.
     * @param updateEntityFn Function to update the entity with the DTO data.
     * 
     * @return A Mono containing the updated media DTO.
     * 
     * @throws NotFoundException    if the entity does not exist.
     * @throws DomainStateException if the conversion status is pending.
     * 
     * @see #findByIdSwitchIfEmpty(String) Finds the media entity by ID.
     * @see #saveInS3AndRepository(Media, FilePart, FilePart)
     *      Reuses for saving in S3 and repository.
     * @see #errorIfConversionIsPending(Media) Checks if the conversion is pending.
     * 
     * @since 1.0
     */
    protected <P extends MediaPutDto> Mono<D> updateInS3AndRepository(P putDto, BiConsumer<E, P> updateEntityFn) {
        FilePart contentFile = putDto.getContentFile();
        FilePart thumbnailFile = putDto.getThumbnailFile();

        return findByIdSwitchIfEmpty(putDto.getId())
                .map(this::errorIfConversionIsPending)
                .flatMap(entity -> {
                    updateEntityFn.accept(entity, putDto);
                    entity.setConversionStatus(ConversionStatus.PENDING);
                    return saveInS3AndRepository(entity, thumbnailFile, contentFile);
                });
    }

    /**
     * Saves a media entity in the repository.
     * 
     * @param entity The media entity to save.
     * @return A Mono containing the saved media entity.
     * @since 1.0
     */
    private Mono<E> saveInRepository(E entity) {
        return repository.save(entity);
    }

    /**
     * Finds a media entity by its ID.
     * 
     * <p>
     * This method retrieves the media entity by its ID and checks if the
     * conversion status is successful. If the status is pending or error, an
     * exception is thrown.
     * </p>
     * 
     * @param id The ID of the media entity to find.
     * 
     * @return A Mono containing the media DTO if found, or an error if not found.
     * 
     * @throws NotFoundException    if the entity does not exist.
     * @throws DomainStateException if the conversion status is pending or
     *                              error.
     * 
     * @see #findByIdErrorHandler(String) Finds the media entity by ID with error
     *      handling.
     * @see #toDtoInternal(Media) Converts the entity to a DTO with a signed
     * 
     * @since 1.0
     */
    public Mono<D> findById(String id) {
        return findByIdErrorHandler(id)
                .flatMap(this::toDtoInternal);
    }

    /**
     * Finds a media entity by its ID with error handling.
     * 
     * <p>
     * Checks retrieved entity for conversion status and throws an error if the
     * status is not successful (i.e., pending or error).
     * </p>
     * 
     * @param id The ID of the media entity.
     * 
     * @return A Mono containing the media DTO if found, or an error if not found.
     * 
     * @throws NotFoundException    if the entity does not exist.
     * @throws DomainStateException if the conversion status is pending or
     *                              error.
     * 
     * @see #findByIdSwitchIfEmpty(String) Finds the media entity by ID.
     * @see #errorIfConversionNotSuccessful(Media) Checks if the conversion was
     *      Successful.
     * 
     * @since 1.0
     */
    protected Mono<E> findByIdErrorHandler(String id) {
        return findByIdSwitchIfEmpty(id)
                .map(this::errorIfConversionNotSuccessful);
    }

    /**
     * Finds a media entity by its ID, returning an error if not found.
     * 
     * @param id The ID of the media entity to find.
     * 
     * @return A Mono containing the media entity if found, or an error if not
     *         found.
     * 
     * @throws NotFoundException if the entity does not exist.
     * 
     * @since 1.0
     */
    private Mono<E> findByIdSwitchIfEmpty(String id) {
        return repository.findById(id)
                .switchIfEmpty(
                        Mono.error(new NotFoundException("The entity does not exist", "not found for id: " + id)));
    }

    /**
     * Finds all media entities for a specific owner with error handling.
     * 
     * <p>
     * Maps the entities to DTOs and wraps any errors in a MediaResponseDto.
     * </p>
     * 
     * @param entities The Flux of media entities to process.
     * 
     * @return A Flux of MediaResponseDto containing the results or error
     *         information.
     * 
     * @see #mapOrWrapErrorDto(Flux) Maps entities to DTOs or wraps
     *      errors.
     * 
     * @since 1.0
     */
    protected Flux<MediaResponseDto> findAllForOwnerWithErrorWrap(Flux<E> entities) {
        return mapOrWrapErrorDto(entities);
    }

    /**
     * Deletes a media entity by its ID.
     * 
     * <p>
     * This method checks the conversion status and deletes the entity from S3 and
     * the repository if the conversion is successful or has failed.
     * </p>
     * 
     * @param id The ID of the media entity to delete.
     * 
     * @return A Mono that completes when the deletion is done.
     * 
     * @throws NotFoundException    if the entity does not exist.
     * @throws DomainStateException if the conversion status is pending or
     *                              error.
     * 
     * @see #deleteIfConversionNotPending(String) Deletes the media entity if its
     *      conversion is not pending.
     * 
     * @since 1.0
     */
    public Mono<Void> delete(String id) {
        return deleteIfConversionNotPending(id);
    }

    /**
     * Deletes a media entity if its conversion is not pending.
     * 
     * <p>
     * Checks the conversion status and deletes the entity from S3 and the
     * repository if the conversion is successful or has failed.
     * </p>
     * 
     * @param id The ID of the media entity to delete.
     * 
     * @return A Mono that completes when the deletion is done.
     * 
     * @throws NotFoundException    if the entity does not exist.
     * @throws DomainStateException if the conversion status is pending or
     *                              error.
     * 
     * @see #findByIdSwitchIfEmpty(String) Finds the media entity by ID.
     * @see #errorIfConversionIsPending(Media) Checks if the conversion is pending.
     * @see #deleteFromS3AndRepository(Media) Deletes the media entity from S3 and
     *      the repository.
     * 
     * @since 1.0
     */
    protected Mono<Void> deleteIfConversionNotPending(String id) {
        return findByIdSwitchIfEmpty(id)
                .map(this::errorIfConversionIsPending)
                .flatMap(this::deleteFromS3AndRepository);
    }

    /**
     * Deletes a media entity from S3 and the repository.
     * 
     * @param entity The media entity to delete.
     * 
     * @return A Mono that completes when the deletion is done.
     * 
     * @see AwsS3Service#deleteArtifact(String) Deletes the media artifact from S3.
     * 
     * @since 1.0
     */
    private Mono<Void> deleteFromS3AndRepository(E entity) {
        return s3Service.deleteArtifact(entity.getId())
                .then(repository.delete(entity));
    }

    /**
     * Checks if the conversion status of a media entity is successful.
     * 
     * <p>
     * If the conversion status is pending or error, an exception is thrown.
     * </p>
     * 
     * @param entity The media entity to check.
     * @return The media entity if the conversion was successful.
     * @throws DomainStateException if the conversion status is pending or error.
     * @see #errorIfConversionIsPending(Media) Checks if the conversion is pending.
     * @since 1.0
     */
    protected E errorIfConversionNotSuccessful(E entity) {
        errorIfConversionIsPending(entity);

        if (entity.getConversionStatus() == ConversionStatus.ERROR)
            throw new DomainStateException("Conversion failed", "error");

        return entity;
    }

    /**
     * Checks if the conversion status of a media entity is pending.
     * 
     * <p>
     * If the conversion status is pending, an exception is thrown.
     * </p>
     * 
     * @param entity The media entity to check.
     * @return The media entity if the conversion is not pending.
     * @throws DomainStateException if the conversion status is pending.
     * @since 1.0
     */
    protected E errorIfConversionIsPending(E entity) {
        ConversionStatus status = entity.getConversionStatus();

        if (status == null)
            throw new DomainStateException("Conversion status is null", "null");

        if (status == ConversionStatus.PENDING)
            throw new DomainStateException("Conversion is still pending", "pending");

        return entity;
    }

    /**
     * Maps a Flux of media entities to MediaResponseDto, wrapping errors in a
     * MediaErrorDto.
     * 
     * <p>
     * Filters out entities with null conversion status and maps them to DTOs or
     * wraps errors.
     * </p>
     * 
     * @param entities The Flux of media entities to process.
     * 
     * @return A Flux of MediaResponseDto containing the results or error
     *         information.
     * 
     * @see #toDtoInternal(Media) Converts the entity to a DTO with a signed url.
     * @see #buildErrorDto(String, String, MediaErrorType) Builds a MediaErrorDto
     *      for entities with pending or failed conversion status.
     * 
     * @since 1.0
     */
    protected Flux<MediaResponseDto> mapOrWrapErrorDto(Flux<E> entities) {
        return entities
                .distinct(E::getId)
                .filter(entity -> entity.getConversionStatus() != null)
                .flatMap(entity -> {
                    if (entity.getConversionStatus() == ConversionStatus.PENDING)
                        return buildErrorDto(entity.getId(), "Conversion is still pending",
                                MediaErrorType.CONVERSION_PENDING);

                    if (entity.getConversionStatus() == ConversionStatus.ERROR)
                        return buildErrorDto(entity.getId(), "Conversion failed", MediaErrorType.CONVERSION_FAILED);

                    return toDtoInternal(entity);
                });
    }

    /**
     * Maps a Flux of media entities to DTOs, filtering out those with unsuccessful
     * conversion statuses.
     * 
     * <p>
     * Only entities with successful conversion status are mapped to DTOs.
     * </p>
     * 
     * @param entities The Flux of media entities to process.
     * 
     * @return A Flux of DTOs containing only successfully converted entities.
     * 
     * @see #toDtoInternal(Media) Converts the entity to a DTO with a signed url.
     * @since 1.0
     */
    protected Flux<D> mapOnlySuccessfulConversions(Flux<E> entities) {
        return entities
                .distinct(E::getId)
                .filter(entity -> entity.getConversionStatus() != null)
                .filter(entity -> entity.getConversionStatus() == ConversionStatus.SUCCESS)
                .flatMap(entity -> toDtoInternal(entity));
    }

    /**
     * Builds a MediaErrorDto with the specified parameters.
     * 
     * @param id        The ID of the media entity.
     * @param message   The error message.
     * @param errorType The type of error.
     * 
     * @return A Mono containing the MediaErrorDto.
     * 
     * @since 1.0
     */
    private Mono<MediaErrorDto> buildErrorDto(String id, String message, MediaErrorType errorType) {
        return Mono.just(MediaErrorDto.builder()
                .id(id)
                .message(message)
                .errorType(errorType)
                .build());
    }

    /**
     * Converts a media entity to a DTO with a dynamically signed thumbnail URL.
     * 
     * @param entity The media entity to convert.
     * @return A Mono containing the media DTO with the signed thumbnail URL.
     * 
     * @see AwsCloudFrontService#getSignedUrl(String) Gets the signed URL
     *      for the media thumbnail.
     * @see #toDto(Media) Converts the entity to a DTO.
     * @since 1.0
     */
    private Mono<D> toDtoInternal(E entity) {
        D dto = toDto(entity);
        String thumbnailKey = entity.getId() + "/" + entity.getThumbnailSuffix();
        dto.setThumbnailUrl(cloudFrontService.getSignedUrl(thumbnailKey).url());
        return Mono.just(dto);
    }

    /**
     * Converts a media entity to a Succces Dto.
     * 
     * @param entity The media entity to convert.
     * @return the media success DTO.
     * 
     * @since 1.0
     */
    abstract protected D toDto(E entity);
}
