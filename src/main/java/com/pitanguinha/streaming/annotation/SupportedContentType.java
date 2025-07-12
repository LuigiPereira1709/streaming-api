package com.pitanguinha.streaming.annotation;

import java.lang.annotation.*;

import org.springframework.http.codec.multipart.FilePart;

import com.pitanguinha.streaming.enums.media.contenttypes.SupportedTypeUtil;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.*;

/**
 * Custom annotation for validating that a file part's content type is one of
 * the supported audio formats.<br>
 * This annotation can be applied to fields or parameters.<br>
 * The caller should handler the null or empty file part case, as this
 * annotation only checks the content type of the file part.<br>
 * Supported content types include:
 * <ul>
 * Audio Formats: opus, ogg, flac, mp3, wav, wav_x, aac.
 * </ul>
 * <ul>
 * Image Formats: png, jpeg, webp.
 * </ul>
 * 
 * @since 1.0
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, PARAMETER })
@Constraint(validatedBy = SupportedContentTypeValidator.class)
public @interface SupportedContentType {
    String message() default "Invalid content type.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends Enum<?>> enumClass();
}

class SupportedContentTypeValidator implements ConstraintValidator<SupportedContentType, FilePart> {
    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(SupportedContentType constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(FilePart filePart, ConstraintValidatorContext context) {
        if (filePart == null)
            return true; // Null file part is valid

        String contentType = filePart.headers().getContentType() != null
                ? filePart.headers().getContentType().toString()
                : null;

        if (contentType == null || contentType.isEmpty()) {
            return false; // Invalid content type
        }

        return SupportedTypeUtil.isSupported(enumClass, contentType);
    }
}
