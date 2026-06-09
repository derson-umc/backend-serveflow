package com.serveflow.model.menu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MenuItemTest {

    private static MenuItem defaultItem() {
        return MenuItem.create(UUID.randomUUID(), "Frango Grelhado", "desc", new BigDecimal("22.00"));
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("cria item disponível, não removido, com id gerado")
        void create_defaults() {
            UUID productId = UUID.randomUUID();
            MenuItem item = MenuItem.create(productId, "Pizza", "Saborosa", new BigDecimal("35.00"));
            assertThat(item.getId()).isNotNull();
            assertThat(item.getProductId()).isEqualTo(productId);
            assertThat(item.getName()).isEqualTo("Pizza");
            assertThat(item.getDescription()).isEqualTo("Saborosa");
            assertThat(item.getPrice()).isEqualByComparingTo("35.00");
            assertThat(item.isAvailable()).isTrue();
            assertThat(item.isRemoved()).isFalse();
            assertThat(item.getRemovedBy()).isNull();
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando nome em branco")
        void create_throwsWhenNameBlank() {
            assertThatThrownBy(() -> MenuItem.create(UUID.randomUUID(), "  ", "desc", new BigDecimal("10.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nome do item");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando preço nulo")
        void create_throwsWhenPriceNull() {
            assertThatThrownBy(() -> MenuItem.create(UUID.randomUUID(), "X", "desc", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Preço");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando preço <= 0")
        void create_throwsWhenPriceZero() {
            assertThatThrownBy(() -> MenuItem.create(UUID.randomUUID(), "X", "desc", BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Preço");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("construtor completo (8 args)")
    class Constructor {

        @Test
        @DisplayName("lança NullPointerException quando id nulo")
        void constructor_throwsWhenIdNull() {
            assertThatThrownBy(() -> new MenuItem(null, UUID.randomUUID(), "X", "d",
                    new BigDecimal("1.00"), true, false, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("lança NullPointerException quando productId nulo")
        void constructor_throwsWhenProductIdNull() {
            assertThatThrownBy(() -> new MenuItem(UUID.randomUUID(), null, "X", "d",
                    new BigDecimal("1.00"), true, false, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateAvailability()")
    class UpdateAvailability {

        @Test
        @DisplayName("torna item indisponível")
        void updateAvailability_toFalse() {
            MenuItem item = defaultItem();
            item.updateAvailability(false);
            assertThat(item.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("torna item disponível novamente")
        void updateAvailability_toTrue() {
            MenuItem item = defaultItem();
            item.updateAvailability(false);
            item.updateAvailability(true);
            assertThat(item.isAvailable()).isTrue();
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("atualiza nome, descrição e preço")
        void update_allFields() {
            MenuItem item = defaultItem();
            item.update("Novo Nome", "Nova Desc", new BigDecimal("99.00"));
            assertThat(item.getName()).isEqualTo("Novo Nome");
            assertThat(item.getDescription()).isEqualTo("Nova Desc");
            assertThat(item.getPrice()).isEqualByComparingTo("99.00");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando novo nome em branco")
        void update_throwsWhenNameBlank() {
            MenuItem item = defaultItem();
            assertThatThrownBy(() -> item.update("  ", "d", new BigDecimal("10.00")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando novo preço <= 0")
        void update_throwsWhenPriceInvalid() {
            MenuItem item = defaultItem();
            assertThatThrownBy(() -> item.update("Nome", "d", BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("markAsRemoved()")
    class MarkAsRemoved {

        @Test
        @DisplayName("marca como removido e registra chefName")
        void markAsRemoved_success() {
            MenuItem item = defaultItem();
            item.markAsRemoved("  Chef Maria  ");
            assertThat(item.isRemoved()).isTrue();
            assertThat(item.getRemovedBy()).isEqualTo("Chef Maria");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando chefName em branco")
        void markAsRemoved_throwsWhenChefBlank() {
            MenuItem item = defaultItem();
            assertThatThrownBy(() -> item.markAsRemoved("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cozinheiro");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando chefName nulo")
        void markAsRemoved_throwsWhenChefNull() {
            MenuItem item = defaultItem();
            assertThatThrownBy(() -> item.markAsRemoved(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("equals() e hashCode()")
    class EqualsHashCode {

        @Test
        @DisplayName("dois itens com mesmo id são iguais")
        void equals_sameId() {
            UUID id = UUID.randomUUID();
            MenuItem m1 = new MenuItem(id, UUID.randomUUID(), "A", "d", new BigDecimal("1.00"), true, false, null);
            MenuItem m2 = new MenuItem(id, UUID.randomUUID(), "B", "d", new BigDecimal("2.00"), false, false, null);
            assertThat(m1).isEqualTo(m2);
            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }

        @Test
        @DisplayName("dois itens com ids diferentes não são iguais")
        void equals_differentId() {
            assertThat(defaultItem()).isNotEqualTo(defaultItem());
        }
    }
}
