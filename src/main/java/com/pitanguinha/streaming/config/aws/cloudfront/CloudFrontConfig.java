package com.pitanguinha.streaming.config.aws.cloudfront;

import static org.springframework.util.Assert.hasText;

import java.util.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;

import org.springframework.context.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pitanguinha.streaming.config.aws.AwsConfig;
import com.pitanguinha.streaming.enums.exceptions.SeverityLevel;
import com.pitanguinha.streaming.exceptions.internal.InternalException;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudfront.*;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

/**
 * Configuration class for AWS CloudFront.
 * 
 * <p>
 * This class provides a configuration for the AWS CloudFront client, which is
 * used to interact with the CloudFront service.<br>
 * Ps: It uses conditional properties to enable or disable the configuration,
 * defaulting to falsae if not specified in the application properties or
 * dynamic properties.
 * </p>
 * 
 * @since 1.0
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CloudFrontProperties.class)
@ConditionalOnProperty(prefix = "aws.cloudfront", name = "enabled", havingValue = "true", matchIfMissing = false)
public class CloudFrontConfig {
    private final CloudFrontProperties properties;
    private final AwsConfig awsConfig;

    /**
     * Creates a CloudFrontClient bean.
     * 
     * <p>
     * This method creates a CloudFrontClient using the provided region, access key,
     * and secret key.
     * </p>
     *
     * @return A CloudFrontClient instance configured with the provided properties.
     * 
     * @see CloudFrontClient The client for AWS CloudFront service.
     * @see #validateConfig() The method to validate the configuration properties.
     * @see AwsConfig#credentialsProvider() The method to get the AWS credentials
     *      provider.
     * 
     * @since 1.0
     */
    @Bean
    public CloudFrontClient cloudFrontClient() {
        validateConfig();
        return CloudFrontClient.builder()
                .region(Region.of("us-east-1"))
                .credentialsProvider(awsConfig.credentialsProvider())
                .build();
    }

    /**
     * Creates a PrivateKey bean.
     * 
     * <p>
     * This method loads the private key from the specified path and creates a
     * PrivateKey instance.
     * </p>
     *
     * @return A PrivateKey instance loaded from the specified path.
     * 
     * @see PrivateKey The class representing a private key.
     * @see #getPrivateKeyFromSecretsManager() The method to retrieve the private
     *      key.
     * 
     * @since 1.0
     */
    @Profile("!test")
    @Bean("cloudFrontPrivateKey")
    public PrivateKey privateKey() {
        try {
            String cleanPem = getPrivateKeyFromSecretsManager()
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(cleanPem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception e) {
            throw new InternalException("Failed to load private key", CloudFrontConfig.class, SeverityLevel.CRITICAL,
                    e);
        }
    }

    /**
     * Creates a CloudFrontUtilities bean.
     * 
     * <p>
     * This method creates a CloudFrontUtilities instance, which is used to
     * interact with CloudFront utilities.
     * </p>
     *
     * @return A CloudFrontUtilities instance.
     * 
     * @see CloudFrontUtilities The utilities for AWS CloudFront service.
     * 
     * @since 1.0
     */
    @Bean
    public CloudFrontUtilities utilities() {
        return CloudFrontUtilities.create();
    }

    /**
     * Retrieves the private key from AWS Secrets Manager.
     * 
     * <p>
     * This method retrieves the private key from AWS Secrets Manager using the
     * secret ID specified in the properties.
     * </p>
     *
     * @return The private key as a String.
     * 
     * @throws JsonMappingException    if there is an error mapping the JSON
     *                                 response.
     * @throws JsonProcessingException if there is an error processing the JSON.
     * 
     * @since 1.0
     */
    private String getPrivateKeyFromSecretsManager() throws JsonMappingException, JsonProcessingException {
        try (SecretsManagerClient client = SecretsManagerClient.create()) {
            GetSecretValueResponse response = client.getSecretValue(
                    GetSecretValueRequest.builder().secretId(properties.privateKeyName()).build());

            String secretJson = response.secretString();
            JsonNode rootNode = new ObjectMapper().readTree(secretJson);
            JsonNode privateKeyNode = rootNode.get("private-key");

            if (privateKeyNode == null || !privateKeyNode.isTextual())
                throw new InternalException("Private key not found in secret", CloudFrontConfig.class,
                        SeverityLevel.CRITICAL);

            return privateKeyNode.asText();
        }
    }

    /**
     * Validates the configuration properties.
     * 
     * <p>
     * This method checks if the access key and secret key are not empty. If any of
     * them is empty, an IllegalArgumentException is thrown.
     * </p>
     *
     * @throws IllegalArgumentException if any of the properties are empty.
     * 
     * @since 1.0
     */
    private void validateConfig() {
        hasText(properties.privateKeyName(), "Private key id must not be empty");
    }
}
