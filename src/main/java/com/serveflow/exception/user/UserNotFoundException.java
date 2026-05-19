package com.serveflow.exception.user;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super(buildMessage(String.valueOf(id)));
    }

    public UserNotFoundException(String identifier) {
        super(buildMessage(identifier));
    }

    private static String buildMessage(String identifier) {
        return "User not found for identifier: %s".formatted(identifier);
    }
}
