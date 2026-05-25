package com.serveflow.exception.stock;

import java.util.UUID;

public class StockItemNotFoundException extends RuntimeException {

    public StockItemNotFoundException(UUID id) {
        super(buildMessage(id));
    }

    private static String buildMessage(UUID id) {
        return "Stock item not found [id=%s]".formatted(id);
    }
}
