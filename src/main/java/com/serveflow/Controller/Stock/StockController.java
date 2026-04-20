package com.serveflow.Controller.Stock;

import com.serveflow.Dto.Stock.Response.ProductRecipeOutput;
import com.serveflow.Dto.Stock.Request.StockItemInput;
import com.serveflow.Dto.Stock.Response.StockItemOutput;
import com.serveflow.Dto.Stock.Response.StockMovementOutput;
import com.serveflow.Dto.Stock.Request.ProductRecipeInput;
import com.serveflow.Dto.Stock.Request.StockEntryInput;
import com.serveflow.Service.Stock.StockService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    // ── stock items ───────────────────────────────────────────────────────────

    @PostMapping("/items")
    public ResponseEntity<StockItemOutput> createItem(@Valid @RequestBody StockItemInput request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.createItem(request));
    }

    @GetMapping("/items")
    public ResponseEntity<List<StockItemOutput>> findAllItems() {
        return ResponseEntity.ok(stockService.findAllItems());
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<StockItemOutput> findItemById(@PathVariable UUID id) {
        return ResponseEntity.ok(stockService.findItemById(id));
    }

    @PatchMapping("/items/{id}/add")
    public ResponseEntity<StockItemOutput> addStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockEntryInput request) {
        return ResponseEntity.ok(stockService.addStock(id, request.quantity(), request.reason()));
    }

    // ── movements ─────────────────────────────────────────────────────────────

    @GetMapping("/items/{id}/movements")
    public ResponseEntity<List<StockMovementOutput>> movementsByItem(@PathVariable UUID id) {
        return ResponseEntity.ok(stockService.findMovementsByStockItem(id));
    }

    @GetMapping("/movements/order/{orderId}")
    public ResponseEntity<List<StockMovementOutput>> movementsByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(stockService.findMovementsByOrder(orderId));
    }

    // ── recipes ───────────────────────────────────────────────────────────────

    @PostMapping("/recipes")
    public ResponseEntity<ProductRecipeOutput> createRecipe(@Valid @RequestBody ProductRecipeInput request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.createRecipe(request));
    }

    @GetMapping("/recipes")
    public ResponseEntity<List<ProductRecipeOutput>> findAllRecipes() {
        return ResponseEntity.ok(stockService.findAllRecipes());
    }

    @GetMapping("/recipes/{id}")
    public ResponseEntity<ProductRecipeOutput> findRecipeById(@PathVariable UUID id) {
        return ResponseEntity.ok(stockService.findRecipeById(id));
    }

    @GetMapping("/recipes/product/{productId}")
    public ResponseEntity<ProductRecipeOutput> findRecipeByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(stockService.findRecipeByProductId(productId));
    }
}
