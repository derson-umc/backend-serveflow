package com.serveflow.model.stock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockMovementTest {

    private static final UUID ITEM_ID = UUID.randomUUID();
    private static final String ITEM_NAME = "Farinha";

    @Nested
    @DisplayName("createEntry()")
    class CreateEntry {

        @Test
        @DisplayName("cria movimentação de entrada com tipo ENTRY")
        void createEntry_success() {
            StockMovement m = StockMovement.createEntry(ITEM_ID, ITEM_NAME,
                    new BigDecimal("5.0"), BigDecimal.ZERO, new BigDecimal("5.0"),
                    "Compra mensal", null);

            assertThat(m.getId()).isNotNull();
            assertThat(m.getStockItemId()).isEqualTo(ITEM_ID);
            assertThat(m.getStockItemName()).isEqualTo(ITEM_NAME);
            assertThat(m.getType()).isEqualTo(MovementType.ENTRY);
            assertThat(m.getQuantity()).isEqualByComparingTo("5.0");
            assertThat(m.getBalanceBefore()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(m.getBalanceAfter()).isEqualByComparingTo("5.0");
            assertThat(m.isEntry()).isTrue();
            assertThat(m.isExit()).isFalse();
        }

        @Test
        @DisplayName("cria entrada com referenceId")
        void createEntry_withReferenceId() {
            UUID refId = UUID.randomUUID();
            StockMovement m = StockMovement.createEntry(ITEM_ID, ITEM_NAME,
                    new BigDecimal("3.0"), new BigDecimal("7.0"), new BigDecimal("10.0"),
                    "Restauração", refId);

            assertThat(m.getReferenceId()).isEqualTo(refId);
        }
    }

    @Nested
    @DisplayName("createExit()")
    class CreateExit {

        @Test
        @DisplayName("cria movimentação de saída com tipo EXIT")
        void createExit_success() {
            StockMovement m = StockMovement.createExit(ITEM_ID, ITEM_NAME,
                    new BigDecimal("2.0"), new BigDecimal("10.0"), new BigDecimal("8.0"),
                    "Saída manual", null);

            assertThat(m.getType()).isEqualTo(MovementType.EXIT);
            assertThat(m.isExit()).isTrue();
            assertThat(m.isEntry()).isFalse();
        }
    }

    @Nested
    @DisplayName("createOrderConsumption()")
    class CreateOrderConsumption {

        @Test
        @DisplayName("cria consumo por pedido com tipo ORDER_CONSUMPTION")
        void createOrderConsumption_success() {
            UUID orderId = UUID.randomUUID();
            StockMovement m = StockMovement.createOrderConsumption(ITEM_ID, ITEM_NAME,
                    new BigDecimal("1.5"), new BigDecimal("5.0"), new BigDecimal("3.5"),
                    "Consumo pedido", orderId);

            assertThat(m.getType()).isEqualTo(MovementType.ORDER_CONSUMPTION);
            assertThat(m.getReferenceId()).isEqualTo(orderId);
            assertThat(m.isExit()).isTrue();
        }
    }

    @Nested
    @DisplayName("createLoss()")
    class CreateLoss {

        @Test
        @DisplayName("cria perda com tipo LOSS e referenceId nulo")
        void createLoss_success() {
            StockMovement m = StockMovement.createLoss(ITEM_ID, ITEM_NAME,
                    new BigDecimal("0.5"), new BigDecimal("3.0"), new BigDecimal("2.5"),
                    "Produto vencido");

            assertThat(m.getType()).isEqualTo(MovementType.LOSS);
            assertThat(m.getReferenceId()).isNull();
            assertThat(m.isExit()).isTrue();
        }
    }

    @Nested
    @DisplayName("createAdjustment()")
    class CreateAdjustment {

        @Test
        @DisplayName("cria ajuste de inventário com tipo ADJUSTMENT")
        void createAdjustment_success() {
            StockMovement m = StockMovement.createAdjustment(ITEM_ID, ITEM_NAME,
                    new BigDecimal("2.0"), new BigDecimal("8.0"), new BigDecimal("10.0"),
                    "Ajuste inventário");

            assertThat(m.getType()).isEqualTo(MovementType.ADJUSTMENT);
            assertThat(m.getReferenceId()).isNull();
        }
    }

    @Nested
    @DisplayName("construtor — validações")
    class ConstructorValidation {

        @Test
        @DisplayName("lança NullPointerException quando id é nulo")
        void constructor_throwsWhenIdNull() {
            assertThatThrownBy(() -> new StockMovement(null, ITEM_ID, ITEM_NAME,
                    MovementType.ENTRY, new BigDecimal("1.0"), BigDecimal.ZERO, new BigDecimal("1.0"),
                    "motivo", null, LocalDateTime.now()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("lança NullPointerException quando stockItemId é nulo")
        void constructor_throwsWhenStockItemIdNull() {
            assertThatThrownBy(() -> new StockMovement(UUID.randomUUID(), null, ITEM_NAME,
                    MovementType.ENTRY, new BigDecimal("1.0"), BigDecimal.ZERO, new BigDecimal("1.0"),
                    "motivo", null, LocalDateTime.now()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("lança NullPointerException quando type é nulo")
        void constructor_throwsWhenTypeNull() {
            assertThatThrownBy(() -> new StockMovement(UUID.randomUUID(), ITEM_ID, ITEM_NAME,
                    null, new BigDecimal("1.0"), BigDecimal.ZERO, new BigDecimal("1.0"),
                    "motivo", null, LocalDateTime.now()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando quantidade é zero")
        void constructor_throwsWhenQuantityZero() {
            assertThatThrownBy(() -> new StockMovement(UUID.randomUUID(), ITEM_ID, ITEM_NAME,
                    MovementType.ENTRY, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    "motivo", null, LocalDateTime.now()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("maior que zero");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando quantidade é negativa")
        void constructor_throwsWhenQuantityNegative() {
            assertThatThrownBy(() -> new StockMovement(UUID.randomUUID(), ITEM_ID, ITEM_NAME,
                    MovementType.ENTRY, new BigDecimal("-1.0"), BigDecimal.ZERO, BigDecimal.ZERO,
                    "motivo", null, LocalDateTime.now()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("lança NullPointerException quando createdAt é nulo")
        void constructor_throwsWhenCreatedAtNull() {
            assertThatThrownBy(() -> new StockMovement(UUID.randomUUID(), ITEM_ID, ITEM_NAME,
                    MovementType.ENTRY, new BigDecimal("1.0"), BigDecimal.ZERO, new BigDecimal("1.0"),
                    "motivo", null, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("equals() e hashCode()")
    class EqualsHashCode {

        @Test
        @DisplayName("dois movimentos com mesmo id são iguais")
        void equals_sameId() {
            UUID id = UUID.randomUUID();
            StockMovement m1 = new StockMovement(id, ITEM_ID, ITEM_NAME,
                    MovementType.ENTRY, new BigDecimal("1.0"), BigDecimal.ZERO, new BigDecimal("1.0"),
                    "a", null, LocalDateTime.now());
            StockMovement m2 = new StockMovement(id, ITEM_ID, ITEM_NAME,
                    MovementType.LOSS, new BigDecimal("2.0"), BigDecimal.ZERO, new BigDecimal("2.0"),
                    "b", null, LocalDateTime.now());

            assertThat(m1).isEqualTo(m2);
            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }

        @Test
        @DisplayName("dois movimentos com ids diferentes não são iguais")
        void notEquals_differentIds() {
            StockMovement m1 = StockMovement.createEntry(ITEM_ID, ITEM_NAME,
                    new BigDecimal("1.0"), BigDecimal.ZERO, new BigDecimal("1.0"), "a", null);
            StockMovement m2 = StockMovement.createEntry(ITEM_ID, ITEM_NAME,
                    new BigDecimal("1.0"), BigDecimal.ZERO, new BigDecimal("1.0"), "a", null);

            assertThat(m1).isNotEqualTo(m2);
        }
    }

    @Nested
    @DisplayName("MovementType enum")
    class MovementTypeEnum {

        @Test
        @DisplayName("todos os tipos têm descrição")
        void allTypesHaveDescription() {
            for (MovementType type : MovementType.values()) {
                assertThat(type.getDescription()).isNotBlank();
            }
        }

        @Test
        @DisplayName("verifica descrições específicas")
        void specificDescriptions() {
            assertThat(MovementType.ENTRY.getDescription()).isEqualTo("Entrada");
            assertThat(MovementType.EXIT.getDescription()).isEqualTo("Saída Manual");
            assertThat(MovementType.ORDER_CONSUMPTION.getDescription()).isEqualTo("Consumo por Pedido");
            assertThat(MovementType.LOSS.getDescription()).isEqualTo("Perda / Desperdício");
            assertThat(MovementType.ADJUSTMENT.getDescription()).isEqualTo("Ajuste de Inventário");
        }
    }
}
