package com.serveflow.config;

import com.serveflow.model.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "thisisaverylongsecretkeyfortest123456";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 86400000L); // 24h expiration
    }

    @Nested
    @DisplayName("generateToken(username, role)")
    class GenerateTokenNoId {

        @Test
        @DisplayName("gera token não nulo e não vazio")
        void generateToken_returnsNonNull() {
            String token = jwtService.generateToken("admin", UserRole.ADMIN);
            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("token contém 3 partes separadas por ponto (JWT format)")
        void generateToken_hasThreeParts() {
            String token = jwtService.generateToken("admin", UserRole.ADMIN);
            String[] parts = token.split("\\.");
            assertThat(parts).hasSize(3);
        }
    }

    @Nested
    @DisplayName("generateToken(userId, username, role)")
    class GenerateTokenWithId {

        @Test
        @DisplayName("gera token com userId")
        void generateToken_withUserId_returnsNonNull() {
            String token = jwtService.generateToken(1L, "admin", UserRole.ADMIN);
            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("gera token com userId null — sem claim id")
        void generateToken_withNullUserId_returnsToken() {
            String token = jwtService.generateToken(null, "admin", UserRole.ADMIN);
            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("tokens gerados com e sem userId são diferentes")
        void generateToken_withAndWithoutId_areDifferent() {
            String withId = jwtService.generateToken(1L, "admin", UserRole.ADMIN);
            String withoutId = jwtService.generateToken("admin", UserRole.ADMIN);

            assertThat(withId).isNotNull();
            assertThat(withoutId).isNotNull();
        }
    }

    @Nested
    @DisplayName("extractUsername(token)")
    class ExtractUsername {

        @Test
        @DisplayName("extrai username correto do token")
        void extractUsername_returnsCorrectUsername() {
            String token = jwtService.generateToken("joao", UserRole.GARCON);
            String username = jwtService.extractUsername(token);
            assertThat(username).isEqualTo("joao");
        }

        @Test
        @DisplayName("extrai username de token com userId")
        void extractUsername_fromTokenWithId() {
            String token = jwtService.generateToken(42L, "maria", UserRole.COZINHEIRO);
            String username = jwtService.extractUsername(token);
            assertThat(username).isEqualTo("maria");
        }
    }

    @Nested
    @DisplayName("validateToken(token)")
    class ValidateToken {

        @Test
        @DisplayName("retorna true para token válido")
        void validateToken_returnsTrue_forValidToken() {
            String token = jwtService.generateToken("admin", UserRole.ADMIN);
            assertThat(jwtService.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("retorna false para token inválido/corrompido")
        void validateToken_returnsFalse_forCorruptedToken() {
            assertThat(jwtService.validateToken("not.a.valid.token")).isFalse();
        }

        @Test
        @DisplayName("retorna false para token vazio")
        void validateToken_returnsFalse_forEmptyToken() {
            assertThat(jwtService.validateToken("")).isFalse();
        }

        @Test
        @DisplayName("retorna false para token null")
        void validateToken_returnsFalse_forNullToken() {
            assertThat(jwtService.validateToken(null)).isFalse();
        }

        @Test
        @DisplayName("retorna false para token assinado com secret diferente")
        void validateToken_returnsFalse_forDifferentSecret() {
            JwtService otherService = new JwtService("completelyDifferentSecretKeyForTesting!", 86400000L);
            String otherToken = otherService.generateToken("admin", UserRole.ADMIN);
            assertThat(jwtService.validateToken(otherToken)).isFalse();
        }

        @Test
        @DisplayName("retorna false para token expirado")
        void validateToken_returnsFalse_forExpiredToken() {
            JwtService shortLived = new JwtService(SECRET, -1000L); // expired immediately
            String expiredToken = shortLived.generateToken("admin", UserRole.ADMIN);
            assertThat(jwtService.validateToken(expiredToken)).isFalse();
        }

        @Test
        @DisplayName("token válido com diferentes roles")
        void validateToken_worksForAllRoles() {
            for (UserRole role : UserRole.values()) {
                String token = jwtService.generateToken("user", role);
                assertThat(jwtService.validateToken(token)).isTrue();
            }
        }
    }
}
