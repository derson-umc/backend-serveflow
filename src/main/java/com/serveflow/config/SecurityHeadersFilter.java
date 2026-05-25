package com.serveflow.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(-200)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        response.setHeader("X-Content-Type-Options",  "nosniff");
        response.setHeader("X-Frame-Options",          "DENY");
        response.setHeader("Referrer-Policy",          "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy",       "camera=(), microphone=(), geolocation=()");

        response.setHeader("Strict-Transport-Security","max-age=31536000; includeSubDomains");

        response.setHeader("Content-Security-Policy",
                "default-src 'self'; img-src 'self' data: blob:; " +
                "script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'");

        chain.doFilter(request, response);
    }
}
