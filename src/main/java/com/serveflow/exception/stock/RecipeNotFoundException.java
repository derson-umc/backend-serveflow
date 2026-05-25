package com.serveflow.exception.stock;

import java.util.UUID;

public class RecipeNotFoundException extends RuntimeException {

    public RecipeNotFoundException(UUID productId) {
        super(buildMessage(productId));
    }

    private static String buildMessage(UUID productId) {
        return "Recipe not found for product [productId=%s]".formatted(productId);
    }
}
