package com.serveflow.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.serveflow.util.IpResolverUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
@Order(-150)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int  MAX_REQUESTS = 10;
    private static final long WINDOW_MS    = 60_000L;

    private static final Set<String> LIMITED_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/forgot-password",
            "/api/auth/identify",
            "/api/auth/reset-password",
            "/api/auth/verify-reset-token"
    );

    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Long>> buckets =
            new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        if (!LIMITED_PATHS.contains(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String ip  = IpResolverUtil.getClientIp(request);
        String key = request.getRequestURI() + "|" + ip;
        long   now = System.currentTimeMillis();

        ConcurrentLinkedQueue<Long> window =
                buckets.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>());

        window.removeIf(ts -> now - ts > WINDOW_MS);

        if (window.size() >= MAX_REQUESTS) {
            log.warn("Rate limit excedido ip={} path={}", ip, request.getRequestURI());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Muitas tentativas. Aguarde 1 minuto e tente novamente.\"}");
            return;
        }

        window.add(now);
        chain.doFilter(request, response);
    }

}
