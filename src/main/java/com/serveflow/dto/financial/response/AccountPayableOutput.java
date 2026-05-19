package com.serveflow.dto.financial.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountPayableOutput(
        UUID id,
        String description,
        LocalDate dueDate,
        BigDecimal amount,
        String status,
        LocalDateTime paidAt,
        BigDecimal paidAmount,
        String category,
        String supplier,
        LocalDateTime createdAt
) {}
