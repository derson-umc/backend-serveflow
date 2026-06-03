package com.serveflow.model.order;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {

    RASCUNHO("Rascunho."),
    ENVIADO("Enviado."),
    EM_PREPARO("Em preparo."),
    PRONTO("Pronto."),
    A_CAMINHO("A caminho."),
    ENTREGUE("Entregue."),
    CANCELADO("Cancelado.");

    private final String description;
    private Set<OrderStatus> allowedTransitions;

    OrderStatus(String description) {
        this.description = description;
    }

    static {
        RASCUNHO.allowedTransitions  = EnumSet.of(ENVIADO, CANCELADO);
        ENVIADO.allowedTransitions   = EnumSet.of(EM_PREPARO, CANCELADO);
        EM_PREPARO.allowedTransitions = EnumSet.of(PRONTO, CANCELADO);
        PRONTO.allowedTransitions    = EnumSet.of(A_CAMINHO, ENTREGUE, CANCELADO);
        A_CAMINHO.allowedTransitions = EnumSet.of(ENTREGUE, CANCELADO);
        ENTREGUE.allowedTransitions  = EnumSet.noneOf(OrderStatus.class);
        CANCELADO.allowedTransitions = EnumSet.noneOf(OrderStatus.class);
    }

    public boolean canTransitionTo(OrderStatus next) {
        return allowedTransitions.contains(next);
    }

    public boolean isFinal() {
        return this == ENTREGUE || this == CANCELADO;
    }

    public String getDescription() {
        return description;
    }
}
