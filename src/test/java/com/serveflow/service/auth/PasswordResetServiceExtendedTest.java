package com.serveflow.service.auth;

import com.serveflow.exception.user.UserNotFoundException;
import com.serveflow.repository.auth.PasswordResetTokenEntity;
import com.serveflow.repository.auth.PasswordResetTokenRepository;
import com.serveflow.repository.user.SpringUserRepository;
import com.serveflow.repository.user.UserEntity;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceExtendedTest {

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
        u.setPassword("encoded");
        return u;
    }

    @Nested
    @DisplayName("requestReset()")
    class RequestReset {

        @Test
        @DisplayName("salva token e tenta enviar email, retorna username")
        void requestReset_savesTokenAndReturnsUsername() {
            UserEntity user = buildUser("joao", "joao@example.com");
            when(userRepository.findByUsername("joao")).thenReturn(Optional.of(user));
            when(tokenRepository.save(any(PasswordResetTokenEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            String result = service.requestReset("joao");

            assertThat(result).isEqualTo("joao");
            verify(tokenRepository).save(any(PasswordResetTokenEntity.class));
        }

        @Test
        @DisplayName("lança UserNotFoundException quando usuário não encontrado")
        void requestReset_throwsWhenUserNotFound() {
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.requestReset("unknown"))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("lança UserNotFoundException quando identifier em branco")
        void requestReset_throwsWhenBlank() {
            assertThatThrownBy(() -> service.requestReset("   "))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("silencia exceção de email quando email é inválido")
        void requestReset_continuesWhenNoEmail() {
            UserEntity user = buildUser("joao", null);
            when(userRepository.findByUsername("joao")).thenReturn(Optional.of(user));
            when(tokenRepository.save(any(PasswordResetTokenEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Should not throw, email sending silently skipped
            String result = service.requestReset("joao");
            assertThat(result).isEqualTo("joao");
        }

        @Test
        @DisplayName("encontra usuário por email quando username não encontrado")
        void requestReset_findsUserByEmail() {
            UserEntity user = buildUser("joao", "joao@example.com");
            when(userRepository.findByUsername("joao@example.com")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(user));
            when(tokenRepository.save(any(PasswordResetTokenEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            String result = service.requestReset("joao@example.com");

            assertThat(result).isEqualTo("joao");
        }
    }

    @Nested
    @DisplayName("identifyUser() — edge cases")
    class IdentifyUserEdge {

        @Test
        @DisplayName("retorna nulls quando identifier é null")
        void identifyUser_null_returnsNullResult() {
            PasswordResetService.IdentifyResult result = service.identifyUser(null);
            assertThat(result.username()).isNull();
            assertThat(result.maskedEmail()).isNull();
        }

        @Test
        @DisplayName("mascara email com 2 ou menos caracteres no local")
        void identifyUser_shortEmail_masked() {
            UserEntity user = buildUser("ab", "ab@example.com");
            when(userRepository.findByUsername("ab")).thenReturn(Optional.of(user));

            PasswordResetService.IdentifyResult result = service.identifyUser("ab");

            assertThat(result.maskedEmail()).contains("@example.com");
            assertThat(result.maskedEmail()).contains("***");
        }

        @Test
        @DisplayName("mascara email com mais de 2 caracteres no local")
        void identifyUser_longEmail_masked() {
            UserEntity user = buildUser("joao", "joaosilva@example.com");
            when(userRepository.findByUsername("joao")).thenReturn(Optional.of(user));

            PasswordResetService.IdentifyResult result = service.identifyUser("joao");

            // Format: first + *** + last @ domain
            assertThat(result.maskedEmail()).startsWith("j");
            assertThat(result.maskedEmail()).contains("***");
            assertThat(result.maskedEmail()).contains("@example.com");
        }

        @Test
        @DisplayName("retorna null para email sem @")
        void identifyUser_noAtEmail_returnsNull() {
            UserEntity user = buildUser("joao", "invalidemail");
            when(userRepository.findByUsername("joao")).thenReturn(Optional.of(user));

            PasswordResetService.IdentifyResult result = service.identifyUser("joao");

            assertThat(result.maskedEmail()).isNull();
        }
    }
}
