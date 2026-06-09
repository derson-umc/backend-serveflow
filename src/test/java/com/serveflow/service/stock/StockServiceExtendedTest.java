package com.serveflow.service.stock;

import com.serveflow.dto.stock.request.ProductRecipeInput;
import com.serveflow.dto.stock.request.RecipeIngredientInput;
import com.serveflow.dto.stock.request.StockAdjustmentInput;
import com.serveflow.dto.stock.response.ProductRecipeOutput;
import com.serveflow.exception.stock.InsufficientStockException;
import com.serveflow.model.order.ItemAdditional;
import com.serveflow.model.order.OrderItem;
import com.serveflow.model.order.OrderItemStatus;
import com.serveflow.model.stock.*;
import com.serveflow.repository.menu.MenuRepository;
import com.serveflow.repository.stock.productrecipe.ProductRecipeRepository;
import com.serveflow.repository.stock.stockalert.StockAlertRepository;
import com.serveflow.repository.stock.stockitem.StockItemRepository;
import com.serveflow.repository.stock.stockmovement.StockMovementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceExtendedTest {

    @Mock StockItemRepository stockItemRepository;
    @Mock ProductRecipeRepository recipeRepository;
    @Mock StockMovementRepository movementRepository;
    @Mock StockAlertRepository alertRepository;
    @Mock MenuRepository menuRepository;

    @InjectMocks StockService service;

    private StockItem stockItem(UUID id, BigDecimal quantity, BigDecimal minimum) {
        return new StockItem(id, "Farinha", "kg", quantity, minimum,
                "Secos", "Fornecedor X", StockItemStatus.ACTIVE,
                LocalDateTime.of(2026, 1, 1, 12, 0), null);
    }

    private ProductRecipe recipe(UUID recipeId, UUID productId, UUID stockItemId) {
        RecipeIngredient ingredient = RecipeIngredient.create(stockItemId, "Farinha", new BigDecimal("0.500"), "kg", null);
        return new ProductRecipe(recipeId, productId, "Hamburguer",
                List.of(ingredient), "Modo de preparo", ProductType.FABRICATED, null);
    }

    private OrderItem orderItem(UUID productId) {
        return new OrderItem(UUID.randomUUID(), productId, "Hamburguer", 2,
                new BigDecimal("25.00"), null, List.of(), OrderItemStatus.ENVIADO, null, null);
    }

    @Nested
    @DisplayName("validateRecipesForOrder()")
    class ValidateRecipesForOrder {

        @Test
        @DisplayName("não lança exceção quando todas as fichas existem")
        void validateRecipes_success() {
            UUID productId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            UUID stockItemId = UUID.randomUUID();
            when(recipeRepository.findByProductId(productId))
                    .thenReturn(Optional.of(recipe(recipeId, productId, stockItemId)));

            service.validateRecipesForOrder(List.of(orderItem(productId)));
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando produto não tem ficha")
        void validateRecipes_throwsWhenNoRecipe() {
            UUID productId = UUID.randomUUID();
            when(recipeRepository.findByProductId(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.validateRecipesForOrder(List.of(orderItem(productId))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ficha técnica");
        }
    }

    @Nested
    @DisplayName("validateStockForOrder()")
    class ValidateStockForOrder {

        @Test
        @DisplayName("não lança exceção quando estoque é suficiente")
        void validateStock_success() {
            UUID productId = UUID.randomUUID();
            UUID stockItemId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            ProductRecipe r = recipe(recipeId, productId, stockItemId);
            StockItem item = stockItem(stockItemId, new BigDecimal("10.0"), new BigDecimal("1.0"));

            when(recipeRepository.findByProductId(productId)).thenReturn(Optional.of(r));
            when(stockItemRepository.findById(stockItemId)).thenReturn(item);

            service.validateStockForOrder(List.of(orderItem(productId)));
        }

        @Test
        @DisplayName("lança InsufficientStockException quando estoque é insuficiente")
        void validateStock_throwsWhenInsufficient() {
            UUID productId = UUID.randomUUID();
            UUID stockItemId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            ProductRecipe r = recipe(recipeId, productId, stockItemId);
            // requires 0.5 * 2 = 1.0 kg, only 0.5 available
            StockItem item = stockItem(stockItemId, new BigDecimal("0.5"), new BigDecimal("0.1"));

            when(recipeRepository.findByProductId(productId)).thenReturn(Optional.of(r));
            when(stockItemRepository.findById(stockItemId)).thenReturn(item);

            assertThatThrownBy(() -> service.validateStockForOrder(List.of(orderItem(productId))))
                    .isInstanceOf(InsufficientStockException.class);
        }

        @Test
        @DisplayName("não valida quando produto não tem ficha técnica")
        void validateStock_skipsWhenNoRecipe() {
            UUID productId = UUID.randomUUID();
            when(recipeRepository.findByProductId(productId)).thenReturn(Optional.empty());

            // Should not throw — no recipe means no stock check
            service.validateStockForOrder(List.of(orderItem(productId)));
            verifyNoInteractions(stockItemRepository);
        }
    }

    @Nested
    @DisplayName("deductForOrder()")
    class DeductForOrder {

        @Test
        @DisplayName("deduz estoque e registra movimentos para cada ingrediente")
        void deductForOrder_success() {
            UUID productId = UUID.randomUUID();
            UUID stockItemId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            ProductRecipe r = recipe(recipeId, productId, stockItemId);
            StockItem item = stockItem(stockItemId, new BigDecimal("10.0"), new BigDecimal("1.0"));
            StockItem updated = stockItem(stockItemId, new BigDecimal("9.0"), new BigDecimal("1.0"));

            when(recipeRepository.findByProductId(productId)).thenReturn(Optional.of(r));
            when(stockItemRepository.findByIdForUpdate(stockItemId)).thenReturn(item);
            when(stockItemRepository.save(item)).thenReturn(updated);

            service.deductForOrder(orderId, List.of(orderItem(productId)));

            verify(stockItemRepository).save(item);
            verify(movementRepository).save(any(StockMovement.class));
        }

        @Test
        @DisplayName("dispara alerta quando estoque fica abaixo do mínimo após dedução")
        void deductForOrder_triggersAlert_whenBelowMinimum() {
            UUID productId = UUID.randomUUID();
            UUID stockItemId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            ProductRecipe r = recipe(recipeId, productId, stockItemId);
            StockItem item = stockItem(stockItemId, new BigDecimal("1.5"), new BigDecimal("2.0"));
            // After dediction: 1.5 - 1.0 = 0.5, below minimum 2.0
            StockItem updated = stockItem(stockItemId, new BigDecimal("0.5"), new BigDecimal("2.0"));

            when(recipeRepository.findByProductId(productId)).thenReturn(Optional.of(r));
            when(stockItemRepository.findByIdForUpdate(stockItemId)).thenReturn(item);
            when(stockItemRepository.save(item)).thenReturn(updated);
            when(alertRepository.existsActiveByStockItemId(stockItemId)).thenReturn(false);
            when(recipeRepository.findAllByStockItemId(stockItemId)).thenReturn(List.of());

            service.deductForOrder(orderId, List.of(orderItem(productId)));

            verify(alertRepository).save(any(StockAlert.class));
        }

        @Test
        @DisplayName("não cria alerta quando já existe alerta ativo")
        void deductForOrder_noAlert_whenAlertAlreadyExists() {
            UUID productId = UUID.randomUUID();
            UUID stockItemId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            ProductRecipe r = recipe(recipeId, productId, stockItemId);
            StockItem item = stockItem(stockItemId, new BigDecimal("1.5"), new BigDecimal("2.0"));
            StockItem updated = stockItem(stockItemId, new BigDecimal("0.5"), new BigDecimal("2.0"));

            when(recipeRepository.findByProductId(productId)).thenReturn(Optional.of(r));
            when(stockItemRepository.findByIdForUpdate(stockItemId)).thenReturn(item);
            when(stockItemRepository.save(item)).thenReturn(updated);
            when(alertRepository.existsActiveByStockItemId(stockItemId)).thenReturn(true);

            service.deductForOrder(orderId, List.of(orderItem(productId)));

            verify(alertRepository, never()).save(any(StockAlert.class));
        }
    }

    @Nested
    @DisplayName("recordLossForOrder()")
    class RecordLossForOrder {

        @Test
        @DisplayName("registra perda para cada ingrediente da receita")
        void recordLossForOrder_success() {
            UUID productId = UUID.randomUUID();
            UUID stockItemId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            ProductRecipe r = recipe(recipeId, productId, stockItemId);
            StockItem item = stockItem(stockItemId, new BigDecimal("5.0"), new BigDecimal("1.0"));

            when(recipeRepository.findByProductId(productId)).thenReturn(Optional.of(r));
            when(stockItemRepository.findById(stockItemId)).thenReturn(item);

            service.recordLossForOrder(orderId, List.of(orderItem(productId)), "Cliente cancelou");

            verify(movementRepository).save(any(StockMovement.class));
        }
    }

    @Nested
    @DisplayName("restoreForOrder()")
    class RestoreForOrder {

        @Test
        @DisplayName("restaura estoque para cada ingrediente da receita")
        void restoreForOrder_success() {
            UUID productId = UUID.randomUUID();
            UUID stockItemId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            ProductRecipe r = recipe(recipeId, productId, stockItemId);
            StockItem item = stockItem(stockItemId, new BigDecimal("5.0"), new BigDecimal("1.0"));
            StockItem updated = stockItem(stockItemId, new BigDecimal("6.0"), new BigDecimal("1.0"));

            when(recipeRepository.findByProductId(productId)).thenReturn(Optional.of(r));
            when(stockItemRepository.findByIdForUpdate(stockItemId)).thenReturn(item);
            when(stockItemRepository.save(item)).thenReturn(updated);

            service.restoreForOrder(orderId, List.of(orderItem(productId)));

            verify(movementRepository).save(any(StockMovement.class));
        }
    }

    @Nested
    @DisplayName("updateRecipe()")
    class UpdateRecipe {

        @Test
        @DisplayName("atualiza ficha técnica e retorna output")
        void updateRecipe_success() {
            UUID recipeId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            UUID stockItemId = UUID.randomUUID();
            ProductRecipe existing = recipe(recipeId, productId, stockItemId);
            ProductRecipe updated = recipe(recipeId, productId, stockItemId);

            when(recipeRepository.findById(recipeId)).thenReturn(existing);
            when(recipeRepository.save(existing)).thenReturn(updated);

            ProductRecipeInput input = new ProductRecipeInput(productId, "Hamburguer", "Novo modo", "FABRICATED",
                    List.of(new RecipeIngredientInput(stockItemId, "Farinha", new BigDecimal("0.5"), "kg", null)));

            ProductRecipeOutput result = service.updateRecipe(recipeId, input);

            assertThat(result.id()).isEqualTo(recipeId);
            verify(recipeRepository).save(existing);
        }
    }

    @Nested
    @DisplayName("recordAdjustment() — incremento de 1.0")
    class RecordAdjustmentIncrease {

        @Test
        @DisplayName("salva item quando quantidade aumenta em 1.0")
        void recordAdjustment_increase() {
            UUID id = UUID.randomUUID();
            StockItem item = stockItem(id, new BigDecimal("10.0"), new BigDecimal("2.0"));
            StockItem updated = stockItem(id, new BigDecimal("11.0"), new BigDecimal("2.0"));
            when(stockItemRepository.findByIdForUpdate(id)).thenReturn(item);
            when(stockItemRepository.save(item)).thenReturn(updated);

            StockAdjustmentInput input = new StockAdjustmentInput(new BigDecimal("11.0"), "Inventário");
            service.recordAdjustment(id, input);

            verify(stockItemRepository).save(item);
            verify(movementRepository).save(any(StockMovement.class));
        }
    }

    @Nested
    @DisplayName("findMovementsFiltered()")
    class FindMovementsFiltered {

        @Test
        @DisplayName("delega ao repositório quando ao menos um filtro é fornecido")
        void findMovementsFiltered_delegatesWhenHasFilter() {
            UUID stockItemId = UUID.randomUUID();
            when(movementRepository.findFiltered(eq(stockItemId), any(), any(), any(), eq(0), eq(50)))
                    .thenReturn(new com.serveflow.dto.stock.response.StockMovementsPageOutput(List.of(), 0, 50, 0L, 1));

            service.findMovementsFiltered(stockItemId, null, null, null, 0, 50);

            verify(movementRepository).findFiltered(eq(stockItemId), any(), any(), any(), eq(0), eq(50));
        }
    }

    @Nested
    @DisplayName("findAllRecipes() e findRecipeById()")
    class FindRecipes {

        @Test
        @DisplayName("findAllRecipes retorna lista mapeada")
        void findAllRecipes_returnsList() {
            UUID recipeId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            UUID stockItemId = UUID.randomUUID();
            when(recipeRepository.findAll()).thenReturn(List.of(recipe(recipeId, productId, stockItemId)));

            var result = service.findAllRecipes();

            assertThat(result).hasSize(1);
            verify(recipeRepository).findAll();
        }

        @Test
        @DisplayName("findRecipeById retorna output correto")
        void findRecipeById_returnsOutput() {
            UUID recipeId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            UUID stockItemId = UUID.randomUUID();
            when(recipeRepository.findById(recipeId)).thenReturn(recipe(recipeId, productId, stockItemId));

            var result = service.findRecipeById(recipeId);

            assertThat(result.id()).isEqualTo(recipeId);
        }
    }

    @Nested
    @DisplayName("findMovementsByStockItem() e findMovementsByOrder()")
    class FindMovements {

        @Test
        @DisplayName("findAllMovements retorna lista mapeada")
        void findAllMovements_returnsList() {
            when(movementRepository.findAll()).thenReturn(List.of());
            assertThat(service.findAllMovements()).isEmpty();
        }

        @Test
        @DisplayName("findMovementsByStockItem delega ao repositório")
        void findMovementsByStockItem_delegates() {
            UUID stockItemId = UUID.randomUUID();
            when(movementRepository.findByStockItemId(stockItemId)).thenReturn(List.of());

            service.findMovementsByStockItem(stockItemId);

            verify(movementRepository).findByStockItemId(stockItemId);
        }

        @Test
        @DisplayName("findMovementsByOrder delega ao repositório")
        void findMovementsByOrder_delegates() {
            UUID orderId = UUID.randomUUID();
            when(movementRepository.findByReferenceId(orderId)).thenReturn(List.of());

            service.findMovementsByOrder(orderId);

            verify(movementRepository).findByReferenceId(orderId);
        }
    }

    @Nested
    @DisplayName("resolveProductType")
    class ResolveProductType {

        @Test
        @DisplayName("COMMERCIAL é aceito como tipo de produto")
        void createRecipe_commercial_success() {
            UUID productId = UUID.randomUUID();
            UUID stockItemId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            ProductRecipe saved = recipe(recipeId, productId, stockItemId);
            when(recipeRepository.save(any(ProductRecipe.class))).thenReturn(saved);

            ProductRecipeInput input = new ProductRecipeInput(productId, "Produto", null, "COMMERCIAL",
                    List.of(new RecipeIngredientInput(stockItemId, "Farinha", new BigDecimal("0.5"), "kg", null)));

            ProductRecipeOutput result = service.createRecipe(input);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("lança IllegalArgumentException para tipo inválido")
        void createRecipe_invalidType_throws() {
            UUID productId = UUID.randomUUID();
            UUID stockItemId = UUID.randomUUID();

            ProductRecipeInput input = new ProductRecipeInput(productId, "Produto", null, "INVALID_TYPE",
                    List.of(new RecipeIngredientInput(stockItemId, "Farinha", new BigDecimal("0.5"), "kg", null)));

            assertThatThrownBy(() -> service.createRecipe(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Tipo de produto inválido");
        }
    }
}
