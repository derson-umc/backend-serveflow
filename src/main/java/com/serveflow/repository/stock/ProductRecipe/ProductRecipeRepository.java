package com.serveflow.repository.stock.ProductRecipe;

import com.serveflow.model.stock.ProductRecipe;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRecipeRepository {
    ProductRecipe save(ProductRecipe recipe);
    ProductRecipe findById(UUID id);
    List<ProductRecipe> findAll();
    Optional<ProductRecipe> findByProductId(UUID productId);
    List<ProductRecipe> findAllByStockItemId(UUID stockItemId);
}
