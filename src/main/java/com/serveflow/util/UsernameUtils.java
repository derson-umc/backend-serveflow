package com.serveflow.util;

import java.util.Locale;

public final class UsernameUtils {

    private UsernameUtils() {}

    public static String normalize(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
