package com.serveflow.util;

public final class EmailMaskUtils {

    private EmailMaskUtils() {}

    public static String mask(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf('@');
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        int visibleChars = Math.min(3, Math.max(1, local.length() - 1));
        return local.substring(0, visibleChars) + "***" + domain;
    }
}
