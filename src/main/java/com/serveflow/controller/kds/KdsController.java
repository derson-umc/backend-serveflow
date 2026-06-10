package com.serveflow.controller.kds;

import com.serveflow.dto.kds.response.KdsOrderOutput;
import com.serveflow.service.kds.KdsEventPublisher;
import com.serveflow.service.kds.KdsMapper;
import com.serveflow.model.order.OrderStatus;
import com.serveflow.service.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.serveflow.model.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Tag(name = "KDS - Cozinha", description = "Visão de pedidos em tempo real para a cozinha")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/kds")
public class KdsController {

    private static final List<OrderStatus> ACTIVE_STATUSES =
            List.of(OrderStatus.PENDENTE, OrderStatus.ENVIADO, OrderStatus.EM_PREPARO, OrderStatus.PRONTO);

    private final OrderService orderService;
    private final KdsEventPublisher publisher;
    private final KdsMapper mapper;

    public KdsController(OrderService orderService,
                         KdsEventPublisher publisher,
                         KdsMapper mapper) {
        this.orderService = orderService;
        this.publisher = publisher;
        this.mapper = mapper;
    }

    @Operation(summary = "Lista pedidos ativos para a cozinha")
    @GetMapping("/orders")
    public ResponseEntity<List<KdsOrderOutput>> openOrders(@AuthenticationPrincipal User user) {
        boolean isPrivileged = user.getRole().isAdmin() || user.getRole().isGerente();
        var orders = ACTIVE_STATUSES.stream()
                .flatMap(status -> orderService.findByStatus(status.name(), user.getUsername(), isPrivileged).stream())
                .map(mapper::toOutput)
                .sorted(Comparator.comparing(KdsOrderOutput::createdAt))
                .toList();

        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Confirma o recebimento do pedido (PENDENTE → ENVIADO)")
    @PatchMapping("/orders/{id}/confirm")
    public ResponseEntity<KdsOrderOutput> confirm(@PathVariable UUID id) {
        var output = mapper.toOutput(orderService.confirm(id));
        return ResponseEntity.ok(output);
    }

    @Operation(summary = "Avança o pedido para EM PREPARO")
    @PatchMapping("/orders/{id}/prepare")
    public ResponseEntity<KdsOrderOutput> prepare(@PathVariable UUID id) {

        var output = mapper.toOutput(orderService.startPreparation(id));
        publisher.publishUpdate(output);

        return ResponseEntity.ok(output);
    }

    @Operation(summary = "Marca o pedido como PRONTO")
    @PatchMapping("/orders/{id}/ready")
    public ResponseEntity<KdsOrderOutput> ready(@PathVariable UUID id) {

        var output = mapper.toOutput(orderService.markReady(id));
        publisher.publishRemove(id, output.status());

        return ResponseEntity.ok(output);
    }

    @Operation(summary = "Finaliza o pedido")
    @PatchMapping("/orders/{id}/complete")
    public ResponseEntity<KdsOrderOutput> complete(@PathVariable UUID id) {

        var output = mapper.toOutput(orderService.complete(id));
        publisher.publishRemove(id, output.status());

        return ResponseEntity.ok(output);
    }
}