package com.serveflow.exception.cashier;

import java.util.UUID;

public class CashSessionNotFoundException extends RuntimeException {

    public CashSessionNotFoundException(UUID id) {
        super(buildMessage(id));
    }

    private static String buildMessage(UUID id) {
        return "Cash session not found [id=%s]".formatted(id);
    }
}
