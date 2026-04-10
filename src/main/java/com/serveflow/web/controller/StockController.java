package com.serveflow.web.controller;

import com.serveflow.domain.model.stock.*;
import com.serveflow.domain.repository.*;
import com.serveflow.web.dto.stock.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    // === Stock Items ===

    @PostMapping("/items")
    public ResponseEntity<StockItemResponseDTO> createItem(
            @RequestBody @Valid CreateStockItemRequestDTO request) {
        StockItem item = StockItem.create(
                request.name(), request.unit(),
                request.currentQuantity(), request.minimumQuantity());
        StockItem saved = stockItemRepository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/items")
    public ResponseEntity<List<StockItemResponseDTO>> listItems() {
        List<StockItemResponseDTO> items = stockItemRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<StockItemResponseDTO> findItem(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(stockItemRepository.findById(id)));
    }

    @PostMapping("/items/{id}/entry")
    public ResponseEntity<StockItemResponseDTO> addStock(
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

    // === Recipes (Fichas Tecnicas) ===

    @PostMapping("/recipes")
    public ResponseEntity<ProductRecipeResponseDTO> createRecipe(
            @RequestBody @Valid CreateProductRecipeRequestDTO request) {
        List<RecipeIngredient> ingredients = request.ingredients().stream()
                .map(dto -> RecipeIngredient.create(
                        dto.stockItemId(), dto.stockItemName(),
                        dto.quantityPerUnit(), dto.unit()))
                .toList();

        ProductRecipe recipe = ProductRecipe.create(
                request.productId(), request.productName(), ingredients);
        ProductRecipe saved = recipeRepository.save(recipe);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/recipes")
    public ResponseEntity<List<ProductRecipeResponseDTO>> listRecipes() {
        List<ProductRecipeResponseDTO> recipes = recipeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/recipes/{id}")
    public ResponseEntity<ProductRecipeResponseDTO> findRecipe(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(recipeRepository.findById(id)));
    }

    @GetMapping("/recipes/product/{productId}")
    public ResponseEntity<ProductRecipeResponseDTO> findRecipeByProduct(@PathVariable UUID productId) {
        ProductRecipe recipe = recipeRepository.findByProductId(productId)
                .orElseThrow(() -> new com.serveflow.domain.exception.RecipeNotFoundException(productId));
        return ResponseEntity.ok(toResponse(recipe));
    }

    // === Movements ===

    @GetMapping("/movements/item/{stockItemId}")
    public ResponseEntity<List<StockMovementResponseDTO>> findMovementsByItem(@PathVariable UUID stockItemId) {
        List<StockMovementResponseDTO> movements = movementRepository.findByStockItemId(stockItemId).stream()
                .map(this::toMovementResponse)
                .toList();
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/movements/order/{orderId}")
    public ResponseEntity<List<StockMovementResponseDTO>> findMovementsByOrder(@PathVariable UUID orderId) {
        List<StockMovementResponseDTO> movements = movementRepository.findByReferenceId(orderId).stream()
                .map(this::toMovementResponse)
                .toList();
        return ResponseEntity.ok(movements);
    }

    // === Mappers (inline, seguindo padrao do projeto) ===

    private StockItemResponseDTO toResponse(StockItem item) {
        return new StockItemResponseDTO(
                item.getId(), item.getName(), item.getUnit(),
                item.getCurrentQuantity(), item.getMinimumQuantity(),
                item.isBelowMinimum(), item.getCreatedAt());
    }

    private ProductRecipeResponseDTO toResponse(ProductRecipe recipe) {
        var ingredients = recipe.getIngredients().stream()
                .map(i -> new ProductRecipeResponseDTO.RecipeIngredientResponseDTO(
                        i.getId(), i.getStockItemId(), i.getStockItemName(),
                        i.getQuantityPerUnit(), i.getUnit()))
                .toList();
        return new ProductRecipeResponseDTO(
                recipe.getId(), recipe.getProductId(),
                recipe.getProductName(), ingredients);
    }

    private StockMovementResponseDTO toMovementResponse(StockMovement m) {
        return new StockMovementResponseDTO(
                m.getId(), m.getStockItemId(), m.getType().name(),
                m.getQuantity(), m.getReason(),
                m.getReferenceId(), m.getCreatedAt());
    }
}
