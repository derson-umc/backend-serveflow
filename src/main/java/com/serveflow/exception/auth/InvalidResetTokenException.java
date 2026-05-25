package com.serveflow.exception.auth;

public class InvalidResetTokenException extends RuntimeException {

    public InvalidResetTokenException() {
        super(buildMessage());
    }

    private static String buildMessage() {
        return "Reset token is invalid or has already been used";
    }
}
