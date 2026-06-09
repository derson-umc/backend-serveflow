package com.serveflow.service.auth;

import com.serveflow.config.JwtService;
import com.serveflow.model.user.User;
import com.serveflow.model.user.UserRole;
import com.serveflow.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceExtendedTest {

    @Mock UserRepository userRepository;
    @Mock JwtService jwtService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock RefreshTokenService refreshTokenService;

    @InjectMocks AuthService authService;

    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new User(1L, "admin", null, "encoded-hash", UserRole.ADMIN, "Administrador");
    }

    @Nested
    @DisplayName("authenticate() — refresh token integration")
    class AuthenticateRefreshToken {

        @Test
        @DisplayName("authenticate inclui refreshToken no resultado quando criado com sucesso")
        void authenticate_includesRefreshToken() {
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(passwordEncoder.matches("senha", "encoded-hash")).thenReturn(true);
            when(jwtService.generateToken(anyLong(), anyString(), any())).thenReturn("access-token");
            when(refreshTokenService.create(1L)).thenReturn("refresh-token-raw");

            AuthService.AuthResult result = authService.authenticate("admin", "senha");

            assertThat(result.token()).isEqualTo("access-token");
            assertThat(result.refreshToken()).isEqualTo("refresh-token-raw");
        }

        @Test
        @DisplayName("authenticate retorna null refreshToken quando criação falha")
        void authenticate_nullRefreshToken_whenCreationFails() {
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(passwordEncoder.matches("senha", "encoded-hash")).thenReturn(true);
            when(jwtService.generateToken(anyLong(), anyString(), any())).thenReturn("access-token");
            when(refreshTokenService.create(1L)).thenThrow(new RuntimeException("DB error"));

            AuthService.AuthResult result = authService.authenticate("admin", "senha");

            assertThat(result.token()).isEqualTo("access-token");
            assertThat(result.refreshToken()).isNull();
        }

        @Test
        @DisplayName("authenticate com senha null usa string vazia para comparação")
        void authenticate_nullPassword_usesEmpty() {
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(passwordEncoder.matches("", "encoded-hash")).thenReturn(false);

            assertThatThrownBy(() -> authService.authenticate("admin", null))
                    .isInstanceOf(Exception.class);

            verify(passwordEncoder).matches("", "encoded-hash");
        }
    }

    @Nested
    @DisplayName("refresh()")
    class Refresh {

        @Test
        @DisplayName("refresh cria novos tokens a partir de refreshToken válido")
        void refresh_returnsNewTokens() {
            when(refreshTokenService.validateAndRotate("raw-token")).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
            when(jwtService.generateToken(anyLong(), anyString(), any())).thenReturn("new-access");
            when(refreshTokenService.create(1L)).thenReturn("new-refresh");

            AuthService.AuthResult result = authService.refresh("raw-token");

            assertThat(result.token()).isEqualTo("new-access");
            assertThat(result.refreshToken()).isEqualTo("new-refresh");
            assertThat(result.userId()).isEqualTo(1L);
            assertThat(result.username()).isEqualTo("admin");
            assertThat(result.role()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("refresh lança exceção quando refreshToken inválido")
        void refresh_throwsWhenInvalidToken() {
            when(refreshTokenService.validateAndRotate("bad-token"))
                    .thenThrow(new IllegalArgumentException("Refresh token inválido"));

            assertThatThrownBy(() -> authService.refresh("bad-token"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("inválido");
        }

        @Test
        @DisplayName("refresh retorna null refreshToken quando criação do novo falha")
        void refresh_nullRefreshToken_whenCreationFails() {
            when(refreshTokenService.validateAndRotate("raw-token")).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
            when(jwtService.generateToken(anyLong(), anyString(), any())).thenReturn("new-access");
            when(refreshTokenService.create(1L)).thenThrow(new RuntimeException("fail"));

            AuthService.AuthResult result = authService.refresh("raw-token");

            assertThat(result.refreshToken()).isNull();
        }
    }
}
