package com.serveflow.config;

import com.serveflow.service.audit.AuditService;
import com.serveflow.util.IpResolverUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.serveflow.model.user.User;
import com.serveflow.repository.user.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Set<String> SKIP_LOG_PREFIXES = Set.of(
            "/actuator", "/swagger-ui", "/v3/api-docs", "/ws"
    );

    private final JwtService     jwtService;
    private final UserRepository userRepository;
    private final AuditService   auditService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String ip    = IpResolverUtil.getClientIp(request);
        Long   userId = null;

        try {
            String username = jwtService.extractUsername(token);
            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
                userId = user.getId();
                log.debug("JWT autenticado user={} ip={}", username, ip);
            }
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expirado ip={} uri={}", ip, request.getRequestURI());
            writeUnauthorized(response, "Token expirado");
            return;
        } catch (MalformedJwtException e) {
            log.warn("Token JWT mal formatado ip={} uri={}", ip, request.getRequestURI());
            writeUnauthorized(response, "Token JWT inválido");
            return;
        } catch (Exception e) {
            log.warn("Erro ao validar JWT ip={} uri={} msg={}", ip, request.getRequestURI(), e.getMessage());
            writeUnauthorized(response, "Erro na autenticação");
            return;
        }

        filterChain.doFilter(request, response);

        if (userId != null && shouldLog(request)) {
            auditService.logAccess(userId, ip, request.getRequestURI(),
                    request.getMethod(), response.getStatus());
        }
    }

    private boolean shouldLog(HttpServletRequest request) {
        String uri    = request.getRequestURI();
        String method = request.getMethod();
        if ("OPTIONS".equals(method)) return false;
        return SKIP_LOG_PREFIXES.stream().noneMatch(uri::startsWith);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
