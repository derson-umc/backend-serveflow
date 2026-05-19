package com.serveflow.dto.stock.response;

import java.math.BigDecimal;

public record StockConsolidatedOutput(
        String insumo,
        String unidade,
        BigDecimal totalEntradas,
        BigDecimal totalSaidas,
        BigDecimal saldoAtual
) {}
