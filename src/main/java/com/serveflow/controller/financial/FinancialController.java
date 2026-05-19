package com.serveflow.controller.financial;

import com.serveflow.dto.financial.request.AccountPayableInput;
import com.serveflow.dto.financial.request.AccountReceivableInput;
import com.serveflow.dto.financial.request.SettlementInput;
import com.serveflow.dto.financial.response.*;
import com.serveflow.service.financial.FinancialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/financial")
public class FinancialController {

    private final FinancialService financialService;

    public FinancialController(FinancialService financialService) {
        this.financialService = financialService;
    }

    @GetMapping("/cash-flow")
    public ResponseEntity<CashFlowOutput> cashFlow() {
        return ResponseEntity.ok(financialService.calculateCashFlow());
    }

    @PostMapping("/receivables")
    public ResponseEntity<AccountReceivableOutput> createReceivable(
            @Valid @RequestBody AccountReceivableInput request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financialService.createReceivable(request));
    }

    @GetMapping("/receivables")
    public ResponseEntity<List<AccountReceivableOutput>> listReceivables() {
        return ResponseEntity.ok(financialService.listReceivables());
    }

    @GetMapping("/receivables/{id}")
    public ResponseEntity<AccountReceivableOutput> findReceivable(@PathVariable UUID id) {
        return ResponseEntity.ok(financialService.findReceivable(id));
    }

    @PatchMapping("/receivables/{id}/settle")
    public ResponseEntity<AccountReceivableOutput> settleReceivable(
            @PathVariable UUID id,
            @Valid @RequestBody SettlementInput request) {
        return ResponseEntity.ok(financialService.settleReceivable(id, request));
    }

    @PatchMapping("/receivables/{id}/cancel")
    public ResponseEntity<AccountReceivableOutput> cancelReceivable(
            @PathVariable UUID id,
            @RequestParam String performedBy) {
        return ResponseEntity.ok(financialService.cancelReceivable(id, performedBy));
    }

    @PostMapping("/payables")
    public ResponseEntity<AccountPayableOutput> createPayable(
            @Valid @RequestBody AccountPayableInput request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financialService.createPayable(request));
    }

    @GetMapping("/payables")
    public ResponseEntity<List<AccountPayableOutput>> listPayables() {
        return ResponseEntity.ok(financialService.listPayables());
    }

    @GetMapping("/payables/{id}")
    public ResponseEntity<AccountPayableOutput> findPayable(@PathVariable UUID id) {
        return ResponseEntity.ok(financialService.findPayable(id));
    }

    @PatchMapping("/payables/{id}/settle")
    public ResponseEntity<AccountPayableOutput> settlePayable(
            @PathVariable UUID id,
            @Valid @RequestBody SettlementInput request) {
        return ResponseEntity.ok(financialService.settlePayable(id, request));
    }

    @PatchMapping("/payables/{id}/cancel")
    public ResponseEntity<AccountPayableOutput> cancelPayable(
            @PathVariable UUID id,
            @RequestParam String performedBy) {
        return ResponseEntity.ok(financialService.cancelPayable(id, performedBy));
    }

}
