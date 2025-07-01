package com.pitanguinha.streaming.enums.media.contenttypes;

/**
 * Utility class for checking if a given content type is supported by a
 * specified enum class.
 * 
 * @since 1.0
 */
public class SupportedTypeUtil {
    @SuppressWarnings("unchecked")
    public static boolean isSupported(Class<?> rawEnumClass, String contentType) {
        if (contentType == null || contentType.isEmpty())
            return false;

        if (!rawEnumClass.isEnum())
            return false;

        if (!SupportedType.class.isAssignableFrom(rawEnumClass)) {
            throw new IllegalArgumentException("Class must implement SupportedType");
        }

        Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) rawEnumClass;

        Object[] enumConstants = enumClass.getEnumConstants();

        for (Object constant : enumConstants) {
            SupportedType type = (SupportedType) constant;
            if (type.getContentType().equalsIgnoreCase(contentType)) {
                return true;
            }
        }

        return false;
    }
}
