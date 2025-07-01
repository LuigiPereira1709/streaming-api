package com.pitanguinha.streaming.enums.exceptions;

/**
 * Enum representing various S3 operation exceptions.
 * 
 * <p>
 * This enum is used to categorize exceptions that occur during S3 operations
 * such as upload, download, and delete.
 * </p>
 * 
 * @since 1.0
 */
public enum S3OperationException {
    UPLOAD_FAILED,
    DOWNLOAD_FAILED,
    DELETE_FAILED
}
