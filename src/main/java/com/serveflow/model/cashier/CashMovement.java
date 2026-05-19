package com.serveflow.model.cashier;

import com.serveflow.model.financial.TransactionType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class CashMovement {

    private final UUID id;
    private final UUID sessionId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final String description;
    private final String category;
    private final String performedBy;
    private final LocalDateTime createdAt;

    public CashMovement(UUID id, UUID sessionId, TransactionType type, BigDecimal amount,
                        String description, String category, String performedBy, LocalDateTime createdAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.performedBy = performedBy;
        this.createdAt = createdAt;
    }
}
