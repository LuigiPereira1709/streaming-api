package com.pitanguinha.streaming.util.test;

import static org.junit.jupiter.api.Assertions.*;

public class InstanceHelper {
    public static void testInstanceOf(Class<?> result, Class<?> expected) {
        assertNotNull(result, "Should not be null: " + result);
        assertEquals(result, expected,
                "Should be an instance of " + expected.getSimpleName() + ":" + result.getSimpleName());
    }
}
