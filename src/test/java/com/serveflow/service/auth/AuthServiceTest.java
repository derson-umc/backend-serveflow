package com.serveflow.service.auth;

import com.serveflow.config.JwtService;
import com.serveflow.exception.user.BusinessRuleException;
import com.serveflow.exception.user.UserNotFoundException;
import com.serveflow.model.user.User;
import com.serveflow.model.user.UserRole;
import com.serveflow.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock JwtService jwtService;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks AuthService authService;

    private User adminUser;

    @BeforeEach
    void setup() {
        adminUser = new User(1L, "admin", null, null, "encoded-hash", UserRole.ADMIN, "Administrador");
    }

    @Test
    @DisplayName("authenticate normaliza username (lowercase + trim) antes de buscar.")
    void authenticate_normalizaUsername() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("senha123", "encoded-hash")).thenReturn(true);
        when(jwtService.generateToken(anyLong(), anyString(), any(UserRole.class))).thenReturn("jwt-token");

        AuthService.AuthResult result = authService.authenticate("  ADMIN  ", "senha123");

        assertThat(result.token()).isEqualTo("jwt-token");
        verify(userRepository).findByUsername("admin");
    }

    @Test
    @DisplayName("authenticate aplica trim na senha antes de comparar.")
    void authenticate_aplicaTrimSenha() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("senha123", "encoded-hash")).thenReturn(true);
        when(jwtService.generateToken(anyLong(), anyString(), any(UserRole.class))).thenReturn("jwt-token");

        authService.authenticate("admin", "  senha123  ");

        verify(passwordEncoder).matches("senha123", "encoded-hash");
    }

    @Test
    @DisplayName("authenticate retorna AuthResult com todos os dados do usuário.")
    void authenticate_retornaAuthResultCompleto() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(eq(1L), eq("admin"), eq(UserRole.ADMIN))).thenReturn("jwt-xyz");

        AuthService.AuthResult result = authService.authenticate("admin", "senha");

        assertThat(result.token()).isEqualTo("jwt-xyz");
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("admin");
        assertThat(result.role()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("authenticate com username inexistente lança UserNotFoundException.")
    void authenticate_usernameInexistente() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate("GHOST", "senha"))
                .isInstanceOf(UserNotFoundException.class);

        verify(jwtService, never()).generateToken(anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("authenticate com senha incorreta lança BusinessRuleException.")
    void authenticate_senhaInvalida() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("errada", "encoded-hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticate("admin", "errada"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Credenciais inválidas");

        verify(jwtService, never()).generateToken(anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("authenticate trata diferentes casos como o mesmo usuário.")
    void authenticate_caseInsensitive() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(anyLong(), anyString(), any())).thenReturn("token");

        authService.authenticate("AdMiN", "senha");
        authService.authenticate("ADMIN", "senha");
        authService.authenticate("admin", "senha");

        verify(userRepository, times(3)).findByUsername("admin");
    }

    @Test
    @DisplayName("authenticate trata null username como vazio (UserNotFound).")
    void authenticate_usernameNulo() {
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate(null, "senha"))
                .isInstanceOf(UserNotFoundException.class);
    }
}
