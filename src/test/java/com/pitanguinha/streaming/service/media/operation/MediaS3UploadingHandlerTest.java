package com.pitanguinha.streaming.service.media.operation;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.*;

import static org.mockito.Mockito.*;
import org.mockito.quality.Strictness;

import java.io.File;
import java.nio.file.Path;

import org.springframework.http.codec.multipart.FilePart;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.pitanguinha.streaming.domain.media.Media;
import com.pitanguinha.streaming.enums.aws.ContentType;
import com.pitanguinha.streaming.exceptions.aws.s3.S3Exception;
import com.pitanguinha.streaming.service.TempDirService;
import com.pitanguinha.streaming.service.aws.AwsS3Service;
import com.pitanguinha.streaming.util.test.creator.media.music.MusicEntityCreator;
import com.pitanguinha.streaming.utils.FileUtils;

import reactor.test.StepVerifier;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MediaS3UploadingHandlerTest {
    @InjectMocks
    MediaS3UploadingHandler<Media> handler;

    @Mock
    AwsS3Service s3Service;

    @Mock
    TempDirService tempDirService;

    @Mock
    ObjectMapper objectMapper;

    Media entity = MusicEntityCreator.createEntity();

    Path mockedPath = mock(Path.class);
    File mockedFile = mock(File.class);

    @BeforeEach
    void setUpGlobalMocks() throws Exception {
        // TempDirService
        when(tempDirService.getOrCreateDir(anyString())).thenReturn(Path.of("test"));
        doNothing().when(tempDirService).deleteDirectory(any(Path.class));
        handler.getOrCreateWorkDir();

        // S3
        when(s3Service.uploadFromString(anyString(), any(ContentType.class), anyString()))
                .thenReturn(Mono.just(true));
        when(s3Service.uploadFilesTransactional(anyString(), any(Path[].class)))
                .thenReturn(Mono.just(true));

        // ObjectMapper
        when(objectMapper.writeValueAsString(any(Object.class)))
                .thenReturn("test");

        // Path and File behavior
        when(mockedPath.toFile()).thenReturn(mockedFile);
        when(mockedFile.length()).thenReturn(1024L); // Simulating a file size of 1KB
    }

    // @Test
    // @DisplayName("When uploading a large media file, it should make the all
    // upload in background")
    // void uploadOrUpdateMedia_Large_ReturnsMonoEntity() throws Exception {
    // try (var mockedFileUtils = mockStatic(FileUtils.class)) {
    // Path mockedPath = mock(Path.class);

    // // FileUtils mocks
    // mockedFileUtils.when(() -> FileUtils.transferTo(any(Path.class),
    // anyString(), any(FilePart.class)))
    // .thenReturn(Mono.just(mockedPath));
    // mockedFileUtils.when(() -> FileUtils.getDuration(any(Path.class)))
    // .thenReturn("test");

    // // Path and File behavior mocks
    // File mockedFile = mock(File.class);
    // when(mockedPath.toFile()).thenReturn(mockedFile);
    // when(mockedFile.length()).thenReturn((long) 100 * 1024 * 1024); // Simulating
    // a large file

    // // S3 mocks
    // CountDownLatch latch = new CountDownLatch(1);
    // var mockedPutObjectResponse = mock(PutObjectResponse.class);
    // when(s3Service.uploadFileAsync(anyString(), any(Path.class)))
    // .thenAnswer(invocation -> {
    // latch.countDown();
    // return CompletableFuture.completedFuture(mockedPutObjectResponse);
    // });

    // // SdkHttpResponse mocks
    // when(mockedPutObjectResponse.sdkHttpResponse())
    // .thenReturn(SdkHttpResponse.builder().statusCode(200).build());

    // handler.uploadOrUpdateMedia(entity, mock(FilePart.class),
    // mock(FilePart.class));

    // latch.await(2, TimeUnit.SECONDS);
    // // assertTrue(latch.await(4, TimeUnit.SECONDS), "Upload did not complete in
    // // time");

    // // Verify that the upload was initiated
    // verify(s3Service, times(1)).uploadFileAsync(anyString(), any(Path.class));
    // verify(s3Service, times(1)).uploadFilesTransactional(anyString(),
    // any(Path[].class));
    // verify(s3Service, times(1)).uploadFromString(anyString(),
    // any(ContentType.class), anyString());
    // }
    // }

    @Test
    @DisplayName("When uploading a too large media file, thrown an error")
    void uploadOrUpdateMedia_Large_ReturnsMonoError() {
        try (var mockedMediaUtils = mockStatic(MediaUtils.class)) {
            Path mockedPath = mock(Path.class);

            // MediaUtils mocks
            mockedMediaUtils.when(() -> MediaUtils.transferTo(any(Path.class), anyString(), any(FilePart.class)))
                    .thenReturn(Mono.just(mockedPath));

            // File and Path behavior mocks
            File mockedFile = mock(File.class);
            when(mockedPath.toFile()).thenReturn(mockedFile);
            when(mockedFile.length()).thenReturn((long) 100 * 1024 * 1024); // Simulating a large file

            handler.uploadOrUpdateMedia(entity, mock(FilePart.class), mock(FilePart.class))
                    .as(StepVerifier::create)
                    .expectError(S3Exception.class)
                    .verify();
        }
    }

    @Test
    @DisplayName("When uploading a normal media file, it should upload synchronously")
    void uploadOrUpdateMedia_Normal_ReturnsMonoEntity() {
        try (var mockedFileUtils = mockStatic(FileUtils.class)) {
            // FileUtils mocks
            mockedFileUtils.when(() -> FileUtils.transferTo(any(Path.class), anyString(), any(FilePart.class)))
                    .thenReturn(Mono.just(mockedPath));

            handler.uploadOrUpdateMedia(entity, mock(FilePart.class), mock(FilePart.class))
                    .as(StepVerifier::create)
                    .expectNextMatches($ -> {
                        verify(s3Service, times(1)).uploadFilesTransactional(anyString(), any(Path[].class));
                        verify(s3Service, times(1)).uploadFromString(anyString(), any(ContentType.class), anyString());
                        return true;
                    })
                    .verifyComplete();
        }
    }

    @Test
    @DisplayName("When has an error uploading a normal media file, it should return an error")
    void uploadOrUpdateMedia_Normal_ReturnsMonoError() {
        try (var mockedFileUtils = mockStatic(FileUtils.class)) {
            // FileUtils mocks
            mockedFileUtils.when(() -> FileUtils.transferTo(any(Path.class), anyString(), any(FilePart.class)))
                    .thenReturn(Mono.just(mockedPath));

            // S3 mocks
            when(s3Service.uploadFilesTransactional(anyString(), any(Path[].class)))
                    .thenReturn(Mono.just(false));

            handler.uploadOrUpdateMedia(entity, mock(FilePart.class), mock(FilePart.class))
                    .as(StepVerifier::create)
                    .expectError(S3Exception.class)
                    .verify();
        }
    }

    @Test
    @DisplayName("When upating a media file without a new file (content or thumbnail), it should only upload the json with metadata")
    void uploadOrUpdateMedia_NoFile_ReturnsMonoEntity() {
        handler.uploadOrUpdateMedia(entity, null, null)
                .as(StepVerifier::create)
                .expectNextMatches($ -> {
                    verify(s3Service, times(1)).uploadFromString(anyString(), any(ContentType.class), anyString());
                    verify(s3Service, never()).uploadFilesTransactional(anyString(), any(Path[].class));
                    verify(s3Service, never()).uploadFileAsync(anyString(), any(Path.class));
                    return true;
                }).verifyComplete();
    }

    @Test
    @DisplayName("When has an error updating a media file without a new file, it should return an error")
    void uploadOrUpdateMedia_NoFile_ReturnsMonoError() {
        when(s3Service.uploadFromString(anyString(), any(ContentType.class), anyString()))
                .thenReturn(Mono.just(false));

        handler.uploadOrUpdateMedia(entity, null, null)
                .as(StepVerifier::create)
                .expectError(S3Exception.class)
                .verify();
    }
}
