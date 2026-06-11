package com.serveflow.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    JwtFilter jwtFilter;

    SecurityConfig config;

    @BeforeEach
    void setUp() throws Exception {
        config = new SecurityConfig(jwtFilter);
        setField("allowedOriginsRaw", "http://localhost:5173,http://localhost:3000");
        setField("swaggerEnabled", true);
    }

    private void setField(String name, Object value) throws Exception {
        Field f = SecurityConfig.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(config, value);
    }

    @Nested
    @DisplayName("passwordEncoder()")
    class PasswordEncoderBean {

        @Test
        @DisplayName("retorna instância de BCryptPasswordEncoder")
        void returnsBCrypt() {
            PasswordEncoder encoder = config.passwordEncoder();
            assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
        }

        @Test
        @DisplayName("codifica e valida senha corretamente")
        void encodesAndMatchesPassword() {
            PasswordEncoder encoder = config.passwordEncoder();
            String raw = "minhasenha123";
            String encoded = encoder.encode(raw);
            assertThat(encoder.matches(raw, encoded)).isTrue();
        }

        @Test
        @DisplayName("senhas diferentes geram hashes distintos")
        void differentPasswordsDifferentHashes() {
            PasswordEncoder encoder = config.passwordEncoder();
            String hash1 = encoder.encode("senha1");
            String hash2 = encoder.encode("senha1");
            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("senha errada não bate com hash")
        void wrongPasswordDoesNotMatch() {
            PasswordEncoder encoder = config.passwordEncoder();
            String encoded = encoder.encode("correta");
            assertThat(encoder.matches("errada", encoded)).isFalse();
        }
    }

    @Nested
    @DisplayName("corsConfigurationSource()")
    class CorsConfig {

        @Test
        @DisplayName("retorna UrlBasedCorsConfigurationSource")
        void returnsCorrectType() {
            CorsConfigurationSource source = config.corsConfigurationSource();
            assertThat(source).isInstanceOf(UrlBasedCorsConfigurationSource.class);
        }

        @Test
        @DisplayName("registra as origens corretas separadas por vírgula")
        void registersCorrectOrigins() {
            UrlBasedCorsConfigurationSource source =
                    (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();
            CorsConfiguration cors = source.getCorsConfigurations().get("/**");
            assertThat(cors).isNotNull();
            assertThat(cors.getAllowedOrigins())
                    .contains("http://localhost:5173", "http://localhost:3000");
        }

        @Test
        @DisplayName("permite os métodos HTTP corretos")
        void allowsCorrectMethods() {
            UrlBasedCorsConfigurationSource source =
                    (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();
            CorsConfiguration cors = source.getCorsConfigurations().get("/**");
            assertThat(cors.getAllowedMethods())
                    .contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        }

        @Test
        @DisplayName("permite credentials")
        void allowsCredentials() {
            UrlBasedCorsConfigurationSource source =
                    (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();
            CorsConfiguration cors = source.getCorsConfigurations().get("/**");
            assertThat(cors.getAllowCredentials()).isTrue();
        }

        @Test
        @DisplayName("permite headers Authorization e Content-Type")
        void allowsAuthorizationHeader() {
            UrlBasedCorsConfigurationSource source =
                    (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();
            CorsConfiguration cors = source.getCorsConfigurations().get("/**");
            assertThat(cors.getAllowedHeaders()).contains("Authorization", "Content-Type");
        }
    }
}
