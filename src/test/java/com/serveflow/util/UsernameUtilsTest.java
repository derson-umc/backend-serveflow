package com.serveflow.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsernameUtilsTest {

    @Nested
    @DisplayName("normalize()")
    class Normalize {

        @Test
        @DisplayName("converte para minúsculas e remove espaços")
        void normalize_lowercaseAndTrim() {
            assertThat(UsernameUtils.normalize("  João123  ")).isEqualTo("joão123");
        }

        @Test
        @DisplayName("retorna null para entrada nula")
        void normalize_null_returnsNull() {
            assertThat(UsernameUtils.normalize(null)).isNull();
        }

        @Test
        @DisplayName("retorna null para string em branco")
        void normalize_blank_returnsNull() {
            assertThat(UsernameUtils.normalize("   ")).isNull();
        }

        @Test
        @DisplayName("mantém string já normalizada inalterada")
        void normalize_alreadyNormalized() {
            assertThat(UsernameUtils.normalize("admin")).isEqualTo("admin");
        }

        @Test
        @DisplayName("converte email para minúsculas")
        void normalize_email() {
            assertThat(UsernameUtils.normalize("User@Example.COM")).isEqualTo("user@example.com");
        }
    }
}
