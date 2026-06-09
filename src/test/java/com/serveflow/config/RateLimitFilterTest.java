package com.serveflow.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    RateLimitFilter filter;

    @Mock FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
    }

    @Nested
    @DisplayName("Paths não limitados passam direto")
    class NonLimitedPaths {

        @Test
        @DisplayName("GET /api/orders não é limitado — passa direto")
        void nonLimitedPath_passesThrough() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/orders");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, chain);

            verify(chain).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(200);
        }

        @Test
        @DisplayName("GET / não é limitado — passa direto")
        void rootPath_passesThrough() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, chain);

            verify(chain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Paths limitados — dentro do limite")
    class LimitedPathsBelowLimit {

        @Test
        @DisplayName("primeiras requisições ao /api/auth/login passam")
        void loginPath_belowLimit_passes() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/auth/login");
            request.setRemoteAddr("127.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, chain);

            verify(chain).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(200);
        }

        @Test
        @DisplayName("exatamente 10 requisições passam (no limite)")
        void loginPath_exactlyLimit_passes() throws Exception {
            RateLimitFilter freshFilter = new RateLimitFilter();
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/auth/login");
            request.setRemoteAddr("10.0.0.99");

            for (int i = 0; i < 10; i++) {
                MockHttpServletResponse response = new MockHttpServletResponse();
                freshFilter.doFilterInternal(request, response, chain);
                assertThat(response.getStatus()).isEqualTo(200);
            }
            verify(chain, times(10)).doFilter(any(), any());
        }
    }

    @Nested
    @DisplayName("Paths limitados — excede o limite")
    class LimitedPathsOverLimit {

        @Test
        @DisplayName("11ª requisição retorna 429 Too Many Requests")
        void loginPath_overLimit_returns429() throws Exception {
            RateLimitFilter freshFilter = new RateLimitFilter();
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/auth/login");
            request.setRemoteAddr("192.168.1.1");

            for (int i = 0; i < 10; i++) {
                MockHttpServletResponse resp = new MockHttpServletResponse();
                freshFilter.doFilterInternal(request, resp, chain);
            }

            MockHttpServletResponse limited = new MockHttpServletResponse();
            freshFilter.doFilterInternal(request, limited, chain);

            assertThat(limited.getStatus()).isEqualTo(429);
            assertThat(limited.getContentAsString()).contains("Muitas tentativas");
            verify(chain, times(10)).doFilter(any(), any());
        }

        @Test
        @DisplayName("rate limit é por IP — outro IP não é afetado")
        void rateLimitIsPerIp() throws Exception {
            RateLimitFilter freshFilter = new RateLimitFilter();

            MockHttpServletRequest req1 = new MockHttpServletRequest();
            req1.setRequestURI("/api/auth/login");
            req1.setRemoteAddr("1.1.1.1");
            for (int i = 0; i < 11; i++) {
                freshFilter.doFilterInternal(req1, new MockHttpServletResponse(), chain);
            }

            MockHttpServletRequest req2 = new MockHttpServletRequest();
            req2.setRequestURI("/api/auth/login");
            req2.setRemoteAddr("2.2.2.2");
            MockHttpServletResponse resp2 = new MockHttpServletResponse();
            freshFilter.doFilterInternal(req2, resp2, chain);

            assertThat(resp2.getStatus()).isEqualTo(200);
        }

        @Test
        @DisplayName("forgot-password também é limitado")
        void forgotPasswordPath_isLimited() throws Exception {
            RateLimitFilter freshFilter = new RateLimitFilter();
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/auth/forgot-password");
            request.setRemoteAddr("5.5.5.5");

            for (int i = 0; i < 11; i++) {
                freshFilter.doFilterInternal(request, new MockHttpServletResponse(), chain);
            }

            MockHttpServletResponse limited = new MockHttpServletResponse();
            freshFilter.doFilterInternal(request, limited, chain);

            assertThat(limited.getStatus()).isEqualTo(429);
        }
    }
}
