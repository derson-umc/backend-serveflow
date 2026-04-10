package com.serveflow.web.dto.stock;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductRecipeResponseDTO(
    UUID id,
    UUID productId,
    String productName,
    List<RecipeIngredientResponseDTO> ingredients
) {
    public record RecipeIngredientResponseDTO(
        UUID id,
        UUID stockItemId,
        String stockItemName,
        BigDecimal quantityPerUnit,
        String unit
    ) {}
}
