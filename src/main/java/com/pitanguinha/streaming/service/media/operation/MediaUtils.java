package com.pitanguinha.streaming.service.media.operation;

import java.nio.file.*;

import org.springframework.http.codec.multipart.FilePart;

import com.pitanguinha.streaming.enums.exceptions.SeverityLevel;
import com.pitanguinha.streaming.exceptions.internal.InternalException;

import reactor.core.publisher.Mono;

/**
 * Utility class for media operations.
 * 
 * @since 1.0
 */
class MediaUtils {
    // /**
    // * Gets the duration of a media file in the format HH:mm:ss.
    // *
    // * <p>
    // * This method uses ffprobe to get the duration of the media file.
    // * </p>
    // *
    // * @param file the media file
    // *
    // * @return the duration of the media file in HH:mm:ss format
    // *
    // * @throws IllegalArgumentException if the file does not exist
    // * @throws InternalException if an error occurs while executing the
    // * ffprobe command
    // *
    // * @see ProcessorManager#execute(String) Used to execute the ffprobe command.
    // *
    // * @since 1.0
    // */
    // public static String getDuration(@NotNull Path file) {
    // if (!Files.exists(file))
    // throw new IllegalArgumentException("File does not exist: " +
    // file.toString());

    // try {
    // String ffprobeCommand = "ffprobe -v error -show_entries format=duration -of
    // default=noprint_wrappers=1:nokey=1 "
    // + file.toAbsolutePath().toString();
    // Process process = ProcessorManager.execute(ffprobeCommand);

    // byte[] output = process.getInputStream().readAllBytes();

    // float duration = Float.parseFloat(new String(output).trim());
    // int hours = (int) (duration / 3600);
    // int minutes = (int) (duration / 60);
    // int seconds = (int) (duration % 60);

    // return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    // } catch (IOException e) {
    // throw new InternalException(
    // "Error getting duration of file " + file.toAbsolutePath().toString(),
    // MediaUtils.class,
    // SeverityLevel.MEDIUM, e);
    // }
    // }

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
        // String extension = getFileExtension(file.filename());
        // String finalFileName = extension.isEmpty()
        //         ? fileName
        //         : fileName + "." + extension;

        Path tempFile = workDir.resolve(fileName);
        return file.transferTo(tempFile)
                .onErrorMap(e -> new InternalException(
                        "Error transferring file to " + workDir.toAbsolutePath().toString() + " with name "
                                + fileName,
                        MediaUtils.class, SeverityLevel.HIGH, e))
                .thenReturn(tempFile);
    }

    // private static String getFileExtension(String originalFileName) {
    //     int dotIndex = originalFileName.lastIndexOf('.');
    //     if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
    //         return originalFileName.substring(dotIndex + 1);
    //     }
    //     return "";
    // }
}
