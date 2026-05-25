package com.serveflow.exception.cashier;

import java.util.UUID;

public class CashSessionAlreadyClosedException extends RuntimeException {

    public CashSessionAlreadyClosedException(UUID id) {
        super(buildMessage(id));
    }

    private static String buildMessage(UUID id) {
        return "Cash session [id=%s] is already closed".formatted(id);
    }
}
