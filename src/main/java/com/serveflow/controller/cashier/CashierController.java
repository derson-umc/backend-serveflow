package com.serveflow.controller.cashier;

import com.serveflow.dto.cashier.request.CashMovementInput;
import com.serveflow.dto.cashier.request.CloseSessionInput;
import com.serveflow.dto.cashier.request.OpenSessionInput;
import com.serveflow.dto.cashier.response.CashMovementOutput;
import com.serveflow.dto.cashier.response.CashSessionOutput;
import com.serveflow.service.cashier.CashierService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cashier")
public class CashierController {

    private final CashierService cashierService;

    public CashierController(CashierService cashierService) {
        this.cashierService = cashierService;
    }

    @GetMapping("/session/current")
    public ResponseEntity<CashSessionOutput> currentSession() {
        return cashierService.getCurrentSession()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/session/open")
    public ResponseEntity<CashSessionOutput> openSession(@Valid @RequestBody OpenSessionInput request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cashierService.openSession(request));
    }

    @PostMapping("/session/{id}/close")
    public ResponseEntity<CashSessionOutput> closeSession(
            @PathVariable UUID id,
            @Valid @RequestBody CloseSessionInput request) {
        return ResponseEntity.ok(cashierService.closeSession(id, request));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<CashSessionOutput>> listSessions() {
        return ResponseEntity.ok(cashierService.listSessions());
    }

    @PostMapping("/session/{sessionId}/movement")
    public ResponseEntity<CashMovementOutput> addMovement(
            @PathVariable UUID sessionId,
            @Valid @RequestBody CashMovementInput request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cashierService.addMovement(sessionId, request));
    }

    @GetMapping("/session/{sessionId}/movements")
    public ResponseEntity<List<CashMovementOutput>> listMovements(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(cashierService.listMovements(sessionId));
    }
}
