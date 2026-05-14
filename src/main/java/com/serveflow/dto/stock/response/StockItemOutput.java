package com.serveflow.dto.stock.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockItemOutput(
        UUID id,
        String name,
        String unit,
        BigDecimal currentQuantity,
        BigDecimal minimumQuantity,
        boolean belowMinimum,
        LocalDateTime createdAt
) {}
