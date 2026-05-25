package com.serveflow.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class IpResolverUtil {

    private IpResolverUtil() {}

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (isBlankOrUnknown(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (isBlankOrUnknown(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            log.debug("IPv6 loopback detectado, normalizando para 127.0.0.1");
            ip = "127.0.0.1";
        }

        return ip;
    }

    private static boolean isBlankOrUnknown(String value) {
        return value == null || value.isBlank() || "unknown".equalsIgnoreCase(value);
    }
}
