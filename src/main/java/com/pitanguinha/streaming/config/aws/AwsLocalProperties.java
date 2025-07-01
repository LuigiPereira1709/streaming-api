package com.pitanguinha.streaming.config.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for AWS Local credentials.
 * 
 * <p>
 * This class holds the access key and secret key for AWS Local credentials.
 * </p>
 * 
 * @since 1.0
 */
@ConfigurationProperties(prefix = "aws.local.credentials")
public record AwsLocalProperties(
        String accessKey,
        String secretKey) {
}
