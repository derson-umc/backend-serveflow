package com.serveflow.Exception.Stock;

import java.util.UUID;

public class RecipeNotFound extends RuntimeException {
    public RecipeNotFound(UUID productId) {
        super("Ficha técnica não encontrada para o produto: " + productId);
    }
}
