package com.serveflow.data.repository.stock;

import com.serveflow.data.entity.stock.ProductRecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringProductRecipeRepository extends JpaRepository<ProductRecipeEntity, UUID> {
    Optional<ProductRecipeEntity> findByProductId(UUID productId);
}
