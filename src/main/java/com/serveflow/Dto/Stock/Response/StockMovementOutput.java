package com.serveflow.Dto.Stock.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockMovementOutput(
        UUID id,
        UUID stockItemId,
        String type,
        BigDecimal quantity,
        String reason,
        UUID referenceId,
        LocalDateTime createdAt
) {}
