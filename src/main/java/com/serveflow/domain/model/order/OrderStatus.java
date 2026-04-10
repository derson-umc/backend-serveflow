package com.serveflow.domain.model.order;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {

    CREATED("Criado"),
    CONFIRMED("Confirmado"),
    IN_PREPARATION("Em preparacao"),
    READY("Pronto"),
    OUT_FOR_DELIVERY("A caminho"),
    DELIVERED("Entregue"),
    CANCELLED("Cancelado");

    private final String description;
    private Set<OrderStatus> allowedTransitions;

    OrderStatus(String description) {
        this.description = description;
    }

    static {
        CREATED.allowedTransitions = EnumSet.of(CONFIRMED, CANCELLED);
        CONFIRMED.allowedTransitions = EnumSet.of(IN_PREPARATION, CANCELLED);
        IN_PREPARATION.allowedTransitions = EnumSet.of(READY, CANCELLED);
        READY.allowedTransitions = EnumSet.of(OUT_FOR_DELIVERY, DELIVERED, CANCELLED);
        OUT_FOR_DELIVERY.allowedTransitions = EnumSet.of(DELIVERED, CANCELLED);
        DELIVERED.allowedTransitions = EnumSet.noneOf(OrderStatus.class);
        CANCELLED.allowedTransitions = EnumSet.noneOf(OrderStatus.class);
    }

    public boolean canTransitionTo(OrderStatus next) {
        return allowedTransitions.contains(next);
    }

    public boolean isFinal() {
        return this == DELIVERED || this == CANCELLED;
    }

    public String getDescription() {
        return description;
    }
}
