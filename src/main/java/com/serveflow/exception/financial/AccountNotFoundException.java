package com.serveflow.exception.financial;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(UUID id) {
        super("Account not found: " + id);
    }
}
