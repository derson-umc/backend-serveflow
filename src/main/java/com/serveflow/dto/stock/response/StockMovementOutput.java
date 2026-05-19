package com.serveflow.dto.stock.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockMovementOutput(
        UUID id,
        UUID stockItemId,
        String stockItemName,
        String type,
        String typeDescription,
        BigDecimal quantity,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        String reason,
        UUID referenceId,
        LocalDateTime createdAt
) {}
