package com.serveflow.exception.cashier;

import java.util.UUID;

public class CashSessionNotFoundException extends RuntimeException {
    public CashSessionNotFoundException(UUID id) {
        super("Cash session not found: " + id);
    }
}
