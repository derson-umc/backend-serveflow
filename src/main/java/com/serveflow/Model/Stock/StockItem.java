package com.serveflow.Model.Stock;

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
    private final LocalDateTime createdAt;
    private Long version;

    public StockItem(UUID id, String name, String unit, BigDecimal currentQuantity,
                     BigDecimal minimumQuantity, LocalDateTime createdAt, Long version) {
        this.id = Objects.requireNonNull(id, "ID do insumo é obrigatório.");
        setName(name);
        setUnit(unit);
        this.currentQuantity = Objects.requireNonNull(currentQuantity, "Quantidade atual é obrigatória.");
        this.minimumQuantity = Objects.requireNonNull(minimumQuantity, "Quantidade mínima é obrigatória.");
        this.createdAt = Objects.requireNonNull(createdAt, "Data de criação é obrigatória.");
        this.version = version;
    }

    public static StockItem create(String name, String unit,
                                   BigDecimal currentQuantity, BigDecimal minimumQuantity) {
        return new StockItem(UUID.randomUUID(), name, unit,
                currentQuantity, minimumQuantity, LocalDateTime.now(), null);
    }

    public void deduct(BigDecimal quantity) {
        Objects.requireNonNull(quantity, "Quantidade a deduzir é obrigatória.");
        if (quantity.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Quantidade a deduzir deve ser maior que zero.");
        if (currentQuantity.compareTo(quantity) < 0)
            throw new IllegalStateException(
                    "Estoque insuficiente para '" + name + "'. "
                    + "Disponível: " + currentQuantity + " " + unit
                    + ", Requerido: " + quantity + " " + unit + ".");
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

    public void update(String name, String unit, BigDecimal minimumQuantity) {
        setName(name);
        setUnit(unit);
        this.minimumQuantity = Objects.requireNonNull(minimumQuantity);
    }

    public UUID getId()                   { return id; }
    public String getName()               { return name; }
    public String getUnit()               { return unit; }
    public BigDecimal getCurrentQuantity() { return currentQuantity; }
    public BigDecimal getMinimumQuantity() { return minimumQuantity; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public Long getVersion()              { return version; }

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
