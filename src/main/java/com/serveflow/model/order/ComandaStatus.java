package com.serveflow.model.order;

public enum ComandaStatus {

    ABERTA("Comanda aberta."),
    EM_FECHAMENTO("Em fechamento."),
    FECHADA("Comanda fechada.");

    private final String description;

    ComandaStatus(String description) {
        this.description = description;
    }

    public boolean isClosed() {
        return this == FECHADA;
    }

    public String getDescription() {
        return description;
    }
}
