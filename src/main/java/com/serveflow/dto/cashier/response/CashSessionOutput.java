package com.serveflow.dto.cashier.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CashSessionOutput(
        UUID id,
        String status,
        BigDecimal initialBalance,
        String observation,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        String openedBy,
        String closedBy,
        String closingObservation
) {}
