package com.serveflow.model.order;

public enum OrderItemStatus {
    PENDENTE,
    ENVIADO,
    EM_PREPARO,
    PRONTO,
    CANCELADO_ANTES_PREPARO,
    CANCELADO_EM_PREPARO;

    public boolean isCanceled() {
        return this == CANCELADO_ANTES_PREPARO || this == CANCELADO_EM_PREPARO;
    }
}
