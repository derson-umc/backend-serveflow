package com.serveflow.repository.stock.productrecipe;

import com.serveflow.exception.stock.RecipeNotFoundException;
import com.serveflow.model.stock.ProductRecipe;
import com.serveflow.model.stock.RecipeIngredient;
import com.serveflow.repository.stock.recipeingredient.RecipeIngredientEntity;
import com.serveflow.repository.stock.recipeingredient.SpringProductRecipeRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@Repository
@Transactional(readOnly = true)
public class ProductRecipeRepositoryImpl implements ProductRecipeRepository {

    private final SpringProductRecipeRepository springRepository;

    public ProductRecipeRepositoryImpl(SpringProductRecipeRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    @Transactional
    public ProductRecipe save(ProductRecipe recipe) {
        boolean isNew = recipe.getVersion() == null;
        ProductRecipeEntity entity;

        if (isNew) {
            entity = toEntity(recipe);
        } else {
            entity = springRepository.findById(recipe.getId())
                    .orElseThrow(() -> new RecipeNotFoundException(recipe.getProductId()));
            updateEntity(entity, recipe);
        }

        return toDomain(springRepository.save(entity));
    }

    @Override
    public ProductRecipe findById(UUID id) {
        return springRepository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new RecipeNotFoundException(id));
    }

    @Override
    public List<ProductRecipe> findAll() {
        return springRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<ProductRecipe> findByProductId(UUID productId) {
        return springRepository.findByProductId(productId).map(this::toDomain);
    }

    @Override
    public List<ProductRecipe> findAllByStockItemId(UUID stockItemId) {
        return springRepository.findByIngredients_StockItemId(stockItemId)
                .stream().map(this::toDomain).toList();
    }

    private ProductRecipe toDomain(ProductRecipeEntity e) {
        return new ProductRecipe(
                e.getIdRecipe(), e.getProductId(), e.getProductName(),
                e.getIngredients().stream().map(this::toIngredientDomain).toList(),
                e.getPreparationMode(),
                e.getProductType(),
                e.getVersion()
        );
    }

    private RecipeIngredient toIngredientDomain(RecipeIngredientEntity e) {
        return new RecipeIngredient(e.getIdIngredient(), e.getStockItemId(),
                e.getStockItemName(), e.getQuantityPerUnit(), e.getUnit(), e.getValidity());
    }

    private ProductRecipeEntity toEntity(ProductRecipe recipe) {
        return updateEntity(new ProductRecipeEntity(), recipe);
    }

    private ProductRecipeEntity updateEntity(ProductRecipeEntity entity, ProductRecipe recipe) {
        entity.setIdRecipe(recipe.getId());
        entity.setVersion(recipe.getVersion());
        entity.setProductId(recipe.getProductId());
        entity.setProductName(recipe.getProductName());
        entity.setPreparationMode(recipe.getPreparationMode());
        entity.setProductType(recipe.getProductType());

        List<RecipeIngredientEntity> updatedIngredients = recipe.getIngredients().stream()
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
        entity.setValidity(ingredient.getValidity());
        return entity;
    }

    private <E, ID> E findOrNew(List<E> list, ID id, Function<E, ID> idGetter, Supplier<E> factory) {
        return list.stream()
                .filter(e -> id != null && id.equals(idGetter.apply(e)))
                .findFirst()
                .orElseGet(factory);
    }
}
