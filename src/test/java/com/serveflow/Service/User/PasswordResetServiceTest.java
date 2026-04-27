package com.serveflow.Service.User;

import com.serveflow.Exception.User.BusinessRuleException;
import com.serveflow.Model.User.PasswordResetToken;
import com.serveflow.Model.User.User;
import com.serveflow.Model.User.UserRole;
import com.serveflow.Repository.User.PasswordResetTokenRepository;
import com.serveflow.Repository.User.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordResetTokenRepository tokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailService emailService;

    @InjectMocks PasswordResetService service;

    private User user;

    @BeforeEach
    void setup() {
        user = new User(7L, "joao", "old-hash", UserRole.GARCON, "Garçom");
        ReflectionTestUtils.setField(service, "fallbackRecipient", "no-reply@test.local");
    }

    @Test
    void requestReset_usuarioInexistente_naoVazaInformacao() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        service.requestReset("ghost");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any());
    }

    @Test
    void requestReset_invalidaTokensAnteriores_eEmiteNovo() {
        when(userRepository.findByUsername("joao")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));

        service.requestReset("joao");

        verify(tokenRepository).invalidateAllByUser(7L);
        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(captor.capture());

        PasswordResetToken issued = captor.getValue();
        assertThat(issued.getUserId()).isEqualTo(7L);
        assertThat(issued.getTokenHash()).hasSize(64);
        assertThat(issued.getExpiresAt()).isAfter(Instant.now().plus(14, ChronoUnit.MINUTES));
        assertThat(issued.isUsed()).isFalse();

        verify(emailService).sendPasswordResetEmail(eq("no-reply@test.local"), eq("joao"), any());
    }

    @Test
    void requestReset_falhaNoEmail_naoPropagaExcecao() {
        when(userRepository.findByUsername("joao")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("smtp down"))
                .when(emailService).sendPasswordResetEmail(any(), any(), any());

        service.requestReset("joao");
    }

    @Test
    void resetPassword_tokenVazio_lancaBusinessRule() {
        assertThatThrownBy(() -> service.resetPassword("", "NovaSenha@2025"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Token inválido ou expirado");
    }

    @Test
    void resetPassword_tokenInexistente_lancaBusinessRule() {
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword("qualquer-token", "NovaSenha@2025"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void resetPassword_tokenExpirado_lancaBusinessRule() {
        PasswordResetToken expired = new PasswordResetToken(
                10L, 7L, "anyhash", Instant.now().minusSeconds(1), null
        );
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.resetPassword("any", "NovaSenha@2025"))
                .isInstanceOf(BusinessRuleException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_tokenJaUsado_lancaBusinessRule() {
        PasswordResetToken used = new PasswordResetToken(
                10L, 7L, "anyhash",
                Instant.now().plusSeconds(60),
                Instant.now().minusSeconds(10)
        );
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(used));

        assertThatThrownBy(() -> service.resetPassword("any", "NovaSenha@2025"))
                .isInstanceOf(BusinessRuleException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_sucesso_persisteSenhaECarimbaTokenComoUsado() {
        PasswordResetToken active = new PasswordResetToken(
                10L, 7L, "anyhash",
                Instant.now().plusSeconds(300), null
        );
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(active));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NovaSenha@2025")).thenReturn("new-hash");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));

        service.resetPassword("any", "NovaSenha@2025");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("new-hash");
        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.GARCON);

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().isUsed()).isTrue();
    }
}
