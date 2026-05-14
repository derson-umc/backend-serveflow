package com.serveflow.repository.stock.ProductRecipe;

import com.serveflow.repository.stock.RecipeIngredient.RecipeIngredientEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product_recipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRecipeEntity {

    @Id
    @Column(name = "id_recipe", updatable = false, nullable = false)
    private UUID idRecipe;

    @Version
    private Long version;

    @Column(name = "product_id", nullable = false, unique = true)
    private UUID productId;

    @Column(name = "product_name", nullable = false, length = 120)
    private String productName;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredientEntity> ingredients = new ArrayList<>();
}
