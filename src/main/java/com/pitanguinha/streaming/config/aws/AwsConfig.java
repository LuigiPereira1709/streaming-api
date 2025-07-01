package com.pitanguinha.streaming.config.aws;

import java.util.Arrays;

import org.springframework.core.env.Environment;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.*;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AwsLocalProperties.class)
public class AwsConfig {
    private final AwsLocalProperties localProperties;
    private final Environment env;

    /**
     * Creates an {@link AwsCredentialsProvider} bean.
     * 
     * <p>
     * This method creates an AWS credentials provider based on the active profile.
     * If the "prod" profile is not active, it uses local credentials; otherwise, it
     * uses the default credentials provider.
     * </p>
     *
     * @return An {@link AwsCredentialsProvider} instance configured with the
     *         appropriate credentials.
     * 
     * @since 1.0
     */
    public AwsCredentialsProvider credentialsProvider() {
        if (!isProdActive())
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(localProperties.accessKey(), localProperties.secretKey()));

        return DefaultCredentialsProvider.create();
    }

    /**
     * Checks if the production profile is active.
     * 
     * <p>
     * This method checks if the "prod" profile is active in the application
     * environment.
     * </p>
     *
     * @return {@code true} if the "prod" profile is active, {@code false}
     *         otherwise.
     * 
     * @since 1.0
     */
    public boolean isProdActive() {
        return Arrays.stream(env.getActiveProfiles())
                .anyMatch("prod"::equalsIgnoreCase);
    }
}
