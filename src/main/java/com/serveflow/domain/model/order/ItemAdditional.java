package com.serveflow.domain.model.order;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class ItemAdditional {

    private UUID id;
    private final String name;
    private final int quantity;
    private final BigDecimal unitPrice;

    public ItemAdditional(String name, int quantity, BigDecimal unitPrice) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Nome do adicional e obrigatorio.");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantidade do adicional deve ser maior que zero.");
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Preco do adicional deve ser maior que zero.");

        this.id = UUID.randomUUID();
        this.name = name.strip();
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public ItemAdditional(UUID id, String name, int quantity, BigDecimal unitPrice) {
        this(name, quantity, unitPrice);
        this.id = id;
    }

    public BigDecimal getTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
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
