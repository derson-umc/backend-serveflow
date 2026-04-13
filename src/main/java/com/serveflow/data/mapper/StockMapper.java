package com.serveflow.data.mapper;

import com.serveflow.data.entity.stock.*;
import com.serveflow.domain.model.stock.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class StockMapper {

    public StockItem toDomain(StockItemEntity e) {
        return new StockItem(
                e.getIdStockItem(), e.getName(), e.getUnit(),
                e.getCurrentQuantity(), e.getMinimumQuantity(),
                e.getCreatedAt(), e.getVersion()
        );
    }

    public StockItemEntity toEntity(StockItem item) {
        var e = new StockItemEntity();
        e.setIdStockItem(item.getId());
        e.setVersion(item.getVersion());
        e.setName(item.getName());
        e.setUnit(item.getUnit());
        e.setCurrentQuantity(item.getCurrentQuantity());
        e.setMinimumQuantity(item.getMinimumQuantity());
        e.setCreatedAt(item.getCreatedAt());
        return e;
    }

    public StockItemEntity updateEntity(StockItemEntity entity, StockItem item) {
        entity.setName(item.getName());
        entity.setUnit(item.getUnit());
        entity.setCurrentQuantity(item.getCurrentQuantity());
        entity.setMinimumQuantity(item.getMinimumQuantity());
        return entity;
    }

    public ProductRecipe toDomain(ProductRecipeEntity e) {
        return new ProductRecipe(
                e.getIdRecipe(), e.getProductId(), e.getProductName(),
                e.getIngredients().stream().map(this::toDomain).toList(),
                e.getVersion()
        );
    }

    private RecipeIngredient toDomain(RecipeIngredientEntity e) {
        return new RecipeIngredient(
                e.getIdIngredient(), e.getStockItemId(), e.getStockItemName(),
                e.getQuantityPerUnit(), e.getUnit()
        );
    }

    public ProductRecipeEntity toEntity(ProductRecipe recipe) {
        return updateEntity(new ProductRecipeEntity(), recipe);
    }

    public ProductRecipeEntity updateEntity(ProductRecipeEntity entity, ProductRecipe recipe) {
        entity.setIdRecipe(recipe.getId());
        entity.setVersion(recipe.getVersion());
        entity.setProductId(recipe.getProductId());
        entity.setProductName(recipe.getProductName());

        var updatedIngredients = recipe.getIngredients().stream()
                .map(ingredient -> syncIngredientEntity(
                        findOrNew(entity.getIngredients(), ingredient.getId(),
                                RecipeIngredientEntity::getIdIngredient, RecipeIngredientEntity::new),
                        ingredient, entity))
                .toList();

        entity.getIngredients().clear();
        entity.getIngredients().addAll(updatedIngredients);

        return entity;
    }

    private RecipeIngredientEntity syncIngredientEntity(RecipeIngredientEntity entity,
                                                         RecipeIngredient ingredient,
                                                         ProductRecipeEntity recipe) {
        entity.setIdIngredient(ingredient.getId());
        entity.setRecipe(recipe);
        entity.setStockItemId(ingredient.getStockItemId());
        entity.setStockItemName(ingredient.getStockItemName());
        entity.setQuantityPerUnit(ingredient.getQuantityPerUnit());
        entity.setUnit(ingredient.getUnit());
        return entity;
    }

    public StockMovement toDomain(StockMovementEntity e) {
        return new StockMovement(
                e.getIdMovement(), e.getStockItemId(),
                MovementType.valueOf(e.getType().name()),
                e.getQuantity(), e.getReason(),
                e.getReferenceId(), e.getCreatedAt()
        );
    }

    public StockMovementEntity toEntity(StockMovement movement) {
        var e = new StockMovementEntity();
        e.setIdMovement(movement.getId());
        e.setStockItemId(movement.getStockItemId());
        e.setType(StockMovementEntity.MovementType.valueOf(movement.getType().name()));
        e.setQuantity(movement.getQuantity());
        e.setReason(movement.getReason());
        e.setReferenceId(movement.getReferenceId());
        e.setCreatedAt(movement.getCreatedAt());
        return e;
    }

    private <E, ID> E findOrNew(List<E> list, ID id, Function<E, ID> idGetter, Supplier<E> factory) {
        return list.stream()
                .filter(e -> id != null && id.equals(idGetter.apply(e)))
                .findFirst()
                .orElseGet(factory);
    }
}
