package com.serveflow.controller.order;

import com.serveflow.dto.order.request.CancelOrderRequest;
import com.serveflow.dto.order.request.OrderInput;
import com.serveflow.dto.order.request.OrderItemInput;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.model.user.User;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.order.OrderService;
import com.serveflow.util.IpResolverUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final AuditService auditService;

    @PostMapping
    public ResponseEntity<OrderOutput> create(
            @Valid @RequestBody OrderInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        OrderOutput output = orderService.create(request, user.getUsername());
        auditService.logAction(user.getId(), "ORDER_CREATE", "Order",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @GetMapping
    public ResponseEntity<List<OrderOutput>> findAll(@AuthenticationPrincipal User user) {
        boolean isPrivileged = user.getRole().isAdmin() || user.getRole().isGerente();
        return ResponseEntity.ok(orderService.findAll(user.getUsername(), isPrivileged));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderOutput> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderOutput>> findByStatus(
            @PathVariable String status,
            @AuthenticationPrincipal User user) {
        boolean isPrivileged = user.getRole().isAdmin() || user.getRole().isGerente();
        return ResponseEntity.ok(orderService.findByStatus(status, user.getUsername(), isPrivileged));
    }

    @PatchMapping("/{id}/request-payment")
    public ResponseEntity<OrderOutput> requestPayment(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        OrderOutput output = orderService.requestPayment(id);
        auditService.logAction(user.getId(), "ORDER_REQUEST_PAYMENT", "Order",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @PatchMapping("/{id}/items/{itemId}/cancel")
    public ResponseEntity<OrderOutput> cancelItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @RequestBody(required = false) CancelOrderRequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        String reason = request != null ? request.reason() : null;
        OrderOutput output = orderService.cancelItem(id, itemId, reason);
        auditService.logAction(user.getId(), "ORDER_ITEM_CANCEL", "Order",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @PostMapping("/{id}/items/add")
    public ResponseEntity<OrderOutput> addItems(
            @PathVariable UUID id,
            @Valid @RequestBody List<OrderItemInput> items,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        OrderOutput output = orderService.appendItems(id, items);
        auditService.logAction(user.getId(), "ORDER_ADD_ITEMS", "Order",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @PatchMapping("/{id}/items")
    public ResponseEntity<OrderOutput> updateItems(
            @PathVariable UUID id,
            @Valid @RequestBody List<OrderItemInput> items,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        OrderOutput output = orderService.updateItems(id, items);
        auditService.logAction(user.getId(), "ORDER_UPDATE_ITEMS", "Order",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<OrderOutput> confirm(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        OrderOutput output = orderService.confirm(id);
        auditService.logAction(user.getId(), "ORDER_CONFIRM", "Order",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @PatchMapping("/{id}/prepare")
    public ResponseEntity<OrderOutput> prepare(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        OrderOutput output = orderService.startPreparation(id);
        auditService.logAction(user.getId(), "ORDER_PREPARE", "Order",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @PatchMapping("/{id}/ready")
    public ResponseEntity<OrderOutput> ready(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        OrderOutput output = orderService.markReady(id);
        auditService.logAction(user.getId(), "ORDER_READY", "Order",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @PatchMapping("/{id}/send")
    public ResponseEntity<OrderOutput> send(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        OrderOutput output = orderService.sendForDelivery(id);
        auditService.logAction(user.getId(), "ORDER_SEND", "Order",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<OrderOutput> complete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        OrderOutput output = orderService.complete(id);
        auditService.logAction(user.getId(), "ORDER_COMPLETE", "Order",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderOutput> cancel(
            @PathVariable UUID id,
            @RequestBody(required = false) CancelOrderRequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        String reason = request != null ? request.reason() : null;
        OrderOutput output = orderService.cancel(id, reason, user.getUsername());
        auditService.logAction(user.getId(), "ORDER_CANCEL", "Order",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }
}
