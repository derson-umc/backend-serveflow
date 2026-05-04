package com.serveflow.Service.Stock;

import com.serveflow.Dto.Stock.Response.ProductRecipeOutput;
import com.serveflow.Dto.Stock.Request.StockItemInput;
import com.serveflow.Dto.Stock.Response.StockItemOutput;
import com.serveflow.Dto.Stock.Response.StockMovementOutput;
import com.serveflow.Dto.Stock.Request.ProductRecipeInput;
import com.serveflow.Model.Stock.ProductRecipe;
import com.serveflow.Model.Stock.RecipeIngredient;
import com.serveflow.Model.Stock.StockItem;
import com.serveflow.Model.Stock.StockMovement;
import com.serveflow.Repository.Stock.ProductRecipe.ProductRecipeRepository;
import com.serveflow.Repository.Stock.StockItem.StockItemRepository;
import com.serveflow.Repository.Stock.StockMovement.StockMovementRepository;
import com.serveflow.Model.Order.OrderItem;
import com.serveflow.Exception.Stock.InsufficientStock;
import com.serveflow.Exception.Stock.RecipeNotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class StockService {

    private final StockItemRepository stockItemRepository;
    private final ProductRecipeRepository recipeRepository;
    private final StockMovementRepository movementRepository;

    public StockService(StockItemRepository stockItemRepository,
                        ProductRecipeRepository recipeRepository,
                        StockMovementRepository movementRepository) {
        this.stockItemRepository = stockItemRepository;
        this.recipeRepository = recipeRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional
    public StockItemOutput createItem(StockItemInput request) {
        StockItem item = StockItem.create(
                request.name(), request.unit(),
                request.currentQuantity(), request.minimumQuantity());
        return toOutput(stockItemRepository.save(item));
    }

    public StockItemOutput findItemById(UUID id) {
        return toOutput(stockItemRepository.findById(id));
    }

    public List<StockItemOutput> findAllItems() {
        return stockItemRepository.findAll().stream().map(this::toOutput).toList();
    }

    @Transactional
    public StockItemOutput addStock(UUID id, BigDecimal quantity, String reason) {
        StockItem item = stockItemRepository.findById(id);
        item.add(quantity);
        StockItem saved = stockItemRepository.save(item);
        movementRepository.save(StockMovement.createEntry(id, quantity, reason, null));
        return toOutput(saved);
    }

    @Transactional
    public ProductRecipeOutput createRecipe(ProductRecipeInput request) {
        List<RecipeIngredient> ingredients = request.ingredients().stream()
                .map(i -> RecipeIngredient.create(i.stockItemId(), i.stockItemName(),
                        i.quantityPerUnit(), i.unit()))
                .toList();
        ProductRecipe recipe = ProductRecipe.create(
                request.productId(), request.productName(), ingredients);
        return toOutput(recipeRepository.save(recipe));
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

    public List<StockMovementOutput> findMovementsByStockItem(UUID stockItemId) {
        return movementRepository.findByStockItemId(stockItemId).stream()
                .map(this::toMovementOutput).toList();
    }

    public List<StockMovementOutput> findMovementsByOrder(UUID orderId) {
        return movementRepository.findByReferenceId(orderId).stream()
                .map(this::toMovementOutput).toList();
    }

    public void validateStockForOrder(List<OrderItem> items) {
        for (OrderItem item : items) {
            ProductRecipe recipe = recipeRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new RecipeNotFound(item.getProductId()));
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
        }
    }

    @Transactional
    public void restoreForOrder(UUID orderId, List<OrderItem> items) {
        for (OrderItem item : items) {
            recipeRepository.findByProductId(item.getProductId()).ifPresent(recipe -> {
                for (RecipeIngredient ingredient : recipe.getIngredients()) {
                    BigDecimal qty = ingredient.getRequiredQuantity(item.getQuantity());
                    StockItem stockItem = stockItemRepository.findByIdForUpdate(ingredient.getStockItemId());
                    stockItem.add(qty);
                    stockItemRepository.save(stockItem);
                    movementRepository.save(StockMovement.createEntry(
                            stockItem.getId(), qty,
                            "Restauração automática - Cancelamento pedido " + orderId
                                    + " - Produto: " + item.getProductName(),
                            orderId
                    ));
                }
            });
        }
    }

    @Transactional
    public void deductForOrder(UUID orderId, List<OrderItem> items) {
        for (OrderItem item : items) {
            ProductRecipe recipe = recipeRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new RecipeNotFound(item.getProductId()));

            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                BigDecimal required = ingredient.getRequiredQuantity(item.getQuantity());

                StockItem stockItem = stockItemRepository.findByIdForUpdate(ingredient.getStockItemId());
                stockItem.deduct(required);
                stockItemRepository.save(stockItem);

                movementRepository.save(StockMovement.createExit(
                        stockItem.getId(),
                        required,
                        "Baixa automática - Pedido " + orderId + " - Produto: " + item.getProductName(),
                        orderId
                ));
            }
        }
    }

    private StockItemOutput toOutput(StockItem item) {
        return new StockItemOutput(
                item.getId(), item.getName(), item.getUnit(),
                item.getCurrentQuantity(), item.getMinimumQuantity(),
                item.isBelowMinimum(), item.getCreatedAt()
        );
    }

    private ProductRecipeOutput toOutput(ProductRecipe recipe) {
        return new ProductRecipeOutput(
                recipe.getId(), recipe.getProductId(), recipe.getProductName(),
                recipe.getIngredients().stream().map(i ->
                        new ProductRecipeOutput.RecipeIngredientOutput(
                                i.getId(), i.getStockItemId(), i.getStockItemName(),
                                i.getQuantityPerUnit(), i.getUnit())
                ).toList()
        );
    }

    private StockMovementOutput toMovementOutput(StockMovement m) {
        return new StockMovementOutput(
                m.getId(), m.getStockItemId(), m.getType().name(),
                m.getQuantity(), m.getReason(), m.getReferenceId(), m.getCreatedAt()
        );
    }
}
