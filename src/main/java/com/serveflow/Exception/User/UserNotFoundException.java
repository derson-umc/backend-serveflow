package com.serveflow.Exception.User;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("Usuário não encontrado com id: " + id);
    }

    public UserNotFoundException(String username) {
        super("Usuário não encontrado: " + username);
    }
}
