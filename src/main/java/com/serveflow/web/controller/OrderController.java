package com.serveflow.web.controller;

import com.serveflow.domain.model.order.OrderStatus;
import com.serveflow.domain.service.OrderService;
import com.serveflow.web.dto.order.OrderRequestDTO;
import com.serveflow.web.dto.order.OrderResponseDTO;
import com.serveflow.web.mapper.OrderWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderWebMapper mapper;

    public OrderController(OrderService orderService, OrderWebMapper mapper) {
        this.orderService = orderService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@RequestBody @Valid OrderRequestDTO request) {
        var addressDto = request.address();
        var manualAddress = mapper.toAddressDomain(addressDto);
        var resolvedAddress = orderService.resolveAddress(
                addressDto != null ? addressDto.cep() : null,
                addressDto != null ? addressDto.number() : null,
                addressDto != null ? addressDto.complement() : null,
                manualAddress
        );
        var order = mapper.toDomain(request, resolvedAddress);
        var created = orderService.create(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> listAll() {
        return ResponseEntity.ok(mapper.toResponseList(orderService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(orderService.findById(id)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponseDTO>> findByStatus(@PathVariable String status) {
        var orderStatus = OrderStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(mapper.toResponseList(orderService.findByStatus(orderStatus)));
    }

    @PatchMapping("/{id}/prepare")
    public ResponseEntity<OrderResponseDTO> startPreparation(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(orderService.startPreparation(id)));
    }

    @PatchMapping("/{id}/ready")
    public ResponseEntity<OrderResponseDTO> markReady(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(orderService.markReady(id)));
    }

    @PatchMapping("/{id}/send")
    public ResponseEntity<OrderResponseDTO> sendForDelivery(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(orderService.sendForDelivery(id)));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<OrderResponseDTO> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(orderService.complete(id)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(orderService.cancel(id)));
    }
}
