package com.serveflow.controller.cashier;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@Tag(name = "Caixa", description = "Fechamento de contas e fluxo de caixa")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/cashier")
public class CashierController {

    @Operation(summary = "Resumo do caixa do dia")
    @GetMapping("/summary")
    public ResponseEntity<CashierSummaryOutput> summary() {
        return ResponseEntity.ok(new CashierSummaryOutput(
                LocalDate.now(),
                new BigDecimal("0.00"),
                0,
                new BigDecimal("0.00")
        ));
    }

    public record CashierSummaryOutput(
            LocalDate date,
            BigDecimal total,
            int closedTables,
            BigDecimal averageTicket
    ) {}
}
