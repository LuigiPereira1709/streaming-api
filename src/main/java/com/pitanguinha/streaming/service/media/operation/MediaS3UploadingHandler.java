package com.pitanguinha.streaming.service.media.operation;

import java.util.*;
import java.nio.file.*;

import org.slf4j.*;

import org.springframework.stereotype.Component;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.pitanguinha.streaming.domain.media.Media;
import com.pitanguinha.streaming.service.TempDirService;
import com.pitanguinha.streaming.service.aws.AwsS3Service;
import static com.pitanguinha.streaming.enums.aws.ContentType.JSON;
import com.pitanguinha.streaming.enums.exceptions.*;
import com.pitanguinha.streaming.exceptions.aws.s3.S3Exception;
import com.pitanguinha.streaming.exceptions.internal.InternalException;

import reactor.core.publisher.*;
import jakarta.annotation.PostConstruct;

/**
 * Service for uploading media files to S3.
 *
 * @param <E> the type of media entity
 * 
 * @since 1.0
 */
@Component
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
class MediaS3UploadingHandler<E extends Media> {
    private static final long ASYNC_FILE_SIZE_LIMIT = 100 * 1024 * 1024; // 100MB
    private final static Logger LOG = LoggerFactory.getLogger(MediaS3UploadingHandler.class);

    private final AwsS3Service s3Service;
    private final TempDirService tempDirService;
    private final ObjectMapper objectMapper;
    private Path workDir;

