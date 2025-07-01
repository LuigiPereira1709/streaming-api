package com.pitanguinha.streaming.service;

import java.io.*;
import java.nio.file.*;

import java.util.*;

import org.slf4j.*;

import org.springframework.stereotype.Service;

import com.pitanguinha.streaming.enums.exceptions.SeverityLevel;
import com.pitanguinha.streaming.exceptions.internal.InternalException;

import org.springframework.beans.factory.annotation.*;

import jakarta.validation.constraints.NotNull;

/**
 * Service class for temp directories.
 * 
 * <p>
 * Is used to make actions on temp directories.<br>
 * Like create directories, create subdirectories, delete directories and files.
 * </p>
 * 
 * @see Supported classes: {@link OpusHandler} and {@link DashAudioHandler}
 * 
 * @since 1.0
 */
@Service
public class TempDirService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TempDirService.class);
    @Value("${spring.application.temp-dir:/tmp/app/streaming/prod/}")
    private Path baseDir;

    // /**
    // * Create directories.
    // *
    // * <p>
    // * This will create the directory and subdirectories if it's not null.
    // * </p>
    // *
    // * @param parentDir The parent directory that will be created.
    // * @param subDirs The subdirectories that will be created.
    // *
    // * @return The path of the parent directory.
    // *
    // * @see #getOrCreateDir(String) Used to get or create the parent directory.
    // * @see #createSubDirectories(Path, String...) Used to create the
    // * subdirectories.
    // * @See #createDirectoriesAsync(ExecutorService, String, String...)
    // Asynchronous
    // * method of this.
    // *
    // * @since 1.0
    // */
    // public Path createDirectories(@NotNull String parentDir, @Nullable String...
    // subDirs) {
    // Path dirPath = getOrCreateDir(parentDir);

    // if (subDirs != null) {
    // createSubDirectories(dirPath, subDirs);
    // }

    // return dirPath;
    // }

    // /**
    // * Create directories asynchronously.
    // *
    // * <p>
    // * This is asynchronous method that will create the directory and
    // * subdirectories if it's not null.
    // * </p>
    // *
    // * @param executor The executor that will be used to {@link
    // CompletableFuture}.
    // * @param parentDir The parent directory that will be created.
    // * @param subDirs The subdirectories that will be created.
    // *
    // * @return The path of the parent directory.
    // *
    // * @see #createDirectories(String, String...) Synchronous method of this.
    // *
    // * @since 1.0
    // */
    // public CompletableFuture<Path> createDirectoriesAsync(ExecutorService
    // executor,
    // @NotNull String parentDir, @Nullable String... subDirs) {
    // return CompletableFuture.supplyAsync(() -> {
    // return createDirectories(parentDir, subDirs);
    // }, executor);
    // }

    /**
     * Get or create the directory.
     * 
     * <p>
     * This will get or create the directory if dir does not exist.
     * </p>
     * 
     * @param dir The directory that will be used to create the directory.
     * 
     * @return The path of the directory.
     * 
     * @throws InternalException if has an error on creating the directory.
     * 
     * @see #isValidString(String...) Used to check if the dir string is valid.
     * @see #getDir(String) Used to get the directory.
     * 
     * @since 1.0
     */
    public Path getOrCreateDir(@NotNull String dirName) {
        isValidString(dirName);

        Path dirPath = getDir(dirName);

        if (dirPath != null) {
            LOGGER.info("The directory already exists: {}", dirPath);
            return dirPath;
        }

        synchronized (dirName.intern()) {
            try {
                LOGGER.debug("Directory created for {}", dirName);
                return Files.createDirectories(this.baseDir.resolve(dirName));
            } catch (IOException e) {
                LOGGER.error("Error creating directory: {}", e.getMessage());
                throw new InternalException(
                        "Error during creating directory for %s: %s".formatted(dirName, e.getMessage()),
                        TempDirService.class, SeverityLevel.HIGH, e);
            }
        }
    }

    /**
     * Get the directory.
     * 
     * <p>
     * This will get the directory if it exists, otherwise it will return null.
     * </p>
     * 
     * @param dir The directory that will be used to create the directory.
     * 
     * @return The path of the directory.
     * 
     * @since 1.0
     */
    private Path getDir(@NotNull String dirName) {
        isValidString(dirName);

        Path dirPath = this.baseDir.resolve(dirName);

        return Files.exists(dirPath) ? dirPath : null;
    }

    // /**
    // * Create subdirectories for a directory.
    // *
    // * <p>
    // * This will create the subdirectories if the base directory exists and the
    // * subdirectories do not exist.
    // * </p>
    // *
    // * @param baseDir The directory that will be used to create the
    // subdirectories.
    // * @param subDirs The subdirectories that will be created.
    // *
    // * @return The paths of the subdirectories.
    // *
    // * @throws IllegalArgumentException if the directory does not exist.
    // * @throws RuntimeException if has an error on creating the
    // * subdirectories.
    // *
    // * @since 1.0
    // */
    // public Path[] createSubDirectories(@NotNull Path baseDir, @NotNull String...
    // subDirs) {
    // if (!Files.exists(baseDir))
    // throw new IllegalArgumentException("Directory does not exist:
    // %s".formatted(baseDir));

    // Path[] subDirsPaths = new Path[subDirs.length];
    // try {
    // for (int i = 0; i < subDirs.length; i++) {
    // Path subDirPath = baseDir.resolve(subDirs[i]);
    // if (!Files.exists(subDirPath)) {
    // Files.createDirectories(subDirPath);
    // LOGGER.debug("Subdir created for {}: {}", subDirs[i],
    // baseDir.resolve(subDirs[i]));
    // }

    // subDirsPaths[i] = subDirPath;
    // }

    // } catch (IOException e) {
    // LOGGER.error("Error creating directory: {}", e.getMessage());
    // throw new RuntimeException("Error during creating directory:
    // %s".formatted(e.getMessage()));
    // }
    // return subDirsPaths;
    // }

    /**
     * Delete the directory.
     * 
     * <p>
     * Walk through the directory and delete all file first and then the
     * directory.
     * </p>
     * 
     * @param dirPath The path of the directory that will be deleted.
     * 
     * @throws IllegalArgumentException if the directory does not exist.
     * @throws InternalException        if has an error on deleting the directory.
     * 
     * @since 1.0
     */
    public void deleteDirectory(@NotNull Path dirPath) {
        try {
            if (!Files.exists(dirPath)) {
                throw new IllegalArgumentException("Directory does not exist: %s".formatted(dirPath));
            }

            Files.walk(dirPath)
                    .sorted(Comparator.reverseOrder()) // Delete files first.
                    .map(Path::toFile)
                    .forEach(File::delete);

            LOGGER.debug("Directory deleted: {}", dirPath);
        } catch (IOException e) {
            LOGGER.error("Error deleting directory: {}", e.getMessage());
            throw new InternalException(
                    "Error during deleting directory: %s".formatted(e.getMessage()), TempDirService.class,
                    SeverityLevel.HIGH, e);
        }
    }

    // /**
    // * Delete a file or directory.
    // *
    // * <p>
    // * This will delete the file or directory(empty) if it exists.<br>
    // * Precociously, it will log a warning if the file or directory does not exist
    // * and will not throw an exception.
    // * </p>
    // *
    // * @param path The path of the file or directory.
    // *
    // * @throws RuntimeException if has an error on deleting the file or directory.
    // *
    // * @since 1.0
    // */
    // public void deletePath(@NotNull Path path) {
    // if (!Files.exists(path)) {
    // LOGGER.warn("File does not exist: {}", path);
    // return;
    // }

    // try {
    // switch (path) {
    // case Path p when Files.isRegularFile(p):
    // if (Files.deleteIfExists(p)) {
    // LOGGER.debug("File deleted: {}", p);
    // break;
    // }
    // LOGGER.warn("File not deleted: {}", path);
    // break;

    // case Path p when Files.isDirectory(p):
    // // Try with resources to auto-close the stream.
    // try (var stream = Files.list(p)) {
    // if (stream.findAny().isEmpty() && Files.deleteIfExists(p)) {
    // LOGGER.debug("Directory deleted: {}", p);
    // break;
    // }
    // LOGGER.warn("Directory not deleted or empty: {}.", p);
    // }
    // break;

    // default:
    // LOGGER.warn("Not an expected file: {}", path);
    // }
    // } catch (IOException e) {
    // LOGGER.error("Error deleting file: {}", e.getMessage());
    // throw new RuntimeException("Error during deleting file:
    // %s".formatted(e.getMessage()));
    // }
    // }

    // /**
    // * Find a file on the directory.
    // *
    // * <p>
    // * This will walk through the directory and find the first file that match
    // * the regex.
    // * </p>
    // *
    // * @param taskDir The path of the directory.
    // * @param regex The regex that will be used to find the file.
    // *
    // * @return The path of the file.
    // *
    // * @throws IOException if has an error on walking through the directory.
    // *
    // * @since 1.0
    // */
    // public Optional<Path> findFileOnDir(@NotNull Path dir, @NotNull String regex)
    // {
    // try {
    // return Files.walk(dir)
    // .filter(Files::isRegularFile)
    // .filter(p -> p.getFileName().toString().matches(regex))
    // .findFirst();
    // } catch (IOException e) {
    // throw new RuntimeException("Error during walking through the directory:
    // %s".formatted(e.getMessage()));
    // }
    // }

    /**
     * Check if the string is valid.
     * 
     * @param str The string that will be checked.
     * 
     * @throws IllegalArgumentException if the string is blank.
     * 
     * @since 1.0
     */
    private void isValidString(@NotNull String... str) {
        for (String s : str) {
            if (s.isBlank()) {
                throw new IllegalArgumentException("String is blank: %s".formatted(s));
            }
        }
    }
}
