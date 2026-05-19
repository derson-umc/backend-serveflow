package com.serveflow.exception.financial;

public class InconsistentAmountException extends RuntimeException {
    public InconsistentAmountException(String message) {
        super(message);
    }
}
