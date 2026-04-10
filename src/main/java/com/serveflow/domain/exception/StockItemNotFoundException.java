package com.serveflow.domain.exception;

import java.util.UUID;

public class StockItemNotFoundException extends RuntimeException {
    public StockItemNotFoundException(UUID id) {
        super("Insumo nao encontrado com ID: " + id);
    }
}
