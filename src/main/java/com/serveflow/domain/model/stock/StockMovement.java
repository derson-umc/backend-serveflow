package com.serveflow.domain.model.stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class StockMovement {

    private final UUID id;
    private final UUID stockItemId;
    private final MovementType type;
    private final BigDecimal quantity;
    private final String reason;
    private final UUID referenceId;
    private final LocalDateTime createdAt;

    public StockMovement(UUID id, UUID stockItemId, MovementType type,
                         BigDecimal quantity, String reason,
                         UUID referenceId, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "ID da movimentação e obrigatória.");
        this.stockItemId = Objects.requireNonNull(stockItemId, "ID do insumo e obrigatório.");
        this.type = Objects.requireNonNull(type, "Tipo de movimentação e obrigatório.");
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        this.quantity = quantity;
        this.reason = reason;
        this.referenceId = referenceId;
        this.createdAt = Objects.requireNonNull(createdAt, "Data de criação e obrigatório.");
    }

    public static StockMovement createEntry(UUID stockItemId, BigDecimal quantity,
                                            String reason, UUID referenceId) {
        return new StockMovement(
                UUID.randomUUID(), stockItemId, MovementType.ENTRY,
                quantity, reason, referenceId, LocalDateTime.now()
        );
    }

    public static StockMovement createExit(UUID stockItemId, BigDecimal quantity,
                                           String reason, UUID referenceId) {
        return new StockMovement(
                UUID.randomUUID(), stockItemId, MovementType.EXIT,
                quantity, reason, referenceId, LocalDateTime.now()
        );
    }

    public boolean isEntry() {
        return type == MovementType.ENTRY;
    }

    public boolean isExit() {
        return type == MovementType.EXIT;
    }

    public UUID getId() {
        return id;
    }

    public UUID getStockItemId() {
        return stockItemId;
    }

    public MovementType getType() {
        return type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getReason() {
        return reason;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockMovement other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
