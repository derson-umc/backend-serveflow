package com.serveflow.dto.stock.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProductRecipeOutput(
        UUID id,
        UUID productId,
        String productName,
        String preparationMode,
        List<RecipeIngredientOutput> ingredients,
        String productType
) {
    public record RecipeIngredientOutput(
            UUID id,
            UUID stockItemId,
            String stockItemName,
            BigDecimal quantityPerUnit,
            String unit,
            LocalDate validity
    ) {}
}
