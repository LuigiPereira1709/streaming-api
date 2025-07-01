package com.pitanguinha.streaming.config.aws.cloudfront;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for AWS CloudFront.
 * 
 * <p>
 * This class is used to bind the properties defined in the application.yml file
 * under the prefix "aws.cloudfront" to Java fields.
 * </p>
 * 
 * @since 1.0
 */
@ConfigurationProperties(prefix = "aws.cloudfront")
public record CloudFrontProperties(
        String distributionId,
        String endpoint,
        String keyPairId,
        String privateKeyName) {
}
