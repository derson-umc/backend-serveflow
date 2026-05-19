package com.serveflow.dto.financial.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record FinancialAuditOutput(
        UUID id,
        String entityType,
        UUID entityId,
        String action,
        String performedBy,
        String description,
        LocalDateTime createdAt
) {}
