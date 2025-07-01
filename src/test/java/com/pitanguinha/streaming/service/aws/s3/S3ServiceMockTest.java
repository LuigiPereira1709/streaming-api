package com.pitanguinha.streaming.service.aws.s3;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.annotation.DirtiesContext;

import com.pitanguinha.streaming.enums.aws.ContentType;
import com.pitanguinha.streaming.service.aws.AwsS3Service;

import com.pitanguinha.streaming.config.aws.s3.S3Properties;

import reactor.test.StepVerifier;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.async.AsyncRequestBody;

@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class S3ServiceMockTest {
    @InjectMocks
    AwsS3Service service;

    @Mock
    S3Client client;

    @Mock
    S3AsyncClient asyncClient;

    @Mock
    S3Properties properties;

    String bucketName = "test-bucket";

    static Path tempDir = Path.of(System.getProperty("java.io.tmpdir"), "s3-test");

    @BeforeAll
    static void setUp() throws Exception {
        Files.createDirectories(tempDir.resolve(UUID.randomUUID().toString()));
    }

    @AfterEach
    void cleanTempDir() throws Exception {
        // Delete all files in the temporary directory
        Files.walk(tempDir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        Files.delete(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @AfterAll
    static void tearDown() throws Exception {
        // Delete the temporary directory
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    @DisplayName("When uploading with request body from a string, return a mono true")
    void uploadFromString_ReturnsMonoTrue() {
        // Mock the S3 client putObject method
        when(client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenAnswer(invocation -> {
                    // Simulate the behavior of the S3 client
                    return PutObjectResponse.builder()
                            .sdkHttpResponse(SdkHttpResponse.builder()
                                    .statusCode(200)
                                    .build())
                            .build();
                });

        // When uploading the string to S3
        service.uploadFromString("key", ContentType.JSON, "content")
                .as(StepVerifier::create)
                .expectNextMatches(result -> {
                    assertTrue(result, "Expected upload to succeed");
                    return true;
                }).verifyComplete();

        // Verify that the S3 client putObject method was called with the correct
        // parameters
        verify(client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("When uploading with request body from a file, return a mono true")
    void uploadFile_ReturnsMonoTrue() throws Exception {
        // Mock the S3 client putObject method
        doReturn(PutObjectResponse.builder()
                .sdkHttpResponse(SdkHttpResponse.builder()
                        .statusCode(200)
                        .build())
                .build())
                .when(client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // Create a temporary file for testing
        Path filePath = tempDir.resolve("test-upload-file").toAbsolutePath();
        Files.createFile(filePath);

        // When uploading the file to S3
        service.uploadFile("key", filePath)
                .as(StepVerifier::create)
                .expectNextMatches(result -> {
                    assertTrue(result, "Expected upload to succeed");
                    return true;
                }).verifyComplete();

        // Verify that the S3 client putObject method was called with the correct
        // parameters
        verify(client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("When uploading file asynchronously, return a completable future")
    void uploadFileAsync_ReturnsCompletableFuture() throws Exception {
        Path file = Files.createFile(tempDir.resolve("test"));

        when(asyncClient.putObject(any(Consumer.class), any(AsyncRequestBody.class)))
                .thenAnswer(invocation -> {
                    CompletableFuture<PutObjectResponse> future = new CompletableFuture<>();
                    future.complete(PutObjectResponse.builder().build());
                    return future;
                });

        // When uploading the file to S3
        CompletableFuture<PutObjectResponse> result = service.uploadFileAsync("key", file);

        // Then the result should be a CompletableFuture
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isDone(), "Result should be done");
    }

    @Test
    @DisplayName("When uploading files transactionally, return a mono true")
    void uploadFilesTransactional_ReturnsMonoTrue() throws Exception {
        // Create the test files
        Path[] files = createFiles();

        // Mock the S3 client putObject method to return true
        doReturn(PutObjectResponse.builder()
                .sdkHttpResponse(SdkHttpResponse.builder()
                        .statusCode(200)
                        .build())
                .build())
                .when(client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // When uploading the files to S3
        service.uploadFilesTransactional("key", files)
                .as(StepVerifier::create)
                .expectNextMatches(result -> {
                    assertTrue(result, "Expected upload to succeed");
                    return true;
                }).verifyComplete();
    }

    @Test
    @DisplayName("When uploading fails excep method(asyncUpload and transactionally method), return a mono false")
    void uploadMethods_ReturnsMonoFalse() throws Exception {
        // Create the test files
        Path[] files = createFiles();
        var responseFail = PutObjectResponse.builder()
                .sdkHttpResponse(SdkHttpResponse.builder()
                        .statusCode(500)
                        .build())
                .build();

        // Mock the S3 client putObject method to return false
        doReturn(responseFail)
                .when(client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // When uploading with request body from a string
        service.uploadFromString("key", ContentType.JSON, "content")
                .as(StepVerifier::create)
                .expectNextMatches(result -> {
                    assertFalse(result, "Expected upload to fail");
                    return true;
                }).verifyComplete();

        // When uploading the file to S3
        service.uploadFile("key", files[0])
                .as(StepVerifier::create)
                .expectNextMatches(result -> {
                    assertFalse(result, "Expected upload to fail");
                    return true;
                }).verifyComplete();
    }

    // @SuppressWarnings("unchecked")
    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("When any file upload fails, the transaction should be rolled back and return a mono false")
    void uploadFilesTransactional_ReturnsMonoFalse() throws Exception {
        // Create the test files
        Path[] files = createFiles();

        // Mock the S3 client putObject method to return false for the second file
        AtomicInteger counter = new AtomicInteger(0);
        when(client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenAnswer(invocation -> {
                    int statusCode = counter.getAndIncrement() == 2 ? 500 : 200;
                    return PutObjectResponse.builder()
                            .sdkHttpResponse(SdkHttpResponse.builder()
                                    .statusCode(statusCode).build())
                            .build();
                });

        // Mock the S3 client deleteObject method to return true
        doReturn(DeleteObjectResponse.builder()
                .sdkHttpResponse(SdkHttpResponse.builder()
                        .statusCode(200)
                        .build())
                .build())
                .when(client).deleteObject(any(Consumer.class));

        // When uploading the files to S3
        service.uploadFilesTransactional("key", files)
                .as(StepVerifier::create)
                .expectNextMatches(result -> {
                    assertFalse(result, "Expected upload to fail and rollback");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("When deleting an artifact, return a mono true")
    void deleteArtifact_ReturnsMonoTrue() {
        // Mock the S3 client deleteObject method
        doReturn(DeleteObjectResponse.builder()
                .sdkHttpResponse(SdkHttpResponse.builder()
                        .statusCode(200)
                        .build())
                .build())
                .when(client).deleteObject(any(Consumer.class));

        // When deleting the artifact from S3
        service.deleteArtifact("key")
                .as(StepVerifier::create)
                .expectNextMatches(result -> {
                    assertTrue(result, "Expected deletion to succeed");
                    return true;
                })
                .verifyComplete();

        // Verify that the S3 client deleteObject method was called with the correct
        // parameters
        verify(client).deleteObject(any(Consumer.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("When deleting an artifact fails, return a mono false")
    void deleteArtifact_ReturnsMonoFalse() {
        // Mock the S3 client deleteObject method to return false
        doReturn(DeleteObjectResponse.builder()
                .sdkHttpResponse(SdkHttpResponse.builder()
                        .statusCode(500)
                        .build())
                .build())
                .when(client).deleteObject(any(Consumer.class));

        // When deleting the artifact from S3
        service.deleteArtifact("key")
                .as(StepVerifier::create)
                .expectNextMatches(result -> {
                    assertFalse(result, "Expected deletion to fail");
                    return true;
                })
                .verifyComplete();
    }

    /**
     * Creates temporary files for testing.
     * 
     * @return An array of paths to the created files.
     * 
     * @since 1.0
     */
    private Path[] createFiles() throws Exception {
        Path[] files = new Path[3];
        for (int i = 0; i < files.length; i++)
            files[i] = Files.createFile(tempDir.resolve("test-upload-files-transactional" + i));

        return files;
    }
}
