package com.serveflow.exception.cashier;

import java.util.UUID;

public class CashSessionAlreadyClosedException extends RuntimeException {
    public CashSessionAlreadyClosedException(UUID id) {
        super("Cash session " + id + " is already closed.");
    }
}
