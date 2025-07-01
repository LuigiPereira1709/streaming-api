package com.pitanguinha.streaming.service.aws;

import java.net.URL;
import java.nio.file.*;
import java.time.Duration;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.*;

import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.pitanguinha.streaming.enums.aws.ContentType;

import reactor.core.publisher.Mono;

import com.pitanguinha.streaming.config.aws.s3.S3Properties;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * Service class for handling S3 operations.
 * 
 * @since 1.0
 */
@Service
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true", matchIfMissing = false)
public class AwsS3Service {
    private final static Logger LOG = LoggerFactory.getLogger(AwsS3Service.class);

    private final S3Client client;
    private final S3AsyncClient asyncClient;
    private final S3Properties properties;
    private final S3Presigner presigner;

    public AwsS3Service(S3Client client, S3AsyncClient asyncClient, S3Properties properties, S3Presigner presigner) {
        this.client = client;
        this.asyncClient = asyncClient;
        this.properties = properties;
        this.presigner = presigner;
    }

    /**
     * Generates a presigned URL for an object in S3.
     * 
     * <p>
     * The presigned URL is valid for 1 hour and can be used to access the object
     * directly.
     * </p>
     * 
     * @param key The key of the object in S3.
     * 
     * @return The presigned URL for the object.
     * 
     * @since 1.0
     */
    public Mono<URL> presignedUrl(String key) {
        var presignedRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(objRequest -> objRequest
                        .bucket(properties.bucketName())
                        .key(key)
                        .build())
                .build();

        return Mono.just(presigner.presignGetObject(presignedRequest).url())
                .doOnError(e -> LOG.error("Failed to generate presigned URL for key: {}", key, e))
                .doOnSuccess(url -> LOG.info("Generated presigned URL for key: {}", key));
    }

    /**
     * Uploads a string to S3.
     * 
     * <p>
     * The string is uploaded using the specified key and content type.<br>
     * The key should be formatted with a trailing slash ("/") to indicate a folder
     * and the name of the file with the extension.
     * </p>
     * 
     * @param key         The S3 key for the string.
     * @param contentType The content type of the string.
     * @param requestBody The string to upload.
     * 
     * @return A boolean Mono indicating whether the upload was successful or not.
     * 
     * @see #putObjectRequest(String, String, String) Used to upload the string.
     * 
     * @since 1.0
     */
    public Mono<Boolean> uploadFromString(String key, ContentType contentType, String requestBody) {
        if (!putObjectRequest(key, contentType.value, requestBody)) {
            LOG.error("Upload String - failed to upload string: {}", key);
            return Mono.just(false);
        }

        LOG.info("Upload String - uploaded string successfully: {}", key);
        return Mono.just(true);
    }

    /**
     * Uploads a file to S3.
     * 
     * <p>
     * The file is uploaded using the specified key and content type.<br>
     * The key will be formatted with a trailing slash ("/") to indicate a folder
     * and the name of the file are appended to they key.
     * </p>
     * 
     * @param key  The S3 key for the file.
     * @param file The file to upload.
     * 
     * @return A boolean Mono indicating whether the upload was successful or not.
     * 
     * @see #putObjectRequest(String, Path) Used to upload the file.
     * @see #formatKey(String) Used to format the key with a trailing slash.
     * 
     * @since 1.0
     */
    public Mono<Boolean> uploadFile(String key, Path file) {
        String fullKey = formatKey(key) + file.getFileName().toString();

        if (!putObjectRequest(fullKey, file)) {
            LOG.error("Upload File - failed to upload file: {}", key);
            return Mono.just(false);
        }

        LOG.info("Upload File - uploaded file successfully: {}", key);
        return Mono.just(true);
    }

    /**
     * Uploads a file to S3 asynchronously.
     * 
     * <p>
     * The file is uploaded using the specified key and content type.<br>
     * The key will be formatted with a trailing slash ("/") to indicate a folder
     * and the name of the file are appended to they key.<br>
     * The throwable is logged if the upload fails, but the caller should handler
     * the error appropriately.
     * </p>
     * 
     * @param key  The S3 key for the file.
     * @param file The file to upload.
     * 
     * @return A CompletableFuture containing the PutObjectResponse.
     * 
     * @see #putObjectRequest(String, Path) Used to upload the file.
     * @see #formatKey(String) Used to format the key with a trailing slash.
     * 
     * @since 1.0
     */
    public CompletableFuture<PutObjectResponse> uploadFileAsync(String key, Path file) {
        String fullKey = formatKey(key) + file.getFileName().toString();

        return asyncClient.putObject(b -> b
                .bucket(properties.bucketName())
                .key(fullKey)
                .contentType(getContentType(file)), AsyncRequestBody.fromFile(file))
                .whenComplete(($, error) -> {
                    if (error != null) {
                        LOG.error("Upload File Async - failed to upload file: {}", key, error);
                    } else {
                        LOG.info("Upload File Async - uploaded file successfully: {}", key);
                    }
                });
    }

