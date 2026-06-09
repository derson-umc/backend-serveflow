package com.serveflow.service.auth;

import com.serveflow.model.auth.RefreshToken;
import com.serveflow.repository.auth.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    RefreshTokenRepository repository;

    @InjectMocks
    RefreshTokenService service;

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("salva token no repositório e retorna raw token não-nulo")
        void create_savesTokenAndReturnsRaw() {
            when(repository.save(any(RefreshToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            String rawToken = service.create(42L);

            assertThat(rawToken).isNotNull().isNotBlank();
            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(repository).save(captor.capture());
            RefreshToken saved = captor.getValue();
            assertThat(saved.userId()).isEqualTo(42L);
            assertThat(saved.tokenHash()).isNotEqualTo(rawToken); // stored as hash
        }
    }

    @Nested
    @DisplayName("validateAndRotate()")
    class ValidateAndRotate {

        @Test
        @DisplayName("retorna userId quando token é válido e não expirado")
        void validateAndRotate_returnsUserId_whenValid() {
            String rawToken = "test-raw-token";
            RefreshToken storedToken = new RefreshToken(
                    1L, 99L, "somehash", Instant.now().plus(1, ChronoUnit.DAYS));
            when(repository.findByTokenHash(any())).thenReturn(Optional.of(storedToken));

            Long userId = service.validateAndRotate(rawToken);

            assertThat(userId).isEqualTo(99L);
            verify(repository).deleteById(1L);
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando token não encontrado")
        void validateAndRotate_throwsWhenTokenNotFound() {
            when(repository.findByTokenHash(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.validateAndRotate("invalid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("inválido");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando token expirado")
        void validateAndRotate_throwsWhenExpired() {
            RefreshToken expiredToken = new RefreshToken(
                    2L, 77L, "hash", Instant.now().minus(1, ChronoUnit.HOURS));
            when(repository.findByTokenHash(any())).thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> service.validateAndRotate("expired-token"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("expirado");

            verify(repository).deleteById(2L);
        }
    }

    @Nested
    @DisplayName("revokeAll()")
    class RevokeAll {

        @Test
        @DisplayName("deleta todos os tokens do usuário")
        void revokeAll_deletesAllForUser() {
            service.revokeAll(55L);
            verify(repository).deleteByUserId(55L);
        }
    }
}
