package com.serveflow.exception.menu;

import java.util.UUID;

public class MenuNotFoundException extends RuntimeException {

    public MenuNotFoundException(UUID id) {
        super(buildMessage(id));
    }

    private static String buildMessage(UUID id) {
        return "Menu not found [id=%s]".formatted(id);
    }
}
