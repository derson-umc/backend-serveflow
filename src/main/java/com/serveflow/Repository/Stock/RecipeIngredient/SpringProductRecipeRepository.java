package com.serveflow.Repository.Stock.RecipeIngredient;

import com.serveflow.Repository.Stock.ProductRecipe.ProductRecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringProductRecipeRepository extends JpaRepository<ProductRecipeEntity, UUID> {
    Optional<ProductRecipeEntity> findByProductId(UUID productId);
}
