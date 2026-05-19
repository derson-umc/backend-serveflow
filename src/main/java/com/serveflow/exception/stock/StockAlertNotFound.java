package com.serveflow.exception.stock;

import java.util.UUID;

public class StockAlertNotFound extends RuntimeException {
    public StockAlertNotFound(UUID id) {
        super("Alerta de estoque não encontrado: " + id);
    }
}
