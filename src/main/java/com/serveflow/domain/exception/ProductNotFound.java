package com.serveflow.domain.exception;

import java.util.UUID;

public class ProductNotFound extends RuntimeException {

    public ProductNotFound(UUID id) {
        super("Produto não encontrado com id: " + id);
    }
}
