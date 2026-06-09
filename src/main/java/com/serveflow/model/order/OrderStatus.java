package com.serveflow.model.order;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {

    PENDENTE("Pendente."),
    ENVIADO("Enviado."),
    EM_PREPARO("Em preparo."),
    PRONTO("Pronto."),
    AGUARDANDO_PAGAMENTO("Aguardando pagamento."),
    A_CAMINHO("A caminho."),
    ENTREGUE("Entregue."),
    CANCELADO("Cancelado.");

    private final String description;
    private Set<OrderStatus> allowedTransitions;

    OrderStatus(String description) {
        this.description = description;
    }

    static {
        PENDENTE.allowedTransitions   = EnumSet.of(ENVIADO, CANCELADO);
        ENVIADO.allowedTransitions    = EnumSet.of(EM_PREPARO, AGUARDANDO_PAGAMENTO, CANCELADO);
        EM_PREPARO.allowedTransitions = EnumSet.of(PRONTO, AGUARDANDO_PAGAMENTO, CANCELADO);
        PRONTO.allowedTransitions               = EnumSet.of(AGUARDANDO_PAGAMENTO, A_CAMINHO, ENTREGUE, CANCELADO);
        AGUARDANDO_PAGAMENTO.allowedTransitions = EnumSet.of(ENTREGUE, CANCELADO);
        A_CAMINHO.allowedTransitions            = EnumSet.of(AGUARDANDO_PAGAMENTO, ENTREGUE, CANCELADO);
        ENTREGUE.allowedTransitions             = EnumSet.noneOf(OrderStatus.class);
        CANCELADO.allowedTransitions            = EnumSet.noneOf(OrderStatus.class);
    }

    public boolean canTransitionTo(OrderStatus next) {
        return allowedTransitions.contains(next);
    }

    public boolean isFinal() {
        return this == ENTREGUE || this == CANCELADO;
    }

    public boolean isPendingPayment() {
        return this == AGUARDANDO_PAGAMENTO;
    }

    public String getDescription() {
        return description;
    }
}
