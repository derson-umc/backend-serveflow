package com.serveflow.model.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {

    private static OrderItem defaultItem() {
        return new OrderItem(
                UUID.randomUUID(), UUID.randomUUID(), "Hamburguer", 2,
                new BigDecimal("15.00"), null, List.of(),
                OrderItemStatus.PENDENTE, null, "ALIMENTO");
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("construtor completo (10 args)")
    class Constructor {

        @Test
        @DisplayName("cria item com todos os campos preenchidos corretamente")
        void constructor_allFields() {
            UUID id = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            OrderItem item = new OrderItem(id, productId, "  Pizza  ", 3,
                    new BigDecimal("25.00"), "  sem gluten  ",
                    List.of(), OrderItemStatus.ENVIADO, "motivo", "ALIMENTO");

            assertThat(item.getId()).isEqualTo(id);
            assertThat(item.getProductId()).isEqualTo(productId);
            assertThat(item.getProductName()).isEqualTo("Pizza");
            assertThat(item.getQuantity()).isEqualTo(3);
            assertThat(item.getUnitPrice()).isEqualByComparingTo("25.00");
            assertThat(item.getObservation()).isEqualTo("sem gluten");
            assertThat(item.getStatus()).isEqualTo(OrderItemStatus.ENVIADO);
            assertThat(item.getCancelReason()).isEqualTo("motivo");
            assertThat(item.getProductCategory()).isEqualTo("ALIMENTO");
        }

        @Test
        @DisplayName("status padrão é PENDENTE quando null é passado")
        void constructor_nullStatus_defaultsPending() {
            OrderItem item = new OrderItem(
                    UUID.randomUUID(), UUID.randomUUID(), "X", 1,
                    new BigDecimal("1.00"), null, null, null, null, null);
            assertThat(item.getStatus()).isEqualTo(OrderItemStatus.PENDENTE);
        }

        @Test
        @DisplayName("lança NullPointerException quando id é nulo")
        void constructor_throwsWhenIdNull() {
            assertThatThrownBy(() -> new OrderItem(
                    null, UUID.randomUUID(), "X", 1,
                    new BigDecimal("1.00"), null, List.of(), null, null, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("lança NullPointerException quando productId é nulo")
        void constructor_throwsWhenProductIdNull() {
            assertThatThrownBy(() -> new OrderItem(
                    UUID.randomUUID(), null, "X", 1,
                    new BigDecimal("1.00"), null, List.of(), null, null, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando productName em branco")
        void constructor_throwsWhenNameBlank() {
            assertThatThrownBy(() -> new OrderItem(
                    UUID.randomUUID(), UUID.randomUUID(), "   ", 1,
                    new BigDecimal("1.00"), null, List.of(), null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nome do produto");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando quantidade <= 0")
        void constructor_throwsWhenQuantityZero() {
            assertThatThrownBy(() -> new OrderItem(
                    UUID.randomUUID(), UUID.randomUUID(), "X", 0,
                    new BigDecimal("1.00"), null, List.of(), null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantidade");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando preço unitário <= 0")
        void constructor_throwsWhenPriceZero() {
            assertThatThrownBy(() -> new OrderItem(
                    UUID.randomUUID(), UUID.randomUUID(), "X", 1,
                    BigDecimal.ZERO, null, List.of(), null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Preço unitário");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando preço nulo")
        void constructor_throwsWhenPriceNull() {
            assertThatThrownBy(() -> new OrderItem(
                    UUID.randomUUID(), UUID.randomUUID(), "X", 1,
                    null, null, List.of(), null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Preço unitário");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("construtor simplificado (6 args)")
    class ShortConstructor {

        @Test
        @DisplayName("cria item sem categoria e com status PENDENTE")
        void shortConstructor_success() {
            UUID productId = UUID.randomUUID();
            OrderItem item = new OrderItem(productId, "Suco", 1,
                    new BigDecimal("8.00"), null, List.of());
            assertThat(item.getStatus()).isEqualTo(OrderItemStatus.PENDENTE);
            assertThat(item.getProductCategory()).isNull();
            assertThat(item.getProductId()).isEqualTo(productId);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("construtor com categoria (7 args)")
    class CategoryConstructor {

        @Test
        @DisplayName("cria item com categoria preenchida e status PENDENTE")
        void categoryConstructor_success() {
            UUID productId = UUID.randomUUID();
            OrderItem item = new OrderItem(productId, "Frango", 2,
                    new BigDecimal("20.00"), null, List.of(), "ALIMENTO");
            assertThat(item.getProductCategory()).isEqualTo("ALIMENTO");
            assertThat(item.getStatus()).isEqualTo(OrderItemStatus.PENDENTE);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getItemPrice()")
    class GetItemPrice {

        @Test
        @DisplayName("retorna unitPrice * quantity")
        void getItemPrice_unitPriceTimesQty() {
            OrderItem item = new OrderItem(
                    UUID.randomUUID(), UUID.randomUUID(), "X", 3,
                    new BigDecimal("10.00"), null, List.of(), null, null, null);
            assertThat(item.getItemPrice()).isEqualByComparingTo("30.00");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAdditionalsPrice()")
    class GetAdditionalsPrice {

        @Test
        @DisplayName("retorna zero quando não há adicionais")
        void getAdditionalsPrice_zeroWhenEmpty() {
            assertThat(defaultItem().getAdditionalsPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("retorna soma dos totais dos adicionais")
        void getAdditionalsPrice_sumsAdditionals() {
            ItemAdditional a1 = new ItemAdditional("Queijo", 2, new BigDecimal("3.00"));
            ItemAdditional a2 = new ItemAdditional("Bacon", 1, new BigDecimal("5.00"));
            OrderItem item = new OrderItem(
                    UUID.randomUUID(), UUID.randomUUID(), "Hamburguer", 1,
                    new BigDecimal("10.00"), null, List.of(a1, a2),
                    OrderItemStatus.PENDENTE, null, null);
            // queijo: 2*3=6, bacon: 1*5=5 → total 11
            assertThat(item.getAdditionalsPrice()).isEqualByComparingTo("11.00");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getTotal()")
    class GetTotal {

        @Test
        @DisplayName("retorna itemPrice + additionalsPrice")
        void getTotal_itemPluAdditionals() {
            ItemAdditional add = new ItemAdditional("Molho", 1, new BigDecimal("2.00"));
            OrderItem item = new OrderItem(
                    UUID.randomUUID(), UUID.randomUUID(), "Frango", 2,
                    new BigDecimal("12.00"), null, List.of(add),
                    OrderItemStatus.PENDENTE, null, null);
            // itemPrice = 2*12 = 24; additionals = 2; total = 26
            assertThat(item.getTotal()).isEqualByComparingTo("26.00");
        }

        @Test
        @DisplayName("retorna apenas itemPrice quando sem adicionais")
        void getTotal_noAdditionals() {
            OrderItem item = defaultItem(); // 2 * 15 = 30
            assertThat(item.getTotal()).isEqualByComparingTo("30.00");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("syncStatus()")
    class SyncStatus {

        @Test
        @DisplayName("atualiza status do item")
        void syncStatus_updatesStatus() {
            OrderItem item = defaultItem();
            item.syncStatus(OrderItemStatus.PRONTO);
            assertThat(item.getStatus()).isEqualTo(OrderItemStatus.PRONTO);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("cancel()")
    class Cancel {

        @Test
        @DisplayName("define status e motivo do cancelamento")
        void cancel_setsCancelStatusAndReason() {
            OrderItem item = defaultItem();
            item.cancel(OrderItemStatus.CANCELADO_ANTES_PREPARO, "Pedido errado");
            assertThat(item.getStatus()).isEqualTo(OrderItemStatus.CANCELADO_ANTES_PREPARO);
            assertThat(item.getCancelReason()).isEqualTo("Pedido errado");
        }

        @Test
        @DisplayName("isCanceled() retorna true para CANCELADO_ANTES_PREPARO")
        void cancel_isCanceled_true_beforePrep() {
            OrderItem item = defaultItem();
            item.cancel(OrderItemStatus.CANCELADO_ANTES_PREPARO, "X");
            assertThat(item.getStatus().isCanceled()).isTrue();
        }

        @Test
        @DisplayName("isCanceled() retorna true para CANCELADO_EM_PREPARO")
        void cancel_isCanceled_true_duringPrep() {
            OrderItem item = defaultItem();
            item.cancel(OrderItemStatus.CANCELADO_EM_PREPARO, "X");
            assertThat(item.getStatus().isCanceled()).isTrue();
        }

        @Test
        @DisplayName("isCanceled() retorna false para PENDENTE")
        void cancel_isCanceled_false_pending() {
            assertThat(defaultItem().getStatus().isCanceled()).isFalse();
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAdditionals() — lista imutável")
    class GetAdditionals {

        @Test
        @DisplayName("getAdditionals() retorna cópia imutável")
        void getAdditionals_isUnmodifiable() {
            OrderItem item = defaultItem();
            assertThatThrownBy(() -> item.getAdditionals().add(
                    new ItemAdditional("X", 1, BigDecimal.ONE)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
