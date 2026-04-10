package com.serveflow.web.controller;

import com.serveflow.application.usecase.PlaceMenuOrderUseCase;
import com.serveflow.domain.model.menu.Menu;
import com.serveflow.domain.model.menu.MenuItem;
import com.serveflow.domain.model.order.OrderType;
import com.serveflow.domain.repository.MenuRepository;
import com.serveflow.domain.service.OrderService;
import com.serveflow.web.dto.menu.*;
import com.serveflow.web.dto.order.OrderResponseDTO;
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
    private final PlaceMenuOrderUseCase placeMenuOrderUseCase;
    private final OrderWebMapper orderWebMapper;

    public MenuController(MenuRepository menuRepository,
                          PlaceMenuOrderUseCase placeMenuOrderUseCase,
                          OrderWebMapper orderWebMapper) {
        this.menuRepository = menuRepository;
        this.placeMenuOrderUseCase = placeMenuOrderUseCase;
        this.orderWebMapper = orderWebMapper;
    }

    @PostMapping
    public ResponseEntity<MenuResponseDTO> create(@RequestBody @Valid CreateMenuRequestDTO request) {
        List<MenuItem> items = request.items().stream()
                .map(dto -> MenuItem.create(dto.productId(), dto.name(), dto.description(), dto.price()))
                .toList();

        Menu menu = Menu.create(request.name(), items);
        Menu saved = menuRepository.save(menu);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<List<MenuResponseDTO>> listAll() {
        List<MenuResponseDTO> menus = menuRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(menus);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(menuRepository.findById(id)));
    }

    @PostMapping("/{menuId}/orders")
    public ResponseEntity<OrderResponseDTO> placeOrder(
            @PathVariable UUID menuId,
            @RequestBody @Valid PlaceMenuOrderRequestDTO request) {

        var selections = request.selections().stream()
                .map(s -> new PlaceMenuOrderUseCase.MenuItemSelection(
                        s.menuItemId(), s.quantity(), s.observation()))
                .toList();

        var address = request.address() != null
                ? orderWebMapper.toAddressDomain(request.address())
                : null;

        var order = placeMenuOrderUseCase.execute(
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
    public ResponseEntity<MenuResponseDTO> unlock(@PathVariable UUID id) {
        Menu menu = menuRepository.findById(id);
        menu.unlock();
        Menu saved = menuRepository.save(menu);
        return ResponseEntity.ok(toResponse(saved));
    }

    private MenuResponseDTO toResponse(Menu menu) {
        var items = menu.getItems().stream()
                .map(item -> new MenuResponseDTO.MenuItemResponseDTO(
                        item.getId(), item.getProductId(), item.getName(),
                        item.getDescription(), item.getPrice(), item.isAvailable()))
                .toList();

        return new MenuResponseDTO(
                menu.getId(), menu.getName(), menu.getStatus().name(),
                menu.getActiveOrderId(), items, menu.getCreatedAt());
    }
}
