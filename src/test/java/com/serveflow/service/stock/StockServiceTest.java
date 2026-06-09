package com.serveflow.service.stock;

import com.serveflow.dto.stock.request.*;
import com.serveflow.dto.stock.response.ProductRecipeOutput;
import com.serveflow.dto.stock.response.StockAlertOutput;
import com.serveflow.dto.stock.response.StockItemOutput;
import com.serveflow.exception.stock.InsufficientStockException;
import com.serveflow.exception.stock.RecipeNotFoundException;
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
class StockServiceTest {

    @Mock
    StockItemRepository stockItemRepository;
    @Mock
    ProductRecipeRepository recipeRepository;
    @Mock
    StockMovementRepository movementRepository;
    @Mock
    StockAlertRepository alertRepository;
    @Mock
    MenuRepository menuRepository;

    @InjectMocks
    StockService service;

    private StockItem stockItem(UUID id, BigDecimal quantity, BigDecimal minimum) {
        return new StockItem(id, "Farinha", "kg", quantity, minimum,
                "Secos", "Fornecedor X",
                StockItemStatus.ACTIVE, LocalDateTime.of(2026, 1, 1, 12, 0), null);
    }

    private StockItem stockItem(UUID id, BigDecimal quantity, BigDecimal minimum, StockItemStatus status) {
        return new StockItem(id, "Farinha", "kg", quantity, minimum,
                "Secos", "Fornecedor X",
                status, LocalDateTime.of(2026, 1, 1, 12, 0), null);
    }

    private ProductRecipe recipe(UUID id, UUID productId) {
        RecipeIngredient ingredient = RecipeIngredient.create(
                UUID.randomUUID(), "Farinha", new BigDecimal("0.500"), "kg", null);
        return new ProductRecipe(id, productId, "Hamburguer",
                List.of(ingredient), "Modo de preparo", ProductType.FABRICATED, null);
    }

    private StockAlert alert(UUID id, UUID stockItemId, boolean resolved) {
        return new StockAlert(id, stockItemId, "Farinha",
                new BigDecimal("0.5"), new BigDecimal("2.0"),
                resolved, LocalDateTime.of(2026, 1, 1, 12, 0), null);
    }

    private StockItemInput itemInput() {
        return new StockItemInput("Farinha", "kg", new BigDecimal("10.0"),
                new BigDecimal("2.0"), "Secos", "Fornecedor X");
    }

    @Nested
    @DisplayName("createItem")
    class CreateItem {

        @Test
        @DisplayName("persiste item e registra movimento inicial quando quantidade é positiva")
        void createItem_persistsAndRecordsMovement_whenQuantityPositive() {
            UUID id = UUID.randomUUID();
            StockItem saved = stockItem(id, new BigDecimal("10.0"), new BigDecimal("2.0"));
            when(stockItemRepository.save(any(StockItem.class))).thenReturn(saved);

            StockItemOutput result = service.createItem(itemInput());

            assertThat(result.id()).isEqualTo(id);
            assertThat(result.name()).isEqualTo("Farinha");
            assertThat(result.currentQuantity()).isEqualByComparingTo("10.0");
            verify(stockItemRepository).save(any(StockItem.class));
            verify(movementRepository).save(any(StockMovement.class));
        }

        @Test
        @DisplayName("persiste item sem registrar movimento quando quantidade é zero")
        void createItem_persistsWithoutMovement_whenQuantityIsZero() {
            UUID id = UUID.randomUUID();
            StockItemInput input = new StockItemInput("Farinha", "kg", BigDecimal.ZERO,
                    new BigDecimal("2.0"), "Secos", null);
            StockItem saved = new StockItem(id, "Farinha", "kg", BigDecimal.ZERO,
                    new BigDecimal("2.0"), "Secos", null,
                    StockItemStatus.ACTIVE, LocalDateTime.of(2026, 1, 1, 12, 0), null);
            when(stockItemRepository.save(any(StockItem.class))).thenReturn(saved);

            service.createItem(input);

            verify(movementRepository, never()).save(any(StockMovement.class));
        }
    }

