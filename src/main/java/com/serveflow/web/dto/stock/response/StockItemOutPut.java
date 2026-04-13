package com.serveflow.web.dto.stock.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockItemOutPut(
    UUID id,
    String name,
    String unit,
    BigDecimal currentQuantity,
    BigDecimal minimumQuantity,
    boolean belowMinimum,
    LocalDateTime createdAt
) {}
