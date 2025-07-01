package com.pitanguinha.streaming.service.aws;

import java.util.*;
import java.time.Instant;
import java.security.PrivateKey;

import org.slf4j.*;
import org.springframework.stereotype.Service;

import com.pitanguinha.streaming.config.aws.cloudfront.CloudFrontProperties;
import com.pitanguinha.streaming.enums.exceptions.SeverityLevel;
import com.pitanguinha.streaming.exceptions.aws.cloudfront.CloudFrontSigningException;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import software.amazon.awssdk.services.cloudfront.*;
import software.amazon.awssdk.services.cloudfront.model.*;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCannedPolicy;

/**
 * Service class for handling AWS CloudFront operations.
 * 
 * @since 1.0
 */
@Service
@ConditionalOnProperty(name = "aws.cloudfront.enabled", havingValue = "true", matchIfMissing = false)
public class AwsCloudFrontService {
    private static final Logger LOG = LoggerFactory.getLogger(AwsCloudFrontService.class);
    private final CloudFrontProperties properties;
    private final PrivateKey privateKey;
    private final CloudFrontClient client;
    private final CloudFrontUtilities utilities;

    @Autowired
    public AwsCloudFrontService(
            CloudFrontProperties properties,
            @Qualifier("cloudFrontPrivateKey") PrivateKey privateKey,
            CloudFrontClient client,
            CloudFrontUtilities utilities) {
        this.properties = properties;
        this.privateKey = privateKey;
        this.client = client;
        this.utilities = utilities;
    }

    /**
     * Invalidates the cache for a specific object in CloudFront.
     * 
     * <p>
     * This method creates an invalidation request for the specified object ID,
     * allowing the updated content to be served.
     * </p>
     * 
     * @param objectId The ID of the object to invalidate.
     * 
     * @return true if the invalidation was successful, false otherwise.
     * 
     * @since 1.0
     */
    public boolean invalidateCache(String objectId) {
        LOG.info("Cache invalidation initiated for object ID: {}", objectId);

        CreateInvalidationResponse response = client.createInvalidation(r -> r
                .distributionId(properties.distributionId())
                .invalidationBatch(b -> b
                        .callerReference("invalidation-" + UUID.randomUUID())
                        .paths(p -> p
                                .items("/video/" + objectId + "/*"))));

        if (!response.sdkHttpResponse().isSuccessful()) {
            LOG.error("Failed to invalidate cache for object ID: {}", objectId);
            return false;
        }

        LOG.info("Cache invalidation successful for object ID: {}", objectId);
        return true;
    }

    /**
     * Generates signed cookies for a specific object ID.
     * 
     * <p>
     * This method creates signed cookies that allow access to the specified object
     * in CloudFront.
     * </p>
     * 
     * @param objectId The ID of the object for which to generate signed cookies.
     * 
     * @return CookiesForCannedPolicy containing the signed cookies.
     * 
     * @see #getCannedSignerRequest(String) Used to create the CannedSignerRequest
     *      for generating signed cookies.
     * 
     * @since 1.0
     */
    public CookiesForCannedPolicy getSignedCookies(String objectId) {
        LOG.info("Generating signed cookies for object ID: {}", objectId);

        String cloudFrontUrl = properties.endpoint() + objectId + "/*";

        CookiesForCannedPolicy cookies = utilities.getCookiesForCannedPolicy(getCannedSignerRequest(cloudFrontUrl));

        LOG.info("Signed cookies generated for object ID: {}", objectId);
        return cookies;
    }

    /**
     * Generates a signed URL for a specific object.
     * 
     * <p>
     * This method creates a signed URL that allows access to the specified object
     * in CloudFront.
     * </p>
     * 
     * @param key The key of the object for which to generate the signed URL.
     * 
     * @return SignedUrl containing the signed URL.
     * 
     * @see #getCannedSignerRequest(String) Used to create the CannedSignerRequest
     *      for generating the signed URL.
     * 
     * @since 1.0
     */
    public SignedUrl getSignedUrl(String key) {
        String cloudFrontUrl = properties.endpoint() + "/" + key;

        SignedUrl signedUrl = utilities.getSignedUrlWithCannedPolicy(getCannedSignerRequest(cloudFrontUrl));
        LOG.info("Signed URL generated for key: {}", key);
        return signedUrl;
    }

    /**
     * Generates a CannedSignerRequest for a specific CloudFront URL.
     * 
     * <p>
     * This method creates a CannedSignerRequest object that contains the necessary
     * information for generating signed URLs or cookies.
     * </p>
     * 
     * @param cloudFrontUrl The CloudFront URL for which to generate the request.
     * 
     * @return CannedSignerRequest containing the necessary information.
     * 
     * @throws CloudFrontSiginingException if an error occurs while generating the
     *                                     CannedSignerRequest.
     * 
     * @since 1.0
     */
    private CannedSignerRequest getCannedSignerRequest(String cloudFrontUrl) {
        try {
            Instant expirationTime = Instant.now().plusSeconds(3600); // 1 hour expiration

            return CannedSignerRequest.builder()
                    .resourceUrl(cloudFrontUrl)
                    .keyPairId(properties.keyPairId())
                    .privateKey(privateKey)
                    .expirationDate(expirationTime)
                    .build();

        } catch (Exception e) {
            LOG.error("Error generating CannedSignerRequest: {}", e.getMessage());
            throw new CloudFrontSigningException(
                    "Failed to generate CannedSignerRequest for CloudFront URL: " + cloudFrontUrl,
                    cloudFrontUrl, SeverityLevel.HIGH, e);
        }
    }
}
