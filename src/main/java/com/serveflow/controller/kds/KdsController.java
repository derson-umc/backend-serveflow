package com.serveflow.controller.kds;

import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.service.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Tag(name = "KDS - Cozinha", description = "Visão de pedidos em tempo real para a cozinha")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/kds")
public class KdsController {

    private static final List<String> ACTIVE_STATUSES = List.of("CREATED", "CONFIRMED", "IN_PREPARATION");

    private final OrderService orderService;
    private final KdsEventPublisher publisher;

    public KdsController(OrderService orderService, KdsEventPublisher publisher) {
        this.orderService = orderService;
        this.publisher = publisher;
    }

    @Operation(summary = "Lista pedidos ativos para a cozinha (CONFIRMED e IN_PREPARATION)")
    @GetMapping("/orders")
    public ResponseEntity<List<KdsOrderOutput>> openOrders() {
        List<KdsOrderOutput> orders = ACTIVE_STATUSES.stream()
                .flatMap(status -> orderService.findByStatus(status).stream())
                .map(this::toKdsOutput)
                .sorted((a, b) -> a.createdAt().compareTo(b.createdAt()))
                .toList();
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Avança o pedido para EM PREPARO e notifica via WebSocket")
    @PatchMapping("/orders/{id}/prepare")
    public ResponseEntity<KdsOrderOutput> prepare(@PathVariable UUID id) {
        KdsOrderOutput output = toKdsOutput(orderService.startPreparation(id));
        publisher.publishUpdate(output);
        return ResponseEntity.ok(output);
    }

    @Operation(summary = "Marca o pedido como PRONTO e remove do KDS via WebSocket")
    @PatchMapping("/orders/{id}/ready")
    public ResponseEntity<KdsOrderOutput> ready(@PathVariable UUID id) {
        KdsOrderOutput output = toKdsOutput(orderService.markReady(id));
        publisher.publishRemove(id);
        return ResponseEntity.ok(output);
    }

    @Operation(summary = "Finaliza o pedido e remove do KDS via WebSocket")
    @PatchMapping("/orders/{id}/complete")
    public ResponseEntity<KdsOrderOutput> complete(@PathVariable UUID id) {
        KdsOrderOutput output = toKdsOutput(orderService.complete(id));
        publisher.publishRemove(id);
        return ResponseEntity.ok(output);
    }

    private KdsOrderOutput toKdsOutput(OrderOutput o) {
        List<KdsItemOutput> items = o.items().stream()
                .map(i -> new KdsItemOutput(
                        i.id(),
                        i.productName(),
                        i.quantity(),
                        i.observation(),
                        i.additionals().stream()
                                .map(a -> a.name() + (a.quantity() > 1 ? " x" + a.quantity() : ""))
                                .toList()
                ))
                .toList();

        return new KdsOrderOutput(o.id(), o.customerName(), o.type(), o.status(), o.createdAt(), items);
    }

    public record KdsOrderOutput(
            UUID id,
            String customerName,
            String type,
            String status,
            LocalDateTime createdAt,
            List<KdsItemOutput> items
    ) {}

    public record KdsItemOutput(
            UUID id,
            String productName,
            int quantity,
            String observation,
            List<String> additionals
    ) {}
}
