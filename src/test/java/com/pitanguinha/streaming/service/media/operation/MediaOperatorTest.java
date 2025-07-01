package com.pitanguinha.streaming.service.media.operation;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.multipart.FilePart;

import com.pitanguinha.streaming.domain.media.Media;
import com.pitanguinha.streaming.exceptions.aws.s3.S3Exception;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class MediaOperatorTest {
    @InjectMocks
    MediaOperator<Media> mediaOperator;

    @Mock
    MediaS3UploadingHandler<Media> s3UploadingHandler;

    @Test
    @DisplayName("WHen uploading or udpating media with success, returns Mono of Entity")
    void uploadOrUpdateToS3_ReturnsMonoOfEntity() {
        // Given
        Media mockedEntity = mock(Media.class);

        // Mock the behavior of the s3UploadingHandler
        when(s3UploadingHandler.uploadOrUpdateMedia(any(Media.class), any(FilePart.class),
                any(FilePart.class)))
                .thenReturn(Mono.just(mockedEntity));

        mediaOperator.uploadOrUpdateToS3(mock(Media.class), mock(FilePart.class), mock(FilePart.class))
                .subscribe(entity -> {
                    // Assert that the returned entity is the mocked entity
                    Assertions.assertEquals(mockedEntity, entity);
                });
    }

    @Test
    @DisplayName("When uploading or updating media with failure, returns Mono error")
    void uploadOrUpdateToS3_ReturnsMonoError() {
        // Mock the behavior of the s3UploadingHandler to throw an exception
        when(s3UploadingHandler.uploadOrUpdateMedia(any(Media.class), any(FilePart.class),
                any(FilePart.class)))
                .thenReturn(Mono.error(new S3Exception("Upload failed", null, null, null, null)));

        mediaOperator.uploadOrUpdateToS3(mock(Media.class), mock(FilePart.class), mock(FilePart.class))
                .doOnError(e -> {
                    // Assert that the error is the expected exception
                    Assertions.assertEquals(S3Exception.class, e);
                }).subscribe();
    }

}
