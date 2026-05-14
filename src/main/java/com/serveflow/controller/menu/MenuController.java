package com.serveflow.controller.menu;

import com.serveflow.dto.menu.request.MenuInput;
import com.serveflow.dto.menu.request.PlaceOrderInput;
import com.serveflow.dto.menu.request.RemoveMenuItemInput;
import com.serveflow.dto.menu.request.UpdateAvailabilityInput;
import com.serveflow.dto.menu.response.MenuOutput;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.service.menu.MenuService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping
    public ResponseEntity<MenuOutput> create(@Valid @RequestBody MenuInput request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<MenuOutput>> findAll() {
        return ResponseEntity.ok(menuService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuOutput> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(menuService.findById(id));
    }

    @PostMapping("/{menuId}/orders")
    public ResponseEntity<OrderOutput> placeOrder(
            @PathVariable UUID menuId,
            @Valid @RequestBody PlaceOrderInput request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.placeOrder(menuId, request));
    }

    @PatchMapping("/{id}/unlock")
    public ResponseEntity<MenuOutput> unlock(@PathVariable UUID id) {
        return ResponseEntity.ok(menuService.unlock(id));
    }

    @PatchMapping("/{menuId}/items/{menuItemId}/availability")
    public ResponseEntity<MenuOutput> updateAvailability(
            @PathVariable UUID menuId,
            @PathVariable UUID menuItemId,
            @Valid @RequestBody UpdateAvailabilityInput request) {
        return ResponseEntity.ok(menuService.updateItemAvailability(menuId, menuItemId, request.available()));
    }

    @DeleteMapping("/{menuId}/items/{menuItemId}")
    public ResponseEntity<MenuOutput> removeItem(
            @PathVariable UUID menuId,
            @PathVariable UUID menuItemId,
            @Valid @RequestBody RemoveMenuItemInput request) {
        return ResponseEntity.ok(menuService.removeItem(menuId, menuItemId, request));
    }
}
