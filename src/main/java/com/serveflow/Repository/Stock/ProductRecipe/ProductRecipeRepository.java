package com.serveflow.Repository.Stock.ProductRecipe;

import com.serveflow.Model.Stock.ProductRecipe;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRecipeRepository {
    ProductRecipe save(ProductRecipe recipe);
    ProductRecipe findById(UUID id);
    List<ProductRecipe> findAll();
    Optional<ProductRecipe> findByProductId(UUID productId);
}
