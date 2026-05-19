package com.serveflow.model.stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class StockAlert {

    private final UUID id;
    private final UUID stockItemId;
    private final String stockItemName;
    private final BigDecimal currentQuantity;
    private final BigDecimal minimumQuantity;
    private boolean resolved;
    private final LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public StockAlert(UUID id, UUID stockItemId, String stockItemName,
                      BigDecimal currentQuantity, BigDecimal minimumQuantity,
                      boolean resolved, LocalDateTime createdAt, LocalDateTime resolvedAt) {
        this.id = Objects.requireNonNull(id);
        this.stockItemId = Objects.requireNonNull(stockItemId);
        this.stockItemName = Objects.requireNonNull(stockItemName);
        this.currentQuantity = Objects.requireNonNull(currentQuantity);
        this.minimumQuantity = Objects.requireNonNull(minimumQuantity);
        this.resolved = resolved;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.resolvedAt = resolvedAt;
    }

    public static StockAlert create(UUID stockItemId, String stockItemName,
                                    BigDecimal currentQuantity, BigDecimal minimumQuantity) {
        return new StockAlert(UUID.randomUUID(), stockItemId, stockItemName,
                currentQuantity, minimumQuantity, false, LocalDateTime.now(), null);
    }

    public void resolve() {
        if (resolved) throw new IllegalStateException("Alerta já resolvido.");
        this.resolved = true;
        this.resolvedAt = LocalDateTime.now();
    }

    public UUID getId()                    { return id; }
    public UUID getStockItemId()           { return stockItemId; }
    public String getStockItemName()       { return stockItemName; }
    public BigDecimal getCurrentQuantity() { return currentQuantity; }
    public BigDecimal getMinimumQuantity() { return minimumQuantity; }
    public boolean isResolved()            { return resolved; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
    public LocalDateTime getResolvedAt()   { return resolvedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockAlert other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
