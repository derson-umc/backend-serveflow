package com.serveflow.repository.stock.recipeingredient;

import com.serveflow.repository.stock.productrecipe.ProductRecipeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "recipe_ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id_ingredient", updatable = false, nullable = false)
    private UUID idIngredient;

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() { return idIngredient; }

    @Override
    public boolean isNew() { return isNew; }

    @PostPersist
    @PostLoad
    void markNotNew() { this.isNew = false; }

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

    @Column(name = "validity")
    private LocalDate validity;
}
