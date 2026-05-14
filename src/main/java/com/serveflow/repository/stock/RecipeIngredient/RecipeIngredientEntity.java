package com.serveflow.repository.stock.RecipeIngredient;

import com.serveflow.repository.stock.ProductRecipe.ProductRecipeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "recipe_ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientEntity {

    @Id
    @Column(name = "id_ingredient", updatable = false, nullable = false)
    private UUID idIngredient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recipe", nullable = false)
    private ProductRecipeEntity recipe;

    @Column(name = "stock_item_id", nullable = false)
    private UUID stockItemId;

    @Column(name = "stock_item_name", nullable = false, length = 100)
    private String stockItemName;

    @Column(name = "quantity_per_unit", nullable = false, precision = 12, scale = 4)
    private BigDecimal quantityPerUnit;

    @Column(nullable = false, length = 20)
    private String unit;
}