    @Nested
    @DisplayName("updateItem")
    class UpdateItem {

        @Test
        @DisplayName("atualiza detalhes e retorna output com id correto")
        void updateItem_updatesAndReturnsOutput() {
            UUID id = UUID.randomUUID();
            StockItem existing = stockItem(id, new BigDecimal("10.0"), new BigDecimal("2.0"));
            StockItem updated = stockItem(id, new BigDecimal("10.0"), new BigDecimal("3.0"));
            when(stockItemRepository.findById(id)).thenReturn(existing);
            when(stockItemRepository.save(existing)).thenReturn(updated);

            StockItemOutput result = service.updateItem(id, itemInput());

            assertThat(result.id()).isEqualTo(id);
            verify(stockItemRepository).findById(id);
            verify(stockItemRepository).save(existing);
        }
    }

    @Nested
    @DisplayName("findItemById")
    class FindItemById {

        @Test
        @DisplayName("retorna output correto quando item existe")
        void findItemById_returnsOutput_whenFound() {
            UUID id = UUID.randomUUID();
            StockItem item = stockItem(id, new BigDecimal("10.0"), new BigDecimal("2.0"));
            when(stockItemRepository.findById(id)).thenReturn(item);

            StockItemOutput result = service.findItemById(id);

            assertThat(result.id()).isEqualTo(id);
            assertThat(result.name()).isEqualTo("Farinha");
            verify(stockItemRepository).findById(id);
        }
    }

    @Nested
    @DisplayName("findAllItems e findActiveItems")
    class FindItems {

        @Test
        @DisplayName("findAllItems retorna lista mapeada com todos os items")
        void findAllItems_returnsMappedList() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            when(stockItemRepository.findAll()).thenReturn(List.of(
                    stockItem(id1, new BigDecimal("10.0"), new BigDecimal("2.0")),
                    stockItem(id2, new BigDecimal("5.0"), new BigDecimal("1.0"))));

            List<StockItemOutput> result = service.findAllItems();

            assertThat(result).hasSize(2);
            verify(stockItemRepository).findAll();
        }

