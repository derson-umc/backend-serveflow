package com.serveflow.exception.financial;

import java.util.UUID;

public class DuplicateSettlementException extends RuntimeException {

    public DuplicateSettlementException(UUID id) {
        super(buildMessage(id));
    }

    private static String buildMessage(UUID id) {
        return "Account [id=%s] has already been settled".formatted(id);
    }
}
