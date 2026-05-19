package com.serveflow.service.auth;

import com.serveflow.model.auth.RefreshToken;
import com.serveflow.repository.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    @Value("${jwt.refresh-expiration-days:30}")
    private int refreshExpirationDays;

    @Transactional
    public String create(Long userId) {
        String rawToken = UUID.randomUUID().toString();
        String hash = sha256(rawToken);
        Instant expiresAt = Instant.now().plus(refreshExpirationDays, ChronoUnit.DAYS);
        repository.save(new RefreshToken(null, userId, hash, expiresAt));
        return rawToken;
    }

    @Transactional
    public Long validateAndRotate(String rawToken) {
        String hash = sha256(rawToken);
        RefreshToken token = repository.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido"));
        if (token.isExpired()) {
            repository.deleteById(token.id());
            throw new IllegalArgumentException("Refresh token expirado");
        }
        repository.deleteById(token.id());
        return token.userId();
    }

    @Transactional
    public void revokeAll(Long userId) {
        repository.deleteByUserId(userId);
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
