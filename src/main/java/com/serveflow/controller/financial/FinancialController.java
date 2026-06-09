package com.serveflow.controller.financial;

import com.serveflow.dto.financial.request.AccountPayableInput;
import com.serveflow.dto.financial.request.AccountReceivableInput;
import com.serveflow.dto.financial.request.SettlementInput;
import com.serveflow.dto.financial.response.*;
import com.serveflow.model.user.User;
import com.serveflow.repository.cashier.SpringCashMovementRepository;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.financial.FinancialService;
import com.serveflow.util.IpResolverUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/financial")
@RequiredArgsConstructor
public class FinancialController {

    private final FinancialService              financialService;
    private final AuditService                  auditService;
    private final SpringCashMovementRepository  movementRepository;

    public record PaymentSummary(String method, int ordersCount, BigDecimal total) {}
    public record CashierReport(LocalDate startDate, LocalDate endDate,
                                List<PaymentSummary> byPaymentMethod, BigDecimal grossTotal) {}

    @GetMapping("/cashier-report")
    public ResponseEntity<CashierReport> cashierReport(
            @RequestParam(defaultValue = "") String startDate,
            @RequestParam(defaultValue = "") String endDate) {

        LocalDate start = startDate.isBlank() ? LocalDate.now() : LocalDate.parse(startDate);
        LocalDate end   = endDate.isBlank()   ? LocalDate.now() : LocalDate.parse(endDate);

        List<Object[]> rows = movementRepository.reportByPaymentMethod(start, end);

        List<PaymentSummary> payments = rows.stream()
                .map(row -> new PaymentSummary(
                        row[0] != null ? row[0].toString() : "SEM_METODO",
                        row[1] != null ? ((Number) row[1]).intValue() : 0,
                        row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO))
                .toList();

        BigDecimal grossTotal = payments.stream()
                .map(PaymentSummary::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(new CashierReport(start, end, payments, grossTotal));
    }

    @GetMapping("/cash-flow")
    public ResponseEntity<CashFlowOutput> cashFlow() {
        return ResponseEntity.ok(financialService.calculateCashFlow());
    }

    @PostMapping("/receivables")
    public ResponseEntity<AccountReceivableOutput> createReceivable(
            @Valid @RequestBody AccountReceivableInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        AccountReceivableOutput output = financialService.createReceivable(request);
        auditService.logAction(user.getId(), "RECEIVABLE_CREATE", "AccountReceivable",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
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
            @Valid @RequestBody SettlementInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        AccountReceivableOutput output = financialService.settleReceivable(id, request);
        auditService.logAction(user.getId(), "RECEIVABLE_SETTLE", "AccountReceivable",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @PatchMapping("/receivables/{id}/cancel")
    public ResponseEntity<AccountReceivableOutput> cancelReceivable(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        AccountReceivableOutput output = financialService.cancelReceivable(id, user.getUsername());
        auditService.logAction(user.getId(), "RECEIVABLE_CANCEL", "AccountReceivable",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @PostMapping("/payables")
    public ResponseEntity<AccountPayableOutput> createPayable(
            @Valid @RequestBody AccountPayableInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        AccountPayableOutput output = financialService.createPayable(request);
        auditService.logAction(user.getId(), "PAYABLE_CREATE", "AccountPayable",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
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
            @Valid @RequestBody SettlementInput request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        AccountPayableOutput output = financialService.settlePayable(id, request);
        auditService.logAction(user.getId(), "PAYABLE_SETTLE", "AccountPayable",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }

    @PatchMapping("/payables/{id}/cancel")
    public ResponseEntity<AccountPayableOutput> cancelPayable(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {

        AccountPayableOutput output = financialService.cancelPayable(id, user.getUsername());
        auditService.logAction(user.getId(), "PAYABLE_CANCEL", "AccountPayable",
                null, IpResolverUtil.getClientIp(httpReq));
        return ResponseEntity.ok(output);
    }
}
