package com.serveflow.model.stock;

import com.serveflow.exception.stock.InsufficientStockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockItemTest {

    private static StockItem buildItem(BigDecimal current, BigDecimal minimum) {
        return new StockItem(UUID.randomUUID(), "Farinha", "kg",
                current, minimum, "INGREDIENTE", "Fornecedor X",
                StockItemStatus.ACTIVE, LocalDateTime.now(), null);
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("cria insumo ativo com data de criação")
        void create_defaultActiveAndCreatedAt() {
            StockItem item = StockItem.create("Sal", "g",
                    new BigDecimal("500.00"), new BigDecimal("100.00"),
                    "TEMPERO", "Atacado ABC");

            assertThat(item.getId()).isNotNull();
            assertThat(item.getName()).isEqualTo("Sal");
            assertThat(item.isActive()).isTrue();
            assertThat(item.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("deduct()")
    class Deduct {

        @Test
        @DisplayName("deduz quantidade do estoque com sucesso")
        void deduct_reducesQuantity() {
            StockItem item = buildItem(new BigDecimal("10.00"), new BigDecimal("2.00"));
            item.deduct(new BigDecimal("3.00"));
            assertThat(item.getCurrentQuantity()).isEqualByComparingTo("7.00");
        }

        @Test
        @DisplayName("lança InsufficientStockException quando estoque insuficiente")
        void deduct_throwsWhenInsufficient() {
            StockItem item = buildItem(new BigDecimal("2.00"), new BigDecimal("0.00"));
            assertThatThrownBy(() -> item.deduct(new BigDecimal("5.00")))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("insuficiente");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando quantidade <= 0")
        void deduct_throwsWhenZeroOrNegative() {
            StockItem item = buildItem(new BigDecimal("10.00"), new BigDecimal("2.00"));
            assertThatThrownBy(() -> item.deduct(BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("lança NullPointerException quando quantidade nula")
        void deduct_throwsWhenNull() {
            StockItem item = buildItem(new BigDecimal("10.00"), new BigDecimal("2.00"));
            assertThatThrownBy(() -> item.deduct(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("add()")
    class Add {

        @Test
        @DisplayName("adiciona quantidade ao estoque com sucesso")
        void add_increasesQuantity() {
            StockItem item = buildItem(new BigDecimal("5.00"), new BigDecimal("1.00"));
            item.add(new BigDecimal("3.00"));
            assertThat(item.getCurrentQuantity()).isEqualByComparingTo("8.00");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando quantidade <= 0")
        void add_throwsWhenZeroOrNegative() {
            StockItem item = buildItem(new BigDecimal("5.00"), new BigDecimal("1.00"));
            assertThatThrownBy(() -> item.add(new BigDecimal("-1.00")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("hasEnoughStock()")
    class HasEnoughStock {

        @Test
        @DisplayName("retorna true quando quantidade atual >= requerida")
        void hasEnoughStock_trueWhenSufficient() {
            StockItem item = buildItem(new BigDecimal("10.00"), new BigDecimal("2.00"));
            assertThat(item.hasEnoughStock(new BigDecimal("10.00"))).isTrue();
        }

        @Test
        @DisplayName("retorna false quando quantidade atual < requerida")
        void hasEnoughStock_falseWhenInsufficient() {
            StockItem item = buildItem(new BigDecimal("3.00"), new BigDecimal("1.00"));
            assertThat(item.hasEnoughStock(new BigDecimal("5.00"))).isFalse();
        }
    }

    @Nested
    @DisplayName("isBelowMinimum()")
    class IsBelowMinimum {

        @Test
        @DisplayName("retorna true quando estoque abaixo do mínimo")
        void isBelowMinimum_true() {
            StockItem item = buildItem(new BigDecimal("1.00"), new BigDecimal("5.00"));
            assertThat(item.isBelowMinimum()).isTrue();
        }

        @Test
        @DisplayName("retorna false quando estoque acima ou igual ao mínimo")
        void isBelowMinimum_false() {
            StockItem item = buildItem(new BigDecimal("5.00"), new BigDecimal("5.00"));
            assertThat(item.isBelowMinimum()).isFalse();
        }
    }

    @Nested
    @DisplayName("deactivate() e activate()")
    class ActivationDeactivation {

        @Test
        @DisplayName("deactivate() muda status para INACTIVE")
        void deactivate_changesStatusToInactive() {
            StockItem item = buildItem(new BigDecimal("5.00"), new BigDecimal("1.00"));
            item.deactivate();
            assertThat(item.isActive()).isFalse();
        }

        @Test
        @DisplayName("activate() muda status para ACTIVE")
        void activate_changesStatusToActive() {
            StockItem item = new StockItem(UUID.randomUUID(), "Sal", "g",
                    new BigDecimal("100.00"), new BigDecimal("10.00"),
                    null, null, StockItemStatus.INACTIVE, LocalDateTime.now(), null);
            item.activate();
            assertThat(item.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("updateDetails()")
    class UpdateDetails {

        @Test
        @DisplayName("atualiza nome, unidade, mínimo, categoria e fornecedor")
        void updateDetails_updatesFields() {
            StockItem item = buildItem(new BigDecimal("5.00"), new BigDecimal("1.00"));
            item.updateDetails("Açúcar", "g", new BigDecimal("50.00"), "INGREDIENTE", "Novo Fornecedor");
            assertThat(item.getName()).isEqualTo("Açúcar");
            assertThat(item.getUnit()).isEqualTo("g");
            assertThat(item.getMinimumQuantity()).isEqualByComparingTo("50.00");
            assertThat(item.getCategory()).isEqualTo("INGREDIENTE");
            assertThat(item.getSupplier()).isEqualTo("Novo Fornecedor");
        }
    }

    @Nested
    @DisplayName("equals() e hashCode()")
    class EqualsHashCode {

        @Test
        @DisplayName("dois itens com mesmo id são iguais")
        void equals_sameId() {
            UUID id = UUID.randomUUID();
            StockItem s1 = new StockItem(id, "A", "kg",
                    BigDecimal.ONE, BigDecimal.ZERO, null, null,
                    StockItemStatus.ACTIVE, LocalDateTime.now(), null);
            StockItem s2 = new StockItem(id, "B", "g",
                    BigDecimal.TEN, BigDecimal.ONE, null, null,
                    StockItemStatus.INACTIVE, LocalDateTime.now(), null);
            assertThat(s1).isEqualTo(s2);
            assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
        }

        @Test
        @DisplayName("dois itens com ids diferentes não são iguais")
        void equals_differentId() {
            StockItem s1 = buildItem(BigDecimal.ONE, BigDecimal.ZERO);
            StockItem s2 = buildItem(BigDecimal.ONE, BigDecimal.ZERO);
            assertThat(s1).isNotEqualTo(s2);
        }
    }

    @Nested
    @DisplayName("construtor — validações")
    class ConstructorValidation {

        @Test
        @DisplayName("lança IllegalArgumentException quando nome em branco")
        void constructor_throwsWhenNameBlank() {
            assertThatThrownBy(() -> new StockItem(UUID.randomUUID(), "  ", "kg",
                    BigDecimal.TEN, BigDecimal.ONE, null, null,
                    StockItemStatus.ACTIVE, LocalDateTime.now(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nome");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando unidade em branco")
        void constructor_throwsWhenUnitBlank() {
            assertThatThrownBy(() -> new StockItem(UUID.randomUUID(), "Farinha", "",
                    BigDecimal.TEN, BigDecimal.ONE, null, null,
                    StockItemStatus.ACTIVE, LocalDateTime.now(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unidade");
        }
    }
}
