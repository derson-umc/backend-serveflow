package com.serveflow.config;

import com.serveflow.model.user.User;
import com.serveflow.model.user.UserRole;
import com.serveflow.repository.user.UserRepository;
import com.serveflow.service.audit.AuditService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    JwtService jwtService;
    @Mock
    UserRepository userRepository;
    @Mock
    AuditService auditService;
    @Mock
    FilterChain filterChain;

    @InjectMocks
    JwtFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User mockUser() {
        return new User(1L, "admin", "admin@test.com", "pass", UserRole.ADMIN, "Admin");
    }

    @Nested
    @DisplayName("sem header Authorization")
    class NoAuthHeader {

        @Test
        @DisplayName("chama chain.doFilter e não autentica quando não há header")
        void noAuthHeader_callsChainDoFilter() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("chama chain.doFilter quando Authorization não começa com Bearer")
        void authHeaderNotBearer_callsChain() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
            request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("token válido")
    class ValidToken {

        @Test
        @DisplayName("popula SecurityContext quando token e usuário são válidos")
        void validToken_populatesSecurityContext() throws ServletException, IOException {
            User user = mockUser();
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
            request.addHeader("Authorization", "Bearer valid-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            when(jwtService.extractUsername("valid-token")).thenReturn("admin");
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                    .isEqualTo(user);
        }

        @Test
        @DisplayName("não sobrescreve autenticação existente no SecurityContext")
        void validToken_doesNotOverwriteExistingAuth() throws ServletException, IOException {
            User user = mockUser();
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
            request.addHeader("Authorization", "Bearer valid-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            when(jwtService.extractUsername("valid-token")).thenReturn("admin");
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

            // First call sets auth
            filter.doFilterInternal(request, response, filterChain);

            // Second call — auth already set, won't overwrite
            MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/orders");
            request2.addHeader("Authorization", "Bearer valid-token");
            MockHttpServletResponse response2 = new MockHttpServletResponse();
            filter.doFilterInternal(request2, response2, filterChain);

            verify(filterChain, times(2)).doFilter(any(), any());
        }

        @Test
        @DisplayName("não autentica quando usuário não encontrado no repositório")
        void validToken_userNotFound_doesNotAuthenticate() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
            request.addHeader("Authorization", "Bearer valid-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            when(jwtService.extractUsername("valid-token")).thenReturn("unknown");
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    @DisplayName("token inválido/expirado")
    class InvalidToken {

        @Test
        @DisplayName("retorna 401 quando token está expirado")
        void expiredToken_returns401() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
            request.addHeader("Authorization", "Bearer expired-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            when(jwtService.extractUsername("expired-token"))
                    .thenThrow(mock(ExpiredJwtException.class));

            filter.doFilterInternal(request, response, filterChain);

            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("retorna 401 quando token é mal formatado")
        void malformedToken_returns401() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
            request.addHeader("Authorization", "Bearer bad-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            when(jwtService.extractUsername("bad-token"))
                    .thenThrow(mock(MalformedJwtException.class));

            filter.doFilterInternal(request, response, filterChain);

            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("retorna 401 para qualquer outra exceção de validação")
        void genericException_returns401() throws ServletException, IOException {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
            request.addHeader("Authorization", "Bearer bad-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            when(jwtService.extractUsername("bad-token"))
                    .thenThrow(new RuntimeException("Unexpected error"));

            filter.doFilterInternal(request, response, filterChain);

            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(any(), any());
        }
    }
}
