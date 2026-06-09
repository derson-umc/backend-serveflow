package com.serveflow.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serveflow.exception.handler.GlobalExceptionHandler;
import com.serveflow.model.user.User;
import com.serveflow.model.user.UserRole;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.auth.AuthService;
import com.serveflow.service.auth.AuthService.AuthResult;
import com.serveflow.service.auth.PasswordResetService;
import com.serveflow.service.auth.RefreshTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerExtendedTest {

    @Mock AuthService authService;
    @Mock AuditService auditService;
    @Mock PasswordResetService passwordResetService;
    @Mock RefreshTokenService refreshTokenService;

    @InjectMocks AuthController controller;

    MockMvc mvc;
    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler(auditService))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthUser(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    @Nested
    @DisplayName("POST /auth/refresh")
    class Refresh {

        @Test
        @DisplayName("retorna 200 com novos tokens")
        void refresh_returns200() throws Exception {
            AuthResult result = new AuthResult("new-token", "new-refresh", 1L, "admin", "ADMIN");
            when(authService.refresh("valid-refresh")).thenReturn(result);

            String body = mapper.writeValueAsString(
                    new AuthController.RefreshRequest("valid-refresh"));

            mvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("new-token"))
                    .andExpect(jsonPath("$.refreshToken").value("new-refresh"));

            verify(authService).refresh("valid-refresh");
        }

        @Test
        @DisplayName("retorna 400 quando refresh token em branco")
        void refresh_returns400_whenBlank() throws Exception {
            String body = "{\"refreshToken\":\"\"}";

            mvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /auth/identify")
    class Identify {

        @Test
        @DisplayName("retorna 200 com username e email mascarado")
        void identify_returns200() throws Exception {
            PasswordResetService.IdentifyResult identResult =
                    new PasswordResetService.IdentifyResult("joao", "j***@example.com");
            when(passwordResetService.identifyUser("joao")).thenReturn(identResult);

            String body = mapper.writeValueAsString(
                    new AuthController.IdentifyRequest("joao"));

            mvc.perform(post("/auth/identify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("joao"))
                    .andExpect(jsonPath("$.maskedEmail").value("j***@example.com"));

            verify(passwordResetService).identifyUser("joao");
        }
    }

    @Nested
    @DisplayName("POST /auth/forgot-password")
    class ForgotPassword {

        @Test
        @DisplayName("retorna 202 com username quando sucesso")
        void forgotPassword_returns202() throws Exception {
            when(passwordResetService.requestReset("joao")).thenReturn("joao");
            doNothing().when(auditService).logPasswordReset(anyString(), anyString(), eq(false));

            String body = mapper.writeValueAsString(
                    new AuthController.ForgotPasswordRequest("joao"));

            mvc.perform(post("/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.username").value("joao"));

            verify(passwordResetService).requestReset("joao");
        }

        @Test
        @DisplayName("retorna 202 mesmo quando usuário não encontrado")
        void forgotPassword_returns202_evenWhenNotFound() throws Exception {
            when(passwordResetService.requestReset(anyString()))
                    .thenThrow(new RuntimeException("User not found"));

            String body = mapper.writeValueAsString(
                    new AuthController.ForgotPasswordRequest("unknown"));

            mvc.perform(post("/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isAccepted());
        }
    }

    @Nested
    @DisplayName("POST /auth/verify-reset-token")
    class VerifyResetToken {

        @Test
        @DisplayName("retorna 200 quando token válido")
        void verifyResetToken_returns200() throws Exception {
            doNothing().when(passwordResetService).verifyToken("joao", "123456");

            String body = "{\"username\":\"joao\",\"token\":\"123456\"}";

            mvc.perform(post("/auth/verify-reset-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());

            verify(passwordResetService).verifyToken("joao", "123456");
        }

        @Test
        @DisplayName("retorna 400 quando token não tem 6 dígitos")
        void verifyResetToken_returns400_whenTokenInvalid() throws Exception {
            String body = "{\"username\":\"joao\",\"token\":\"12345\"}";

            mvc.perform(post("/auth/verify-reset-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /auth/reset-password")
    class ResetPassword {

        @Test
        @DisplayName("retorna 200 quando reset bem sucedido")
        void resetPassword_returns200() throws Exception {
            doNothing().when(passwordResetService).resetPassword("joao", "123456", "newpass1");
            doNothing().when(auditService).logPasswordReset(anyString(), anyString(), eq(true));

            String body = "{\"username\":\"joao\",\"token\":\"123456\",\"newPassword\":\"newpass1\"}";

            mvc.perform(post("/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());

            verify(passwordResetService).resetPassword("joao", "123456", "newpass1");
        }
    }

    @Nested
    @DisplayName("POST /auth/logout")
    class Logout {

        @Test
        @DisplayName("retorna 204 e revoga tokens quando usuário autenticado")
        void logout_returns204_withAuthUser() throws Exception {
            User user = new User(1L, "admin", "admin@test.com", "pass", UserRole.ADMIN, "Admin");
            setAuthUser(user);

            mvc.perform(post("/auth/logout"))
                    .andExpect(status().isNoContent());

            verify(refreshTokenService).revokeAll(1L);
            verify(auditService).logLogout(eq(1L), eq("admin"), anyString());
        }

        @Test
        @DisplayName("retorna 204 sem chamar revoke quando usuário não autenticado")
        void logout_returns204_withNullUser() throws Exception {
            mvc.perform(post("/auth/logout"))
                    .andExpect(status().isNoContent());

            verify(refreshTokenService, never()).revokeAll(any());
        }
    }
}
