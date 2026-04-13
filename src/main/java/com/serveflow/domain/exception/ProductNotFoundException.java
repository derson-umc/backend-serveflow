package com.serveflow.domain.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(UUID id) {
        super("Produto não encontrado com id: " + id);
    }
}
