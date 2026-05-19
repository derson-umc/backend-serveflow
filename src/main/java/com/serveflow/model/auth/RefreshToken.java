package com.serveflow.model.auth;

import java.time.Instant;

public record RefreshToken(Long id, Long userId, String tokenHash, Instant expiresAt) {

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
