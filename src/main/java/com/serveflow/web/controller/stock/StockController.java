package com.serveflow.web.controller.stock;

import com.serveflow.domain.model.stock.*;
import com.serveflow.domain.repository.*;
import com.serveflow.web.dto.stock.request.ProductRecipeInput;
import com.serveflow.web.dto.stock.request.StockItemInput;
import com.serveflow.web.dto.stock.response.ProductRecipeOutPut;
import com.serveflow.web.dto.stock.response.StockItemOutPut;
import com.serveflow.web.dto.stock.response.StockMovementOutPut;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Valid
@RestController
@RequestMapping("/api/v1/stock")
public class StockController {

    private final StockItemRepository stockItemRepository;
    private final ProductRecipeRepository recipeRepository;
    private final StockMovementRepository movementRepository;

    public StockController(StockItemRepository stockItemRepository,
                           ProductRecipeRepository recipeRepository,
                           StockMovementRepository movementRepository) {
        this.stockItemRepository = stockItemRepository;
        this.recipeRepository = recipeRepository;
        this.movementRepository = movementRepository;
    }

    @PostMapping("/items")
    public ResponseEntity<StockItemOutPut> createItem(
            @RequestBody @Valid StockItemInput request) {
        StockItem item = StockItem.create(
                request.name(), request.unit(),
                request.currentQuantity(), request.minimumQuantity());
        StockItem saved = stockItemRepository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/items")
    public ResponseEntity<List<StockItemOutPut>> listItems() {
        List<StockItemOutPut> items = stockItemRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<StockItemOutPut> findItem(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(stockItemRepository.findById(id)));
    }

    @PostMapping("/items/{id}/entry")
    public ResponseEntity<StockItemOutPut> addStock(
            @PathVariable UUID id,
            @RequestParam java.math.BigDecimal quantity,
            @RequestParam(required = false) String reason) {
        StockItem item = stockItemRepository.findById(id);
        item.add(quantity);
        StockItem saved = stockItemRepository.save(item);

        StockMovement movement = StockMovement.createEntry(id, quantity, reason, null);
        movementRepository.save(movement);

        return ResponseEntity.ok(toResponse(saved));
    }

    @PostMapping("/recipes")
    public ResponseEntity<ProductRecipeOutPut> createRecipe(
            @RequestBody @Valid ProductRecipeInput request) {
        List<RecipeIngredient> ingredients = request.ingredients().stream()
                .map(dto -> RecipeIngredient.create(
                        dto.stockItemId(), dto.stockItemName(),
                        dto.quantityPerUnit(), dto.unit()))
                .toList();

        com.serveflow.domain.model.stock.ProductRecipe recipe = com.serveflow.domain.model.stock.ProductRecipe.create(
                request.productId(), request.productName(), ingredients);
        com.serveflow.domain.model.stock.ProductRecipe saved = recipeRepository.save(recipe);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/recipes")
    public ResponseEntity<List<ProductRecipeOutPut>> listRecipes() {
        List<ProductRecipeOutPut> recipes = recipeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/recipes/{id}")
    public ResponseEntity<ProductRecipeOutPut> findRecipe(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(recipeRepository.findById(id)));
    }

    @GetMapping("/recipes/product/{productId}")
    public ResponseEntity<ProductRecipeOutPut> findRecipeByProduct(@PathVariable UUID productId) {
        com.serveflow.domain.model.stock.ProductRecipe recipe = recipeRepository.findByProductId(productId)
                .orElseThrow(() -> new com.serveflow.domain.exception.RecipeNotFoundException(productId));
        return ResponseEntity.ok(toResponse(recipe));
    }

    @GetMapping("/movements/item/{stockItemId}")
    public ResponseEntity<List<StockMovementOutPut>> findMovementsByItem(@PathVariable UUID stockItemId) {
        List<StockMovementOutPut> movements = movementRepository.findByStockItemId(stockItemId).stream()
                .map(this::toMovementResponse)
                .toList();
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/movements/order/{orderId}")
    public ResponseEntity<List<StockMovementOutPut>> findMovementsByOrder(@PathVariable UUID orderId) {
        List<StockMovementOutPut> movements = movementRepository.findByReferenceId(orderId).stream()
                .map(this::toMovementResponse)
                .toList();
        return ResponseEntity.ok(movements);
    }

    private StockItemOutPut toResponse(StockItem item) {
        return new StockItemOutPut(
                item.getId(), item.getName(), item.getUnit(),
                item.getCurrentQuantity(), item.getMinimumQuantity(),
                item.isBelowMinimum(), item.getCreatedAt());
    }

    private ProductRecipeOutPut toResponse(com.serveflow.domain.model.stock.ProductRecipe recipe) {
        var ingredients = recipe.getIngredients().stream()
                .map(i -> new ProductRecipeOutPut.RecipeIngredientResponseDTO(
                        i.getId(), i.getStockItemId(), i.getStockItemName(),
                        i.getQuantityPerUnit(), i.getUnit()))
                .toList();
        return new ProductRecipeOutPut(
                recipe.getId(), recipe.getProductId(),
                recipe.getProductName(), ingredients);
    }

    private StockMovementOutPut toMovementResponse(StockMovement m) {
        return new StockMovementOutPut(
                m.getId(), m.getStockItemId(), m.getType().name(),
                m.getQuantity(), m.getReason(),
                m.getReferenceId(), m.getCreatedAt());
    }
}
