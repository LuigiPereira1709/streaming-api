package com.pitanguinha.streaming.utils;

import java.nio.file.*;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;

import com.pitanguinha.streaming.enums.exceptions.SeverityLevel;
import com.pitanguinha.streaming.exceptions.internal.InternalException;

import reactor.core.publisher.Mono;

/**
 * Utility class for file operations.
 * 
 * @since 1.0
 */
public class FileUtils {
    /**
     * Transfers a file to a specified directory.
     * 
     * <p>
     * This method transfers the contents of a FilePart to a specified
     * directory and returns the path of the transferred file.<br>
     * The callable should handle any exceptions that may occur during the
     * transfer.
     * </p>
     * 
     * @param workDir  the directory to transfer the file to
     * @param fileName the name of the file
     * @param file     the FilePart to transfer
     * 
     * @return the path of the transferred file
     * 
     * @throws InternalException if an error occurs during the transfer.
     * 
     * @since 1.0
     */
    public static Mono<Path> transferTo(Path workDir, String fileName, FilePart file) {
        Path tempFile = workDir.resolve(fileName);
        return file.transferTo(tempFile)
                .onErrorMap(e -> new InternalException(
                        "Error transferring file to " + workDir.toAbsolutePath().toString() + " with name "
                                + fileName,
                        FileUtils.class, SeverityLevel.HIGH, e))
                .thenReturn(tempFile);
    }

    /**
     * Checks if the file size is supported.
     * 
     * <p>
     * This method checks if the size of the provided FilePart is within the
     * specified maximum file size limit. If the file size exceeds the limit or is
     * less than or equal to zero, an IllegalArgumentException is thrown.
     * </p>
     * 
     * @param file        the FilePart to check
     * @param maxFileSize the maximum allowed file size in MB
     * 
     * @throws IllegalArgumentException if the file size is not supported.
     */
    public static Mono<Void> isFileSizeSupported(FilePart file, int maxFileSize) {
        if (maxFileSize <= 0)
            throw new IllegalArgumentException("Max file size must be greater than 0");

        if (file == null)
            return Mono.empty();

        return file.content()
                .map(dataBfr -> {
                    int readableBytes = dataBfr.readableByteCount();
                    DataBufferUtils.release(dataBfr);
                    return (long) readableBytes;
                })
                .reduce(Long::sum)
                .flatMap(fileSize -> {
                    long maxFileSizeInBytes = maxFileSize * 1024 * 1024 * 1024;
                    if (fileSize >= maxFileSizeInBytes || fileSize <= 0) {
                        return Mono.error(new IllegalArgumentException(
                                "File size is not supported. File size: " + fileSize + " bytes, Max file size: "
                                        + maxFileSizeInBytes + " bytes"));
                    }
                    return Mono.empty();
                });
    }
}
