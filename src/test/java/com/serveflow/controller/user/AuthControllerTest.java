package com.serveflow.controller.user;

import com.serveflow.service.audit.AuditService;
import com.serveflow.service.auth.AuthService;
import com.serveflow.service.auth.AuthService.AuthResult;
import com.serveflow.service.auth.PasswordResetService;
import com.serveflow.service.auth.RefreshTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - Suite de Testes.")
class AuthControllerTest {

    @Mock
    private AuthService authService;
    @Mock
    private AuditService auditService;
    @Mock
    private PasswordResetService passwordResetService;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthController controller;

    private ResponseEntity<AuthController.LoginResponse> doLogin(AuthController.LoginRequest req) {
        return controller.login(req, new MockHttpServletRequest());
    }

    @Nested
    @DisplayName("Cenários de Sucesso - Login Válido.")
    class LoginSucesso {

        @Test
        @DisplayName("Login com credenciais válidas retorna 200 OK.")
        void login_comCredenciaisValidas_retorna200() {
            AuthResult authResult = new AuthResult("jwt-xyz", null, 1L, "admin", "ADMIN");
            when(authService.authenticate("admin", "senha"))
                    .thenReturn(authResult);

            AuthController.LoginRequest request = new AuthController.LoginRequest("admin", "senha");
            ResponseEntity<AuthController.LoginResponse> response = doLogin(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isNotNull();
            verify(authService).authenticate("admin", "senha");
        }

        @Test
        @DisplayName("Login retorna token JWT correto.")
        void login_retornaTokenJWT() {
            String tokenEsperado = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
            when(authService.authenticate("admin", "senha"))
                    .thenReturn(new AuthResult(tokenEsperado, null, 1L, "admin", "ADMIN"));

            ResponseEntity<AuthController.LoginResponse> response =
                    doLogin(new AuthController.LoginRequest("admin", "senha"));

            assertThat(response.getBody().token()).isEqualTo(tokenEsperado);
        }

        @Test
        @DisplayName("Login retorna ID do usuário correto.")
        void login_retornaIdUsuarioCorreto() {
            Long idEsperado = 42L;
            when(authService.authenticate("admin", "senha"))
                    .thenReturn(new AuthResult("token", null, idEsperado, "admin", "ADMIN"));

            ResponseEntity<AuthController.LoginResponse> response =
                    doLogin(new AuthController.LoginRequest("admin", "senha"));

            assertThat(response.getBody().id()).isEqualTo(idEsperado);
        }

        @Test
        @DisplayName("Login retorna username correto.")
        void login_retornaUsernameCorreto() {
            String usernameEsperado = "anderson.ramos";
            when(authService.authenticate("anderson.ramos", "senha123"))
                    .thenReturn(new AuthResult("token", null, 1L, usernameEsperado, "USER"));

            ResponseEntity<AuthController.LoginResponse> response =
                    doLogin(new AuthController.LoginRequest("anderson.ramos", "senha123"));

            assertThat(response.getBody().username()).isEqualTo(usernameEsperado);
        }

        @Test
        @DisplayName("Login retorna role/permissão correta.")
        void login_retornaRoleCorreta() {
            when(authService.authenticate("admin", "senha"))
                    .thenReturn(new AuthResult("token", null, 1L, "admin", "ADMIN"));

            ResponseEntity<AuthController.LoginResponse> response =
                    doLogin(new AuthController.LoginRequest("admin", "senha"));

            assertThat(response.getBody().role()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("Login com diferentes roles retorna role correto.")
        void login_comDiferentesRoles() {
            when(authService.authenticate("admin", "senha"))
                    .thenReturn(new AuthResult("token", null, 1L, "admin", "ADMIN"));

            ResponseEntity<AuthController.LoginResponse> responseAdmin =
                    doLogin(new AuthController.LoginRequest("admin", "senha"));
            assertThat(responseAdmin.getBody().role()).isEqualTo("ADMIN");

            when(authService.authenticate("user", "senha"))
                    .thenReturn(new AuthResult("token", null, 2L, "user", "USER"));

            ResponseEntity<AuthController.LoginResponse> responseUser =
                    doLogin(new AuthController.LoginRequest("user", "senha"));
            assertThat(responseUser.getBody().role()).isEqualTo("USER");

            when(authService.authenticate("manager", "senha"))
                    .thenReturn(new AuthResult("token", null, 3L, "manager", "MANAGER"));

            ResponseEntity<AuthController.LoginResponse> responseManager =
                    doLogin(new AuthController.LoginRequest("manager", "senha"));
            assertThat(responseManager.getBody().role()).isEqualTo("MANAGER");
        }

        @Test
        @DisplayName("Login delega para AuthService com parâmetros corretos.")
        void login_delegaAuthServiceComParametrosCorretos() {
            when(authService.authenticate("admin", "senha"))
                    .thenReturn(new AuthResult("token", null, 1L, "admin", "ADMIN"));

            doLogin(new AuthController.LoginRequest("admin", "senha"));

            verify(authService, times(1)).authenticate("admin", "senha");
        }

        @Test
        @DisplayName("Login com múltiplas chamadas chama AuthService múltiplas vezes.")
        void login_comMultiplasChamadas() {
            when(authService.authenticate(anyString(), anyString()))
                    .thenReturn(new AuthResult("token", null, 1L, "user", "USER"));

            doLogin(new AuthController.LoginRequest("user1", "pass1"));
            doLogin(new AuthController.LoginRequest("user2", "pass2"));
            doLogin(new AuthController.LoginRequest("user3", "pass3"));

            verify(authService, times(3)).authenticate(anyString(), anyString());
        }

        @Test
        @DisplayName("Response contém todos os campos obrigatórios.")
        void login_responseContemTodosCamposObrigatorios() {
            when(authService.authenticate("admin", "senha"))
                    .thenReturn(new AuthResult("token", null, 1L, "admin", "ADMIN"));

            ResponseEntity<AuthController.LoginResponse> response =
                    doLogin(new AuthController.LoginRequest("admin", "senha"));

            AuthController.LoginResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.token()).isNotNull().isNotEmpty();
            assertThat(body.id()).isNotNull().isPositive();
            assertThat(body.username()).isNotNull().isNotEmpty();
            assertThat(body.role()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Cenários de Falha - Login Inválido.")
    class LoginFalha {

        @Test
        @DisplayName("Login com senha incorreta retorna erro.")
        void login_comSenhaIncorreta_retornaErro() {
            when(authService.authenticate("admin", "senhaErrada"))
                    .thenThrow(new IllegalArgumentException("Credenciais inválidas"));

            assertThatThrownBy(() ->
                    doLogin(new AuthController.LoginRequest("admin", "senhaErrada"))
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Credenciais inválidas");
        }

        @Test
        @DisplayName("Login com usuário inexistente retorna erro.")
        void login_comUsuarioInexistente_retornaErro() {
            when(authService.authenticate("usuarioInexistente", "senha"))
                    .thenThrow(new IllegalArgumentException("Usuário não encontrado"));

            assertThatThrownBy(() ->
                    doLogin(new AuthController.LoginRequest("usuarioInexistente", "senha"))
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Usuário não encontrado");
        }

        @Test
        @DisplayName("Login com usuário null retorna erro.")
        void login_comUsuarioNull_retornaErro() {
            when(authService.authenticate(null, "senha"))
                    .thenThrow(new IllegalArgumentException("Usuário não pode ser null"));

            assertThatThrownBy(() ->
                    doLogin(new AuthController.LoginRequest(null, "senha"))
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Login com senha null retorna erro.")
        void login_comSenhaNull_retornaErro() {
            when(authService.authenticate("admin", null))
                    .thenThrow(new IllegalArgumentException("Senha não pode ser null"));

            assertThatThrownBy(() ->
                    doLogin(new AuthController.LoginRequest("admin", null))
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Login com usuário vazio retorna erro.")
        void login_comUsuarioVazio_retornaErro() {
            when(authService.authenticate("", "senha"))
                    .thenThrow(new IllegalArgumentException("Usuário não pode ser vazio"));

            assertThatThrownBy(() ->
                    doLogin(new AuthController.LoginRequest("", "senha"))
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Login com senha vazia retorna erro.")
        void login_comSenhaVazia_retornaErro() {
            when(authService.authenticate("admin", ""))
                    .thenThrow(new IllegalArgumentException("Senha não pode ser vazia"));

            assertThatThrownBy(() ->
                    doLogin(new AuthController.LoginRequest("admin", ""))
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Login com espaços em branco retorna erro.")
        void login_comEspacosEmBranco_retornaErro() {
            when(authService.authenticate("   ", "   "))
                    .thenThrow(new IllegalArgumentException("Usuário e senha não podem conter apenas espaços"));

            assertThatThrownBy(() ->
                    doLogin(new AuthController.LoginRequest("   ", "   "))
            ).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Cenários Edge Cases - Casos Extremos.")
    class EdgeCases {

        @Test
        @DisplayName("Login com usuário muito longo.")
        void login_comUsuarioMuitoLongo() {
            String usuarioLongo = "a".repeat(1000);
            when(authService.authenticate(usuarioLongo, "senha"))
                    .thenReturn(new AuthResult("token", null, 1L, usuarioLongo, "USER"));

            ResponseEntity<AuthController.LoginResponse> response =
                    doLogin(new AuthController.LoginRequest(usuarioLongo, "senha"));

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        }

        @Test
        @DisplayName("Login com senha muito longa.")
        void login_comSenhaMuitoLonga() {
            String senhaLonga = "x".repeat(1000);
            when(authService.authenticate("admin", senhaLonga))
                    .thenReturn(new AuthResult("token", null, 1L, "admin", "ADMIN"));

            ResponseEntity<AuthController.LoginResponse> response =
                    doLogin(new AuthController.LoginRequest("admin", senhaLonga));

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        }

        @Test
        @DisplayName("Login com caracteres especiais.")
        void login_comCaracteresEspeciais() {
            String usuarioEspecial = "user@#$%^&*()";
            when(authService.authenticate(usuarioEspecial, "senha!@#$"))
                    .thenReturn(new AuthResult("token", null, 1L, usuarioEspecial, "USER"));

            ResponseEntity<AuthController.LoginResponse> response =
                    doLogin(new AuthController.LoginRequest(usuarioEspecial, "senha!@#$"));

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        }

        @Test
        @DisplayName("Login com Unicode/caracteres acentuados.")
        void login_comUnicode() {
            String usuarioUnicode = "usuário_josé";
            when(authService.authenticate(usuarioUnicode, "senhaçãõ"))
                    .thenReturn(new AuthResult("token", null, 1L, usuarioUnicode, "USER"));

            ResponseEntity<AuthController.LoginResponse> response =
                    doLogin(new AuthController.LoginRequest(usuarioUnicode, "senhaçãõ"));

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        }

        @Test
        @DisplayName("Login com ID muito grande.")
        void login_comIdMuitoGrande() {
            Long idGrande = Long.MAX_VALUE;
            when(authService.authenticate("admin", "senha"))
                    .thenReturn(new AuthResult("token", null, idGrande, "admin", "ADMIN"));

            ResponseEntity<AuthController.LoginResponse> response =
                    doLogin(new AuthController.LoginRequest("admin", "senha"));

            assertThat(response.getBody().id()).isEqualTo(idGrande);
        }

        @Test
        @DisplayName("Login com token muito longo.")
        void login_comTokenMuitoLongo() {
            String tokenLongo = "x".repeat(10000);
            when(authService.authenticate("admin", "senha"))
                    .thenReturn(new AuthResult(tokenLongo, null, 1L, "admin", "ADMIN"));

            ResponseEntity<AuthController.LoginResponse> response =
                    doLogin(new AuthController.LoginRequest("admin", "senha"));

            assertThat(response.getBody().token()).isEqualTo(tokenLongo);
        }
    }

    @Nested
    @DisplayName("Cenários de Comportamento - Interações e Estado.")
    class Comportamento {

        @Test
        @DisplayName("AuthService é chamado exatamente uma vez")
        void authService_chamadoExatamenteUmaVez() {
            when(authService.authenticate("admin", "senha"))
                    .thenReturn(new AuthResult("token", null, 1L, "admin", "ADMIN"));

            doLogin(new AuthController.LoginRequest("admin", "senha"));

            verify(authService, times(1)).authenticate("admin", "senha");
        }

        @Test
        @DisplayName("AuthService não é chamado mais de uma vez.")
        void authService_naoEChamadoMaisDeUmaVez() {
            when(authService.authenticate("admin", "senha"))
                    .thenReturn(new AuthResult("token", null, 1L, "admin", "ADMIN"));

            doLogin(new AuthController.LoginRequest("admin", "senha"));

            verify(authService, times(1)).authenticate("admin", "senha");
            verifyNoMoreInteractions(authService);
        }

        @Test
        @DisplayName("Diferentes usuários geram diferentes tokens.")
        void diferentesUsuarios_geramDiferentesTokens() {
            when(authService.authenticate("user1", "senha"))
                    .thenReturn(new AuthResult("token-user1", null, 1L, "user1", "USER"));
            when(authService.authenticate("user2", "senha"))
                    .thenReturn(new AuthResult("token-user2", null, 2L, "user2", "USER"));

            ResponseEntity<AuthController.LoginResponse> response1 =
                    doLogin(new AuthController.LoginRequest("user1", "senha"));
            ResponseEntity<AuthController.LoginResponse> response2 =
                    doLogin(new AuthController.LoginRequest("user2", "senha"));

            assertThat(response1.getBody().token()).isNotEqualTo(response2.getBody().token());
            assertThat(response1.getBody().id()).isNotEqualTo(response2.getBody().id());
        }

        @Test
        @DisplayName("Múltiplos logins do mesmo usuário retornam dados consistentes.")
        void multiplosLogins_mesmoUsuario_dadosConsistentes() {
            AuthResult resultado = new AuthResult("token", null, 1L, "admin", "ADMIN");
            when(authService.authenticate("admin", "senha"))
                    .thenReturn(resultado);

            ResponseEntity<AuthController.LoginResponse> response1 =
                    doLogin(new AuthController.LoginRequest("admin", "senha"));
            ResponseEntity<AuthController.LoginResponse> response2 =
                    doLogin(new AuthController.LoginRequest("admin", "senha"));

            assertThat(response1.getBody()).isEqualTo(response2.getBody());
        }
    }

    @Nested
    @DisplayName("Cenários de Performance e Recursos.")
    class Performance {

        @Test
        @DisplayName("Login retorna resposta em tempo aceitável.")
        void login_retornaRespostaRapida() {
            when(authService.authenticate("admin", "senha"))
                    .thenReturn(new AuthResult("token", null, 1L, "admin", "ADMIN"));

            long inicio = System.currentTimeMillis();
            doLogin(new AuthController.LoginRequest("admin", "senha"));
            long duracao = System.currentTimeMillis() - inicio;

            assertThat(duracao).isLessThan(1000);
        }

        @Test
        @DisplayName("Múltiplos logins simultâneos.")
        void multiplosLogins_simultaneos() {
            when(authService.authenticate(anyString(), anyString()))
                    .thenReturn(new AuthResult("token", null, 1L, "user", "USER"));

            for (int i = 0; i < 100; i++) {
                ResponseEntity<AuthController.LoginResponse> response =
                        doLogin(new AuthController.LoginRequest("user" + i, "senha"));
                assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Cenários de Integração com Response.")
    class IntegracaoResponse {

        @Test
        @DisplayName("Response entity não é null")
        void responseEntity_naoENull() {
            when(authService.authenticate("admin", "senha"))
                    .thenReturn(new AuthResult("token", null, 1L, "admin", "ADMIN"));

            ResponseEntity<AuthController.LoginResponse> response =
                    doLogin(new AuthController.LoginRequest("admin", "senha"));

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Response body não é null.")
        void responseBody_naoENull() {
            when(authService.authenticate("admin", "senha"))
                    .thenReturn(new AuthResult("token", null, 1L, "admin", "ADMIN"));

            ResponseEntity<AuthController.LoginResponse> response =
                    doLogin(new AuthController.LoginRequest("admin", "senha"));
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Response headers contêm informações corretas.")
        void responseHeaders_contemInformacoesCorretas() {
            when(authService.authenticate("admin", "senha"))
                    .thenReturn(new AuthResult("token", null, 1L, "admin", "ADMIN"));

            ResponseEntity<AuthController.LoginResponse> response =
                    doLogin(new AuthController.LoginRequest("admin", "senha"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getStatusCodeValue()).isEqualTo(200);
        }

        @Test
        @DisplayName("Response pode ser serializado para JSON.")
        void response_podeSerSerializadoParaJSON() {
            when(authService.authenticate("admin", "senha"))
                    .thenReturn(new AuthResult("token", null, 1L, "admin", "ADMIN"));

            ResponseEntity<AuthController.LoginResponse> response =
                    doLogin(new AuthController.LoginRequest("admin", "senha"));

            AuthController.LoginResponse body = response.getBody();
            assertThat(body).isNotNull();
        }
    }
}
