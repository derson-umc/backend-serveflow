package com.serveflow.exception.order;

import java.util.UUID;

public class OrderNotFound extends RuntimeException {
    public OrderNotFound(UUID id) {
        super("Pedido não encontrado: " + id);
    }
}
