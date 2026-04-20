package com.serveflow.Exception.Menu;

import java.util.UUID;

public class MenuNotFound extends RuntimeException {
    public MenuNotFound(UUID id) {
        super("Menu não encontrado com ID: " + id);
    }
}
