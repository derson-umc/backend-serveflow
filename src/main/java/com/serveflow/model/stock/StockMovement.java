package com.serveflow.model.stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class StockMovement {

    private final UUID id;
    private final UUID stockItemId;
    private final String stockItemName;
    private final MovementType type;
    private final BigDecimal quantity;
    private final BigDecimal balanceBefore;
    private final BigDecimal balanceAfter;
    private final String reason;
    private final UUID referenceId;
    private final LocalDateTime createdAt;

    public StockMovement(UUID id, UUID stockItemId, String stockItemName, MovementType type,
                         BigDecimal quantity, BigDecimal balanceBefore, BigDecimal balanceAfter,
                         String reason, UUID referenceId, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "ID da movimentação é obrigatório.");
        this.stockItemId = Objects.requireNonNull(stockItemId, "ID do insumo é obrigatório.");
        this.stockItemName = stockItemName;
        this.type = Objects.requireNonNull(type, "Tipo de movimentação é obrigatório.");
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        this.quantity = quantity;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.reason = reason;
        this.referenceId = referenceId;
        this.createdAt = Objects.requireNonNull(createdAt, "Data de criação é obrigatória.");
    }

    public static StockMovement createEntry(UUID stockItemId, String stockItemName,
                                            BigDecimal quantity, BigDecimal balanceBefore,
                                            BigDecimal balanceAfter, String reason, UUID referenceId) {
        return new StockMovement(UUID.randomUUID(), stockItemId, stockItemName,
                MovementType.ENTRY, quantity, balanceBefore, balanceAfter,
                reason, referenceId, LocalDateTime.now());
    }

    public static StockMovement createExit(UUID stockItemId, String stockItemName,
                                           BigDecimal quantity, BigDecimal balanceBefore,
                                           BigDecimal balanceAfter, String reason, UUID referenceId) {
        return new StockMovement(UUID.randomUUID(), stockItemId, stockItemName,
                MovementType.EXIT, quantity, balanceBefore, balanceAfter,
                reason, referenceId, LocalDateTime.now());
    }

    public static StockMovement createOrderConsumption(UUID stockItemId, String stockItemName,
                                                       BigDecimal quantity, BigDecimal balanceBefore,
                                                       BigDecimal balanceAfter, String reason, UUID orderId) {
        return new StockMovement(UUID.randomUUID(), stockItemId, stockItemName,
                MovementType.ORDER_CONSUMPTION, quantity, balanceBefore, balanceAfter,
                reason, orderId, LocalDateTime.now());
    }

    public static StockMovement createLoss(UUID stockItemId, String stockItemName,
                                           BigDecimal quantity, BigDecimal balanceBefore,
                                           BigDecimal balanceAfter, String reason) {
        return new StockMovement(UUID.randomUUID(), stockItemId, stockItemName,
                MovementType.LOSS, quantity, balanceBefore, balanceAfter,
                reason, null, LocalDateTime.now());
    }

    public static StockMovement createAdjustment(UUID stockItemId, String stockItemName,
                                                 BigDecimal quantity, BigDecimal balanceBefore,
                                                 BigDecimal balanceAfter, String reason) {
        return new StockMovement(UUID.randomUUID(), stockItemId, stockItemName,
                MovementType.ADJUSTMENT, quantity, balanceBefore, balanceAfter,
                reason, null, LocalDateTime.now());
    }

    public boolean isEntry()  { return type == MovementType.ENTRY; }
    public boolean isExit()   { return type == MovementType.EXIT || type == MovementType.ORDER_CONSUMPTION || type == MovementType.LOSS; }

    public UUID getId()               { return id; }
    public UUID getStockItemId()      { return stockItemId; }
    public String getStockItemName()  { return stockItemName; }
    public MovementType getType()     { return type; }
    public BigDecimal getQuantity()   { return quantity; }
    public BigDecimal getBalanceBefore() { return balanceBefore; }
    public BigDecimal getBalanceAfter()  { return balanceAfter; }
    public String getReason()         { return reason; }
    public UUID getReferenceId()      { return referenceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockMovement other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
