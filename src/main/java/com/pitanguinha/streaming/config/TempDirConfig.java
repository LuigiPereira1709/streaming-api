package com.pitanguinha.streaming.config;

import java.nio.file.*;
import java.io.IOException;

import org.slf4j.*;
import org.springframework.context.annotation.*;

import com.pitanguinha.streaming.enums.exceptions.SeverityLevel;
import com.pitanguinha.streaming.exceptions.internal.InternalException;

import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;

/**
 * Configuration class for temp directories.
 * 
 * <p>
 * Is used initialize the temp directories on application startup.<br>
 * And also offer a instance of TempService.
 * </p>
 * 
 * @See TempService The Class that will be used to enclose the actions for the
 *      temp directories.
 * 
 * @since 1.0
 */
@Configuration
public class TempDirConfig {
    private final static Logger LOGGER = LoggerFactory.getLogger(TempDirConfig.class);

    @Value("${spring.application.temp-dir:/tmp/app/streaming/prod/}")
    private String tempDir;

    /**
     * Initialize the temp directories on application startup.
     * 
     * @since 1.0
     */
    @PostConstruct
    private void initializeTempDirectories() {
        LOGGER.info("Initializing creation of temp directories on application startup");
        try {
            Path tempDirPath = Path.of(tempDir);
            if (!Files.exists(tempDirPath)) {
                Files.createDirectories(tempDirPath);
                LOGGER.info("Temp directory created at {}", tempDir);
            }

        } catch (IOException e) {
            LOGGER.error("Error creating temp directories", e);
            throw new InternalException("Error creating temp directories", TempDirConfig.class, SeverityLevel.CRITICAL,
                    e);
        }
    }
}
