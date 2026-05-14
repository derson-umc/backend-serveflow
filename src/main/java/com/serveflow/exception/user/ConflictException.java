package com.serveflow.exception.user;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
