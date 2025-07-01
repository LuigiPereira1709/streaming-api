package com.pitanguinha.streaming.config.aws.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for AWS S3.
 * 
 * <p>
 * This class is used to bind the properties defined in the application.yml file
 * under the prefix "aws.s3" to Java fields.
 * </p>
 * 
 * @since 1.0
 */
@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(
        String region,
        String endpoint,
        String bucketName) {
}
