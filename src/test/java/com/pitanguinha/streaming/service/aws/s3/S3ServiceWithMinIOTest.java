package com.pitanguinha.streaming.service.aws.s3;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.pitanguinha.streaming.MinioContainer;
import com.pitanguinha.streaming.enums.aws.ContentType;
import com.pitanguinha.streaming.service.aws.AwsS3Service;

import reactor.test.StepVerifier;

import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.core.sync.RequestBody;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import java.io.*;
import java.nio.file.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "aws.s3.enabled=true")
@ContextConfiguration(classes = MinioContainer.class)
public class S3ServiceWithMinIOTest {
    @Autowired
    AwsS3Service service;

    static S3Client client;

    static S3AsyncClient asyncClient;

    static String bucketName = "test-bucket";

    static Path tempDir = Path.of(System.getProperty("java.io.tmpdir"), "s3-test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.s3.region", () -> "us-east-1");
        registry.add("aws.s3.bucket-name", () -> bucketName);
        registry.add("aws.s3.endpoint", () -> MinioContainer.endpoint);
        registry.add("aws.local.credentials.access-key", () -> MinioContainer.ACCESS_KEY);
        registry.add("aws.local.credentials.secret-key", () -> MinioContainer.SECRET_KEY);
    }

    @BeforeAll
    static void setUp(@Autowired S3Client client, @Autowired S3AsyncClient asyncClient) throws IOException {
        Files.createDirectories(tempDir.resolve(UUID.randomUUID().toString()));
        S3ServiceWithMinIOTest.asyncClient = asyncClient;
        S3ServiceWithMinIOTest.client = client;
        client.createBucket(b -> b.bucket(bucketName));
    }

    @AfterAll
    static void tearDown() throws IOException {
        // Delete the temporary directory
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    @DisplayName("When generating a presigned URL, return a valid URL")
    void presignedUrl_ReturnsValidUrl() {
        String key = "test-presigned-url.txt";

        // When generating a presigned URL
        StepVerifier.create(service.presignedUrl(key))
                // Then the URL should be valid
                .assertNext(url -> {
                    assertNotNull(url, "URL should not be null");
                    assertTrue(url.toString().contains(key), "URL should contain the key");
                }).verifyComplete();
    }

    @Test
    @DisplayName("When uploading from a string, the file should exist in S3")
    void uploadFromString_ReturnsMonoTrue() {
        String key = "test-upload-string.txt";
        String content = "Hello, World!";

        // When uploading the string to S3
        service.uploadFromString(key, ContentType.JSON, content)
                // Then the upload should complete successfully
                .as(StepVerifier::create)
                .expectNextMatches(uploaded -> {
                    assertTrue(uploaded, "File should be uploaded successfully");
                    return true;
                })
                .verifyComplete();

        // Verify that the file exists in S3
        var objects = client.listObjectsV2(b -> b.bucket(bucketName).prefix(key)).contents();
        assertEquals(1, objects.size());
    }

    @Test
    @DisplayName("When uploading from a file, the file should exist in S3")
    void uploadFromFile_ReturnsMonoTrue() throws Exception {
        String key = "test-upload-file.txt";

        // Create a test file
        Path path = Files.createFile(tempDir.resolve(key));

        // When uploading the file to S3
        service.uploadFile(key, path)
                // Then the upload should complete successfully
                .as(StepVerifier::create)
                .expectNextMatches(uploaded -> {
                    assertTrue(uploaded, "File should be uploaded successfully");
                    return true;
                })
                .verifyComplete();

        // Verify that the file exists in S3
        var objects = client.listObjectsV2(b -> b.bucket(bucketName).prefix(key)).contents();
        assertEquals(1, objects.size());
    }

    @Test
    @DisplayName("When uploading file asynchronously, the file should exist in S3")
    void uploadFileAsync_ReturnsCompletableFuture() throws Exception {
        String key = "test-upload-file-async.txt";

        // Create a test file
        Path path = Files.createFile(tempDir.resolve(key));

        // When uploading the file asynchronously
        CompletableFuture<PutObjectResponse> future = service.uploadFileAsync(key, path);

        future.join();

        // Then the future should complete successfully
        assertTrue(future.isDone());
        assertNotNull(future.get());

        // Verify that the file exists in S3
        var objects = client.listObjectsV2(b -> b.bucket(bucketName).prefix(key)).contents();
        assertEquals(1, objects.size());
    }

    @Test
    @DisplayName("When uploading transactionally, the files for key should exist in S3")
    void uploadFilesTransactional_ReturnsMonoTrue() throws Exception {
        String key = "test-upload-files-transactional.txt";

        Path[] paths = new Path[3];
        // Create a test file
        for (int i = 0; i < paths.length; i++) {
            paths[i] = Files.createFile(tempDir.resolve((key + i).concat(".txt")));
        }

        // When uploading the file to S3
        service.uploadFilesTransactional(key, paths)
                // Then the upload should complete successfully
                .as(StepVerifier::create)
                .expectNextMatches(uploaded -> {
                    assertTrue(uploaded, "Files should be uploaded successfully");
                    return true;
                })
                .verifyComplete();

        // Verify that the file exists in S3
        var objects = client.listObjectsV2(b -> b.bucket(bucketName).prefix(key)).contents();
        assertEquals(paths.length, objects.size());
    }

    @Test
    @DisplayName("When deleting an artifact, the files should not exist in S3")
    void deleteArtifact_ReturnsMonoTrue() throws Exception {
        String key = "test-delete-artifact";

        // Create a test files and upload then to S3
        for (int i = 0; i < 3; i++) {
            Path path = Files.createFile(tempDir.resolve((key + i).concat(".txt")));
            client.putObject(b -> b.bucket(bucketName).key(key), RequestBody.fromFile(path));
        }

        // When deleting the artifact from S3
        service.deleteArtifact(key)
                // Then the deletion should complete successfully
                .as(StepVerifier::create)
                .expectNextMatches(deleted -> {
                    assertTrue(deleted, "Artifact should be deleted successfully");
                    return true;
                })
                .verifyComplete();

        // Verify that the file does not exist in S3
        var objects = client.listObjectsV2(b -> b.bucket(bucketName).prefix(key)).contents();
        assertEquals(0, objects.size());
    }
}
