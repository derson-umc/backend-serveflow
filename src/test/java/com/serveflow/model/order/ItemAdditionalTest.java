package com.serveflow.model.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemAdditionalTest {

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("construtor completo (4 args)")
    class Constructor {

        @Test
        @DisplayName("cria adicional com campos corretos")
        void constructor_success() {
            UUID id = UUID.randomUUID();
            ItemAdditional a = new ItemAdditional(id, "  Bacon  ", 2, new BigDecimal("5.00"));
            assertThat(a.getId()).isEqualTo(id);
            assertThat(a.getName()).isEqualTo("Bacon");
            assertThat(a.getQuantity()).isEqualTo(2);
            assertThat(a.getUnitPrice()).isEqualByComparingTo("5.00");
        }

        @Test
        @DisplayName("lança NullPointerException quando id é nulo")
        void constructor_throwsWhenIdNull() {
            assertThatThrownBy(() -> new ItemAdditional(null, "Queijo", 1, new BigDecimal("2.00")))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando nome em branco")
        void constructor_throwsWhenNameBlank() {
            assertThatThrownBy(() -> new ItemAdditional(UUID.randomUUID(), "  ", 1, new BigDecimal("2.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nome do adicional");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando quantidade <= 0")
        void constructor_throwsWhenQuantityZero() {
            assertThatThrownBy(() -> new ItemAdditional(UUID.randomUUID(), "X", 0, new BigDecimal("2.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantidade do adicional");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando preço nulo")
        void constructor_throwsWhenPriceNull() {
            assertThatThrownBy(() -> new ItemAdditional(UUID.randomUUID(), "X", 1, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Preço do adicional");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando preço <= 0")
        void constructor_throwsWhenPriceZero() {
            assertThatThrownBy(() -> new ItemAdditional(UUID.randomUUID(), "X", 1, BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Preço do adicional");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("construtor sem id (3 args)")
    class ShortConstructor {

        @Test
        @DisplayName("gera id automaticamente")
        void shortConstructor_generatesId() {
            ItemAdditional a = new ItemAdditional("Queijo", 1, new BigDecimal("3.00"));
            assertThat(a.getId()).isNotNull();
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getTotal()")
    class GetTotal {

        @Test
        @DisplayName("retorna unitPrice * quantity")
        void getTotal_unitPriceTimesQty() {
            ItemAdditional a = new ItemAdditional("Bacon", 3, new BigDecimal("4.00"));
            assertThat(a.getTotal()).isEqualByComparingTo("12.00");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("equals() e hashCode()")
    class EqualsHashCode {

        @Test
        @DisplayName("dois adicionais com mesmo nome são iguais")
        void equals_sameName() {
            ItemAdditional a1 = new ItemAdditional("Queijo", 1, new BigDecimal("2.00"));
            ItemAdditional a2 = new ItemAdditional("Queijo", 2, new BigDecimal("3.00"));
            assertThat(a1).isEqualTo(a2);
            assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        }

        @Test
        @DisplayName("dois adicionais com nomes diferentes não são iguais")
        void equals_differentName() {
            ItemAdditional a1 = new ItemAdditional("Queijo", 1, new BigDecimal("2.00"));
            ItemAdditional a2 = new ItemAdditional("Bacon", 1, new BigDecimal("2.00"));
            assertThat(a1).isNotEqualTo(a2);
        }
    }
}
