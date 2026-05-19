package com.serveflow.model.stock;

public enum StockItemStatus {
    ACTIVE("Ativo"),
    INACTIVE("Inativo");

    private final String description;

    StockItemStatus(String description) { this.description = description; }

    public String getDescription() { return description; }
}