        @Test
        @DisplayName("findAllItems retorna lista vazia quando não há items")
        void findAllItems_returnsEmptyList_whenNone() {
            when(stockItemRepository.findAll()).thenReturn(List.of());

            List<StockItemOutput> result = service.findAllItems();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("findActiveItems delega ao repositório com status ACTIVE")
        void findActiveItems_delegatesWithActiveStatus() {
            UUID id = UUID.randomUUID();
            when(stockItemRepository.findByStatus(StockItemStatus.ACTIVE))
                    .thenReturn(List.of(stockItem(id, new BigDecimal("10.0"), new BigDecimal("2.0"))));

            List<StockItemOutput> result = service.findActiveItems();

            assertThat(result).hasSize(1);
            verify(stockItemRepository).findByStatus(StockItemStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("toggleStatus")
    class ToggleStatus {

        @Test
        @DisplayName("desativa item ativo e retorna status INACTIVE")
        void toggleStatus_deactivatesActiveItem() {
            UUID id = UUID.randomUUID();
            StockItem active = stockItem(id, new BigDecimal("10.0"), new BigDecimal("2.0"), StockItemStatus.ACTIVE);
            StockItem inactive = stockItem(id, new BigDecimal("10.0"), new BigDecimal("2.0"), StockItemStatus.INACTIVE);
            when(stockItemRepository.findById(id)).thenReturn(active);
            when(stockItemRepository.save(active)).thenReturn(inactive);

            StockItemOutput result = service.toggleStatus(id);

            assertThat(result.status()).isEqualTo("INACTIVE");
        }

        @Test
        @DisplayName("ativa item inativo e retorna status ACTIVE")
        void toggleStatus_activatesInactiveItem() {
            UUID id = UUID.randomUUID();
            StockItem inactive = stockItem(id, new BigDecimal("10.0"), new BigDecimal("2.0"), StockItemStatus.INACTIVE);
            StockItem active = stockItem(id, new BigDecimal("10.0"), new BigDecimal("2.0"), StockItemStatus.ACTIVE);
            when(stockItemRepository.findById(id)).thenReturn(inactive);
            when(stockItemRepository.save(inactive)).thenReturn(active);

            StockItemOutput result = service.toggleStatus(id);

            assertThat(result.status()).isEqualTo("ACTIVE");
        }
    }

    @Nested
    @DisplayName("addStock")
    class AddStock {

        @Test
        @DisplayName("adiciona estoque, registra movimento de entrada e retorna output atualizado")
        void addStock_addsAndRecordsMovement() {
            UUID id = UUID.randomUUID();
            StockItem item = stockItem(id, new BigDecimal("10.0"), new BigDecimal("2.0"));
            StockItem updated = stockItem(id, new BigDecimal("15.0"), new BigDecimal("2.0"));
            when(stockItemRepository.findByIdForUpdate(id)).thenReturn(item);
            when(stockItemRepository.save(item)).thenReturn(updated);

            StockEntryInput input = new StockEntryInput(new BigDecimal("5.0"), "Compra mensal", null);
            StockItemOutput result = service.addStock(id, input);

            assertThat(result.currentQuantity()).isEqualByComparingTo("15.0");
            verify(movementRepository).save(any(StockMovement.class));
        }
    }

    @Nested
    @DisplayName("recordLoss")
    class RecordLoss {

        @Test
        @DisplayName("registra perda e movimento quando estoque é suficiente")
        void recordLoss_deductsAndRecordsMovement_whenEnoughStock() {
            UUID id = UUID.randomUUID();
            StockItem item = stockItem(id, new BigDecimal("10.0"), new BigDecimal("2.0"));
            StockItem updated = stockItem(id, new BigDecimal("8.0"), new BigDecimal("2.0"));
            when(stockItemRepository.findByIdForUpdate(id)).thenReturn(item);
            when(stockItemRepository.save(item)).thenReturn(updated);

            StockLossInput input = new StockLossInput(new BigDecimal("2.0"), "Produto vencido");
            StockItemOutput result = service.recordLoss(id, input);

            assertThat(result.currentQuantity()).isEqualByComparingTo("8.0");
            verify(movementRepository).save(any(StockMovement.class));
        }

        @Test
        @DisplayName("propaga InsufficientStockException quando quantidade de perda excede estoque")
        void recordLoss_throwsInsufficientStock_whenNotEnough() {
            UUID id = UUID.randomUUID();
            StockItem item = stockItem(id, new BigDecimal("1.0"), new BigDecimal("2.0"));
            when(stockItemRepository.findByIdForUpdate(id)).thenReturn(item);

            StockLossInput input = new StockLossInput(new BigDecimal("5.0"), "Quebra");

            assertThatThrownBy(() -> service.recordLoss(id, input))
                    .isInstanceOf(InsufficientStockException.class);
        }
    }

    @Nested
    @DisplayName("recordAdjustment")
    class RecordAdjustment {

        @Test
        @DisplayName("registra ajuste de incremento de quantidade e movimento")
        void recordAdjustment_recordsIncreaseAndMovement() {
            UUID id = UUID.randomUUID();
            StockItem item = stockItem(id, new BigDecimal("10.0"), new BigDecimal("2.0"));
            StockItem updated = stockItem(id, new BigDecimal("15.0"), new BigDecimal("2.0"));
            when(stockItemRepository.findByIdForUpdate(id)).thenReturn(item);
            when(stockItemRepository.save(item)).thenReturn(updated);

            StockAdjustmentInput input = new StockAdjustmentInput(new BigDecimal("15.0"), "Inventário mensal");
            service.recordAdjustment(id, input);

            verify(stockItemRepository).save(item);
            verify(movementRepository).save(any(StockMovement.class));
        }

        @Test
        @DisplayName("registra ajuste de redução de quantidade e movimento")
        void recordAdjustment_recordsDecreaseAndMovement() {
            UUID id = UUID.randomUUID();
            StockItem item = stockItem(id, new BigDecimal("10.0"), new BigDecimal("2.0"));
            StockItem updated = stockItem(id, new BigDecimal("7.0"), new BigDecimal("2.0"));
            when(stockItemRepository.findByIdForUpdate(id)).thenReturn(item);
            when(stockItemRepository.save(item)).thenReturn(updated);

            StockAdjustmentInput input = new StockAdjustmentInput(new BigDecimal("7.0"), "Inventário mensal");
            service.recordAdjustment(id, input);

            verify(stockItemRepository).save(item);
            verify(movementRepository).save(any(StockMovement.class));
        }
    }

    @Nested
    @DisplayName("createRecipe")
    class CreateRecipe {

        @Test
        @DisplayName("persiste ficha técnica e retorna output com id correto")
        void createRecipe_persistsAndReturnsOutput() {
            UUID id = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            ProductRecipe saved = recipe(id, productId);
            when(recipeRepository.save(any(ProductRecipe.class))).thenReturn(saved);

            ProductRecipeInput input = new ProductRecipeInput(
                    productId, "Hamburguer", "Modo de preparo", "FABRICATED",
                    List.of(new RecipeIngredientInput(UUID.randomUUID(), "Farinha",
                            new BigDecimal("0.5"), "kg", null)));

            ProductRecipeOutput result = service.createRecipe(input);

            assertThat(result.id()).isEqualTo(id);
            assertThat(result.productId()).isEqualTo(productId);
            verify(recipeRepository).save(any(ProductRecipe.class));
        }
    }

    @Nested
    @DisplayName("findRecipeByProductId")
    class FindRecipeByProductId {

        @Test
        @DisplayName("retorna output quando ficha existe para o produto")
        void findRecipeByProductId_returnsOutput_whenFound() {
            UUID productId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            ProductRecipe saved = recipe(recipeId, productId);
            when(recipeRepository.findByProductId(productId)).thenReturn(Optional.of(saved));

            ProductRecipeOutput result = service.findRecipeByProductId(productId);

            assertThat(result.productId()).isEqualTo(productId);
        }

        @Test
        @DisplayName("propaga RecipeNotFoundException quando nenhuma ficha existe para o produto")
        void findRecipeByProductId_throwsRecipeNotFound_whenAbsent() {
            UUID productId = UUID.randomUUID();
            when(recipeRepository.findByProductId(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findRecipeByProductId(productId))
                    .isInstanceOf(RecipeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findMovementsFiltered")
    class FindMovementsFiltered {

        @Test
        @DisplayName("lança IllegalArgumentException quando nenhum filtro é informado")
        void findMovementsFiltered_throwsIllegalArgument_whenNoFilters() {
            assertThatThrownBy(() -> service.findMovementsFiltered(null, null, null, null, 0, 50))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("filtro");
        }
    }

    @Nested
    @DisplayName("findAllActiveAlerts")
    class FindAllActiveAlerts {

        @Test
        @DisplayName("retorna lista de alertas ativos mapeados")
        void findAllActiveAlerts_returnsMappedAlerts() {
            UUID alertId = UUID.randomUUID();
            UUID stockItemId = UUID.randomUUID();
            when(alertRepository.findAllActive())
                    .thenReturn(List.of(alert(alertId, stockItemId, false)));

            List<StockAlertOutput> result = service.findAllActiveAlerts();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(alertId);
            verify(alertRepository).findAllActive();
        }
    }

    @Nested
    @DisplayName("resolveAlert")
    class ResolveAlert {

        @Test
        @DisplayName("resolve alerta e retorna output com resolved = true")
        void resolveAlert_resolvesAndReturnsOutput() {
            UUID alertId = UUID.randomUUID();
            UUID stockItemId = UUID.randomUUID();
            StockAlert found = alert(alertId, stockItemId, false);
            when(alertRepository.findById(alertId)).thenReturn(found);
            when(alertRepository.save(any(StockAlert.class))).thenAnswer(inv -> inv.getArgument(0));

            StockAlertOutput result = service.resolveAlert(alertId);

            assertThat(result.resolved()).isTrue();
            verify(alertRepository).findById(alertId);
            verify(alertRepository).save(found);
        }
    }
}
