package com.serveflow.exception.financial;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(UUID id) {
        super(buildMessage(id));
    }

    private static String buildMessage(UUID id) {
        return "Account not found [id=%s]".formatted(id);
    }
}
