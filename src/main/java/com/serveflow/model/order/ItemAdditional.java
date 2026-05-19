package com.serveflow.model.order;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class ItemAdditional {

    private final UUID id;
    private final String name;
    private final int quantity;
    private final BigDecimal unitPrice;

    public ItemAdditional(UUID id, String name, int quantity, BigDecimal unitPrice) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Nome do adicional é obrigatório.");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantidade do adicional deve ser maior que zero.");
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Preço do adicional deve ser maior que zero.");

        this.id = Objects.requireNonNull(id, "ID do adicional é obrigatório.");
        this.name = name.strip();
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public ItemAdditional(String name, int quantity, BigDecimal unitPrice) {
        this(UUID.randomUUID(), name, quantity, unitPrice);
    }

    public BigDecimal getTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public UUID getId()             { return id; }
    public String getName()         { return name; }
    public int getQuantity()        { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemAdditional other)) return false;
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() { return Objects.hash(name); }
}
