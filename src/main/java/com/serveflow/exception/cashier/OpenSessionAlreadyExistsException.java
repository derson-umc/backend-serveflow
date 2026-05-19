package com.serveflow.exception.cashier;

public class OpenSessionAlreadyExistsException extends RuntimeException {
    public OpenSessionAlreadyExistsException() {
        super("There is already an open cash session. Close it before opening a new one.");
    }
}
