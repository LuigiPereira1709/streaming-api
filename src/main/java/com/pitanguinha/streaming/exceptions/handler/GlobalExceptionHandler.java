package com.pitanguinha.streaming.exceptions.handler;

import org.slf4j.*;
import org.springframework.http.HttpStatus;
import org.springframework.core.codec.DecodingException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.pitanguinha.streaming.exceptions.domain.*;
import com.pitanguinha.streaming.exceptions.search.*;
import com.pitanguinha.streaming.exceptions.aws.s3.S3Exception;
import com.pitanguinha.streaming.exceptions.internal.InternalException;
import com.pitanguinha.streaming.exceptions.aws.cloudfront.CloudFrontSigningException;

import reactor.core.publisher.Mono;

/**
 * Global exception handler for handling exceptions related to AWS services.
 * This class intercepts exceptions thrown by controllers and provides a
 * standardized response format.
 *
 * @since 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles exceptions related to CloudFront signing operations.
     *
     * @param ex The exception thrown during CloudFront signing operations.
     * @return A ResponseEntity containing the error details and HTTP status.
     */
    @ExceptionHandler(CloudFrontSigningException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleCloudFrontSigningException(CloudFrontSigningException ex) {
        LOG.warn("[CloudFrontSigningException] Error occured: {} - {}", ex.getMessage(), ex.getDetails(), ex);

        return buildResponseError(
                "CloudFront signing error occurred",
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                Map.of("url", ex.getDetails().url()));
    }

    /**
     * Handles exceptions related to AWS S3 operations.
     *
     * @param ex The exception thrown during S3 operations.
     * @return A ResponseEntity containing the error details and HTTP status.
     */
    @ExceptionHandler(S3Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleS3Exception(S3Exception ex) {
        LOG.warn("[S3Exception] Error occured: {} - {}", ex.getMessage(), ex.getDetails(), ex);

        var details = ex.getDetails();
        var detailsMap = new LinkedHashMap<String, Object>(Map.of(
                "objectKey", details.objectKey(),
                "operation", details.operation().name()));

        return buildResponseError(
                "S3 operation error occurred",
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                detailsMap);
    }

    /**
     * Handles exceptions related to domain state operations.
     *
     * @param ex The exception thrown during domain state operations.
     * @return A ResponseEntity containing the error details and HTTP status.
     */
    @ExceptionHandler(DomainStateException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDomainStateException(DomainStateException ex) {
        LOG.warn("[DomainStateException] Error occured: {} - {}", ex.getMessage(), ex.getDetails(), ex);

        var details = ex.getDetails();
        var detailsMap = new LinkedHashMap<String, Object>(Map.of(
                "stateName", details.stateName()));

        return buildResponseError(
                "Domain state error occurred",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                detailsMap);
    }

    /**
     * Handles exceptions related to resource not found operations.
     *
     * @param ex The exception thrown during resource not found operations.
     * @return A ResponseEntity containing the error details and HTTP status.
     */
    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNotFoundException(NotFoundException ex) {
        LOG.warn("[NotFoundException] Error occured: {} - {}", ex.getMessage(), ex.getDetails(), ex);

        var details = ex.getDetails();
        var detailsMap = new LinkedHashMap<String, Object>(Map.of(
                "errorMessage", details.errorMessage()));

        return buildResponseError(
                "Resource not found",
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                detailsMap);
    }

    /**
     * Handles exceptions related to invalid search types.
     *
     * @param ex The exception thrown during search type operations.
     * @return A ResponseEntity containing the error details and HTTP status.
     */
    @ExceptionHandler(InvalidSearchTypeException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidSearchTypeException(InvalidSearchTypeException ex) {
        LOG.warn("[InvalidSearchTypeException] Error occured: {} - {}", ex.getMessage(), ex.getDetails(), ex);

        var details = ex.getDetails();
        var detailsMap = new LinkedHashMap<String, Object>(Map.of(
                "searchType", details.searchType(),
                "errorMessage", details.errorMessage(),
                "message", details.message()));

        return buildResponseError(
                "Invalid search type",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                detailsMap);
    }

    /**
     * Handles exceptions related to search type arguments.
     *
     * @param ex The exception thrown during search type arguments operations.
     * @return A ResponseEntity containing the error details and HTTP status.
     */
    @ExceptionHandler(SearchTypeArgumentsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleSearchTypeArgumentsException(SearchTypeArgumentsException ex) {
        LOG.warn("[SearchTypeArgumentsException] Error occured: {} - {}", ex.getMessage(), ex.getDetails(), ex);

        var details = ex.getDetails();
        var detailsMap = new LinkedHashMap<String, Object>(Map.of(
                "errorType", details.getClass().getSimpleName()));

        return buildResponseError(
                "Search type arguments error",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                detailsMap);
    }

    /**
     * Handles illegal argument exceptions that occur during request processing.
     *
     * @param ex The exception thrown due to an illegal argument.
     * @return A ResponseEntity containing the error details and HTTP status.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgumentException(IllegalArgumentException ex) {
        LOG.warn("[IllegalArgumentException] Error occured: {}", ex.getMessage(), ex);

        return buildResponseError(
                "Invalid argument provided",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                Map.of("error", ex.getClass().getSimpleName()));
    }

    /**
     * Handles internal exceptions that occur within the application.
     *
     * @param ex The exception thrown during internal operations.
     * @return A ResponseEntity containing the error details and HTTP status.
     */
    @ExceptionHandler(InternalException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleCustomInternalException(InternalException ex) {
        LOG.warn("[InternalException] Error occured: {} - {}", ex.getMessage(), ex.getDetails(), ex);

        var detail = ex.getDetails();
        var detailsMap = new LinkedHashMap<String, Object>(Map.of(
                "severityLevel", detail.severityLevel().name(),
                "propagationTime", detail.propagationTime().toString()));

        return buildResponseError(
                "Internal error occurred",
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                detailsMap);
    }

    /**
     * Handles validation errors that occur during request binding.
     *
     * @param ex The exception thrown during request binding.
     * @return A ResponseEntity containing the error details and HTTP status.
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleWebExchangeBindException(WebExchangeBindException ex) {
        LOG.warn("[WebExchangeBindException] Validation error: {}", ex.getMessage(), ex);

        var errors = ex.getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        fieldError -> fieldError.getField(),
                        Collectors.mapping(
                                fieldError -> fieldError.getDefaultMessage(),
                                Collectors.toList())));

        return buildResponseError(
                "Validation error occurred",
                "Invalid input data",
                HttpStatus.BAD_REQUEST,
                Map.of("validationErrors", errors));
    }

    /**
     * Handles decoding exceptions that occur during request body decoding.
     *
     * @param ex The exception thrown during request body decoding.
     * @return A ResponseEntity containing the error details and HTTP status.
     */
    @ExceptionHandler(DecodingException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDecodingException(DecodingException ex) {
        LOG.warn("[DecodingException] Error occurred: {}", ex.getMessage(), ex);

        return buildResponseError(
                "Decoding error occurred",
                "Failed to decode the request body",
                HttpStatus.BAD_REQUEST,
                Map.of("error", ex.getClass().getSimpleName(), "errorMessage", ex.getMessage()));
    }

    /**
     * Handles any other exceptions that are not specifically handled by the
     * above methods.
     *
     * @param ex The exception thrown during any operation.
     * @return A ResponseEntity containing the error details and HTTP status.
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex) {
        LOG.warn("[Exception] An unexpected error occurred: {}", ex.getMessage(), ex);

        return buildResponseError(
                "An unexpected error occurred",
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                Map.of("error", ex.getClass().getSimpleName(), "message", ex.getMessage()));
    }

    /**
     * Builds a standardized error response.
     *
     * @param message      The main message of the error response.
     * @param errorMessage The specific error message.
     * @param status       The HTTP status to be returned.
     * @param details      Additional details about the error, if any.
     * @return A Mono containing the ResponseEntity with the error response.
     */
    private Mono<ResponseEntity<ErrorResponse>> buildResponseError(String message, String errorMessage,
            HttpStatus status, Map<String, Object> details) {
        return Mono.just(ResponseEntity
                .status(status)
                .body(new ErrorResponse(
                        message,
                        errorMessage,
                        Instant.now().toString(),
                        details)));
    }
}

/**
 * Represents the structure of the error response.
 * This class is used to encapsulate the error details returned by the
 * GlobalExceptionHandler.
 *
 * @param message      The main message of the error response.
 * @param errorMessage The specific error message.
 * @param timestamp    The timestamp when the error occurred.
 * @param details      Additional details about the error, if any.
 */
record ErrorResponse(
        String message,
        String errorMessage,
        String timestamp,
        Map<String, Object> details) {
};
