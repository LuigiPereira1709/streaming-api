package com.pitanguinha.streaming.util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;

import java.util.Comparator;

import static org.mockito.Mockito.*;

import org.springframework.http.codec.multipart.FilePart;

import com.pitanguinha.streaming.utils.FileUtils;

import reactor.test.StepVerifier;
import reactor.core.publisher.Mono;

public class FileUtilsTest {
    FileUtils utils;

    @Test
    @DisplayName("Should transfer file to provided directory path")
    void transferTo_ReturnFileInProvidedDirectoryPath() throws Exception {
        // 1. Create a temporary directory for the test and a test file
        Path targetPath = Path.of(System.getProperty("java.io.tmpdir"), "test-transfer-to");
        Files.createDirectories(targetPath);
        Path testFile = Files.createFile(targetPath.resolve("test.txt"));

        // 2. When the transferTo method is called, it should copy the test file to the
        // target directory
        // Simulating the real behavior of transferTo method
        FilePart mockFile = mock(FilePart.class);
        when(mockFile.filename()).thenReturn("test.mp3");

        doAnswer(invocation -> {
            Path target = invocation.getArgument(0);
            Files.copy(testFile, target);
            return Mono.empty();
        }).when(mockFile).transferTo(any(Path.class));

        // When: transferTo is called with the target path and a file name
        FileUtils.transferTo(targetPath, "test.mp3", mockFile)
                // Then: the file should be copied to the target directory
                .as(StepVerifier::create)
                .expectNextMatches(filePath -> {
                    // Check if the file exists in the target directory
                    assertTrue(Files.exists(filePath), "File should exist in the target directory");
                    assertTrue(filePath.endsWith("test.mp3"), "File name should be test.mp3");
                    return true;
                });

        // 3. Delete the target directory after all
        Files.walk(targetPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
