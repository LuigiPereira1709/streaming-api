package com.pitanguinha.streaming;

import java.time.Duration;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.springframework.boot.test.context.TestConfiguration;

/**
 * This class is used to create and manage a MinIO container for testing
 * purposes.
 * 
 * It provides methods to start the container, create a bucket, and tear down
 * the container.
 * 
 * @since 1.0
 */
@TestConfiguration
@SuppressWarnings("resource")
public class MinioContainer {
    public static String endpoint;
    public static final String ACCESS_KEY = "minioadmin";
    public static final String SECRET_KEY = "minioadmin";

    public static final GenericContainer<?> MINIO = new GenericContainer<>("minio/minio:latest")
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
            .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY)
            .withCommand("server", "/data", "--address", ":9000")
            .waitingFor(Wait.forHttp("/minio/health/live").forStatusCode(200)
                    .withStartupTimeout(Duration.ofSeconds(60)));

    static {
        MINIO.start();
        endpoint = "http://" + MINIO.getHost() + ":" + MINIO.getMappedPort(9000);
    }
}
