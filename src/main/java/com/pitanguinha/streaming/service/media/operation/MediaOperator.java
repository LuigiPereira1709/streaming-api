package com.pitanguinha.streaming.service.media.operation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;

import com.pitanguinha.streaming.domain.media.Media;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Service for media operations, including uploading media files to S3 and
 * retrieving media duration.
 *
 * @param <E> the type of media entity
 * 
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true", matchIfMissing = false)
public class MediaOperator<E extends Media> {
    private final MediaS3UploadingHandler<E> s3UploadingHandler;

    /**
     * Uploads or updates media files to S3.
     * 
     * <p>
     * This method uploads or updates media files to S3, including both the
     * thumbnail and content files. It returns a Mono that completes when the
     * upload or update operation is finished.
     * </p>
     * 
     * @param entity        the media entity
     * @param thumbnailFile the thumbnail file
     * @param contentFile   the content file
     * 
     * @return A Mono of the media entity after uploading or update whatever are
     *         provided.
     * 
     * @see MediaS3UploadingHandler#uploadOrUpdateMedia(Media, FilePart,
     *      FilePart)
     * 
     * @since 1.0
     */
    public Mono<E> uploadOrUpdateToS3(E entity, FilePart thumbnailFile, FilePart contentFile) {
        return s3UploadingHandler.uploadOrUpdateMedia(entity, thumbnailFile, contentFile);
    }
}