    /**
     * Constructor for MediaS3UploadingService.
     *
     * @param s3Service      the S3 service
     * @param tempDirService the temporary directory service
     * @param objectMapper   the object mapper
     */
    public MediaS3UploadingHandler(AwsS3Service s3Service, TempDirService tempDirService, ObjectMapper objectMapper) {
        this.s3Service = s3Service;
        this.tempDirService = tempDirService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void getOrCreateWorkDir() {
        this.workDir = tempDirService.getOrCreateDir("uploads");
    }

    // /**
    // * Uploads or updates media files to S3.
    // *
    // * <p>
    // * This method handles the upload of media files to S3, including thumbnail
    // and
    // * content files. It determines whether to upload large files asynchronously
    // or
    // * normal files synchronously based on their size.<br>
    // * If the content file is larger than the limit defined in
    // * {@link #ASYNC_FILE_SIZE_LIMIT},
    // * it uploads the files asynchronously. Otherwise, it uploads the files
    // * synchronously.<br>
    // * It also uploads the metadata as a JSON string, even if no files are
    // * provided.
    // * </p>
    // *
    // * @param entity the media entity
    // * @param thumbnailFile the thumbnail file
    // * @param contentFile the content file
    // *
    // * @return A Mono of the media entity after uploading whatever are provided.
    // *
    // * @throws RuntimeException if there is an error uploading the media files
    // *
    // * @see #uploadAllInBackground(Media, Path...) Uploads large files to S3.
    // * @see #uploadNormal(Media, Path...) Uploads normal files to S3.
    // * @see #uploadJsonMetadata(Media) Uploads the metadata as a JSON string.
    // *
    // * @since 1.0
    // */
    // public Mono<E> uploadOrUpdateMedia(E entity, FilePart thumbnailFile, FilePart
    // contentFile) {
    // String id = entity.getId();
    // return getFilePaths(id, thumbnailFile, contentFile)
    // .flatMap(filesPaths -> {
    // updateDurationAndStatus(entity, filesPaths, contentFile != null);

    // Mono<Void> uploadMono = Mono.empty();
    // if (contentFile != null && isLargeFile(filesPaths)) {
    // uploadAllInBackground(entity, filesPaths);
    // } else if (thumbnailFile != null || contentFile != null) {
    // uploadMono = uploadNormal(entity, filesPaths);
    // } else {
    // uploadMono = uploadJsonMetadata(entity);
    // }

    // return uploadMono
    // .thenReturn(entity);
    // });
    // }

    // /**
    // * Uploads files to S3 asynchronously if they are large.
    // *
    // * <p>
    // * This method uploads files with a size greater than the limit defined in
    // * {@link #ASYNC_FILE_SIZE_LIMIT}.<br>
    // * It uploads the files to S3 and then uploads the metadata as a JSON
    // * string.<br>
    // * Ps: It's fire and forget, meaning it does not wait for the upload to
    // * complete.
    // * </p>
    // *
    // * @param entity the media entity
    // * @param paths the paths to the files to be uploaded
    // *
    // * @return A Mono of the media entity after uploading the files and metadata.
    // *
    // * @see AwsS3Service#uploadFileAsync(String, Path) Uploads a file to S3.
    // * @see #uploadNormal(String, String, Path...) Uploads files with a size
    // * less than the limit.
    // * @see #uploadJsonMetadata(Media) Uploads the metadata as a JSON string.
    // *
    // * @since 1.0
    // */
    // private void uploadAllInBackground(E entity, Path... paths) {
    // Mono.defer(() -> {
    // Path thumbnailPath = paths.length == 2 ? paths[0] : null;
    // Path contentPath = thumbnailPath == null ? paths[0] : paths[1];

    // String id = entity.getId();
    // String contentKey = id + "/" + contentPath.getFileName().toString();

    // return Mono.fromFuture(s3Service.uploadFileAsync(contentKey, contentPath))
    // .flatMap(response -> {
    // if (!response.sdkHttpResponse().isSuccessful()) {
    // return Mono.error(new S3Exception("Error async uploading file to S3 for
    // entity: " + id,
    // contentKey, S3OperationException.UPLOAD_FAILED, SeverityLevel.HIGH));
    // }

    // return thumbnailPath != null
    // ? uploadNormal(entity, thumbnailPath)
    // : uploadJsonMetadata(entity);
    // })
    // .doOnSubscribe(
    // $ -> LOG.info("Large file upload started for id: {} size: {}", id,
    // contentPath.toFile().length()))
    // .doOnSuccess($ -> LOG.info("Large file upload completed for id: {}", id))
    // .doOnError(e -> LOG.error("Error uploading large file to S3 for id: {} - {}",
    // id, e.getMessage()));
    // }).subscribe();
    // }

    /**
     * Uploads or updates media files to S3.
     *
     * <p>
     * This method handles the upload of media files to S3, including thumbnail
     * and content files.<br>
     * It also uploads the metadata as a JSON string, even if no files are
     * provided.
     * </p>
     *
     * @param entity        the media entity
     * @param thumbnailFile the thumbnail file
     * @param contentFile   the content file
     *
     * @return A Mono of the media entity after uploading whatever are provided.
     *
     * @throws S3Exception If content file is too large for upload.
     *
     * @see #uploadNormal(Media, Path...) Uploads normal files to S3.
     * @see #uploadJsonMetadata(Media) Uploads the metadata as a JSON string.
     *
     * @since 1.0
     */

    public Mono<E> uploadOrUpdateMedia(E entity, FilePart thumbnailFile, FilePart contentFile) {
        String id = entity.getId();
        return getFilePaths(entity, thumbnailFile, contentFile)
                .flatMap(filesPaths -> {
                    if (contentFile != null && isLargeFile(filesPaths)) {
                        throw new S3Exception(
                                "Content file is too large for upload at moment, please try a smaller file\n"
                                        + "Size supported: " + ASYNC_FILE_SIZE_LIMIT / (1024 * 1024) + "MB",
                                id, S3OperationException.UPLOAD_FAILED, SeverityLevel.LOW);
                    }

                    return (thumbnailFile != null || contentFile != null
                            ? uploadNormal(entity, filesPaths)
                            : uploadJsonMetadata(entity))
                            .thenReturn(entity);
                });
    }

    /**
     * Uploads files with a size less than the limit defined in
     * {@link #ASYNC_FILE_SIZE_LIMIT}.
     * 
     * <p>
     * This method uploads files with a size less than the limit defined in
     * {@link #ASYNC_FILE_SIZE_LIMIT}.<br>
     * It uploads the files to S3 and then uploads the metadata as a JSON string.
     * </p>
     *
     * @param entity the media entity
     * @param paths  the paths to the files to be uploaded
     * 
     * @return A boolean indicating whether the upload was successful.
     * 
     * @throws S3Exception if there is an error uploading the files to S3.
     *
     * @see AwsS3Service#uploadFilesTransactional(String, Path...) Uploads files
     *      to S3.
     * @see #uploadJsonMetadata(Media) Uploads the metadata as a JSON string.
     * 
     * @since 1.0
     */
    private Mono<Boolean> uploadNormal(E entity, Path... paths) {
        return s3Service.uploadFilesTransactional(entity.getId(), paths)
                .flatMap(response -> {
                    if (!response) {
                        LOG.error("Error uploading files to S3 for id: {}", entity.getId());
                        throw new S3Exception("Error uploading files to S3 for entity: " + entity.getId(),
                                entity.getId() + "/files", S3OperationException.UPLOAD_FAILED,
                                SeverityLevel.MEDIUM);
                    }
                    return uploadJsonMetadata(entity);
                });
    }

    /**
     * Uploads the metadata as a JSON string.
     * 
     * <p>
     * This method uploads the metadata to S3 as a JSON string.
     * </p>
     *
     * @param entity the media entity
     * 
     * @return A boolean indicating whether the upload was successful.
     * 
     * @throws S3Exception if there is an error uploading the metadata.
     * 
     * @see AwsS3Service#uploadFromString(String, String, String) Uploads a string
     *      to S3.
     * @see #createJsonWithMetadata(Media) Creates a JSON string with metadata
     *      from the entity.
     * 
     * @since 1.0
     */
    private Mono<Boolean> uploadJsonMetadata(E entity) {
        return s3Service.uploadFromString(entity.getId() + "/metadata.json", JSON, createJsonWithMetadata(entity))
                .map(response -> {
                    if (!response) {
                        LOG.error("Error uploading metadata to S3 for id: {}", entity.getId());
                        throw new S3Exception("Error uploading metadata to S3 for entity: " + entity.getId(),
                                entity.getId() + "/metadata.json", S3OperationException.UPLOAD_FAILED,
                                SeverityLevel.HIGH);
                    }
                    return true;
                });
    }

    /**
     * Transfers the files to a temporary directory.
     * 
     * <p>
     * The temporary directory is created using the {@link #workDir} and the ID
     * of the media entity.
     * </p>
     *
     * @param entity        the media entity
     * @param thumbnailFile the thumbnail file
     * @param contentFile   the content file
     * 
     * @return an array of paths to the transferred files
     * 
     * @see MediaUtils#transferTo(Path, String, FilePart) Transfers a file to a
     *      specified directory.
     * @see TempDirService#getOrCreateDir(String) Gets or creates a
     *      temporary directory.
     * 
     * @since 1.0
     */
    private Mono<Path[]> getFilePaths(E entity, FilePart thumbnailFile, FilePart contentFile) {
        if (thumbnailFile == null && contentFile == null) {
            return Mono.just(new Path[0]);
        }

        String id = entity.getId();

        Path dirPath = tempDirService.getOrCreateDir(workDir.resolve(id + "-" + System.currentTimeMillis()).toString());
        List<Mono<Path>> transfers = new ArrayList<>();

        if (thumbnailFile != null) {
            transfers.add(MediaUtils.transferTo(dirPath, "thumbnail", thumbnailFile)
                    .onErrorResume(e -> {
                        LOG.error("Error transferring thumbnail file to temporary directory: {}", e.getMessage());
                        return Mono.error(new InternalException(
                                "Error transferring thumbnail file for entity: " + id, MediaS3UploadingHandler.class,
                                SeverityLevel.HIGH, e));
                    }));
        }

        if (contentFile != null) {
            transfers.add(MediaUtils.transferTo(dirPath, "content", contentFile)
                    .onErrorResume(e -> {
                        LOG.error("Error transferring content file to temporary directory: {}", e.getMessage());
                        return Mono.error(new InternalException(
                                "Error transferring content file for entity: " + id, MediaS3UploadingHandler.class,
                                SeverityLevel.HIGH, e));
                    }));
        }

        return Flux.concat(transfers)
                .collectList()
                .map(list -> list.toArray(new Path[0]));
    }

    /**
     * Creates a JSON string with metadata from the entity.
     *
     * @param entity the media entity
     * 
     * @return the JSON string with metadata
     * 
     * @see Media#getMetadata() Gets the metadata of the media entity.
     * 
     * @throws InternalException if there is an error creating the JSON string.
     * 
     * @since 1.0
     */
    private String createJsonWithMetadata(E entity) {
        Map<String, String> metadata = entity.getMetadata();

        String json;

        try {
            json = objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            LOG.error("Error creating JSON with metadata: {}", e.getMessage());
            throw new InternalException(
                    "Error creating JSON with metadata for entity: " + entity.getId(),
                    MediaS3UploadingHandler.class, SeverityLevel.CRITICAL, e);
        }

        return json;
    }

    /**
     * Checks if the provided files are large files.
     *
     * @param filesPaths the paths to the files
     * 
     * @return true if the content file is larger than the limit, false otherwise
     * 
     * @since 1.0
     */
    private boolean isLargeFile(Path[] filesPaths) {
        Path contentFile = filesPaths.length == 2 ? filesPaths[1] : filesPaths[0];
        return contentFile.toFile().length() >= ASYNC_FILE_SIZE_LIMIT;
    }
}
