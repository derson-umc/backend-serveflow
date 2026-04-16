package com.serveflow.domain.exception;

import java.util.UUID;

public class RecipeNotFound extends RuntimeException {
    public RecipeNotFound(UUID productId) {
        super("Ficha técnica não encontrada para o produto com ID: " + productId);
    }
}
