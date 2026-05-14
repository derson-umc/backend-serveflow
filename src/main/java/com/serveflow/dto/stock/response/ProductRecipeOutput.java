package com.serveflow.dto.stock.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductRecipeOutput(
        UUID id,
        UUID productId,
        String productName,
        List<RecipeIngredientOutput> ingredients
) {
    public record RecipeIngredientOutput(
            UUID id,
            UUID stockItemId,
            String stockItemName,
            BigDecimal quantityPerUnit,
            String unit
    ) {}
}
