package com.serveflow.domain.repository;

import com.serveflow.domain.model.stock.ProductRecipe;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRecipeRepository {
    ProductRecipe save(ProductRecipe recipe);
    Optional<ProductRecipe> findByProductId(UUID productId);
    ProductRecipe findById(UUID id);
    List<ProductRecipe> findAll();
}
