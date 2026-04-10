package com.serveflow.domain.exception;

import java.util.UUID;

public class RecipeNotFoundException extends RuntimeException {
    public RecipeNotFoundException(UUID productId) {
        super("Ficha tecnica nao encontrada para o produto com ID: " + productId);
    }
}
