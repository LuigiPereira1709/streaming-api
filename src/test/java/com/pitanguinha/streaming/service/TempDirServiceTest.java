package com.pitanguinha.streaming.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.Comparator;

import org.slf4j.*;

import org.junit.jupiter.api.*;

import org.springframework.test.context.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.application.temp-dir=${java.io.tmpdir}/temp-dir-service-test")
public class TempDirServiceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TempDirServiceTest.class);

    @Autowired
    private TempDirService service;

    private static Path tempDir;

    @BeforeAll
    static void setUp(@Value("${spring.application.temp-dir}") String tempDir) throws IOException {
        TempDirServiceTest.tempDir = Paths.get(tempDir);
    }

    @AfterEach
    void cleanBaseDirectory() throws IOException {
        LOGGER.debug("Cleaning base directory: {}", tempDir);
        // Delete files and directories in the base directory
        // except the base directory itself
        Files.walk(tempDir)
                .filter(path -> !path.equals(tempDir))
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        if (Files.isDirectory(path)) {
                            Files.deleteIfExists(path);
                        } else {
                            Files.deleteIfExists(path);
                        }
                    } catch (IOException e) {
                        LOGGER.error("Error cleaning directory: {}", path, e);
                    }
                });
    }

    @AfterAll
    static void tearDown() throws IOException {
        LOGGER.debug("Tearing down TempDirServiceTest: {}", tempDir);
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        LOGGER.error("Error deleting path: {}", path, e);
                    }
                });

        if (Files.exists(tempDir)) {
            LOGGER.warn("Test directory was not been completely deleted: {}", tempDir);
        }
    }

    @Test
    @DisplayName("Should get or create a directory if not exists")
    void getOrCreateDir_ReturnsDirPath() throws IOException {
        // Given: A directory path
        String dirName = "test-dir";

        // When: The directory not exists
        Path result0 = service.getOrCreateDir(dirName);
        // Then: The directory must be created
        assertTrue(Files.exists(result0), "Directory must be created: " + result0);

        // When: The directory already exists
        Path result1 = service.getOrCreateDir(dirName);

        // // Then: The path must be the same
        assertEquals(result0, result1, "Directory path must be the same: " +
                result1);
    }

    @Test
    @DisplayName("Should delete a directory if exists")
    void deleteDirectory_ReturnsVoid() throws IOException {
        // Given: A directory path
        Path dirPath = Files.createDirectory(tempDir.resolve("test-dir"));

        // When: Delete the directory
        service.deleteDirectory(dirPath);

        // Then: The directory must be deleted
        assertFalse(Files.exists(dirPath), "Directory must be deleted: " + dirPath);
    }

    @Test
    @DisplayName("When delete a directory that does not exist, throw an IllegalArgumentException")
    void deleteDirectory_ThrowsIOException() {
        Path dirPath = tempDir.resolve("test-dir");

        assertThrows(IllegalArgumentException.class, () -> service.deleteDirectory(dirPath),
                "Directory must not exist: " + dirPath);
    }

    // /**
    // * Helper method to create directories
    // *
    // * @param quantity
    // * @return List of Path
    // * @throws IOException
    // */
    // private List<Path> createDirectoriesHelper(int quantity, @Nullable Path dir)
    // throws IOException {
    // List<Path> dirs = new ArrayList<>();

    // Path baseDir = dir != null ? dir : tempDir;

    // for (int i = 0; i < quantity; i++) {
    // dirs.add(Files.createDirectory(baseDir.resolve("test-dir-" + i)));
    // }

    // return dirs;
    // }

    // /**
    // * Helper method to create files
    // *
    // * @param dir
    // * @param quantityPerDir
    // */
    // private void createFilesHelper(Path dir, int quantityPerDir) {
    // IntStream.range(0, quantityPerDir).forEach(i -> {
    // try {
    // Files.createFile(dir.resolve("test-file-" + i + ".txt"));
    // } catch (IOException e) {
    // LOGGER.error("Error creating file in directory: {}", dir, e);
    // }
    // });
    // }

    // /**
    // * Helper method to get sub directories path
    // *
    // * @return Path[]
    // */
    // private String[] getSubdirsPathHelper() {
    // return IntStream.range(0, 3)
    // .mapToObj(i -> "sub-dir-" + i)
    // .toArray(String[]::new);
}
