package com.serveflow.web.dto.stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockMovementResponseDTO(
    UUID id,
    UUID stockItemId,
    String type,
    BigDecimal quantity,
    String reason,
    UUID referenceId,
    LocalDateTime createdAt
) {}
