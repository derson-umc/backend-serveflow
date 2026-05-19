package com.serveflow.dto.financial.response;

import java.math.BigDecimal;

public record CashFlowOutput(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal balance,
        int pendingReceivablesCount,
        int pendingPayablesCount,
        BigDecimal pendingReceivablesTotal,
        BigDecimal pendingPayablesTotal
) {}
