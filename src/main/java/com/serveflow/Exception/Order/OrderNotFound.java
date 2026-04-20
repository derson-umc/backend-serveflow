package com.serveflow.Exception.Order;

import java.util.UUID;

public class OrderNotFound extends RuntimeException {
    public OrderNotFound(UUID id) {
        super("Pedido não encontrado: " + id);
    }
}
