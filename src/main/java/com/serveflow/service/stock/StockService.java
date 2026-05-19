package com.serveflow.service.stock;

import com.serveflow.dto.stock.request.*;
import com.serveflow.dto.stock.response.ProductRecipeOutput;
import com.serveflow.dto.stock.response.StockAlertOutput;
import com.serveflow.dto.stock.response.StockConsolidatedOutput;
import com.serveflow.dto.stock.response.StockItemOutput;
import com.serveflow.dto.stock.response.StockMovementOutput;
import com.serveflow.dto.stock.response.StockMovementsPageOutput;
import com.serveflow.exception.stock.RecipeNotFound;
import com.serveflow.exception.stock.InsufficientStock;
import com.serveflow.model.order.OrderItem;
import com.serveflow.model.stock.*;
import com.serveflow.repository.menu.MenuRepository;
import com.serveflow.repository.stock.ProductRecipe.ProductRecipeRepository;
import com.serveflow.repository.stock.StockAlert.StockAlertRepository;
import com.serveflow.repository.stock.StockItem.StockItemRepository;
import com.serveflow.repository.stock.StockMovement.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class StockService {

    private final StockItemRepository stockItemRepository;
    private final ProductRecipeRepository recipeRepository;
    private final StockMovementRepository movementRepository;
    private final StockAlertRepository alertRepository;
    private final MenuRepository menuRepository;

    public StockService(StockItemRepository stockItemRepository,
                        ProductRecipeRepository recipeRepository,
                        StockMovementRepository movementRepository,
                        StockAlertRepository alertRepository,
                        MenuRepository menuRepository) {
        this.stockItemRepository = stockItemRepository;
        this.recipeRepository = recipeRepository;
        this.movementRepository = movementRepository;
        this.alertRepository = alertRepository;
        this.menuRepository = menuRepository;
    }


    @Transactional
    public StockItemOutput createItem(StockItemInput request) {
        StockItem item = StockItem.create(
                request.name(), request.unit(),
                request.currentQuantity(), request.minimumQuantity(),
                request.category(), request.supplier(), request.averageCost());
        StockItem saved = stockItemRepository.save(item);

        if (request.currentQuantity().compareTo(BigDecimal.ZERO) > 0) {
            movementRepository.save(StockMovement.createEntry(
                    saved.getId(), saved.getName(),
                    request.currentQuantity(), BigDecimal.ZERO, request.currentQuantity(),
                    "Estoque inicial", null));
        }

        return toOutput(saved);
    }

    @Transactional
    public StockItemOutput updateItem(UUID id, StockItemInput request) {
        StockItem item = stockItemRepository.findById(id);
        item.updateDetails(
                request.name(), request.unit(), request.minimumQuantity(),
                request.category(), request.supplier(), request.averageCost());
        return toOutput(stockItemRepository.save(item));
    }

    public StockItemOutput findItemById(UUID id) {
        return toOutput(stockItemRepository.findById(id));
    }

    public List<StockItemOutput> findAllItems() {
        return stockItemRepository.findAll().stream().map(this::toOutput).toList();
    }

    public List<StockItemOutput> findActiveItems() {
        return stockItemRepository.findByStatus(StockItemStatus.ACTIVE).stream().map(this::toOutput).toList();
    }

    @Transactional
    public StockItemOutput toggleStatus(UUID id) {
        StockItem item = stockItemRepository.findById(id);
        if (item.isActive()) item.deactivate(); else item.activate();
        return toOutput(stockItemRepository.save(item));
    }

    @Transactional
    public StockItemOutput addStock(UUID id, StockEntryInput request) {
        StockItem item = stockItemRepository.findByIdForUpdate(id);
        BigDecimal before = item.getCurrentQuantity();
        item.add(request.quantity());
        StockItem saved = stockItemRepository.save(item);
        BigDecimal after = saved.getCurrentQuantity();

        String reason = request.reason() != null && !request.reason().isBlank()
                ? request.reason()
                : "Entrada de estoque";
        if (request.supplier() != null && !request.supplier().isBlank())
            reason += " | Fornecedor: " + request.supplier();
        if (request.unitCost() != null)
            reason += " | Custo unitário: R$ " + request.unitCost().toPlainString();

        movementRepository.save(StockMovement.createEntry(
                id, item.getName(), request.quantity(), before, after, reason, null));

        return toOutput(saved);
    }

    @Transactional
    public StockItemOutput recordLoss(UUID id, StockLossInput request) {
        StockItem item = stockItemRepository.findByIdForUpdate(id);
        if (!item.hasEnoughStock(request.quantity())) {
            throw new InsufficientStock("Quantidade de perda (" + request.quantity() + " " + item.getUnit()
                    + ") maior que o estoque disponível (" + item.getCurrentQuantity() + " " + item.getUnit() + ").");
        }
        BigDecimal before = item.getCurrentQuantity();
        item.deduct(request.quantity());
        StockItem saved = stockItemRepository.save(item);
        BigDecimal after = saved.getCurrentQuantity();

        movementRepository.save(StockMovement.createLoss(
                id, item.getName(), request.quantity(), before, after, request.reason()));

        if (saved.isBelowMinimum()) triggerLowStockActions(saved);

        return toOutput(saved);
    }

    @Transactional
    public StockItemOutput recordAdjustment(UUID id, StockAdjustmentInput request) {
        StockItem item = stockItemRepository.findByIdForUpdate(id);
        BigDecimal before = item.getCurrentQuantity();
        BigDecimal difference = request.newQuantity().subtract(before).abs();
        int cmp = request.newQuantity().compareTo(before);

        if (cmp > 0) {
            item.add(request.newQuantity().subtract(before));
        } else if (cmp < 0) {
            item.deduct(before.subtract(request.newQuantity()));
        }

        StockItem saved = cmp == 0 ? item : stockItemRepository.save(item);

        movementRepository.save(StockMovement.createAdjustment(
                id, item.getName(), difference,
                before, request.newQuantity(),
                "Ajuste de inventário: " + request.reason()));

        return toOutput(saved);
    }

    @Transactional
    public ProductRecipeOutput createRecipe(ProductRecipeInput request) {
        ProductType productType = resolveProductType(request.productType());
        List<RecipeIngredient> ingredients = toIngredients(request.ingredients());
        ProductRecipe recipe = ProductRecipe.create(
                request.productId(), request.productName(),
                ingredients, request.preparationMode(), productType);
        return toOutput(recipeRepository.save(recipe));
    }

    @Transactional
    public ProductRecipeOutput updateRecipe(UUID id, ProductRecipeInput request) {
        ProductRecipe existing = recipeRepository.findById(id);
        List<RecipeIngredient> ingredients = toIngredients(request.ingredients());
        existing.replaceIngredients(ingredients);
        existing.updatePreparationMode(request.preparationMode());
        if (request.productType() != null) {
            existing.updateProductType(resolveProductType(request.productType()));
        }
        return toOutput(recipeRepository.save(existing));
    }

    public ProductRecipeOutput findRecipeById(UUID id) {
        return toOutput(recipeRepository.findById(id));
    }

    public List<ProductRecipeOutput> findAllRecipes() {
        return recipeRepository.findAll().stream().map(this::toOutput).toList();
    }

    public ProductRecipeOutput findRecipeByProductId(UUID productId) {
        return toOutput(recipeRepository.findByProductId(productId)
                .orElseThrow(() -> new RecipeNotFound(productId)));
    }


    public List<StockConsolidatedOutput> findConsolidatedReport() {
        return movementRepository.findConsolidatedReport();
    }


    public List<StockMovementOutput> findAllMovements() {
        return movementRepository.findAll().stream().map(this::toMovementOutput).toList();
    }

    public List<StockMovementOutput> findMovementsByStockItem(UUID stockItemId) {
        return movementRepository.findByStockItemId(stockItemId).stream()
                .map(this::toMovementOutput).toList();
    }

    public List<StockMovementOutput> findMovementsByOrder(UUID orderId) {
        return movementRepository.findByReferenceId(orderId).stream()
                .map(this::toMovementOutput).toList();
    }

    public StockMovementsPageOutput findMovementsFiltered(UUID stockItemId, String type, LocalDate startDate, LocalDate endDate, int page, int size) {
        if (stockItemId == null && type == null && startDate == null && endDate == null) {
            throw new IllegalArgumentException("Informe pelo menos um filtro para listar movimentações.");
        }
        MovementType movementType = type != null ? MovementType.valueOf(type) : null;
        LocalDate start = startDate;
        LocalDate end   = endDate;
        return movementRepository.findFiltered(stockItemId, movementType, start, end, page, size);
    }

    public List<StockAlertOutput> findAllActiveAlerts() {
        return alertRepository.findAllActive().stream().map(this::toAlertOutput).toList();
    }

    @Transactional
    public StockAlertOutput resolveAlert(UUID alertId) {
        StockAlert alert = alertRepository.findById(alertId);
        alert.resolve();
        return toAlertOutput(alertRepository.save(alert));
    }

    @Transactional(readOnly = true)
    public void validateRecipesForOrder(List<OrderItem> items) {
        for (OrderItem item : items) {
            recipeRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Produto '" + item.getProductName() + "' não possui ficha técnica cadastrada. "
                            + "Cadastre os ingredientes no menu Fichas Técnicas antes de realizar pedidos."
                    ));
        }
    }

    @Transactional(readOnly = true)
    public void validateStockForOrder(List<OrderItem> items) {
        for (OrderItem item : items) {
            recipeRepository.findByProductId(item.getProductId()).ifPresent(recipe -> {
                for (RecipeIngredient ingredient : recipe.getIngredients()) {
                    BigDecimal required = ingredient.getRequiredQuantity(item.getQuantity());
                    StockItem stockItem = stockItemRepository.findById(ingredient.getStockItemId());
                    if (!stockItem.hasEnoughStock(required)) {
                        throw new InsufficientStock(
                                "Estoque insuficiente para '" + stockItem.getName() + "'. "
                                + "Disponível: " + stockItem.getCurrentQuantity() + " " + stockItem.getUnit()
                                + ", Requerido: " + required + " " + stockItem.getUnit() + "."
                        );
                    }
                }
            });
        }
    }

    @Transactional
    public void deductForOrder(UUID orderId, List<OrderItem> items) {
        for (OrderItem item : items) {
            recipeRepository.findByProductId(item.getProductId()).ifPresent(recipe -> {
                for (RecipeIngredient ingredient : recipe.getIngredients()) {
                    BigDecimal required = ingredient.getRequiredQuantity(item.getQuantity());

                    StockItem stockItem = stockItemRepository.findByIdForUpdate(ingredient.getStockItemId());
                    BigDecimal before = stockItem.getCurrentQuantity();
                    stockItem.deduct(required);
                    stockItemRepository.save(stockItem);
                    BigDecimal after = stockItem.getCurrentQuantity();

                    movementRepository.save(StockMovement.createOrderConsumption(
                            stockItem.getId(), stockItem.getName(), required, before, after,
                            "Insumo: " + stockItem.getName()
                                    + " | Qtd: " + required.toPlainString() + " " + stockItem.getUnit()
                                    + " | Produto: " + item.getProductName(),
                            orderId
                    ));

                    if (stockItem.isBelowMinimum()) {
                        triggerLowStockActions(stockItem);
                    }
                }
            });
        }
    }

    @Transactional
    public void restoreForOrder(UUID orderId, List<OrderItem> items) {
        for (OrderItem item : items) {
            recipeRepository.findByProductId(item.getProductId()).ifPresent(recipe -> {
                for (RecipeIngredient ingredient : recipe.getIngredients()) {
                    BigDecimal qty = ingredient.getRequiredQuantity(item.getQuantity());
                    StockItem stockItem = stockItemRepository.findByIdForUpdate(ingredient.getStockItemId());
                    BigDecimal before = stockItem.getCurrentQuantity();
                    stockItem.add(qty);
                    stockItemRepository.save(stockItem);
                    BigDecimal after = stockItem.getCurrentQuantity();
                    movementRepository.save(StockMovement.createEntry(
                            stockItem.getId(), stockItem.getName(), qty, before, after,
                            "Restauração automática | Cancelamento pedido | Produto: " + item.getProductName(),
                            orderId
                    ));
                }
            });
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void triggerLowStockActions(StockItem stockItem) {
        if (!alertRepository.existsActiveByStockItemId(stockItem.getId())) {
            alertRepository.save(StockAlert.create(
                    stockItem.getId(), stockItem.getName(),
                    stockItem.getCurrentQuantity(), stockItem.getMinimumQuantity()
            ));
        }
        recipeRepository.findAllByStockItemId(stockItem.getId()).forEach(recipe ->
                menuRepository.disableItemsByProductId(recipe.getProductId())
        );
    }

    private ProductType resolveProductType(String raw) {
        if (raw == null || raw.isBlank()) return ProductType.FABRICATED;
        try {
            return ProductType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de produto inválido: " + raw + ". Use FABRICATED ou COMMERCIAL.");
        }
    }

    private List<RecipeIngredient> toIngredients(List<RecipeIngredientInput> inputs) {
        return inputs.stream()
                .map(i -> RecipeIngredient.create(
                        i.stockItemId(), i.stockItemName(),
                        i.quantityPerUnit(), i.unit(), i.validity()))
                .toList();
    }

    // ── Output mappers ────────────────────────────────────────────────────────

    private StockItemOutput toOutput(StockItem item) {
        return new StockItemOutput(
                item.getId(), item.getName(), item.getUnit(),
                item.getCurrentQuantity(), item.getMinimumQuantity(),
                item.isBelowMinimum(),
                item.getCategory(), item.getSupplier(), item.getAverageCost(),
                item.getStatus().name(), item.getCreatedAt()
        );
    }

    private ProductRecipeOutput toOutput(ProductRecipe recipe) {
        return new ProductRecipeOutput(
                recipe.getId(), recipe.getProductId(), recipe.getProductName(),
                recipe.getPreparationMode(),
                recipe.getIngredients().stream().map(i ->
                        new ProductRecipeOutput.RecipeIngredientOutput(
                                i.getId(), i.getStockItemId(), i.getStockItemName(),
                                i.getQuantityPerUnit(), i.getUnit(), i.getValidity())
                ).toList(),
                recipe.getProductType().name()
        );
    }

    private StockAlertOutput toAlertOutput(StockAlert a) {
        return new StockAlertOutput(
                a.getId(), a.getStockItemId(), a.getStockItemName(),
                a.getCurrentQuantity(), a.getMinimumQuantity(),
                a.isResolved(), a.getCreatedAt(), a.getResolvedAt()
        );
    }

    private StockMovementOutput toMovementOutput(StockMovement m) {
        return new StockMovementOutput(
                m.getId(), m.getStockItemId(), m.getStockItemName(),
                m.getType().name(), m.getType().getDescription(),
                m.getQuantity(), m.getBalanceBefore(), m.getBalanceAfter(),
                m.getReason(), m.getReferenceId(), m.getCreatedAt()
        );
    }
}
