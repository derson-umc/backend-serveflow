package com.serveflow.domain.exception;

import java.util.UUID;

public class OrderNotFound extends RuntimeException {

    public OrderNotFound(UUID id) {
        super("Pedido não encontrado com id: " + id);
    }
}
