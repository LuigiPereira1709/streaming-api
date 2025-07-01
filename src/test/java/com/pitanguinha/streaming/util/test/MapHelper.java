package com.pitanguinha.streaming.util.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

public class MapHelper {
    public static void testValuesValid(Map<String, ?> expected) {
        assert expected != null && !expected.isEmpty() : "Expected should not be null or empty";

        expected.forEach(($, value) -> {
            assertAll("Values", () -> {
                assertNotNull(value, "Value should not be null.");
                assertFalse(value.toString().isEmpty(), "Value should not be empty.");
            });
        });
    }
}
