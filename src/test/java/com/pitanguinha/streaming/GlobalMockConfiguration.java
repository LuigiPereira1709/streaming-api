package com.pitanguinha.streaming;

import java.security.PrivateKey;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * * Global configuration for mocking AWS services in tests.
 * 
 * <p>
 * This configuration class sets up dynamic properties for AWS CloudFront and
 * S3, and provides a mock implementation of the CloudFront private key.
 * </p>
 * 
 * @since 1.0
 */
@TestConfiguration
public class GlobalMockConfiguration {
    /**
     * Registers dynamic properties for AWS services used in tests.
     * 
     * <p>
     * This method is used to set up properties for AWS CloudFront and S3,
     * allowing tests to run without needing actual AWS credentials or services.
     * </p>
     * 
     * @param registry the registry to which dynamic properties are added
     */
    public static void dynamicProperties(DynamicPropertyRegistry registry) {
        // Cloudfront properties
        registry.add("aws.cloudfront.enabled", () -> "true");
        registry.add("aws.cloudfront.access-key", () -> "mock");
        registry.add("aws.cloudfront.secret-key", () -> "mock-secret");
        registry.add("aws.cloudfront.region", () -> "us-east-1");
        registry.add("aws.cloudfront.private-key-path", () -> "mock-private");
        registry.add("aws.cloudfront.key-pair-id", () -> "mock-key-pair-id");
        registry.add("aws.cloudfront.distribution-id", () -> "mock-distribution-id");

        // S3 properties
        registry.add("aws.s3.enabled", () -> "true");
        registry.add("aws.s3.region", () -> "us-east-1");
        registry.add("aws.s3.endpoint", () -> "http://localhost:9000");
        registry.add("aws.s3.access-key", () -> "mock");
        registry.add("aws.s3.secret-key", () -> "mock-secret");
        registry.add("aws.s3.bucket-name", () -> "mock-bucket");

        // Services properties
        registry.add("services.music.enabled", () -> "true");
        registry.add("services.podcast.enabled", () -> "true");
    }

    /**
     * Provides a mock implementation of the CloudFront private key.
     * 
     * <p>
     * This bean is used in tests to simulate the behavior of the CloudFront
     * private key without needing an actual key.
     * </p>
     * 
     * @return a mock PrivateKey instance
     */
    @Bean
    @Qualifier("cloudFrontPrivateKey")
    public PrivateKey cloudFrontPrivateKey() {
        return Mockito.mock(PrivateKey.class);
    }
}
