package com.pitanguinha.streaming;

import org.junit.jupiter.api.Test;

import org.springframework.test.context.*;
import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({ TestcontainersConfiguration.class, GlobalMockConfiguration.class })
class ApplicationTests {

    @Test
    void contextLoads() {
    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        GlobalMockConfiguration.dynamicProperties(registry);
    }
}
