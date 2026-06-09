package com.serveflow.repository.stock.productrecipe;

import com.serveflow.model.stock.ProductType;
import com.serveflow.repository.stock.recipeingredient.RecipeIngredientEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product_recipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRecipeEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id_recipe", updatable = false, nullable = false)
    private UUID idRecipe;

    @Version
    private Long version;

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() { return idRecipe; }

    @Override
    public boolean isNew() { return isNew; }

    @PostPersist
    @PostLoad
    void markNotNew() { this.isNew = false; }

    @Column(name = "product_id", nullable = false, unique = true)
    private UUID productId;

    @Column(name = "product_name", nullable = false, length = 120)
    private String productName;

    @Column(name = "preparation_mode", columnDefinition = "TEXT")
    private String preparationMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 20)
    private ProductType productType = ProductType.FABRICATED;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredientEntity> ingredients = new ArrayList<>();
}
