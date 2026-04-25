package com.serveflow.Model.PasswordReset;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class PasswordResetToken {

    public static final long TTL_MINUTES = 60L;

    private final Long id;
    private final Long userId;
    private final String token;
    private final Instant expiresAt;
    private final boolean used;

    public PasswordResetToken(Long id, Long userId, String token, Instant expiresAt, boolean used) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId é obrigatório");
        this.token = Objects.requireNonNull(token, "token é obrigatório");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt é obrigatório");
        this.used = used;
    }

    public static PasswordResetToken issueFor(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        Instant expires = Instant.now().plusSeconds(TTL_MINUTES * 60);
        return new PasswordResetToken(null, userId, token, expires, false);
    }

    public PasswordResetToken markUsed() {
        return new PasswordResetToken(id, userId, token, expiresAt, true);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }
}
