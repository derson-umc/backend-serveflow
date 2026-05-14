package com.serveflow.model.stock;

public enum MovementType {

    ENTRY("Entrada"),
    EXIT("Saída");

    private final String description;

    MovementType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
