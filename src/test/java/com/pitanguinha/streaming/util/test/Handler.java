package com.pitanguinha.streaming.util.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class Handler {
    public static void testCollectionArgs(String result, Object expected) {
        assertTrue(result != null && !result.isEmpty(), "Expected should not be null or empty.");

        switch (expected) {
            case Collection<?> collection -> {
                collection.forEach(value -> {
                    assertTrue(result.contains(value.toString()), value + " should be present in the command.");
                });
            }
            case Map<?, ?> map -> {
                map.forEach(($, value) -> {
                    assertTrue(result.contains(value.toString()), value + " should be present in the command.");
                });
            }

            default -> throw new IllegalArgumentException(
                    "Expected type not supported, only Map and Collection.\nThe passed type was: "
                            + expected.getClass());
        }
    }
}
