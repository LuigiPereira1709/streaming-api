package com.pitanguinha.streaming.exceptions;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;

@WebFluxTest(TestController.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Test CloudFrontSigningException handling")
    void testHandleCloudFrontSigningException() {
        webTestClient.get().uri("/test/cloudFrontSigningException")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("CloudFront signing error occurred")
                .jsonPath("$.details.url").isEqualTo("http://example.com");
    }

    @Test
    @DisplayName("Test S3Exception handling")
    void testHandleS3Exception() {
        webTestClient.get().uri("/test/s3Exception")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("S3 operation error occurred")
                .jsonPath("$.details.objectKey").exists()
                .jsonPath("$.details.operation").exists();
    }

    @Test
    @DisplayName("Test DomainStateException handling")
    void testHandleDomainStateException() {
        webTestClient.get().uri("/test/domainStateException")
                .exchange()
                .expectStatus().is4xxClientError() // BAD_REQUEST no seu handler
                .expectBody()
                .jsonPath("$.message").isEqualTo("Domain state error occurred")
                .jsonPath("$.details.stateName").exists();
    }

    @Test
    @DisplayName("Test NotFoundException handling")
    void testHandleNotFoundException() {
        webTestClient.get().uri("/test/notFoundException")
                .exchange()
                .expectStatus().is4xxClientError() // NOT_FOUND no seu handler
                .expectBody()
                .jsonPath("$.message").isEqualTo("Resource not found")
                .jsonPath("$.details.message").exists()
                .jsonPath("$.details.errorMessage").exists();
    }

    @Test
    @DisplayName("Test InvalidSearchTypeException handling")
    void testHandleInvalidSearchTypeException() {
        webTestClient.get().uri("/test/invalidSearchTypeException")
                .exchange()
                .expectStatus().is4xxClientError() // BAD_REQUEST no seu handler
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid search type")
                .jsonPath("$.details.message").exists()
                .jsonPath("$.details.searchType").exists()
                .jsonPath("$.details.errorMessage").exists();
    }

    @Test
    @DisplayName("Test SearchTypeArgumentsException handling")
    void testHandleSearchTypeArgumentsException() {
        webTestClient.get().uri("/test/searchTypeArgumentsException")
                .exchange()
                .expectStatus().is4xxClientError() // BAD_REQUEST no seu handler
                .expectBody()
                .jsonPath("$.message").isEqualTo("Search type arguments error")
                .jsonPath("$.details.errorMessage").exists();
    }

    @Test
    @DisplayName("Test InternalException handling")
    void testHandleInternalException() {
        webTestClient.get().uri("/test/internalException")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Internal error occurred")
                .jsonPath("$.details.severityLevel").exists()
                .jsonPath("$.details.propagationTime").exists();
    }

    @Test
    @DisplayName("Test WebExchangeBindException handling")
    void testHandleWebExchangeBindException() {
        webTestClient.post().uri("/test/webExchangeBindException")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().is4xxClientError() // BAD_REQUEST no seu handler
                .expectBody()
                .jsonPath("$.message").isEqualTo("Validation error occurred")
                .jsonPath("$.details.validationErrors").isMap();
    }

    @Test
    @DisplayName("Test DecodingException handling")
    void testHandleDecodingException() {
        webTestClient.post().uri("/test/decodingException")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .exchange()
                .expectStatus().is4xxClientError() // BAD_REQUEST no seu handler
                .expectBody()
                .jsonPath("$.message").isEqualTo("Decoding error occurred")
                .jsonPath("$.details.error").exists()
                .jsonPath("$.details.errorMessage").exists();
    }

    @Test
    @DisplayName("Test generic exception handling")
    void testHandleGenericException() {
        webTestClient.get().uri("/test/genericException")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("An unexpected error occurred");
    }
}
