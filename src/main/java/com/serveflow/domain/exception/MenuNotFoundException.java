package com.serveflow.domain.exception;

import java.util.UUID;

public class MenuNotFoundException extends RuntimeException {
    public MenuNotFoundException(UUID id) {
        super("Menu nao encontrado com ID: " + id);
    }
}
