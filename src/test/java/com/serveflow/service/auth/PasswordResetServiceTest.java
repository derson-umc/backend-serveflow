package com.serveflow.service.auth;

import com.serveflow.exception.auth.InvalidResetTokenException;
import com.serveflow.exception.user.UserNotFoundException;
import com.serveflow.repository.auth.PasswordResetTokenEntity;
import com.serveflow.repository.auth.PasswordResetTokenRepository;
import com.serveflow.repository.user.SpringUserRepository;
import com.serveflow.repository.user.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    PasswordResetTokenRepository tokenRepository;

    @Mock
    SpringUserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JavaMailSender mailSender;

    @InjectMocks
    PasswordResetService service;

    private UserEntity buildUser(String username, String email) {
        UserEntity u = new UserEntity();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("encodedPass");
        return u;
    }

    @Nested
    @DisplayName("identifyUser()")
    class IdentifyUser {

        @Test
        @DisplayName("retorna IdentifyResult com username e email mascarado quando usuário encontrado por username")
        void identifyUser_foundByUsername() {
            UserEntity user = buildUser("joao", "joao@example.com");
            when(userRepository.findByUsername("joao")).thenReturn(Optional.of(user));

            PasswordResetService.IdentifyResult result = service.identifyUser("joao");

            assertThat(result.username()).isEqualTo("joao");
            assertThat(result.maskedEmail()).isNotNull();
            assertThat(result.maskedEmail()).contains("@example.com");
        }

        @Test
        @DisplayName("retorna IdentifyResult com nulls quando usuário não encontrado")
        void identifyUser_notFound_returnsNullResult() {
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            PasswordResetService.IdentifyResult result = service.identifyUser("desconhecido");

            assertThat(result.username()).isNull();
            assertThat(result.maskedEmail()).isNull();
        }

        @Test
        @DisplayName("retorna IdentifyResult nulo quando identifier em branco")
        void identifyUser_blank_returnsNullResult() {
            PasswordResetService.IdentifyResult result = service.identifyUser("   ");
            assertThat(result.username()).isNull();
            assertThat(result.maskedEmail()).isNull();
        }

        @Test
        @DisplayName("encontra usuário por email quando username não encontrado")
        void identifyUser_foundByEmail() {
            UserEntity user = buildUser("joao", "joao@example.com");
            when(userRepository.findByUsername("joao@example.com")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(user));

            PasswordResetService.IdentifyResult result = service.identifyUser("joao@example.com");

            assertThat(result.username()).isEqualTo("joao");
        }
    }

    @Nested
    @DisplayName("verifyToken()")
    class VerifyToken {

        @Test
        @DisplayName("não lança exceção quando token válido e não expirado")
        void verifyToken_success() {
            PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
            entity.setUsername("joao");
            entity.setToken("123456");
            entity.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            entity.setUsed(false);

            when(tokenRepository.findByUsernameAndTokenAndUsedFalse("joao", "123456"))
                    .thenReturn(Optional.of(entity));

            // Should not throw
            service.verifyToken("joao", "123456");
        }

        @Test
        @DisplayName("lança InvalidResetTokenException quando token não encontrado")
        void verifyToken_throwsWhenNotFound() {
            when(tokenRepository.findByUsernameAndTokenAndUsedFalse(anyString(), anyString()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.verifyToken("joao", "wrongcode"))
                    .isInstanceOf(InvalidResetTokenException.class);
        }

        @Test
        @DisplayName("lança InvalidResetTokenException quando token expirado")
        void verifyToken_throwsWhenExpired() {
            PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
            entity.setUsername("joao");
            entity.setToken("123456");
            entity.setExpiresAt(LocalDateTime.now().minusMinutes(1));
            entity.setUsed(false);

            when(tokenRepository.findByUsernameAndTokenAndUsedFalse("joao", "123456"))
                    .thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> service.verifyToken("joao", "123456"))
                    .isInstanceOf(InvalidResetTokenException.class);
        }

        @Test
        @DisplayName("lança InvalidResetTokenException quando username em branco")
        void verifyToken_throwsWhenBlankUsername() {
            assertThatThrownBy(() -> service.verifyToken("   ", "123456"))
                    .isInstanceOf(InvalidResetTokenException.class);
        }
    }

    @Nested
    @DisplayName("resetPassword()")
    class ResetPassword {

        @Test
        @DisplayName("redefine senha, marca token como usado e salva")
        void resetPassword_success() {
            PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
            entity.setUsername("joao");
            entity.setToken("123456");
            entity.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            entity.setUsed(false);

            UserEntity user = buildUser("joao", "joao@example.com");

            when(tokenRepository.findByUsernameAndTokenAndUsedFalse("joao", "123456"))
                    .thenReturn(Optional.of(entity));
            when(userRepository.findByUsername("joao")).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("newpass")).thenReturn("encoded_newpass");
            when(userRepository.save(any(UserEntity.class))).thenReturn(user);
            when(tokenRepository.save(any(PasswordResetTokenEntity.class))).thenReturn(entity);

            service.resetPassword("joao", "123456", "newpass");

            verify(passwordEncoder).encode("newpass");
            verify(userRepository).save(user);
            assertThat(entity.isUsed()).isTrue();
        }

        @Test
        @DisplayName("lança UserNotFoundException quando usuário não encontrado")
        void resetPassword_throwsWhenUserNotFound() {
            PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
            entity.setUsername("unknown");
            entity.setToken("123456");
            entity.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            entity.setUsed(false);

            when(tokenRepository.findByUsernameAndTokenAndUsedFalse("unknown", "123456"))
                    .thenReturn(Optional.of(entity));
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.resetPassword("unknown", "123456", "newpass"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
