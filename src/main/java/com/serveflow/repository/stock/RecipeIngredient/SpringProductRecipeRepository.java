package com.serveflow.repository.stock.recipeingredient;

import com.serveflow.repository.stock.productrecipe.ProductRecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringProductRecipeRepository extends JpaRepository<ProductRecipeEntity, UUID> {
    Optional<ProductRecipeEntity> findByProductId(UUID productId);
    List<ProductRecipeEntity> findByIngredients_StockItemId(UUID stockItemId);
}
