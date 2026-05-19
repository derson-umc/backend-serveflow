package com.serveflow.model.stock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class RecipeIngredient {

    private final UUID id;
    private final UUID stockItemId;
    private final String stockItemName;
    private final BigDecimal quantityPerUnit;
    private final String unit;
    private final LocalDate validity;

    public RecipeIngredient(UUID id, UUID stockItemId, String stockItemName,
                            BigDecimal quantityPerUnit, String unit, LocalDate validity) {
        this.id = Objects.requireNonNull(id, "ID do ingrediente é obrigatório.");
        this.stockItemId = Objects.requireNonNull(stockItemId, "ID do insumo é obrigatório.");
        this.stockItemName = Objects.requireNonNull(stockItemName, "Nome do insumo é obrigatório.");
        if (quantityPerUnit == null || quantityPerUnit.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Quantidade por unidade deve ser maior que zero.");
        this.quantityPerUnit = quantityPerUnit;
        this.unit = Objects.requireNonNull(unit, "Unidade de medida é obrigatória.");
        this.validity = validity;
    }

    public static RecipeIngredient create(UUID stockItemId, String stockItemName,
                                          BigDecimal quantityPerUnit, String unit, LocalDate validity) {
        return new RecipeIngredient(UUID.randomUUID(), stockItemId, stockItemName, quantityPerUnit, unit, validity);
    }

    public BigDecimal getRequiredQuantity(int productQuantity) {
        return quantityPerUnit.multiply(BigDecimal.valueOf(productQuantity));
    }

    public UUID getId()                  { return id; }
    public UUID getStockItemId()         { return stockItemId; }
    public String getStockItemName()     { return stockItemName; }
    public BigDecimal getQuantityPerUnit() { return quantityPerUnit; }
    public String getUnit()              { return unit; }
    public LocalDate getValidity()       { return validity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecipeIngredient other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
