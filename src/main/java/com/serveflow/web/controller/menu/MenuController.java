package com.serveflow.web.controller.menu;

import com.serveflow.application.usecase.StartOrder;
import com.serveflow.domain.model.menu.Menu;
import com.serveflow.domain.model.menu.MenuItem;
import com.serveflow.domain.model.order.OrderType;
import com.serveflow.domain.repository.MenuRepository;
import com.serveflow.web.dto.menu.request.MenuInput;
import com.serveflow.web.dto.menu.response.MenuOutPut;
import com.serveflow.web.dto.menu.response.MenuItemSelectionOutPut;
import com.serveflow.web.dto.order.response.OrderOutput;
import com.serveflow.web.mapper.OrderWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/menus")
public class MenuController {

    private final MenuRepository menuRepository;
    private final StartOrder startOrder;
    private final OrderWebMapper orderWebMapper;

    public MenuController(MenuRepository menuRepository,
                          StartOrder startOrder,
                          OrderWebMapper orderWebMapper) {
        this.menuRepository = menuRepository;
        this.startOrder = startOrder;
        this.orderWebMapper = orderWebMapper;
    }

    @PostMapping
    public ResponseEntity<MenuOutPut> create(@RequestBody @Valid MenuInput request) {
        List<MenuItem> items = request.items().stream()
                .map(dto -> MenuItem.create(dto.productId(), dto.name(), dto.description(), dto.price()))
                .toList();

        Menu menu = Menu.create(request.name(), items);
        Menu saved = menuRepository.save(menu);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<List<MenuOutPut>> listAll() {
        List<MenuOutPut> menus = menuRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(menus);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuOutPut> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(menuRepository.findById(id)));
    }

    @PostMapping("/{menuId}/orders")
    public ResponseEntity<OrderOutput> placeOrder(
            @PathVariable UUID menuId,
            @RequestBody @Valid MenuItemSelectionOutPut request) {

        var selections = request.selections().stream()
                .map(s -> new StartOrder.MenuItemSelection(
                        s.menuItemId(), s.quantity(), s.observation()))
                .toList();

        var address = request.address() != null
                ? orderWebMapper.toAddressDomain(request.address())
                : null;

        var order = startOrder.execute(
                menuId,
                request.customerName(),
                OrderType.valueOf(request.type().toUpperCase()),
                address,
                request.observation(),
                selections
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(orderWebMapper.toResponse(order));
    }

    @PatchMapping("/{id}/unlock")
    public ResponseEntity<MenuOutPut> unlock(@PathVariable UUID id) {
        Menu menu = menuRepository.findById(id);
        menu.unlock();
        Menu saved = menuRepository.save(menu);
        return ResponseEntity.ok(toResponse(saved));
    }

    private MenuOutPut toResponse(Menu menu) {
        var items = menu.getItems().stream()
                .map(item -> new MenuOutPut.MenuItemResponseDTO(
                        item.getId(), item.getProductId(), item.getName(),
                        item.getDescription(), item.getPrice(), item.isAvailable()))
                .toList();

        return new MenuOutPut(
                menu.getId(), menu.getName(), menu.getStatus().name(),
                menu.getActiveOrderId(), items, menu.getCreatedAt());
    }
}
