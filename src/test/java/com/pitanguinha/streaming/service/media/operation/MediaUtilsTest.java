package com.pitanguinha.streaming.service.media.operation;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;

import java.util.Comparator;

import static org.mockito.Mockito.*;

import org.springframework.http.codec.multipart.FilePart;

import reactor.test.StepVerifier;
import reactor.core.publisher.Mono;

public class MediaUtilsTest {
    MediaUtils utils;

    // @Test
    // @DisplayName("When file exists, should return duration in HH:MM:SS format")
    // void getDuration_ReturnDurationInHHMMSSFormat() throws Exception {
    // try (MockedStatic<ProcessorManager> processorManagerMock =
    // mockStatic(ProcessorManager.class);
    // MockedStatic<Files> filesMock = mockStatic(Files.class)) {
    // // 1. Mock the Path to simulate a file
    // Path pathMock = mock(Path.class);
    // when(pathMock.toAbsolutePath()).thenReturn(pathMock);

    // // 2. Simulate that the file exists
    // filesMock.when(() -> Files.exists(pathMock)).thenReturn(true);

    // // 3. Mock the ProcessorManager to simulate the execution of a command
    // Process processMock = mock(Process.class);
    // processorManagerMock.when(() -> ProcessorManager.execute(anyString()))
    // .thenReturn(processMock);

    // // 4. Mock the Process to simulate the input stream that return the duration
    // InputStream inputStreamMock = mock(InputStream.class);
    // when(processMock.getInputStream()).thenReturn(inputStreamMock);
    // when(inputStreamMock.readAllBytes()).thenReturn("42".getBytes()); // 42
    // seconds as a string

    // // When: call the getDuration method with the mocked path
    // String duration = MediaUtils.getDuration(pathMock);
    // // Then: the duration should be in HH:MM:SS format
    // assertEquals("00:00:42", duration, "Duration should be 00:00:42");
    // }
    // }

    // @Test
    // @DisplayName("When file does not exist, should throw
    // IllegalArgumentException")
    // void getDuration_ThrowsIllegalArgumentException_WhenFileDoesNotExist() {
    // // 1. Create a Path that does not exist
    // Path nonExistentPath = Path.of("non-existent-file.mp3");

    // // 2. When: call the getDuration method with the non-existent path
    // // Then: it should throw an IllegalArgumentException
    // assertThrows(IllegalArgumentException.class, () ->
    // MediaUtils.getDuration(nonExistentPath),
    // "Expected IllegalArgumentException when file does not exist");
    // }

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
        MediaUtils.transferTo(targetPath, "test.mp3", mockFile)
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
