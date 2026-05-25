package com.serveflow.controller.stock;

import com.serveflow.dto.stock.request.*;
import com.serveflow.dto.stock.response.ProductRecipeOutput;
import com.serveflow.dto.stock.response.StockAlertOutput;
import com.serveflow.dto.stock.response.StockConsolidatedOutput;
import com.serveflow.dto.stock.response.StockItemOutput;
import com.serveflow.dto.stock.response.StockMovementOutput;
import com.serveflow.dto.stock.response.StockMovementsPageOutput;
import com.serveflow.model.user.User;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.stock.StockService;
import com.serveflow.util.IpResolverUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final AuditService auditService;

    @GetMapping("/report/consolidated")
    public ResponseEntity<List<StockConsolidatedOutput>> consolidatedReport() {
        return ResponseEntity.ok(stockService.findConsolidatedReport());
    }

    @PostMapping("/items")
    public ResponseEntity<StockItemOutput> createItem(
            @Valid @RequestBody StockItemInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        StockItemOutput output = stockService.createItem(request);
        auditService.logAction(user.getId(), "STOCK_ITEM_CREATE", "StockItem",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<StockItemOutput> updateItem(
            @PathVariable UUID id,
            @Valid @RequestBody StockItemInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        StockItemOutput output = stockService.updateItem(id, request);
        auditService.logAction(user.getId(), "STOCK_ITEM_UPDATE", "StockItem",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @GetMapping("/items")
    public ResponseEntity<List<StockItemOutput>> findAllItems() {
        return ResponseEntity.ok(stockService.findAllItems());
    }

    @GetMapping("/items/active")
    public ResponseEntity<List<StockItemOutput>> findActiveItems() {
        return ResponseEntity.ok(stockService.findActiveItems());
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<StockItemOutput> findItemById(@PathVariable UUID id) {
        return ResponseEntity.ok(stockService.findItemById(id));
    }

    @PatchMapping("/items/{id}/toggle-status")
    public ResponseEntity<StockItemOutput> toggleStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        StockItemOutput output = stockService.toggleStatus(id);
        auditService.logAction(user.getId(), "STOCK_ITEM_TOGGLE_STATUS", "StockItem",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @PostMapping("/items/{id}/entry")
    public ResponseEntity<StockItemOutput> addStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockEntryInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        StockItemOutput output = stockService.addStock(id, request);
        auditService.logAction(user.getId(), "STOCK_ENTRY", "StockItem",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @PostMapping("/items/{id}/loss")
    public ResponseEntity<StockItemOutput> recordLoss(
            @PathVariable UUID id,
            @Valid @RequestBody StockLossInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        StockItemOutput output = stockService.recordLoss(id, request);
        auditService.logAction(user.getId(), "STOCK_LOSS", "StockItem",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @PostMapping("/items/{id}/adjust")
    public ResponseEntity<StockItemOutput> recordAdjustment(
            @PathVariable UUID id,
            @Valid @RequestBody StockAdjustmentInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        StockItemOutput output = stockService.recordAdjustment(id, request);
        auditService.logAction(user.getId(), "STOCK_ADJUSTMENT", "StockItem",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @GetMapping("/movements")
    public ResponseEntity<List<StockMovementOutput>> findAllMovements() {
        return ResponseEntity.ok(stockService.findAllMovements());
    }

    @GetMapping("/movements/filter")
    public ResponseEntity<StockMovementsPageOutput> filterMovements(
            @RequestParam(required = false) UUID stockItemId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(stockService.findMovementsFiltered(stockItemId, type, startDate, endDate, page, size));
    }

    @GetMapping("/items/{id}/movements")
    public ResponseEntity<List<StockMovementOutput>> movementsByItem(@PathVariable UUID id) {
        return ResponseEntity.ok(stockService.findMovementsByStockItem(id));
    }

    @GetMapping("/movements/order/{orderId}")
    public ResponseEntity<List<StockMovementOutput>> movementsByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(stockService.findMovementsByOrder(orderId));
    }

    @PostMapping("/recipes")
    public ResponseEntity<ProductRecipeOutput> createRecipe(
            @Valid @RequestBody ProductRecipeInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        ProductRecipeOutput output = stockService.createRecipe(request);
        auditService.logAction(user.getId(), "RECIPE_CREATE", "ProductRecipe",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/recipes/{id}")
    public ResponseEntity<ProductRecipeOutput> updateRecipe(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRecipeInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        ProductRecipeOutput output = stockService.updateRecipe(id, request);
        auditService.logAction(user.getId(), "RECIPE_UPDATE", "ProductRecipe",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
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

    @GetMapping("/alerts")
    public ResponseEntity<List<StockAlertOutput>> findActiveAlerts() {
        return ResponseEntity.ok(stockService.findAllActiveAlerts());
    }

    @PatchMapping("/alerts/{id}/resolve")
    public ResponseEntity<StockAlertOutput> resolveAlert(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        StockAlertOutput output = stockService.resolveAlert(id);
        auditService.logAction(user.getId(), "STOCK_ALERT_RESOLVE", "StockAlert",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }
}
