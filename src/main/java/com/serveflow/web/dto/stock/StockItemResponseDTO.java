package com.serveflow.web.dto.stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockItemResponseDTO(
    UUID id,
    String name,
    String unit,
    BigDecimal currentQuantity,
    BigDecimal minimumQuantity,
    boolean belowMinimum,
    LocalDateTime createdAt
) {}
