package com.pitanguinha.streaming.config.aws.s3;

import java.net.URI;

import static org.springframework.util.Assert.*;

import org.springframework.context.annotation.*;

import com.pitanguinha.streaming.config.aws.AwsConfig;

import lombok.RequiredArgsConstructor;
import jakarta.annotation.PostConstruct;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.*;

/**
 * Configuration class for AWS S3.
 * 
 * <p>
 * This class provides a configuration for the AWS S3 client, which is used to
 * interact with the S3 service. It creates an S3Client bean that can be used to
 * perform operations on S3 buckets and objects.<br>
 * Ps: It uses conditional properties to enable or disable the configuration,
 * defaulting to falsae if not specified in the application properties or
 * dynamic properties.
 * </p>
 * 
 * @since 1.0
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(S3Properties.class)
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true", matchIfMissing = false)
public class S3Config {
    private final S3Properties properties;
    private final AwsConfig awsConfig;
    private AwsCredentialsProvider credentialsProvider;
    private boolean isProdActive;

    /**
     * Initializes the S3Config class.
     * 
     * <p>
     * This method is called after the bean is constructed and initializes the
     * credentials provider and checks if the production profile is active.
     * </p>
     *
     * @see AwsConfig The configuration class for AWS credentials.
     * 
     * @since 1.0
     */
    @PostConstruct
    private void init() {
        credentialsProvider = awsConfig.credentialsProvider();
        isProdActive = awsConfig.isProdActive();
    }

    /**
     * Validates the configuration properties.
     * 
     * <p>
     * This method checks if the bucket name, region, access key, and secret key are
     * not empty. If any of them are empty, an IllegalArgumentException is thrown.
     * </p>
     *
     * @throws IllegalArgumentException if any of the properties are empty.
     * 
     * @since 1.0
     */
    @PostConstruct
    private void validateConfig() {
        hasText(properties.region(), "Region must not be empty");

        if (!isProdActive)
            hasText(properties.endpoint(), "Endpoint must not be empty for non-production profiles");
    }

    /**
     * Creates an S3Presigner bean.
     * 
     * <p>
     * This method creates an S3Presigner using the provided region, access key, and
     * secret key.<br>
     * The presigner retuned is configured based on the active profile.
     * </p>
     *
     * @return An S3Presigner instance configured with the provided properties.
     * 
     * @see S3Presigner The presigner for AWS S3 service.
     * 
     * @since 1.0
     */
    @Bean
    public S3Presigner s3Presigner() {
        var presigner = S3Presigner.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(credentialsProvider);

        if (!isProdActive) {
            presigner.endpointOverride(URI.create(properties.endpoint()));
        }

        return presigner.build();
    }

    /**
     * Creates an S3Client bean.
     * 
     * <p>
     * This method creates an S3Client using the provided region, access key, and
     * secret key.<br>
     * The client retuned is configured based on the active profile.
     * </p>
     *
     * @return An S3Client instance configured with the provided properties.
     * 
     * @see S3Client The client for AWS S3 service.
     * 
     * @since 1.0
     */
    @Bean
    public S3Client s3Client() {
        var client = S3Client.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(credentialsProvider);

        if (!isProdActive) {
            client.endpointOverride(URI.create(properties.endpoint()));
            client.serviceConfiguration(
                    S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build());
        }

        return client.build();
    }

    /**
     * Creates an S3AsyncClient bean.
     * 
     * <p>
     * This method creates an S3AsyncClient using the provided region, access key,
     * and secret key.<br>
     * The client retuned is configured based on the active profile.
     * </p>
     *
     * @return An S3AsyncClient instance configured with the provided properties.
     * 
     * @see S3AsyncClient The async client for AWS S3 service.
     * 
     * @since 1.0
     */
    @Bean
    public S3AsyncClient s3AsyncClient() {
        var asyncClient = S3AsyncClient.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(credentialsProvider)
                .multipartEnabled(true);

        if (!isProdActive) {
            asyncClient.endpointOverride(URI.create(properties.endpoint()));
            asyncClient.serviceConfiguration(
                    S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build());
        }

        return asyncClient.build();
    }
}
