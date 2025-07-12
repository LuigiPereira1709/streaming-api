package com.pitanguinha.streaming.utils;

import java.nio.file.*;

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
}
