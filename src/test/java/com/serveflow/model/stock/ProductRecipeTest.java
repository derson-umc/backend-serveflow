package com.serveflow.model.stock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductRecipeTest {

    private static RecipeIngredient ingredient(UUID stockItemId) {
        return RecipeIngredient.create(stockItemId, "Farinha", new BigDecimal("0.5"), "kg", null);
    }

    private static ProductRecipe recipe() {
        UUID productId = UUID.randomUUID();
        return ProductRecipe.create(productId, "Hamburguer",
                List.of(ingredient(UUID.randomUUID())), "Modo de preparo", ProductType.FABRICATED);
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("cria receita com ingredientes válidos")
        void create_success() {
            UUID productId = UUID.randomUUID();
            List<RecipeIngredient> ings = List.of(ingredient(UUID.randomUUID()));

            ProductRecipe r = ProductRecipe.create(productId, "Pizza", ings, "Assar 20min", ProductType.FABRICATED);

            assertThat(r.getId()).isNotNull();
            assertThat(r.getProductId()).isEqualTo(productId);
            assertThat(r.getProductName()).isEqualTo("Pizza");
            assertThat(r.getIngredients()).hasSize(1);
            assertThat(r.getProductType()).isEqualTo(ProductType.FABRICATED);
        }

        @Test
        @DisplayName("usa FABRICATED quando productType é nulo")
        void create_defaultsToFabricated_whenTypeNull() {
            ProductRecipe r = ProductRecipe.create(UUID.randomUUID(), "X",
                    List.of(ingredient(UUID.randomUUID())), null, null);

            assertThat(r.getProductType()).isEqualTo(ProductType.FABRICATED);
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando ingredientes nulos")
        void create_throwsWhenIngredientsNull() {
            assertThatThrownBy(() -> ProductRecipe.create(UUID.randomUUID(), "X", null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ao menos um ingrediente");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando ingredientes vazios")
        void create_throwsWhenIngredientsEmpty() {
            assertThatThrownBy(() -> ProductRecipe.create(UUID.randomUUID(), "X", List.of(), null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ao menos um ingrediente");
        }

        @Test
        @DisplayName("lança NullPointerException quando productId é nulo")
        void create_throwsWhenProductIdNull() {
            assertThatThrownBy(() -> new ProductRecipe(UUID.randomUUID(), null, "X",
                    List.of(ingredient(UUID.randomUUID())), null, null, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando productName está em branco")
        void create_throwsWhenNameBlank() {
            assertThatThrownBy(() -> new ProductRecipe(UUID.randomUUID(), UUID.randomUUID(), "  ",
                    List.of(ingredient(UUID.randomUUID())), null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nome do produto");
        }
    }

    @Nested
    @DisplayName("replaceIngredients()")
    class ReplaceIngredients {

        @Test
        @DisplayName("substitui ingredientes com sucesso")
        void replaceIngredients_success() {
            ProductRecipe r = recipe();
            UUID newStockId = UUID.randomUUID();
            List<RecipeIngredient> newIngs = List.of(ingredient(newStockId));

            r.replaceIngredients(newIngs);

            assertThat(r.getIngredients()).hasSize(1);
            assertThat(r.getIngredients().get(0).getStockItemId()).isEqualTo(newStockId);
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando lista nula")
        void replaceIngredients_throwsWhenNull() {
            ProductRecipe r = recipe();
            assertThatThrownBy(() -> r.replaceIngredients(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ao menos um ingrediente");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando lista vazia")
        void replaceIngredients_throwsWhenEmpty() {
            ProductRecipe r = recipe();
            assertThatThrownBy(() -> r.replaceIngredients(List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("addIngredient()")
    class AddIngredient {

        @Test
        @DisplayName("adiciona ingrediente novo com sucesso")
        void addIngredient_success() {
            ProductRecipe r = recipe();
            int before = r.getIngredients().size();
            RecipeIngredient newIng = ingredient(UUID.randomUUID());

            r.addIngredient(newIng);

            assertThat(r.getIngredients()).hasSize(before + 1);
        }

        @Test
        @DisplayName("lança NullPointerException quando ingrediente é nulo")
        void addIngredient_throwsWhenNull() {
            ProductRecipe r = recipe();
            assertThatThrownBy(() -> r.addIngredient(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("lança IllegalStateException quando insumo já existe")
        void addIngredient_throwsWhenDuplicate() {
            UUID stockItemId = UUID.randomUUID();
            ProductRecipe r = ProductRecipe.create(UUID.randomUUID(), "X",
                    new ArrayList<>(List.of(ingredient(stockItemId))), null, null);

            assertThatThrownBy(() -> r.addIngredient(ingredient(stockItemId)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("já existe");
        }
    }

    @Nested
    @DisplayName("removeIngredient()")
    class RemoveIngredient {

        @Test
        @DisplayName("lança IllegalStateException quando há apenas um ingrediente")
        void removeIngredient_throwsWhenOnlyOne() {
            ProductRecipe r = recipe();
            UUID ingId = r.getIngredients().get(0).getId();

            assertThatThrownBy(() -> r.removeIngredient(ingId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ao menos um ingrediente");
        }

        @Test
        @DisplayName("remove ingrediente quando há mais de um")
        void removeIngredient_success() {
            UUID stock1 = UUID.randomUUID();
            UUID stock2 = UUID.randomUUID();
            ProductRecipe r = ProductRecipe.create(UUID.randomUUID(), "X",
                    new ArrayList<>(List.of(ingredient(stock1), ingredient(stock2))), null, null);
            UUID ingToRemoveId = r.getIngredients().get(0).getId();

            r.removeIngredient(ingToRemoveId);

            assertThat(r.getIngredients()).hasSize(1);
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando ingrediente não encontrado")
        void removeIngredient_throwsWhenNotFound() {
            UUID stock1 = UUID.randomUUID();
            UUID stock2 = UUID.randomUUID();
            ProductRecipe r = ProductRecipe.create(UUID.randomUUID(), "X",
                    new ArrayList<>(List.of(ingredient(stock1), ingredient(stock2))), null, null);

            assertThatThrownBy(() -> r.removeIngredient(UUID.randomUUID()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    @Nested
    @DisplayName("canPrepare()")
    class CanPrepare {

        @Test
        @DisplayName("retorna true quando estoque é suficiente")
        void canPrepare_trueWhenEnough() {
            UUID stockItemId = UUID.randomUUID();
            ProductRecipe r = ProductRecipe.create(UUID.randomUUID(), "X",
                    List.of(ingredient(stockItemId)), null, null);

            // ingredient has 0.5 per unit, need 1 unit = 0.5 required
            Map<UUID, BigDecimal> stock = Map.of(stockItemId, new BigDecimal("1.0"));
            assertThat(r.canPrepare(1, stock)).isTrue();
        }

        @Test
        @DisplayName("retorna false quando estoque é insuficiente")
        void canPrepare_falseWhenNotEnough() {
            UUID stockItemId = UUID.randomUUID();
            ProductRecipe r = ProductRecipe.create(UUID.randomUUID(), "X",
                    List.of(ingredient(stockItemId)), null, null);

            Map<UUID, BigDecimal> stock = Map.of(stockItemId, new BigDecimal("0.1"));
            assertThat(r.canPrepare(1, stock)).isFalse();
        }

        @Test
        @DisplayName("retorna false quando stockItemId não está no mapa")
        void canPrepare_falseWhenMissing() {
            UUID stockItemId = UUID.randomUUID();
            ProductRecipe r = ProductRecipe.create(UUID.randomUUID(), "X",
                    List.of(ingredient(stockItemId)), null, null);

            assertThat(r.canPrepare(1, Map.of())).isFalse();
        }
    }

    @Nested
    @DisplayName("updatePreparationMode() e updateProductType()")
    class UpdateMethods {

        @Test
        @DisplayName("updatePreparationMode atualiza corretamente")
        void updatePreparationMode_success() {
            ProductRecipe r = recipe();
            r.updatePreparationMode("Novo modo");
            assertThat(r.getPreparationMode()).isEqualTo("Novo modo");
        }

        @Test
        @DisplayName("updateProductType atualiza corretamente")
        void updateProductType_success() {
            ProductRecipe r = recipe();
            r.updateProductType(ProductType.COMMERCIAL);
            assertThat(r.getProductType()).isEqualTo(ProductType.COMMERCIAL);
        }
    }

    @Nested
    @DisplayName("equals() e hashCode()")
    class EqualsHashCode {

        @Test
        @DisplayName("duas receitas com mesmo id são iguais")
        void equals_sameId() {
            UUID id = UUID.randomUUID();
            ProductRecipe r1 = new ProductRecipe(id, UUID.randomUUID(), "A",
                    List.of(ingredient(UUID.randomUUID())), null, null, null);
            ProductRecipe r2 = new ProductRecipe(id, UUID.randomUUID(), "B",
                    List.of(ingredient(UUID.randomUUID())), null, null, null);

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("duas receitas com ids diferentes não são iguais")
        void notEquals_differentIds() {
            ProductRecipe r1 = recipe();
            ProductRecipe r2 = recipe();
            assertThat(r1).isNotEqualTo(r2);
        }
    }
}
