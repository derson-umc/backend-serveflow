package com.serveflow.domain.model.menu;

public enum MenuStatus {

    OPEN("Aberto"),
    LOCKED("Travado");

    private final String description;

    MenuStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
