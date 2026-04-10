package com.serveflow.domain.model.stock;

public enum MovementType {

    ENTRY("Entrada"),
    EXIT("Saida");

    private final String description;

    MovementType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
