package com.pitanguinha.streaming.exceptions;

import org.springframework.web.bind.annotation.*;

import com.pitanguinha.streaming.dto.media.MediaPostDto;
import com.pitanguinha.streaming.enums.exceptions.*;
import com.pitanguinha.streaming.exceptions.domain.*;
import com.pitanguinha.streaming.enums.media.SearchType;
import com.pitanguinha.streaming.exceptions.search.*;

import jakarta.validation.Valid;

import com.pitanguinha.streaming.exceptions.aws.s3.S3Exception;
import com.pitanguinha.streaming.exceptions.aws.cloudfront.CloudFrontSigningException;
import com.pitanguinha.streaming.exceptions.internal.InternalException;

@RestController
@RequestMapping("/test")
public class ControllerExceptionsTest {
    @GetMapping("/cloudFrontSigningException")
    public void throwCloudFrontSigningException() {
        throw new CloudFrontSigningException("Test CloudFront signing exception", "http://example.com",
                SeverityLevel.HIGH);
    }

    @GetMapping("/s3Exception")
    public void throwS3Exception() {
        throw new S3Exception("Test S3 exception", "key", S3OperationException.UPLOAD_FAILED, SeverityLevel.MEDIUM);
    }

    @GetMapping("/domainStateException")
    public void throwDomainStateException() {
        throw new DomainStateException("Test domain state exception", "unused");
    }

    @GetMapping("/notFoundException")
    public void throwNotFoundException() {
        throw new NotFoundException("Test not found exception", "unused");
    }

    @GetMapping("/invalidSearchTypeException")
    public void throwInvalidSearchTypeException() {
        throw new InvalidSearchTypeException("Test invalid search type exception", "unused", SearchType.TITLE);
    }

    @GetMapping("/searchTypeArgumentsException")
    public void throwSearchTypeArgumentsException() {
        throw new SearchTypeArgumentsException("Test search type arguments exception");
    }

    @GetMapping("/internalException")
    public void throwInternalException() {
        throw new InternalException("Test internal exception", TestController.class, SeverityLevel.CRITICAL);
    }

    @PostMapping("/webExchangeBindException")
    public void throwWebExchangeBindException(@Valid @RequestBody MediaPostDto postDto) {
        // This method is intentionally left empty to simulate a binding exception.
        // In a real scenario, you would handle the binding logic here.
        // If the request body cannot be bound, it will throw a
        // WebExchangeBindException.
    }

    @PostMapping("/decodingException")
    public void thrownDecodingException(@ModelAttribute MediaPostDto postDto) {
        // This method is intentionally left empty to simulate a decoding exception.
        // In a real scenario, you would handle the decoding logic here.
        // If the request body cannot be decoded, it will throw a DecodingException.
    }

    @GetMapping("/genericException")
    public void throwGenericException() {
        throw new RuntimeException("Test generic exception");
    }
}