    /**
     * Uploads files to S3 with a transactional approach.
     * 
     * <p>
     * Try to upload all files, file by file, if any file fails to upload,<br>
     * it will rollback all previously uploaded files.<br>
     * This is done by deleting the successfully uploaded files, ensuring that the
     * S3 bucket remains in a consistent state.<br>
     * The key should be formatted with a trailing slash ("/") to indicate a folder
     * and the name of the file with the extension.
     * </p>
     * 
     * @param key   The S3 key prefix for the files.
     * @param files The files to upload.
     * 
     * @return A boolean Mono indicating whether the upload was successful or
     *         not.
     * 
     * @see #formatKey(String) Used to format the key with a trailing slash.
     * @see #putObjectRequest(String, Path) Used to upload each file.
     * @see #rollbackUploadFiles(List) Used to rollback the uploaded files in case
     *      of failure.
     * 
     * @since 1.0
     */
    public Mono<Boolean> uploadFilesTransactional(String key, Path... files) {
        List<String> uploadedKeys = new ArrayList<>();

        for (Path file : files) {
            String fullKey = formatKey(key) + file.getFileName().toString();

            if (!putObjectRequest(fullKey, file)) {
                LOG.error("Upload Files Transactional - failed to upload file: {}", fullKey);
                rollbackUploadFiles(key, uploadedKeys);
                return Mono.just(false);
            }

            uploadedKeys.add(fullKey);
        }

        LOG.info("Upload Files Transactional - all files uploaded successfully. Keys: {}", uploadedKeys);
        return Mono.just(true);
    }

    /**
     * Rollbacks the uploaded files by deleting them from S3.
     * 
     * @param key           The S3 key prefix for the files.
     * @param uploadedFiles The list of uploaded files to delete.
     * 
     * @see #deleteArtifact(String) Used to delete the uploaded files.
     * 
     * @since 1.0
     */
    private void rollbackUploadFiles(String key, List<String> uploadedFiles) {
        int uploadedFilesCount = uploadedFiles.size();
        LOG.info("Rolling back upload files - deleting {} files with prefix: {}", uploadedFilesCount, key);

        AtomicInteger deletedFilesCount = new AtomicInteger(0);
        uploadedFiles.stream()
                .map(file -> file.substring(file.lastIndexOf("/") + 1))
                .forEach(fullKey -> {
                    if (!deleteObjectRequest(fullKey)) {
                        LOG.error("Rollback - failed to delete file: {}", fullKey);
                        return;
                    }
                    LOG.info("Rollback - deleted file: {}", fullKey);
                    deletedFilesCount.incrementAndGet();
                });

        if (deletedFilesCount.get() == uploadedFilesCount) {
            LOG.info("Rollback - all {} files deleted successfully.", uploadedFilesCount);
            return;
        }

        LOG.error("Rollback - failed to delete {} files.", uploadedFilesCount - deletedFilesCount.get());
    }

    /**
     * Puts an object request to S3.
     * 
     * <p>
     * Puts an object request to S3 using the specified key, content type, and
     * request body from a string.
     * </p>
     * 
     * @param key         The S3 key for the string.
     * @param contentType The content type of the string.
     * @param requestBody The string to upload.
     * 
     * @return true if the upload is successful, false otherwise.
     * 
     * @see #putObjectRequest(String, String, String) Used to upload the string.
     * 
     * @since 1.0
     */
    private boolean putObjectRequest(String key, String contentType, String requestBody) {
        return client.putObject(buildPutObject(key, contentType),
                RequestBody.fromString(requestBody)).sdkHttpResponse().isSuccessful();
    }

    /**
     * Puts an object request to S3.
     * 
     * <p>
     * Puts an object request to S3 using the specified key and file.
     * </p>
     * 
     * @param key  The S3 key for the file.
     * @param file The file to upload.
     * 
     * @return true if the file is uploaded successfully, false otherwise.
     * 
     * @see #putObjectRequest(String, Path) Used to upload the file.
     * 
     * @since 1.0
     */
    private boolean putObjectRequest(String key, Path file) {
        return client.putObject(buildPutObject(key, getContentType(file)),
                RequestBody.fromFile(file)).sdkHttpResponse().isSuccessful();

    }

    /**
     * Builds a PutObjectRequest for uploading an object to S3.
     * 
     * @param key         The S3 key for the object.
     * @param contentType The content type of the object.
     * 
     * @return A PutObjectRequest object.
     * 
     * @since 1.0
     */
    private PutObjectRequest buildPutObject(String key, String contentType) {
        return PutObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(key)
                .contentType(contentType)
                .build();
    }

    /**
     * Deletes an artifact from S3.
     * 
     * <p>
     * Deletes all objects with the specified key (artifact ID) from S3.<br>
     * </p>
     * 
     * @param key The key (artifact ID) to delete.
     * 
     * @return A boolean Mono indicating whether the deletion was successful or not.
     * 
     * @since 1.0
     */
    public Mono<Boolean> deleteArtifact(String key) {
        if (!deleteObjectRequest(key)) {
            LOG.error("Delete Artifact - failed to delete all objects for prefix: {}", key);
            return Mono.just(false);
        }

        LOG.info("Delete Artifact - deleted all objects for prefix: {}", key);
        return Mono.just(true);
    }

    /**
     * Deletes an object from S3.
     * 
     * @param key The key of the object to delete.
     * 
     * @return true if the object is deleted successfully, false otherwise.
     * 
     * @since 1.0
     */
    private boolean deleteObjectRequest(String key) {
        return client.deleteObject(b -> b
                .bucket(properties.bucketName())
                .key(key)
                .build()).sdkHttpResponse().isSuccessful();
    }

    /**
     * Gets the content type of a file.
     * 
     * @param file The file to get the content type for.
     * 
     * @return The content type of the file.
     * 
     * @since 1.0
     */
    private String getContentType(Path file) {
        try {
            return Files.probeContentType(file);
        } catch (Exception e) {
            LOG.error("Error getting content type for file: {}", file, e);
            return "application/octet-stream"; // Default content type
        }
    }

    /**
     * Formats the key for S3 by appending a trailing slash if it doesn't already
     * have one.
     * 
     * @param key The key to format.
     * 
     * @return The formatted key.
     * 
     * @since 1.0
     */
    private String formatKey(String key) {
        return key.endsWith("/") ? key : key + "/";
    }
}
