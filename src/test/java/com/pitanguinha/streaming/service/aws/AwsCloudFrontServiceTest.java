package com.pitanguinha.streaming.service.aws;

import static org.mockito.Mockito.*;

import java.security.PrivateKey;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;

import com.pitanguinha.streaming.config.aws.cloudfront.CloudFrontProperties;
import com.pitanguinha.streaming.exceptions.aws.cloudfront.CloudFrontSigningException;

import static org.junit.jupiter.api.Assertions.*;

import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.cloudfront.*;
import software.amazon.awssdk.services.cloudfront.model.*;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCannedPolicy;

@ExtendWith(MockitoExtension.class)
public class AwsCloudFrontServiceTest {
    @InjectMocks
    AwsCloudFrontService service;

    @Mock
    CloudFrontClient client;

    @Mock
    CloudFrontProperties properties;

    @Mock
    CloudFrontUtilities utilities;

    @Mock
    @Qualifier("cloudFrontPrivateKey")
    PrivateKey privateKey;

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("When cache invalidation is successful, return true")
    void invalidateCache_Success() {
        var mockResponse = mock(CreateInvalidationResponse.class);
        var sdkHttpResponse = SdkHttpResponse.builder().statusCode(200).build();

        when(mockResponse.sdkHttpResponse()).thenReturn(sdkHttpResponse);
        when(client.createInvalidation(any(Consumer.class)))
                .thenReturn(mockResponse);

        assertTrue(service.invalidateCache("test-object-id"), "Cache invalidation should succeed");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("When cache invalidation fails, return false")
    void invalidateCache_Failure() {
        var mockResponse = mock(CreateInvalidationResponse.class);
        var sdkHttpResponse = SdkHttpResponse.builder().statusCode(500).build();

        when(mockResponse.sdkHttpResponse()).thenReturn(sdkHttpResponse);
        when(client.createInvalidation(any(Consumer.class)))
                .thenReturn(mockResponse);

        assertFalse(service.invalidateCache("test-object-id"), "Cache invalidation should fail");
    }

    @Test
    @DisplayName("Should generate signed cookies for a object directory successfully")
    void getSignedCookies_Success() {
        var mockCookies = mock(CookiesForCannedPolicy.class);

        when(utilities.getCookiesForCannedPolicy(any(CannedSignerRequest.class)))
                .thenReturn(mockCookies);

        var result = service.getSignedCookies("test-object-id");

        assertNotNull(result, "Signed cookies should not be null");
        assertEquals(mockCookies, result, "Signed cookies should match the mocked cookies");
        verify(utilities).getCookiesForCannedPolicy(any(CannedSignerRequest.class));
    }

    @Test
    @DisplayName("When generating signed cookies fails, throw exception")
    void getSignedCookies_Failure() {
        when(utilities.getCookiesForCannedPolicy(any(CannedSignerRequest.class)))
                .thenThrow(new RuntimeException("Failed to generate signed cookies"));

        assertThrows(RuntimeException.class, () -> service.getSignedCookies("test-object-id"),
                "Should throw exception when generating signed cookies fails");
    }

    @Test
    @DisplayName("Should generate signed URL successfully")
    void getSignedUrl_Success() {
        var mockSignedUrl = mock(SignedUrl.class);

        when(utilities.getSignedUrlWithCannedPolicy(any(CannedSignerRequest.class)))
                .thenReturn(mockSignedUrl);

        var result = service.getSignedUrl("test-key");

        assertNotNull(result, "Signed URL should not be null");
        assertEquals(mockSignedUrl, result, "Signed URL should match the mocked signed URL");
        verify(utilities).getSignedUrlWithCannedPolicy(any(CannedSignerRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when generating signed URL fails")
    void getSignedUrl_Failure() {
        when(utilities.getSignedUrlWithCannedPolicy(any(CannedSignerRequest.class)))
                .thenThrow(new CloudFrontSigningException("Failed to generate signed URL", null, null));

        assertThrows(CloudFrontSigningException.class, () -> service.getSignedUrl("test-key"),
                "Should throw exception when generating signed URL fails");
    }
}
