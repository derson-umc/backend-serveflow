package com.serveflow.dto.stock.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockAlertOutput(
        UUID id,
        UUID stockItemId,
        String stockItemName,
        BigDecimal currentQuantity,
        BigDecimal minimumQuantity,
        boolean resolved,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {}
