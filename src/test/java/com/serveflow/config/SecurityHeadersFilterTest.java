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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SecurityHeadersFilterTest {

    SecurityHeadersFilter filter;

    @Mock FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new SecurityHeadersFilter();
    }

    @Nested
    @DisplayName("headers de segurança")
    class SecurityHeaders {

        @Test
        @DisplayName("adiciona X-Content-Type-Options: nosniff")
        void addsXContentTypeOptions() throws Exception {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/orders");
            MockHttpServletResponse res = new MockHttpServletResponse();

            filter.doFilterInternal(req, res, chain);

            assertThat(res.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        }

        @Test
        @DisplayName("adiciona X-Frame-Options: DENY")
        void addsXFrameOptions() throws Exception {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/orders");
            MockHttpServletResponse res = new MockHttpServletResponse();

            filter.doFilterInternal(req, res, chain);

            assertThat(res.getHeader("X-Frame-Options")).isEqualTo("DENY");
        }

        @Test
        @DisplayName("adiciona Referrer-Policy")
        void addsReferrerPolicy() throws Exception {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/orders");
            MockHttpServletResponse res = new MockHttpServletResponse();

            filter.doFilterInternal(req, res, chain);

            assertThat(res.getHeader("Referrer-Policy")).isEqualTo("strict-origin-when-cross-origin");
        }

        @Test
        @DisplayName("adiciona Permissions-Policy")
        void addsPermissionsPolicy() throws Exception {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/orders");
            MockHttpServletResponse res = new MockHttpServletResponse();

            filter.doFilterInternal(req, res, chain);

            assertThat(res.getHeader("Permissions-Policy"))
                    .isEqualTo("camera=(), microphone=(), geolocation=()");
        }

        @Test
        @DisplayName("adiciona Strict-Transport-Security com max-age de 1 ano")
        void addsHSTS() throws Exception {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/orders");
            MockHttpServletResponse res = new MockHttpServletResponse();

            filter.doFilterInternal(req, res, chain);

            assertThat(res.getHeader("Strict-Transport-Security"))
                    .contains("max-age=31536000")
                    .contains("includeSubDomains");
        }

        @Test
        @DisplayName("adiciona Content-Security-Policy com default-src self")
        void addsCSP() throws Exception {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/orders");
            MockHttpServletResponse res = new MockHttpServletResponse();

            filter.doFilterInternal(req, res, chain);

            assertThat(res.getHeader("Content-Security-Policy")).contains("default-src 'self'");
        }
    }

    @Nested
    @DisplayName("encadeamento do filtro")
    class FilterChaining {

        @Test
        @DisplayName("chama chain.doFilter após adicionar headers")
        void callsChainAfterHeaders() throws Exception {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/orders");
            MockHttpServletResponse res = new MockHttpServletResponse();

            filter.doFilterInternal(req, res, chain);

            verify(chain).doFilter(req, res);
        }

        @Test
        @DisplayName("funciona em qualquer URI")
        void worksForAnyUri() throws Exception {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/auth/login");
            MockHttpServletResponse res = new MockHttpServletResponse();

            filter.doFilterInternal(req, res, chain);

            verify(chain).doFilter(req, res);
            assertThat(res.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        }
    }
}
