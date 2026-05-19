package com.serveflow.dto.financial.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountReceivableOutput(
        UUID id,
        String description,
        LocalDate dueDate,
        BigDecimal amount,
        String status,
        LocalDateTime receivedAt,
        BigDecimal receivedAmount,
        String category,
        UUID sourceOrderId,
        LocalDateTime createdAt
) {}
