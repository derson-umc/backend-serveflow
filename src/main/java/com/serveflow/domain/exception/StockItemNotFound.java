package com.serveflow.domain.exception;

import java.util.UUID;

public class StockItemNotFound extends RuntimeException {
    public StockItemNotFound(UUID id) {
        super("Insumo não encontrado com ID: " + id);
    }
}
