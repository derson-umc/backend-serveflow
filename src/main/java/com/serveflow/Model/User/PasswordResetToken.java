package com.serveflow.Model.User;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Modelo de domínio puro do token de redefinição de senha.
 * Persiste apenas o hash do token (nunca o valor original).
 */
@Getter
public class PasswordResetToken {

    private final Long id;
    private final Long userId;
    private final String tokenHash;
    private final Instant expiresAt;
    private final Instant usedAt;

    public PasswordResetToken(Long id, Long userId, String tokenHash, Instant expiresAt, Instant usedAt) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId é obrigatório");
        this.tokenHash = Objects.requireNonNull(tokenHash, "tokenHash é obrigatório");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt é obrigatório");
        this.usedAt = usedAt;
    }

    public static PasswordResetToken issue(Long userId, String tokenHash, Instant expiresAt) {
        return new PasswordResetToken(null, userId, tokenHash, expiresAt, null);
    }

    public PasswordResetToken markUsed(Instant when) {
        return new PasswordResetToken(id, userId, tokenHash, expiresAt, when);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isValid(Instant now) {
        return !isUsed() && !isExpired(now);
    }
}
