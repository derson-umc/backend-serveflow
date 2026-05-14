package com.serveflow.exception.stock;

import java.util.UUID;

public class StockItemNotFound extends RuntimeException {
    public StockItemNotFound(UUID id) {
        super("Insumo de estoque não encontrado: " + id);
    }
}
