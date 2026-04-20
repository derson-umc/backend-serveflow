package com.serveflow.Exception.Stock;

import java.util.UUID;

public class StockItemNotFound extends RuntimeException {
    public StockItemNotFound(UUID id) {
        super("Insumo de estoque não encontrado: " + id);
    }
}
