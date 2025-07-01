package com.pitanguinha.streaming.mapper.helper;

import java.util.Arrays;

/**
 * Helper class for mapping to values.
 *
 * @since 1.0
 */
public class MapperHelper {
    /**
     * Maps a string value to an enum constant of the specified enum class.
     *
     * @param enumClass The class of the enum to map to.
     * @param value     The string value to map.
     * @param regex     The regex pattern to normalize the enum names and the value.
     * @return The matching enum constant.
     * @throws IllegalArgumentException If no matching enum constant is found.
     */
    public static Enum<?> mapStringToEnum(Class<? extends Enum<?>> enumClass, String value, String regex) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> {
                    String normalizedEnum = StringHelper.normalize(e.name(), regex, "");
                    String normalizedValue = StringHelper.normalize(value, regex, "");
                    return normalizedEnum.equalsIgnoreCase(normalizedValue);
                })
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Invalid value '%s' for enum %s", value, enumClass.getSimpleName())));
    }
}
