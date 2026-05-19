package com.serveflow.exception.financial;

import java.util.UUID;

public class DuplicateSettlementException extends RuntimeException {
    public DuplicateSettlementException(UUID id) {
        super("Account " + id + " has already been settled.");
    }
}
