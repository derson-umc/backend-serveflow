package com.serveflow.controller.cashier;

import com.serveflow.dto.cashier.request.CashMovementInput;
import com.serveflow.dto.cashier.request.CloseSessionInput;
import com.serveflow.dto.cashier.request.OpenSessionInput;
import com.serveflow.dto.cashier.request.SettleOrderInput;
import com.serveflow.dto.cashier.response.CashMovementOutput;
import com.serveflow.dto.cashier.response.CashSessionOutput;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.model.user.User;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.cashier.CashierService;
import com.serveflow.service.order.OrderService;
import com.serveflow.util.IpResolverUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cashier")
@RequiredArgsConstructor
public class CashierController {

    private static final List<String> PENDING_PAYMENT_STATUSES = List.of("READY");

    private final CashierService        cashierService;
    private final AuditService          auditService;
    private final CashierEventPublisher eventPublisher;
    private final OrderService          orderService;

    @GetMapping("/session/current")
    public ResponseEntity<CashSessionOutput> currentSession() {
        return cashierService.getCurrentSession()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/session/open")
    public ResponseEntity<CashSessionOutput> openSession(
            @Valid @RequestBody OpenSessionInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        String username = user.getUsername();
        String ip       = IpResolverUtil.getClientIp(httpReq);
        CashSessionOutput output = cashierService.openSession(request, username);
        auditService.logAction(user.getId(), "CASH_SESSION_OPEN", "CashSession",
                null, ip);
        eventPublisher.publishSessionOpened(output);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PostMapping("/session/{id}/close")
    public ResponseEntity<CashSessionOutput> closeSession(
            @PathVariable UUID id,
            @Valid @RequestBody CloseSessionInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        String username = user.getUsername();
        String ip       = IpResolverUtil.getClientIp(httpReq);
        CashSessionOutput output = cashierService.closeSession(id, request, username);
        auditService.logAction(user.getId(), "CASH_SESSION_CLOSE", "CashSession",
                null, ip);
        eventPublisher.publishSessionClosed(output);
        return ResponseEntity.ok(output);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<CashSessionOutput>> listSessions() {
        return ResponseEntity.ok(cashierService.listSessions());
    }

    @PostMapping("/session/{sessionId}/movement")
    public ResponseEntity<CashMovementOutput> addMovement(
            @PathVariable UUID sessionId,
            @Valid @RequestBody CashMovementInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        String username = user.getUsername();
        String ip       = IpResolverUtil.getClientIp(httpReq);
        CashMovementOutput output = cashierService.addMovement(sessionId, request, username, "MANUAL");
        auditService.logAction(user.getId(), "CASH_MOVEMENT_ADD", "CashMovement",
                null, ip);
        eventPublisher.publishMovement(output);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @GetMapping("/session/{sessionId}/movements")
    public ResponseEntity<List<CashMovementOutput>> listMovements(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(cashierService.listMovements(sessionId));
    }

    @GetMapping("/orders/pending")
    public ResponseEntity<List<CashierOrderOutput>> pendingOrders() {
        List<CashierOrderOutput> orders = PENDING_PAYMENT_STATUSES.stream()
                .flatMap(status -> orderService.findByStatus(status).stream())
                .map(this::toCashierOutput)
                .sorted((a, b) -> a.createdAt().compareTo(b.createdAt()))
                .toList();
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<CashierOrderOutput> cancelOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        String ip = IpResolverUtil.getClientIp(httpReq);
        OrderOutput output = orderService.cancel(id);
        auditService.logAction(user.getId(), "ORDER_CANCEL_CASHIER", "Order", null, ip);
        return ResponseEntity.ok(toCashierOutput(output));
    }

    @PostMapping("/orders/{id}/settle")
    public ResponseEntity<CashierOrderOutput> settleOrder(
            @PathVariable UUID id,
            @Valid @RequestBody SettleOrderInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        String ip = IpResolverUtil.getClientIp(httpReq);
        OrderOutput output = orderService.settleFromCashier(id, request.paymentMethod());
        auditService.logAction(user.getId(), "ORDER_SETTLE", "Order", null, ip);
        return ResponseEntity.ok(toCashierOutput(output));
    }

    private CashierOrderOutput toCashierOutput(OrderOutput o) {
        return new CashierOrderOutput(
                o.id(), o.customerName(), o.type(), o.status(),
                o.paymentMethod(), o.totalValue(), o.createdAt(),
                o.items().stream()
                        .map(i -> new CashierOrderOutput.CashierItemOutput(
                                i.productName(), i.quantity(), i.total()))
                        .toList()
        );
    }

    public record CashierOrderOutput(
            UUID           id,
            String         customerName,
            String         type,
            String         status,
            String         paymentMethod,
            BigDecimal     totalValue,
            LocalDateTime  createdAt,
            List<CashierItemOutput> items
    ) {
        public record CashierItemOutput(
                String     productName,
                int        quantity,
                BigDecimal total
        ) {}
    }
}
