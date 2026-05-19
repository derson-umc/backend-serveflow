package com.serveflow.model.financial;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class FinancialAudit {

    private final UUID id;
    private final String entityType;
    private final UUID entityId;
    private final String action;
    private final String performedBy;
    private final String description;
    private final LocalDateTime createdAt;

    public FinancialAudit(UUID id, String entityType, UUID entityId, String action,
                          String performedBy, String description, LocalDateTime createdAt) {
        this.id = id;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.performedBy = performedBy;
        this.description = description;
        this.createdAt = createdAt;
    }

    public static FinancialAudit register(String entityType, UUID entityId, String action,
                                          String performedBy, String description) {
        return new FinancialAudit(UUID.randomUUID(), entityType, entityId, action,
                performedBy, description, LocalDateTime.now());
    }
}
