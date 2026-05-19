package com.serveflow.model.stock;

public enum MovementType {

    ENTRY("Entrada"),
    EXIT("Saída Manual"),
    ORDER_CONSUMPTION("Consumo por Pedido"),
    LOSS("Perda / Desperdício"),
    ADJUSTMENT("Ajuste de Inventário");

    private final String description;

    MovementType(String description) { this.description = description; }

    public String getDescription() { return description; }
}
