package com.serveflow.model.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    @Nested
    @DisplayName("isExpired()")
    class IsExpired {

        @Test
        @DisplayName("retorna true quando token está expirado")
        void isExpired_whenPastExpiration() {
            RefreshToken token = new RefreshToken(1L, 100L, "hash",
                    Instant.now().minus(1, ChronoUnit.HOURS));
            assertThat(token.isExpired()).isTrue();
        }

        @Test
        @DisplayName("retorna false quando token ainda é válido")
        void isNotExpired_whenFutureExpiration() {
            RefreshToken token = new RefreshToken(1L, 100L, "hash",
                    Instant.now().plus(1, ChronoUnit.HOURS));
            assertThat(token.isExpired()).isFalse();
        }
    }
}
