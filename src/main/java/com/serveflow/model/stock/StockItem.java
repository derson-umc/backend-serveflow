package com.serveflow.model.stock;

import com.serveflow.exception.stock.InsufficientStock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class StockItem {

    private final UUID id;
    private String name;
    private String unit;
    private BigDecimal currentQuantity;
    private BigDecimal minimumQuantity;
    private String category;
    private String supplier;
    private BigDecimal averageCost;
    private StockItemStatus status;
    private final LocalDateTime createdAt;
    private Long version;

    public StockItem(UUID id, String name, String unit, BigDecimal currentQuantity,
                     BigDecimal minimumQuantity, String category, String supplier,
                     BigDecimal averageCost, StockItemStatus status,
                     LocalDateTime createdAt, Long version) {
        this.id = Objects.requireNonNull(id, "ID do insumo é obrigatório.");
        setName(name);
        setUnit(unit);
        this.currentQuantity = Objects.requireNonNull(currentQuantity, "Quantidade atual é obrigatória.");
        this.minimumQuantity = Objects.requireNonNull(minimumQuantity, "Quantidade mínima é obrigatória.");
        this.category = category;
        this.supplier = supplier;
        this.averageCost = averageCost;
        this.status = status != null ? status : StockItemStatus.ACTIVE;
        this.createdAt = Objects.requireNonNull(createdAt, "Data de criação é obrigatória.");
        this.version = version;
    }

    public static StockItem create(String name, String unit, BigDecimal currentQuantity,
                                   BigDecimal minimumQuantity, String category,
                                   String supplier, BigDecimal averageCost) {
        return new StockItem(UUID.randomUUID(), name, unit, currentQuantity, minimumQuantity,
                category, supplier, averageCost, StockItemStatus.ACTIVE, LocalDateTime.now(), null);
    }

    public void deduct(BigDecimal quantity) {
        Objects.requireNonNull(quantity, "Quantidade a deduzir é obrigatória.");
        if (quantity.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Quantidade a deduzir deve ser maior que zero.");
        if (currentQuantity.compareTo(quantity) < 0)
            throw new InsufficientStock("Estoque insuficiente para '" + name + "'. "
                    + "Disponível: " + currentQuantity.toPlainString() + " " + unit
                    + ", Requerido: " + quantity.toPlainString() + " " + unit + ".");
        this.currentQuantity = this.currentQuantity.subtract(quantity);
    }

    public void add(BigDecimal quantity) {
        Objects.requireNonNull(quantity, "Quantidade a adicionar é obrigatória.");
        if (quantity.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Quantidade a adicionar deve ser maior que zero.");
        this.currentQuantity = this.currentQuantity.add(quantity);
    }

    public boolean hasEnoughStock(BigDecimal required) {
        return currentQuantity.compareTo(required) >= 0;
    }

    public boolean isBelowMinimum() {
        return currentQuantity.compareTo(minimumQuantity) < 0;
    }

    public boolean isActive() {
        return status == StockItemStatus.ACTIVE;
    }

    public void updateDetails(String name, String unit, BigDecimal minimumQuantity,
                              String category, String supplier, BigDecimal averageCost) {
        setName(name);
        setUnit(unit);
        this.minimumQuantity = Objects.requireNonNull(minimumQuantity);
        this.category = category;
        this.supplier = supplier;
        this.averageCost = averageCost;
    }

    public void deactivate() {
        this.status = StockItemStatus.INACTIVE;
    }

    public void activate() {
        this.status = StockItemStatus.ACTIVE;
    }

    public UUID getId()                    { return id; }
    public String getName()                { return name; }
    public String getUnit()                { return unit; }
    public BigDecimal getCurrentQuantity() { return currentQuantity; }
    public BigDecimal getMinimumQuantity() { return minimumQuantity; }
    public String getCategory()            { return category; }
    public String getSupplier()            { return supplier; }
    public BigDecimal getAverageCost()     { return averageCost; }
    public StockItemStatus getStatus()     { return status; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
    public Long getVersion()               { return version; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockItem other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    private void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Nome do insumo é obrigatório.");
        this.name = name.strip();
    }

    private void setUnit(String unit) {
        if (unit == null || unit.isBlank())
            throw new IllegalArgumentException("Unidade de medida é obrigatória.");
        this.unit = unit.strip();
    }


}
