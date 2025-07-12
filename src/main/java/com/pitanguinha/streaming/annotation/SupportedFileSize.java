package com.pitanguinha.streaming.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;

import jakarta.validation.*;
import reactor.core.publisher.Mono;

/**
 * Custom annotation for validating that a file part's size is within the
 * supported limits.<br>
 * Applicable to fields of type {@link FilePart}.<br>
 * The file size must be greater than 0 and less than or equal to the
 * specified maximum file size (default is 1 MB).
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD })
@Constraint(validatedBy = SupportedFileSizeValidator.class)
public @interface SupportedFileSize {
    String message() default "Invalid file size.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int maxFileSize() default 1;
}

class SupportedFileSizeValidator implements ConstraintValidator<SupportedFileSize, FilePart> {
    private long maxFileSize;

    @Override
    public void initialize(SupportedFileSize constraintAnnotation) {
        this.maxFileSize = constraintAnnotation.maxFileSize() * 1024 * 1024; // Convert MB to bytes
    }

    @Override
    public boolean isValid(FilePart file, ConstraintValidatorContext context) {
        if (file == null)
            return true; // Null file part is valid

        Mono<Long> bytesCount = file.content()
                .map(dataBfr -> {
                    int readableBytes = dataBfr.readableByteCount();
                    DataBufferUtils.release(dataBfr);
                    return (long) readableBytes;
                })
                .reduce(Long::sum);

        long fileSize = bytesCount.blockOptional().orElse(0L);

        if (fileSize >= maxFileSize || fileSize <= 0)
            return false;

        return true;
    }
}
