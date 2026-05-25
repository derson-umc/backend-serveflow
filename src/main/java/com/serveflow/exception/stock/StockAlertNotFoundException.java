package com.serveflow.exception.stock;

import java.util.UUID;

public class StockAlertNotFoundException extends RuntimeException {

    public StockAlertNotFoundException(UUID id) {
        super(buildMessage(id));
    }

    private static String buildMessage(UUID id) {
        return "Stock alert not found [id=%s]".formatted(id);
    }
}
