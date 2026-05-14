package com.serveflow.controller.order;

import com.serveflow.dto.order.request.OrderInput;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.service.order.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderOutput> create(@Valid @RequestBody OrderInput request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<OrderOutput>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderOutput> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderOutput>> findByStatus(@PathVariable String status) {
        return ResponseEntity.ok(orderService.findByStatus(status));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<OrderOutput> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.confirm(id));
    }

    @PatchMapping("/{id}/prepare")
    public ResponseEntity<OrderOutput> prepare(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.startPreparation(id));
    }

    @PatchMapping("/{id}/ready")
    public ResponseEntity<OrderOutput> ready(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.markReady(id));
    }

    @PatchMapping("/{id}/send")
    public ResponseEntity<OrderOutput> send(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.sendForDelivery(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<OrderOutput> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.complete(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderOutput> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.cancel(id));
    }
}
