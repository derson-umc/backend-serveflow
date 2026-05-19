package com.serveflow.dto.cashier.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CashMovementOutput(
        UUID id,
        UUID sessionId,
        String type,
        BigDecimal amount,
        String description,
        String category,
        String performedBy,
        LocalDateTime createdAt
) {}
